package com.missiondata.oss.sqlprocessor;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class SQLMetaDataProcessor
{

  public void execute(ConnectionSource connectionSource) throws SQLSystemException
  {
    Connection connection = connectionSource.getConnection();
    try
    {
      execute(connection);
    }
    finally
    {
      connectionSource.returnConnection(connection);
    }
  }

  public void execute(final Connection connection) throws SQLSystemException
  {
    ResultSet resultSet = null;
    try
    {
      resultSet = getResultSet(connection.getMetaData());
      if (resultSet != null)
      {
        ProxyRestrictingResultSet.RestrictedResultSet restrictedResultSet = ProxyRestrictingResultSet.restrict(resultSet);
        while (resultSet.next())
        {
          process(restrictedResultSet);
        }
      }
    }
    catch(SQLException e)
    {
      throw new SQLSystemException("SQLException processing meta data", e);
    }
    finally
    {
      try
      {
        if(resultSet != null)
        {
          resultSet.close();
        }
      }
      catch(SQLException ignore)
      {
      }
    }
  }

  protected abstract ResultSet getResultSet(DatabaseMetaData databaseMetaData) throws SQLException;
  protected abstract void process(ResultSet resultSet) throws SQLException;
}
