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

import java.net.URL;
import java.sql.*;

/**
 * @author Leslie Hensley
 */
public class RestrictedResultSet implements ResultSet
{
  public RestrictedResultSet(ResultSet resultSet)
  {
    this.resultSet = resultSet;
  }

  public byte[] getBytes(int a) throws java.sql.SQLException
  {
    return resultSet.getBytes(
      a);
  }

  public byte[] getBytes(java.lang.String a) throws java.sql.SQLException
  {
    return resultSet.getBytes(
      a);
  }

  public boolean next() throws java.sql.SQLException
  {
    throw new UnsupportedOperationException("ResultSet.next() cannot be called on this result set");
  }

  public boolean previous() throws java.sql.SQLException
  {
    throw new UnsupportedOperationException("ResultSet.previous() cannot be called on this result set");
  }

  public boolean getBoolean(int a) throws java.sql.SQLException
  {
    return resultSet.getBoolean(
      a);
  }

  public boolean getBoolean(java.lang.String a) throws java.sql.SQLException
  {
    return resultSet.getBoolean(
      a);
  }

  public int getType() throws java.sql.SQLException
  {
    return resultSet.getType(
    );
  }

  public long getLong(java.lang.String a) throws java.sql.SQLException
  {
    return resultSet.getLong(
      a);
  }

  public long getLong(int a) throws java.sql.SQLException
  {
    return resultSet.getLong(
      a);
  }

  public void close() throws java.sql.SQLException
  {
    throw new UnsupportedOperationException("ResultSet.close() cannot be called on this result set");
  }

  public java.lang.Object getObject(int a) throws java.sql.SQLException
  {
    return resultSet.getObject(
      a);
  }

  public java.lang.Object getObject(java.lang.String a) throws java.sql.SQLException
  {
    return resultSet.getObject(
      a);
  }

  public java.lang.Object getObject(java.lang.String a, java.util.Map b) throws java.sql.SQLException
  {
    return resultSet.getObject(
      a, b);
  }

  public java.lang.Object getObject(int a, java.util.Map b) throws java.sql.SQLException
  {
    return resultSet.getObject(
      a, b);
  }

  public java.sql.Ref getRef(int a) throws java.sql.SQLException
  {
    return resultSet.getRef(
      a);
  }

  public java.sql.Ref getRef(java.lang.String a) throws java.sql.SQLException
  {
    return resultSet.getRef(
      a);
  }

  public java.sql.Date getDate(java.lang.String a, java.util.Calendar b) throws java.sql.SQLException
  {
    return resultSet.getDate(
      a, b);
  }

  public java.sql.Date getDate(int a) throws java.sql.SQLException
  {
    return resultSet.getDate(
      a);
  }

  public java.sql.Date getDate(int a, java.util.Calendar b) throws java.sql.SQLException
  {
    return resultSet.getDate(
      a, b);
  }

  public java.sql.Date getDate(java.lang.String a) throws java.sql.SQLException
  {
    return resultSet.getDate(
      a);
  }

  public boolean wasNull() throws java.sql.SQLException
  {
    return resultSet.wasNull(
    );
  }

  public java.lang.String getString(int a) throws java.sql.SQLException
  {
    return resultSet.getString(
      a);
  }

  public java.lang.String getString(java.lang.String a) throws java.sql.SQLException
  {
    return resultSet.getString(
      a);
  }

  public byte getByte(java.lang.String a) throws java.sql.SQLException
  {
    return resultSet.getByte(
      a);
  }

  public byte getByte(int a) throws java.sql.SQLException
  {
    return resultSet.getByte(
      a);
  }

  public short getShort(int a) throws java.sql.SQLException
  {
    return resultSet.getShort(
      a);
  }

  public short getShort(java.lang.String a) throws java.sql.SQLException
  {
    return resultSet.getShort(
      a);
  }

  public int getInt(int a) throws java.sql.SQLException
  {
    return resultSet.getInt(
      a);
  }

  public int getInt(java.lang.String a) throws java.sql.SQLException
  {
    return resultSet.getInt(
      a);
  }

  public float getFloat(int a) throws java.sql.SQLException
  {
    return resultSet.getFloat(
      a);
  }

  public float getFloat(java.lang.String a) throws java.sql.SQLException
  {
    return resultSet.getFloat(
      a);
  }

  public double getDouble(int a) throws java.sql.SQLException
  {
    return resultSet.getDouble(
      a);
  }

  public double getDouble(java.lang.String a) throws java.sql.SQLException
  {
    return resultSet.getDouble(
      a);
  }

  public java.math.BigDecimal getBigDecimal(int a, int b) throws java.sql.SQLException
  {
    return resultSet.getBigDecimal(
      a, b);
  }

  public java.math.BigDecimal getBigDecimal(int a) throws java.sql.SQLException
  {
    return resultSet.getBigDecimal(
      a);
  }

  public java.math.BigDecimal getBigDecimal(java.lang.String a) throws java.sql.SQLException
  {
    return resultSet.getBigDecimal(
      a);
  }

  public java.math.BigDecimal getBigDecimal(java.lang.String a, int b) throws java.sql.SQLException
  {
    return resultSet.getBigDecimal(
      a, b);
  }

  public java.sql.Time getTime(int a) throws java.sql.SQLException
  {
    return resultSet.getTime(
      a);
  }

  public java.sql.Time getTime(java.lang.String a) throws java.sql.SQLException
  {
    return resultSet.getTime(
      a);
  }

  public java.sql.Time getTime(java.lang.String a, java.util.Calendar b) throws java.sql.SQLException
  {
    return resultSet.getTime(
      a, b);
  }

  public java.sql.Time getTime(int a, java.util.Calendar b) throws java.sql.SQLException
  {
    return resultSet.getTime(
      a, b);
  }

  public java.sql.Timestamp getTimestamp(int a, java.util.Calendar b) throws java.sql.SQLException
  {
    return resultSet.getTimestamp(
      a, b);
  }

  public java.sql.Timestamp getTimestamp(java.lang.String a) throws java.sql.SQLException
  {
    return resultSet.getTimestamp(
      a);
  }

  public java.sql.Timestamp getTimestamp(java.lang.String a, java.util.Calendar b) throws java.sql.SQLException
  {
    return resultSet.getTimestamp(
      a, b);
  }

  public java.sql.Timestamp getTimestamp(int a) throws java.sql.SQLException
  {
    return resultSet.getTimestamp(
      a);
  }

  public java.io.InputStream getAsciiStream(java.lang.String a) throws java.sql.SQLException
  {
    return resultSet.getAsciiStream(
      a);
  }

  public java.io.InputStream getAsciiStream(int a) throws java.sql.SQLException
  {
    return resultSet.getAsciiStream(
      a);
  }

  public java.io.InputStream getUnicodeStream(int a) throws java.sql.SQLException
  {
    return resultSet.getUnicodeStream(
      a);
  }

  public java.io.InputStream getUnicodeStream(java.lang.String a) throws java.sql.SQLException
  {
    return resultSet.getUnicodeStream(
      a);
  }

  public java.io.InputStream getBinaryStream(java.lang.String a) throws java.sql.SQLException
  {
    return resultSet.getBinaryStream(
      a);
  }

  public java.io.InputStream getBinaryStream(int a) throws java.sql.SQLException
  {
    return resultSet.getBinaryStream(
      a);
  }

  public java.sql.SQLWarning getWarnings() throws java.sql.SQLException
  {
    return resultSet.getWarnings(
    );
  }

  public void clearWarnings() throws java.sql.SQLException
  {
    resultSet.clearWarnings(
    );
  }

  public java.lang.String getCursorName() throws java.sql.SQLException
  {
    return resultSet.getCursorName(
    );
  }

  public ResultSetMetaData getMetaData() throws SQLException
  {
    return resultSet.getMetaData();
  }

  public int findColumn(java.lang.String a) throws java.sql.SQLException
  {
    return resultSet.findColumn(
      a);
  }

  public java.io.Reader getCharacterStream(java.lang.String a) throws java.sql.SQLException
  {
    return resultSet.getCharacterStream(
      a);
  }

  public java.io.Reader getCharacterStream(int a) throws java.sql.SQLException
  {
    return resultSet.getCharacterStream(
      a);
  }

  public boolean isBeforeFirst() throws java.sql.SQLException
  {
    return resultSet.isBeforeFirst(
    );
  }

  public boolean isAfterLast() throws java.sql.SQLException
  {
    return resultSet.isAfterLast(
    );
  }

  public boolean isFirst() throws java.sql.SQLException
  {
    return resultSet.isFirst(
    );
  }

  public boolean isLast() throws java.sql.SQLException
  {
    return resultSet.isLast(
    );
  }

  public void beforeFirst() throws java.sql.SQLException
  {
    resultSet.beforeFirst(
    );
  }

  public void afterLast() throws java.sql.SQLException
  {
    resultSet.afterLast(
    );
  }

  public boolean first() throws java.sql.SQLException
  {
    return resultSet.first(
    );
  }

  public boolean last() throws java.sql.SQLException
  {
    return resultSet.last(
    );
  }

  public int getRow() throws java.sql.SQLException
  {
    return resultSet.getRow(
    );
  }

  public boolean absolute(int a) throws java.sql.SQLException
  {
    return resultSet.absolute(
      a);
  }

  public boolean relative(int a) throws java.sql.SQLException
  {
    return resultSet.relative(
      a);
  }

  public void setFetchDirection(int a) throws java.sql.SQLException
  {
    resultSet.setFetchDirection(
      a);
  }

  public int getFetchDirection() throws java.sql.SQLException
  {
    return resultSet.getFetchDirection(
    );
  }

  public void setFetchSize(int a) throws java.sql.SQLException
  {
    resultSet.setFetchSize(
      a);
  }

  public int getFetchSize() throws java.sql.SQLException
  {
    return resultSet.getFetchSize(
    );
  }

  public int getConcurrency() throws java.sql.SQLException
  {
    return resultSet.getConcurrency(
    );
  }

  public boolean rowUpdated() throws java.sql.SQLException
  {
    return resultSet.rowUpdated(
    );
  }

  public boolean rowInserted() throws java.sql.SQLException
  {
    return resultSet.rowInserted(
    );
  }

  public boolean rowDeleted() throws java.sql.SQLException
  {
    return resultSet.rowDeleted(
    );
  }

  public void updateNull(int a) throws java.sql.SQLException
  {
    resultSet.updateNull(
      a);
  }

  public void updateNull(java.lang.String a) throws java.sql.SQLException
  {
    resultSet.updateNull(
      a);
  }

  public void updateBoolean(int a, boolean b) throws java.sql.SQLException
  {
    resultSet.updateBoolean(
      a, b);
  }

  public void updateBoolean(java.lang.String a, boolean b) throws java.sql.SQLException
  {
    resultSet.updateBoolean(
      a, b);
  }

  public void updateByte(java.lang.String a, byte b) throws java.sql.SQLException
  {
    resultSet.updateByte(
      a, b);
  }

  public void updateByte(int a, byte b) throws java.sql.SQLException
  {
    resultSet.updateByte(
      a, b);
  }

  public void updateShort(int a, short b) throws java.sql.SQLException
  {
    resultSet.updateShort(
      a, b);
  }

  public void updateShort(java.lang.String a, short b) throws java.sql.SQLException
  {
    resultSet.updateShort(
      a, b);
  }

  public void updateInt(int a, int b) throws java.sql.SQLException
  {
    resultSet.updateInt(
      a, b);
  }

  public void updateInt(java.lang.String a, int b) throws java.sql.SQLException
  {
    resultSet.updateInt(
      a, b);
  }

  public void updateLong(int a, long b) throws java.sql.SQLException
  {
    resultSet.updateLong(
      a, b);
  }

  public void updateLong(java.lang.String a, long b) throws java.sql.SQLException
  {
    resultSet.updateLong(
      a, b);
  }

  public void updateFloat(java.lang.String a, float b) throws java.sql.SQLException
  {
    resultSet.updateFloat(
      a, b);
  }

  public void updateFloat(int a, float b) throws java.sql.SQLException
  {
    resultSet.updateFloat(
      a, b);
  }

  public void updateDouble(int a, double b) throws java.sql.SQLException
  {
    resultSet.updateDouble(
      a, b);
  }

  public void updateDouble(java.lang.String a, double b) throws java.sql.SQLException
  {
    resultSet.updateDouble(
      a, b);
  }

  public void updateBigDecimal(java.lang.String a, java.math.BigDecimal b) throws java.sql.SQLException
  {
    resultSet.updateBigDecimal(
      a, b);
  }

  public void updateBigDecimal(int a, java.math.BigDecimal b) throws java.sql.SQLException
  {
    resultSet.updateBigDecimal(
      a, b);
  }

  public void updateString(java.lang.String a, java.lang.String b) throws java.sql.SQLException
  {
    resultSet.updateString(
      a, b);
  }

  public void updateString(int a, java.lang.String b) throws java.sql.SQLException
  {
    resultSet.updateString(
      a, b);
  }

  public void updateBytes(int columnIndex, byte x[]) throws SQLException
  {
    resultSet.updateBytes(columnIndex, x);
  }

  public void updateBytes(String columnName, byte x[]) throws SQLException
  {
    resultSet.updateBytes(columnName, x);
  }

  public void updateDate(int a, java.sql.Date b) throws java.sql.SQLException
  {
    resultSet.updateDate(
      a, b);
  }

  public void updateDate(java.lang.String a, java.sql.Date b) throws java.sql.SQLException
  {
    resultSet.updateDate(
      a, b);
  }

  public void updateTime(java.lang.String a, java.sql.Time b) throws java.sql.SQLException
  {
    resultSet.updateTime(
      a, b);
  }

  public void updateTime(int a, java.sql.Time b) throws java.sql.SQLException
  {
    resultSet.updateTime(
      a, b);
  }

  public void updateTimestamp(int a, java.sql.Timestamp b) throws java.sql.SQLException
  {
    resultSet.updateTimestamp(
      a, b);
  }

  public void updateTimestamp(java.lang.String a, java.sql.Timestamp b) throws java.sql.SQLException
  {
    resultSet.updateTimestamp(
      a, b);
  }

  public void updateAsciiStream(int a, java.io.InputStream b, int c) throws java.sql.SQLException
  {
    resultSet.updateAsciiStream(
      a, b, c);
  }

  public void updateAsciiStream(java.lang.String a, java.io.InputStream b, int c) throws java.sql.SQLException
  {
    resultSet.updateAsciiStream(
      a, b, c);
  }

  public void updateBinaryStream(int a, java.io.InputStream b, int c) throws java.sql.SQLException
  {
    resultSet.updateBinaryStream(
      a, b, c);
  }

  public void updateBinaryStream(java.lang.String a, java.io.InputStream b, int c) throws java.sql.SQLException
  {
    resultSet.updateBinaryStream(
      a, b, c);
  }

  public void updateCharacterStream(int a, java.io.Reader b, int c) throws java.sql.SQLException
  {
    resultSet.updateCharacterStream(
      a, b, c);
  }

  public void updateCharacterStream(java.lang.String a, java.io.Reader b, int c) throws java.sql.SQLException
  {
    resultSet.updateCharacterStream(
      a, b, c);
  }

  public void updateObject(int a, java.lang.Object b) throws java.sql.SQLException
  {
    resultSet.updateObject(
      a, b);
  }

  public void updateObject(int a, java.lang.Object b, int c) throws java.sql.SQLException
  {
    resultSet.updateObject(
      a, b, c);
  }

  public void updateObject(java.lang.String a, java.lang.Object b) throws java.sql.SQLException
  {
    resultSet.updateObject(
      a, b);
  }

  public void updateObject(java.lang.String a, java.lang.Object b, int c) throws java.sql.SQLException
  {
    resultSet.updateObject(
      a, b, c);
  }

  public void insertRow() throws java.sql.SQLException
  {
    resultSet.insertRow(
    );
  }

  public void updateRow() throws java.sql.SQLException
  {
    resultSet.updateRow(
    );
  }

  public void deleteRow() throws java.sql.SQLException
  {
    resultSet.deleteRow(
    );
  }

  public void refreshRow() throws java.sql.SQLException
  {
    resultSet.refreshRow(
    );
  }

  public void cancelRowUpdates() throws java.sql.SQLException
  {
    resultSet.cancelRowUpdates(
    );
  }

  public void moveToInsertRow() throws java.sql.SQLException
  {
    resultSet.moveToInsertRow(
    );
  }

  public void moveToCurrentRow() throws java.sql.SQLException
  {
    resultSet.moveToCurrentRow(
    );
  }

  public java.sql.Statement getStatement() throws java.sql.SQLException
  {
    return resultSet.getStatement(
    );
  }

  public java.sql.Blob getBlob(int a) throws java.sql.SQLException
  {
    return resultSet.getBlob(
      a);
  }

  public java.sql.Blob getBlob(java.lang.String a) throws java.sql.SQLException
  {
    return resultSet.getBlob(
      a);
  }

  public java.sql.Clob getClob(java.lang.String a) throws java.sql.SQLException
  {
    return resultSet.getClob(
      a);
  }

  public java.sql.Clob getClob(int a) throws java.sql.SQLException
  {
    return resultSet.getClob(
      a);
  }

  public java.sql.Array getArray(int a) throws java.sql.SQLException
  {
    return resultSet.getArray(
      a);
  }

  public java.sql.Array getArray(java.lang.String a) throws java.sql.SQLException
  {
    return resultSet.getArray(
      a);
  }

  /**
   * TO DO
   * @param columnIndex
   * @return
   * @throws SQLException
   */
  public URL getURL(int columnIndex) throws SQLException
  {
    return null;
  }

  public URL getURL(String columnName) throws SQLException
  {
    return null;
  }

  public void updateRef(int columnIndex, Ref x) throws SQLException
  {
  }

  public void updateRef(String columnName, Ref x) throws SQLException
  {
  }

  public void updateBlob(int columnIndex, Blob x) throws SQLException
  {
  }

  public void updateBlob(String columnName, Blob x) throws SQLException
  {
  }

  public void updateClob(int columnIndex, Clob x) throws SQLException
  {
  }

  public void updateClob(String columnName, Clob x) throws SQLException
  {
  }

  public void updateArray(int columnIndex, Array x) throws SQLException
  {
  }

  public void updateArray(String columnName, Array x) throws SQLException
  {
  }

  private ResultSet resultSet;
}
