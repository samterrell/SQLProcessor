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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * ConnectionSource for DataSource connection pooling
 *
 * @author Steven Yelton
 */
public class DataSourceConnectionSource implements ConnectionSource
{
  private DataSource dataSource;

  /**
   * @param name designates the datasource in the <em>java:com/env</em> context;  <code>"java:com/env/<b><i><u>name</u></i></b>"</code>
   */
  public DataSourceConnectionSource(String name)
  {
    try
    {
      Context initialContext = new InitialContext();
      if(initialContext == null )
      {
        throw new SystemException("Boom - No Context");
      }
      dataSource =(DataSource)initialContext.lookup("java:comp/env/" + name);
      if (dataSource == null)
      {
        throw new SystemException("DS is null");
      }
    }
    catch (NamingException e)
    {
      throw new SystemException(e);
    }
  }

  /**
   * Retrieve a connectiom from the underlying datasource
   *
   * @return a connection from the
   */
  public Connection getConnection()
  {
    try
    {
      Connection connection = dataSource.getConnection();
      return connection;
    }
    catch (SQLException e)
    {
      throw new SystemException(e);
    }
  }

  /**
   *
   * @param connection
   */
  public void returnConnection(Connection connection)
  {
    try
    {
      connection.close();
    }
    catch (SQLException e)
    {
      throw new SystemException(e);
    }
  }
}
