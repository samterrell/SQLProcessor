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

import java.sql.Connection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class LeakTrackingConnectionSource implements ConnectionSource
{
  private ConnectionSource delegated;
  private Map outstandingConnectionsToStackTraces = new HashMap();

  public LeakTrackingConnectionSource(ConnectionSource delegated)
  {
    this.delegated = delegated;
  }

  public Connection getConnection()
  {
    Connection connection = delegated.getConnection();
    Throwable throwable = new Throwable();
    throwable.fillInStackTrace();
    StackTraceElement[] trace = throwable.getStackTrace();

    synchronized(outstandingConnectionsToStackTraces)
    {
      outstandingConnectionsToStackTraces.put(connection, trace);
    }
    return connection;
  }

  public void returnConnection(Connection connection)
  {
    synchronized(outstandingConnectionsToStackTraces)
    {
      if(outstandingConnectionsToStackTraces.remove(connection) == null)
      {
        System.err.println("We've never seen this connection before?");
      }
    }
    delegated.returnConnection(connection);
  }

  public Collection getOutstandingStackTraces()
  {
    return outstandingConnectionsToStackTraces.values();
  }

  public ConnectionSource getDelegated()
  {
    return delegated;
  }
}
