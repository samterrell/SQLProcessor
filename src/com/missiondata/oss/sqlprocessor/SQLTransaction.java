/*
 * SQLProcessor - a facade for the JDBC API
 * Copyright (C) 2001-2003 Mission Data
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.missiondata.oss.sqlprocessor;

import com.missiondata.oss.exception.SystemException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * SQLTransaction simplifies the infrastructure requirements for transactions.
 * <p>
 * By either overloading the {@link #transactionBody(java.sql.Connection)} or adding
 * {@link AbstractSQLProcessorBase} instances via {@link #add(AbstractSQLProcessorBase)}
 *
 * @author Steven Yelton
 */
public class SQLTransaction
{
  private List sqlStatements = new ArrayList();

  public SQLTransaction()
  {
  }

  public void add(AbstractSQLProcessorBase sqlProcessor)
  {
    sqlStatements.add(sqlProcessor);
  }

  public void add(SQLTransaction sqlTransaction)
  {
    sqlStatements.add(sqlTransaction);
  }

  public void execute(Connection connection)
  {
    Boolean storedAutoCommit = null;
    try
    {
      storedAutoCommit = Boolean.valueOf(connection.getAutoCommit());
      if(storedAutoCommit.booleanValue()==true)
      {
        setAutoCommit(connection, false);
      }

      TransactionConnection transactionConnection = transactionConnection(connection);
      defaultTransactionBody(transactionConnection);

      if (!abortted && !transactionConnection.isAborted())
      {
        commitTransaction(connection);
      }
      else
      {
        rollback(connection);
      }
    }
    catch (SQLException e)
    {
      rollback(connection);
      throw new SQLSystemException("Error executing transaction.  Rolledback", e);
    }
    catch (RuntimeException e)
    {
      rollback(connection);
      throw e;      
    }
    catch (Exception e)
    {
      rollback(connection);
      throw new SystemException("Error executing transaction.  Rolledback", e);
    }
    finally
    {
      if(storedAutoCommit!=null)
      {
        setAutoCommit(connection, storedAutoCommit.booleanValue());
      }
    }
  }

  private void rollback(Connection connection)
  {
    try
    {
      connection.rollback();
    }
    catch (SQLException e1)
    {
      throw new SQLSystemException("Error rolling back transaction", e1);
    }
  }

  /**
   * @param dataSourceJndiName - the JNDI name of a DataSource that the sql will act on
   */
  public void execute(String dataSourceJndiName)
  {
    execute(new DataSourceConnectionSource(dataSourceJndiName));
  }

  public void execute(ConnectionSource connectionSource)
  {
    Connection connection = null;
    try
    {
      connection = connectionSource.getConnection();
      execute(connection);
    }
    finally
    {
      connectionSource.returnConnection(connection);
    }
  }

  private TransactionConnection transactionConnection(Connection connection)
  {
    return new TransactionConnection(connection);
  }

  protected void defaultTransactionBody(Connection connection)
  {
    transactAdded(connection);
    transactionBody(connection);
  }

  protected void transactionBody(Connection connection)
  {
  }

  protected void transactAdded(Connection connection)
  {
    for (Iterator i = sqlStatements.iterator(); i.hasNext();)
    {
      Object o = i.next();
      if (o instanceof SQLTransaction)
      {
        ((SQLTransaction) o).defaultTransactionBody(connection);
      }
      else
      {
        ((AbstractSQLProcessorBase) o).execute(connection);
      }
    }
  }

  private void commitTransaction(Connection connection)
  {
    try
    {
      connection.commit();
    }
    catch (SQLException e)
    {
      throw new SQLSystemException("Error commiting transaction", e);
    }
  }

  private void setAutoCommit(Connection connection, boolean autocommit)
  {
    try
    {
      connection.setAutoCommit(autocommit);
    }
    catch (SQLException e)
    {
      throw new SQLSystemException("Error setting autocommit to true", e);
    }
  }

  private boolean abortted = false;

  public void abort()
  {
    abortted = true;
  }
}
