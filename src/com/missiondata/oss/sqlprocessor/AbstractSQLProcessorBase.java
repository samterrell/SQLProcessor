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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
   * @param rhs Parameter to compare for equality
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
   * Should be overriden to process the results of queries if you need to conditionally bail out early from the results.
   *
   * @see #process(java.sql.ResultSet)
   *
   * @param resultSet The result set to process
   * @return true to continue with processing.  false to abort processing
   * @throws SQLException thrown from underlying JDBC
   */
  protected boolean processAndContinue(ResultSet resultSet) throws SQLException
  {
    process(resultSet);
    return true;
  }

  /**
   * Should be overriden to process the results of queries.  Note that the
   * result set is iterated over by this class, so the <code>next</code> and <code>close</code>
   * methods should never be called on <code>resultSet</code>
   *
   * This method is called if a ResultSet exists, otherwise another
   * process method is called.
   *
   * @param resultSet The result set to process
   * @see #process(int rowsUpdated)
   * @see #process(int rowsUpdated, BigInteger insertedId)
   * @throws java.sql.SQLException Thrown from underlying JDBC
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
   * @param rowsUpdated The number of rows updated.
   * @see #process(ResultSet resultSet)
   * @see #process(int rowsUpdated, BigInteger insertedId)
   * @throws java.sql.SQLException Thrown from underlying JDBC
   */
  @SuppressWarnings("unused")
  protected void process(int rowsUpdated) throws SQLException
  {
  }

  /** Should be overridden to process the results of insertions.
   *
   * This method is called if there are no resultsets, and an
   * insertion id was available during processing.
   *
   * @param rowsUpdated The number of rows updated
   * @param insertedId The inserted row ID
   * @see #process(int rowsUpdated)
   * @see #process(ResultSet resultSet)
   * @throws java.sql.SQLException Thrown from underlying JDBC

   */
  @SuppressWarnings("unused")
  protected void process(int rowsUpdated, BigInteger insertedId) throws SQLException
  {
  }

  /** Should be overridden to process the results metadata.
   *
   * This method is called on queries with the result's metadata.
   * @param metadata The metadata for the results.
   */
  @SuppressWarnings("unused")
  protected void processMetaData(ResultSetMetaData metadata) {
  }

  /**
   * Should be used if a transaction is needed across multiple interactions
   * with the database.
   * @param connection The connection on which to execute this SQL
   * @return The number of rows updated
   * @throws SQLSystemException A wrapped SQLException with an added message.
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
   * @param evaluator Evaluator to be added.
   */
  public void addEvaluator(ParameterEvaluator evaluator)
  {
    chainingParameterEvaluator.addEvaluator(evaluator);
  }

  /**
   * Runs the update, insert or query that was specified in the constructor.
   *
   * @param dataSourceJndiName - the JNDI name of a DataSource that the sql will act on
   * @return The number of rows updated
   */
  @SuppressWarnings("unused")
  public int execute(String dataSourceJndiName)
  {
    return execute(new DataSourceConnectionSource(dataSourceJndiName));
  }

  /**
   * Runs the update, insert or query that was specified in the constructor.
   *
   * @param connectionSource  the ConnectionSource for the database that the sql will act on
   * @return The number of rows updated
   * @throws SQLSystemException A wrapped SQLException with an added message
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

      ResultSet resultSet;
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
              processMetaData(resultSet.getMetaData());
              boolean continueProcessing = true;
              ProxyRestrictingResultSet.RestrictedResultSet restrictedResultSet = ProxyRestrictingResultSet.restrict(resultSet);
              while (continueProcessing && resultSet.next())
              {
                results = true;
                continueProcessing = processAndContinue(restrictedResultSet);
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

      for (Object o : taggedSQL.getParameterIndices(key))
      {
        int idx = (Integer) o;
        if (value instanceof SQLNull)
        {
          preparedStatement.setNull(idx, ((SQLNull) value).getType());
        } else
        {
          preparedStatement.setObject(idx, value);
        }
      }
    }
  }

  protected Object getValue(String parameter)
  {
    Object value = chainingParameterEvaluator.getParameterValue(parameter, null);
    if(value == null)
    {
      throw new IllegalArgumentException("The parameter |" + parameter + "| was not set\n" +
        "SQL Description: " + getDescription() + "\n" +
        "SQL: " + getRawSQL());
    }
    return value;
  }

  private Object getValueForLogging(String key)
  {
    Object value;
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
   * @param connection Connection to generate prepared statement on.
   * @return Returns prepared statement.
   * @throws java.sql.SQLException Thrown by underlying JDBC
   */
  @SuppressWarnings("unused")
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
   * @param conn The JDBC connection to call this statement on.
   * @see #getInsertedIds()
   * @see #getLastInsertedId()
   *
   * @return Row ID of last inserted row.
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
   * @return Array of all row ID's from inserts.
   */
  public BigInteger[] getInsertedIds()
  {
    return insertedIds.toArray(new BigInteger[insertedIds.size()]);
  }

  /**
   * Get last id that was inserted during this sqlprocessor's
   * execution. Null is returned if no inserted ids were
   * encountered during processing.
   *
   * @see #fetchLastInsertedId(java.sql.Connection))
   * @see #getInsertedIds()
   *
   * @return Row ID of last Insert.
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
   * @param loggingCapability See(@LoggingCapability)
   */
  public void setLoggingCapability(LoggingCapability loggingCapability)
  {
    AbstractSQLProcessorBase.loggingImpl = loggingCapability;
  }

  /**
   * {@link InsertedIdCapability} provides AbstractSQLProcessor with the ability
   * to store the ids generated by INSERT statements.
   *
   * @param insertedIdCapability See(@InsertedIdCapability)
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


  private InsertedIdCapability insertedIdCapability;

  private boolean setupOverridden=true;

  private List<BigInteger> insertedIds = new LinkedList<BigInteger>();

  private static LoggingCapability loggingImpl = LoggingCapabilityFactory.getLoggingCapability("sqlprocessor");
}