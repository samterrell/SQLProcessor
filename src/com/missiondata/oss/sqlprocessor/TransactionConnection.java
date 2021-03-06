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

import java.sql.*;
import java.util.Map;
import java.util.Properties;

/**
 * @author Steven Yelton
 */
public class TransactionConnection implements Connection
{
  private Connection connection;
  private boolean aborted;

  public TransactionConnection(Connection connection)
  {
    this.connection = connection;
  }

  public boolean isAborted()
  {
    return aborted;
  }

  public int getHoldability() throws SQLException
  {
    return connection.getHoldability();
  }

  public int getTransactionIsolation() throws SQLException
  {
    return connection.getTransactionIsolation();
  }

  public void clearWarnings() throws SQLException
  {
    connection.clearWarnings();
  }

  public void close() throws SQLException
  {
  }

  public void commit() throws SQLException
  {
  }

  public void rollback() throws SQLException
  {
    aborted = true;
  }

  public boolean getAutoCommit() throws SQLException
  {
    return connection.getAutoCommit();
  }

  public boolean isClosed() throws SQLException
  {
    return connection.isClosed();
  }

  public boolean isReadOnly() throws SQLException
  {
    return connection.isReadOnly();
  }

  public void setHoldability(int holdability) throws SQLException
  {
    connection.setHoldability(holdability);
  }

  public void setTransactionIsolation(int level) throws SQLException
  {
    connection.setTransactionIsolation(level);
  }

  public void setAutoCommit(boolean autoCommit) throws SQLException
  {
  }

  public void setReadOnly(boolean readOnly) throws SQLException
  {
    connection.setReadOnly(readOnly);
  }

  public String getCatalog() throws SQLException
  {
    return connection.getCatalog();
  }

  public void setCatalog(String catalog) throws SQLException
  {
    connection.setCatalog(catalog);
  }

  public DatabaseMetaData getMetaData() throws SQLException
  {
    return connection.getMetaData();
  }

  public SQLWarning getWarnings() throws SQLException
  {
    return connection.getWarnings();
  }

  public Savepoint setSavepoint() throws SQLException
  {
    return connection.setSavepoint();
  }

  public void releaseSavepoint(Savepoint savepoint) throws SQLException
  {
    connection.releaseSavepoint(savepoint);
  }

  public void rollback(Savepoint savepoint) throws SQLException
  {
    connection.rollback(savepoint);
  }

  public Statement createStatement() throws SQLException
  {
    return connection.createStatement();
  }

  public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException
  {
    return connection.createStatement(resultSetType, resultSetConcurrency);
  }

  public Statement createStatement(int resultSetType, int resultSetConcurrency,
          int resultSetHoldability) throws SQLException
  {
    return connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
  }

  public Map<String,Class<?>> getTypeMap() throws SQLException
  {
    return connection.getTypeMap();
  }

  public void setTypeMap(Map<String,Class<?>> map) throws SQLException
  {
    connection.setTypeMap(map);
  }

  public String nativeSQL(String sql) throws SQLException
  {
    return connection.nativeSQL(sql);
  }

  public CallableStatement prepareCall(String sql) throws SQLException
  {
    return connection.prepareCall(sql);
  }

  public CallableStatement prepareCall(String sql, int resultSetType,
        int resultSetConcurrency) throws SQLException
  {
    return connection.prepareCall(sql, resultSetType, resultSetConcurrency);
  }

  public CallableStatement prepareCall(String sql, int resultSetType,
        int resultSetConcurrency,
        int resultSetHoldability) throws SQLException
  {
    return connection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
  }

  public PreparedStatement prepareStatement(String sql)
throws SQLException
  {
    return connection.prepareStatement(sql);
  }

  public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
throws SQLException
  {
    return connection.prepareStatement(sql, autoGeneratedKeys);
  }

  public PreparedStatement prepareStatement(String sql, int resultSetType,
             int resultSetConcurrency)
throws SQLException
  {
    return connection.prepareStatement(sql, resultSetType, resultSetConcurrency);
  }

  public PreparedStatement prepareStatement(String sql, int resultSetType,
             int resultSetConcurrency, int resultSetHoldability)
throws SQLException
  {
    return connection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
  }

  public PreparedStatement prepareStatement(String sql, int columnIndexes[])
throws SQLException
  {
    return connection.prepareStatement(sql, columnIndexes);
  }

  public Savepoint setSavepoint(String name) throws SQLException
  {
    return connection.setSavepoint(name);
  }

  public PreparedStatement prepareStatement(String sql, String columnNames[])
throws SQLException
  {
    return connection.prepareStatement(sql, columnNames);
  }

  public Clob createClob() throws SQLException
  {
    return connection.createClob();
  }

  public Blob createBlob() throws SQLException
  {
    return connection.createBlob();
  }

  public NClob createNClob() throws SQLException
  {
    return connection.createNClob();
  }

  public SQLXML createSQLXML() throws SQLException
  {
    return connection.createSQLXML();
  }

  public boolean isValid(int timeout) throws SQLException
  {
    return connection.isValid(timeout);
  }

  public void setClientInfo(String name, String value) throws SQLClientInfoException
  {
    connection.setClientInfo(name,value);
  }

  public void setClientInfo(Properties properties) throws SQLClientInfoException
  {
    connection.setClientInfo(properties);
  }

  public String getClientInfo(String name) throws SQLException
  {
    return connection.getClientInfo(name);
  }

  public Properties getClientInfo() throws SQLException
  {
    return connection.getClientInfo();
  }

  public Array createArrayOf(String typeName, Object[] elements) throws SQLException
  {
    return connection.createArrayOf(typeName, elements);
  }

  public Struct createStruct(String typeName, Object[] attributes) throws SQLException
  {
    return connection.createStruct(typeName, attributes);
  }

  public <T> T unwrap(Class<T> iface) throws SQLException
  {
    return connection.unwrap(iface);
  }

  public boolean isWrapperFor(Class<?> iface) throws SQLException
  {
    return connection.isWrapperFor(iface);
  }
}
