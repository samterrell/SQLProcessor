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

import com.mockobjects.ExpectationCounter;
import com.mockobjects.MockObject;

import java.sql.Connection;

public class _MockConnectionSource extends MockObject implements ConnectionSource
{
  public _MockConnectionSource(Connection connection)
  {
    this.connection = connection;
  }

  public Connection getConnection()
  {
    getConnectionCalls.inc();
    return connection;
  }

  public void returnConnection(Connection connection)
  {
    returnConnectionCalls.inc();
  }

  public void setExpectedGetConnectionCalls(int callCount)
  {
    getConnectionCalls.setExpected(callCount);
  }

  public void setExpectedReturnConnectionCalls(int callCount)
  {
    returnConnectionCalls.setExpected(callCount);
  }

  private Connection connection;

  private ExpectationCounter getConnectionCalls = new ExpectationCounter("MockConnectionSource.getConnection");
  private ExpectationCounter returnConnectionCalls = new ExpectationCounter("MockConnectionSource.returnConnection");
}
