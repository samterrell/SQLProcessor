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

import junit.framework.TestCase;
import com.mockobjects.sql.MockConnection;

import java.sql.Connection;
import java.util.Stack;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

public class _LeakTrackerTest extends TestCase
{
  MockConnection connOne = new MockConnection();
  MockConnection connTwo = new MockConnection();
  LeakTrackingConnectionSource tracker;

  public _LeakTrackerTest(String s)
  {
    super(s);
  }

  protected void setUp() throws Exception
  {
    Collection conns = new ArrayList();
    conns.add(connOne);
    conns.add(connTwo);
    ConnectionSource source = new TestConnectionSource(conns);
    tracker = new LeakTrackingConnectionSource(source);
  }

  protected void tearDown() throws Exception
  {
    tracker = null;
  }

  public void testNoLeaks()
  {

    Connection connection = tracker.getConnection();
    Connection connection2 = tracker.getConnection();
    tracker.returnConnection(connection2);
    tracker.returnConnection(connection);

    assertEquals(0, tracker.getOutstandingStackTraces().size());
  }

  public void testLeak()
  {
    Connection connection = tracker.getConnection();
    assertEquals(1, tracker.getOutstandingStackTraces().size());
    connection = tracker.getConnection();
    assertEquals(2, tracker.getOutstandingStackTraces().size());

    Collection traces = tracker.getOutstandingStackTraces();
    Iterator it = traces.iterator();

    while(it.hasNext())
    {
      StackTraceElement[] elements = (StackTraceElement[])it.next();
      System.out.println("\n\nTrace:\n");
      for(int i = 0; i < elements.length; i++)
      {
        StackTraceElement element = elements[i];
        System.out.println(element);
      }
    }
  }

  private static class TestConnectionSource implements ConnectionSource
  {
    private Stack connections = new Stack();

    public TestConnectionSource(Collection conns)
    {
      connections.addAll(conns);
    }

    public Connection getConnection()
    {
      return (Connection)connections.pop();
    }

    public void returnConnection(Connection connection)
    {
      connections.push(connection);
    }
  }
}
