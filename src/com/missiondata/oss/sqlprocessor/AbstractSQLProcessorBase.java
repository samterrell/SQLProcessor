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


import java.math.BigInteger;
import java.sql.*;
import java.util.*;

/**
 * Base functionality for various SQLProcessor implementations. Allows for the
 * original {@link SQLProcessor} to maintain its current behavior in legacy code,
 * while allowing for newer implementations.
 *
 * @see SQLProcessor
 * @see MultiBeanSQLProcessor
 *
 * @author Darren Day
 */
abstract public class AbstractSQLProcessorBase
{
  /**
   * @param description will be used in logging statements
   * @param sqlText will be parsed into valid SQL
   */
  public AbstractSQLProcessorBase(String description, String sqlText)
  {
    this.rawSQL = sqlText;
    this.taggedSQL = new TaggedSQL(sqlText);
    this.description = description;
  }

  /**
   * Tests if results were returned from the query.
   *
   * @return true if at least one row is returned from the query, otherwise
   *  false
   */
  public boolean resultsExist()
  {
    return results;
  }

  /**
   * For when only a single value is needed as the result of a query
   *
   * @return the first column of the last row returned by the query
   */
  public Object getSingleResult()
  {
    return result;
  }

  /**
   * A convenience method that test rhs for equality with the first column
   * of the last row returned by the query
   *
   * @return false if no rows are returned from the query or if the first
   *  column returned from the query is not <code>.equals</code>
   */
  public boolean isSingleResultEqual(Object rhs)
  {
    return resultsExist() && rhs.equals(getSingleResult());
  }

  protected boolean setUp()
  {
    setupOverridden = false;
    return true;
  }

  protected boolean isSetUp()
  {
    return setUp() && setupOverridden;
  }

  /**
   * Should be overriden to process the results of queries.  Note that the
   * result set is iterated over by this class, so the <code>next</code> and <code>close</code>
   * methods should never be called on <code>resultSet</code>
   *
   * This method is called if a ResultSet exists, otherwise another
   * process method is called.
   *
   * @see #process(int rowsUpdated)
   * @see #process(int rowsUpdated, BigInteger insertedId)
   */
  protected void process(ResultSet resultSet) throws SQLException
  {
    result = resultSet.getObject(1);
  }

  /** Should be overridden to process the results of insertions.
   *
   * This method is called if no ResultSet or insertion id was
   * available during processing
   *
   * @see #process(ResultSet resultSet)
   * @see #process(int rowsUpdated, BigInteger insertedId)
   */
  protected void process(int rowsUpdated) throws SQLException
  {
  }

  /** Should be overridden to process the results of insertions.
   *
   * This method is called if there are no resultsets, and an
   * insertion id was available during processing.
   *
   * @see #process(int rowsUpdated)
   * @see #process(ResultSet resultSet)

   */
  protected void process(int rowsUpdated, BigInteger insertedId) throws SQLException
  {
  }

  /**
   * Should be used if a transaction is needed across multiple interactions
   * with the database.
   */
  public int execute(final Connection connection) throws SQLSystemException
  {
    return execute(new ConnectionSource()
    {
      public Connection getConnection()
      {
        return connection;
      }

      public void returnConnection(Connection c)
      {
      }
    });
  }

  /**
   * Add another parameter evaluator.
   *
   * A new parameter evaluator is added to the end of a list of possible parameter evaluators.
   * Evaluators are called in the order they are added until a non-null value is returned.
   *
   * @param evaluator
   */
  public void addEvaluator(ParameterEvaluator evaluator)
  {
    chainingParameterEvaluator.addEvaluator(evaluator);
  }

  /**
   * Runs the update, insert or query that was specified in the constructor.
   *
   * @param connectionSource  the ConnectionSource for the database that the sql will act on
   */
  public int execute(ConnectionSource connectionSource) throws SQLSystemException
  {
    int rowsUpdated = 0;
    setupOverridden = true;

    result = null;
    Connection connection = connectionSource.getConnection();
    PreparedStatement preparedStatement = null;

    try
    {
      preparedStatement = connection.prepareStatement(taggedSQL.getPreparedString());

      ResultSet resultSet = null;
      while (isSetUp())
      {
        prepareStatement(preparedStatement);

        logStatement();

        if (taggedSQL.isQuery())
        {
          resultSet = null;
          try
          {
            resultSet = preparedStatement.executeQuery();

            if (resultSet != null)
            {
              RestrictedResultSet restrictedResultSet = new RestrictedResultSet(resultSet);
              while (resultSet.next())
              {
                results = true;
                process(restrictedResultSet);
              }
            }
          }
          finally
          {
            try
            {
              if (resultSet != null)
              {
                resultSet.close();
                resultSet = null;
              }
            }
            catch (SQLException ignore)
            {

            }
          }
        }
        else
        {
          int rowsUpdatedHere = preparedStatement.executeUpdate();
          if (taggedSQL.isInsert())
          {
            checkForInsertedId(connection, rowsUpdatedHere);
          }
          else
          {
            process(rowsUpdatedHere);
          }
          rowsUpdated += rowsUpdatedHere;
        }
      }
      return rowsUpdated;
    }
    catch (SQLException e)
    {
      throw new SQLSystemException("SQL Description: " + description + "\nSQL: " + getSQLText(), e);
    }
    finally
    {
      try
      {
        if (preparedStatement != null)
        {
          preparedStatement.close();
          preparedStatement = null;
        }
      }
      catch (SQLException ignore)
      {
        logWarning("SQLException cleaning up preparedStatement", ignore);
      }
      connectionSource.returnConnection(connection);
    }
  }

  private void prepareStatement(PreparedStatement preparedStatement) throws SQLException
  {
    Iterator iterator = taggedSQL.getParameterKeys();
    while (iterator.hasNext())
    {
      String key = (String) iterator.next();
      Object value = getValue(key);

      if (value == null)
      {
        throw new IllegalArgumentException("The parameter |" + key + "| was not set\n" +
          "SQL Description: " + description + "\n" +
          "SQL: " + rawSQL);
      }

      for (Iterator i = taggedSQL.getParameterIndices(key).iterator(); i.hasNext();)
      {
        int idx = ((Integer) i.next()).intValue();
        if (value instanceof SQLNull)
        {
          preparedStatement.setNull(idx, ((SQLNull) value).getType());
        }
        else
        {
          preparedStatement.setObject(idx, value);
        }
      }
    }
  }

  protected Object getValue(String parameter)
  {
    return chainingParameterEvaluator.getParameterValue(parameter, null);
  }

  private Object getValueForLogging(String key)
  {
    Object value = null;
    try
    {
      value = getValue(key);
    }

    catch (IllegalArgumentException e)
    {
      value = "!SET{" + key + "}!";
    }

    return value;
  }

  private String getSQLText()
  {
    StringBuffer sqlText = new StringBuffer(taggedSQL.getPreparedStringForLogging());
    StringBuffer output = new StringBuffer(sqlText.length());
    int parameterIndex = 1;
    for (int i = 0; i < sqlText.length(); i++)
    {
      if (sqlText.charAt(i) == '?')
      {
        output.append(prettyParameter(getValueForLogging(taggedSQL.getParameterKey(parameterIndex++))));
      }
      else
      {
        output.append(sqlText.charAt(i));
      }
    }
    return output.toString();
  }

  private void logStatement()
  {
    logInfo(prettyPrint());
  }

  protected String prettyPrint()
  {
    return description + ": " + getSQLText();
  }

  private String prettyParameter(Object value)
  {
    if (!(value instanceof Number))
    {
      return "'" + value + "'";
    }
    else
    {
      return value.toString();
    }
  }

  /**
   * If you just want to use the tagged sql syntax and pretty printing power of the sqlprocessor,
   * Use this method instead of executes
   */
  public PreparedStatement getPreparedStatement(ConnectionSource connectionSource) throws SQLException
  {
    return getPreparedStatement(connectionSource.getConnection());
  }

  /**
   * If you just want to use the tagged sql syntax and pretty printing power of the sqlprocessor,
   * Use this method instead of executes
   */
  public PreparedStatement getPreparedStatement(Connection connection) throws SQLException
  {
    PreparedStatement preparedStatement = connection.prepareStatement(taggedSQL.getPreparedString());
    prepareStatement(preparedStatement);
    logStatement();

    return preparedStatement;
  }

  protected String getDescription()
  {
    return description;
  }

  protected String getRawSQL()
  {
    return rawSQL;
  }

  private void checkForInsertedId(Connection conn, int rowsUpdated) throws SQLException
  {
    BigInteger id = fetchLastInsertedId(conn);
    if (id != null)
    {
      insertedIds.add(id);
      process(rowsUpdated, id);
    }
    else
    {
      process(rowsUpdated);
    }
  }

  /**
   * Get the id of the last item inserted into the table.
   * If not implemented by a derived class, a null is always
   * returned and no inserted ids are tracked during execution.
   *
   * @see #getInsertedIds()
   * @see #getLastInsertedId()
   *
   */
  protected BigInteger fetchLastInsertedId(Connection conn)
  {
    return insertedIdCapability==null ? null : insertedIdCapability.fetchLastInsertedId(conn);
  }

  /**
   * Get array of ids that were inserted during this sqlprocessor's
   * execution. An empty array is returned if no inserted ids were
   * encountered during processing.
   *
   * @see #fetchLastInsertedId(java.sql.Connection))
   * @see #getLastInsertedId()
   *
   */
  public BigInteger[] getInsertedIds()
  {
    return (BigInteger[]) insertedIds.toArray(new BigInteger[insertedIds.size()]);
  }

  /**
   * Get last id that was inserted during this sqlprocessor's
   * execution. Null is returned if no inserted ids were
   * encountered during processing.
   *
   * @see #fetchLastInsertedId(java.sql.Connection))
   * @see #getInsertedIds()
   *
   */
  public BigInteger getLastInsertedId()
  {
    BigInteger last = null;
    if (getInsertedIds().length > 0)
    {
      last = getInsertedIds()[getInsertedIds().length - 1];
    }

    return last;
  }

  /**
   * A LoggingCapability defines how an AbstractSQLProcessor can interface
   * with the current system's loggin environment.
   *
   * AbstractSQLProcessor strives to provide useful logging messages, such
   * as info messages consisting of executed statements in a form that can
   * be copied and used in database query tools
   *
   * @param loggingCapability
   */
  public void setLoggingCapability(LoggingCapability loggingCapability)
  {
    this.loggingImpl = loggingCapability;
  }

  /**
   * {@link InsertedIdCapability} provides AbstractSQLProcessor with the ability
   * to store the ids generated by INSERT statements.
   *
   * @param insertedIdCapability
   */
  public void setInsertedIdCapability(InsertedIdCapability insertedIdCapability)
  {
    this.insertedIdCapability = insertedIdCapability;
  }

  protected void logInfo(String information)
  {
    if(loggingImpl!=null)
    {
      loggingImpl.logInfo(information);
    }
  }

  protected void logWarning(String warning, Throwable t)
  {
    if(loggingImpl!=null)
    {
      loggingImpl.logWarning(warning, t);
    }
  }

  protected void logError(String error, Throwable t)
  {
    if(loggingImpl!=null)
    {
      loggingImpl.logError(error,t);
    }
  }

  protected TaggedSQL taggedSQL;
  private String rawSQL;
  private Object result;
  private String description;
  private boolean results;
  private ChainingParameterEvaluator chainingParameterEvaluator = new ChainingParameterEvaluator();


  private LoggingCapability loggingImpl;
  private InsertedIdCapability insertedIdCapability;

  private boolean setupOverridden=true;

  private List insertedIds = new LinkedList();

}