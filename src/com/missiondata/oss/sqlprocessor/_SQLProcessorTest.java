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
import com.mockobjects.sql.MockConnection;
import com.mockobjects.sql.MockMultiRowResultSet;
import com.mockobjects.sql.MockPreparedStatement;
import junit.framework.TestCase;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;
import java.util.Date;
import java.util.logging.Level;
import java.math.BigInteger;

/**
 * @author Leslie Hensley
 */
public class _SQLProcessorTest extends TestCase
{
  public _SQLProcessorTest(String name)
  {
    super(name);
  }

  public void setUp()
  {
    mockResultSet = createMockResultSet();
    mockPreparedStatement = new _MockPreparedStatement();
    mockPreparedStatement.setExpectedExecuteCalls(1);
    mockPreparedStatement.setExpectedCloseCalls(1);

    mockConnection = new MockConnection();

    mockConnectionSource = new _MockConnectionSource(mockConnection);
    mockConnectionSource.setExpectedGetConnectionCalls(1);
    mockConnectionSource.setExpectedReturnConnectionCalls(1);
  }

  private MockMultiRowResultSet createMockResultSet()
  {
    Object[][] values = new Object[][]{{new Integer(5), "mowing"}, {new Integer(7), "painting"}};
    MockMultiRowResultSet mockResultSet = new MockMultiRowResultSet();
    mockResultSet.setupColumnNames(new String[]{"id", "job"});
    mockResultSet.setupRows(values);
    mockResultSet.setExpectedNextCalls(3);
    mockResultSet.setExpectedCloseCalls(1);

    return mockResultSet;
  }

  public void testUpdate() throws SystemException
  {
    mockPreparedStatement.addExpectedSetParameter(1, "closed");
    mockPreparedStatement.addExpectedSetParameter(2, "bar");
    mockPreparedStatement.setupUpdateCount(1);

    mockConnection.addExpectedPreparedStatementString("UPDATE foo SET state = ? WHERE name = ?");
    mockConnection.addExpectedPreparedStatement(mockPreparedStatement);

    SQLProcessor sqlProcessor = new SQLProcessor("UPDATE #table# SET state = |state| WHERE name = |name|");
    sqlProcessor.set("table", "foo");
    sqlProcessor.set("state", "closed");
    sqlProcessor.set("name", "bar");

    sqlProcessor.execute(mockConnectionSource);

    mockPreparedStatement.verify();
    mockConnection.verify();
    mockConnectionSource.verify();
  }

  public void testQueryParameter()
  {
    mockPreparedStatement.addResultSet(mockResultSet);
    mockPreparedStatement.addExpectedSetParameter(1, "closed");
    mockPreparedStatement.addExpectedSetParameter(2, "bar");
    mockPreparedStatement.setExpectedExecuteCalls(1);
    mockPreparedStatement.setExpectedCloseCalls(1);

    mockConnection.addExpectedPreparedStatementString("SELECT id, job FROM foo WHERE state = ? AND name = ?");
    mockConnection.addExpectedPreparedStatement(mockPreparedStatement);

    SQLProcessor sqlProcessor = new SQLProcessor("SELECT id, job FROM #table# WHERE state = |state| AND name = |name|");
    sqlProcessor.set("table", "foo");
    sqlProcessor.set("state", "closed");
    sqlProcessor.set("name", "bar");

    sqlProcessor.execute(mockConnectionSource);
  }

  public void testQueryRepeatedParameter()
  {
    mockPreparedStatement.addResultSet(mockResultSet);
    mockPreparedStatement.addExpectedSetParameter(1, "closed");
    mockPreparedStatement.addExpectedSetParameter(2, "bar");
    mockPreparedStatement.addExpectedSetParameter(3, "closed");
    mockPreparedStatement.setExpectedExecuteCalls(1);
    mockPreparedStatement.setExpectedCloseCalls(1);

    mockConnection.addExpectedPreparedStatementString("SELECT id, job FROM foo WHERE state = ? AND name = ? AND state2 = ?");
    mockConnection.addExpectedPreparedStatement(mockPreparedStatement);

    SQLProcessor sqlProcessor = new SQLProcessor("SELECT id, job FROM #table# WHERE state = |state| AND name = |name| AND state2 = |state|");
    sqlProcessor.set("table", "foo");
    sqlProcessor.set("state", "closed");
    sqlProcessor.set("name", "bar");

    sqlProcessor.execute(mockConnectionSource);
  }

  public void testQuery()
  {
    mockPreparedStatement.addResultSet(mockResultSet);
    mockPreparedStatement.addExpectedSetParameter(1, "closed");
    mockPreparedStatement.addExpectedSetParameter(2, "bar");
    mockPreparedStatement.setExpectedExecuteCalls(1);
    mockPreparedStatement.setExpectedCloseCalls(1);

    mockConnection.addExpectedPreparedStatementString("SELECT id, job FROM foo WHERE state = ? AND name = ?");
    mockConnection.addExpectedPreparedStatement(mockPreparedStatement);

    final Object[][] collectedValues = new Object[2][2];
    SQLProcessor sqlProcessor =
      new SQLProcessor("SELECT id, job FROM #table# WHERE state = |state| AND name = |name|")
      {
        protected void process(ResultSet resultSet) throws SQLException
        {
          collectedValues[index][0] = resultSet.getObject(1);
          collectedValues[index][1] = resultSet.getObject(2);
          index++;
        }

        private int index = 0;
      };

    sqlProcessor.set("table", "foo");
    sqlProcessor.set("state", "closed");
    sqlProcessor.set("name", "bar");

    sqlProcessor.execute(mockConnectionSource);

    mockResultSet.verify();
    mockPreparedStatement.verify();
    mockConnection.verify();
    mockConnectionSource.verify();
  }

  public void testSQLException()
  {
    mockPreparedStatement.setupThrowExceptionOnExecute(new SQLException("Test SQLException"));
    mockPreparedStatement.addExpectedSetParameter(1, "closed");
    mockPreparedStatement.addExpectedSetParameter(2, "bar");
    mockPreparedStatement.setExpectedExecuteCalls(1);
    mockPreparedStatement.setExpectedCloseCalls(1);

    mockConnection.addExpectedPreparedStatementString("SELECT id, job FROM foo WHERE state = ? AND name = ?");
    mockConnection.addExpectedPreparedStatement(mockPreparedStatement);

    try
    {
      SQLProcessor sqlProcessor =
        new SQLProcessor("Query to test exceptions", "SELECT id, job FROM #table# WHERE state = |state| AND name = |name|");
      sqlProcessor.set("table", "foo");
      sqlProcessor.set("state", "closed");
      sqlProcessor.set("name", "bar");

      sqlProcessor.execute(mockConnectionSource);

      assertTrue("Exception expected", false);
    }
    catch (SQLSystemException e)
    {
      assertEquals("SQL Description: Query to test exceptions\nSQL: SELECT id, job FROM foo WHERE state = 'closed' AND name = 'bar'\nNested exception message: Test SQLException",
        e.getMessage());
    }

    mockPreparedStatement.verify();
    mockConnection.verify();
    mockConnectionSource.verify();
  }

  public void testResultSetNext() throws SystemException
  {
    mockResultSet.setExpectedNextCalls(1);

    mockPreparedStatement.addResultSet(mockResultSet);
    mockPreparedStatement.addExpectedSetParameter(1, "closed");
    mockPreparedStatement.addExpectedSetParameter(2, "bar");
    mockPreparedStatement.setExpectedExecuteCalls(1);
    mockPreparedStatement.setExpectedCloseCalls(1);

    mockConnection.addExpectedPreparedStatement(mockPreparedStatement);
    mockConnection.addExpectedPreparedStatementString("SELECT id, job FROM foo WHERE state = ? AND name = ?");

    try
    {
      SQLProcessor sqlProcessor =
        new SQLProcessor("SELECT id, job FROM #table# WHERE state = |state| AND name = |name|")
        {
          protected void process(ResultSet resultSet) throws SQLException
          {

            boolean prevFailed = false;
            try
            {
              resultSet.previous();
              assertTrue("Shouldn't reach here", false);
            }
            catch (UnsupportedOperationException e)
            {
              prevFailed = true;
            }
            assertTrue("previous threw exception",prevFailed);

            boolean nextFailed = false;
            try
            {
              resultSet.next();
              assertTrue("Shouldn't reach here", false);
            }
            catch (UnsupportedOperationException e)
            {
              nextFailed = true;
            }
            assertTrue("next threw exception",nextFailed);

            boolean closeFailed = false;
            try
            {
              resultSet.close();
              assertTrue("Shouldn't reach here", false);
            }
            catch (UnsupportedOperationException e)
            {
              closeFailed = true;
            }
            assertTrue("close threw exception",closeFailed);

            resultSet.next();
            assertTrue("Should not reach here", false);
          }
        };

      sqlProcessor.set("table", "foo");
      sqlProcessor.set("state", "closed");
      sqlProcessor.set("name", "bar");

      sqlProcessor.execute(mockConnectionSource);
      assertTrue("Exception expected", false);
    }
    catch (UnsupportedOperationException e)
    {
      assertEquals("ResultSet.next() cannot be called on this result set", e.getMessage());
    }

    mockResultSet.verify();
    mockPreparedStatement.verify();
    mockConnection.verify();
    mockConnectionSource.verify();
  }

  public void testResultSetClose()
  {
    mockResultSet.setExpectedNextCalls(1);

    mockPreparedStatement.addResultSet(mockResultSet);
    mockPreparedStatement.addExpectedSetParameter(1, "closed");
    mockPreparedStatement.addExpectedSetParameter(2, "bar");
    mockPreparedStatement.setExpectedExecuteCalls(1);
    mockPreparedStatement.setExpectedCloseCalls(1);

    mockConnection.addExpectedPreparedStatement(mockPreparedStatement);
    mockConnection.addExpectedPreparedStatementString("SELECT id, job FROM foo WHERE state = ? AND name = ?");

    try
    {
      SQLProcessor sqlProcessor =
        new SQLProcessor("SELECT id, job FROM #table# WHERE state = |state| AND name = |name|")
        {
          protected void process(ResultSet resultSet) throws SQLException
          {
            resultSet.close();
          }
        };

      setAndExecute(sqlProcessor, mockConnectionSource);

      assertTrue("Exception expected", false);
    }
    catch (UnsupportedOperationException e)
    {
      assertEquals("ResultSet.close() cannot be called on this result set", e.getMessage());
    }

    verifyAll();
  }

  public void testSetBean()
  {
    mockPreparedStatement.addResultSet(mockResultSet);
    mockPreparedStatement.addExpectedSetParameter(1, "closed");
    mockPreparedStatement.addExpectedSetParameter(2, "bar");
    mockPreparedStatement.setExpectedExecuteCalls(1);
    mockPreparedStatement.setExpectedCloseCalls(1);

    mockConnection.addExpectedPreparedStatement(mockPreparedStatement);
    mockConnection.addExpectedPreparedStatementString("SELECT id, job FROM foo WHERE state = ? AND name = ?");

    SQLProcessor sqlProcessor = new SQLProcessor("SELECT id, job FROM #table# WHERE state = |state| AND name = |name|");

    sqlProcessor.setBean(new Object()
    {
      public String getName()
      {
        return "baz";
      }

      public String getState()
      {
        return "closed";
      }
    });
    sqlProcessor.set("table", "foo");
    sqlProcessor.set("name", "bar");
    sqlProcessor.execute(mockConnectionSource);

    verifyAll();
  }

  public void testIterator()
  {
    class BeanTester
    {
      public BeanTester(String state)
      {
        this.state = state;
      }

      public String getState()
      {
        return state;
      }

      private String state;
    }
    List beanList = new LinkedList();
    beanList.add(new BeanTester("closed"));
    beanList.add(new BeanTester("open"));
    beanList.add(new BeanTester("delayed"));

    mockPreparedStatement.addExpectedSetParameter(1, "closed");
    mockPreparedStatement.addExpectedSetParameter(1, "open");
    mockPreparedStatement.addExpectedSetParameter(1, "delayed");
    mockPreparedStatement.addExpectedSetParameter(2, "bar");
    mockPreparedStatement.setupUpdateCount(1);
    mockPreparedStatement.setExpectedExecuteCalls(3);

    mockConnection.addExpectedPreparedStatementString("UPDATE foo SET state = ? WHERE name = ?");
    mockConnection.addExpectedPreparedStatement(mockPreparedStatement);

    SQLProcessor sqlProcessor = new SQLProcessor("UPDATE #table# SET state = |state| WHERE name = |name|");

    sqlProcessor.setBean(beanList.iterator());
    sqlProcessor.set("table", "foo");
    sqlProcessor.set("name", "bar");
    sqlProcessor.execute(mockConnectionSource);

    mockPreparedStatement.verify();
    mockConnection.verify();
    mockConnectionSource.verify();
  }

  public void testResultsExistTrue()
  {
    mockPreparedStatement.addResultSet(mockResultSet);
    mockPreparedStatement.addExpectedSetParameter(1, "closed");
    mockPreparedStatement.addExpectedSetParameter(2, "bar");
    mockPreparedStatement.setExpectedExecuteCalls(1);
    mockPreparedStatement.setExpectedCloseCalls(1);

    mockConnection.addExpectedPreparedStatementString("SELECT id, job FROM foo WHERE state = ? AND name = ?");
    mockConnection.addExpectedPreparedStatement(mockPreparedStatement);
    SQLProcessor sqlProcessor = new SQLProcessor("SELECT id, job FROM #table# WHERE state = |state| AND name = |name|");

    setAndExecute(sqlProcessor, mockConnectionSource);
    assertTrue(sqlProcessor.resultsExist());
    verifyAll();
  }

  public void testResultsExistFalse()
  {
    mockResultSet.setupRows(new Object[0][0]);
    mockResultSet.setExpectedNextCalls(1);

    mockPreparedStatement.addResultSet(mockResultSet);
    mockPreparedStatement.addExpectedSetParameter(1, "closed");
    mockPreparedStatement.addExpectedSetParameter(2, "bar");
    mockPreparedStatement.setExpectedExecuteCalls(1);
    mockPreparedStatement.setExpectedCloseCalls(1);

    mockConnection.addExpectedPreparedStatementString("SELECT id, job FROM foo WHERE state = ? AND name = ?");
    mockConnection.addExpectedPreparedStatement(mockPreparedStatement);

    SQLProcessor sqlProcessor =
      new SQLProcessor("SELECT id, job FROM #table# WHERE state = |state| AND name = |name|");

    setAndExecute(sqlProcessor, mockConnectionSource);
    assertFalse(sqlProcessor.resultsExist());
    verifyAll();
  }

  public void testGetSingleResult()
  {
    mockResultSet.setupRows(new Object[][]{{"testval"}});
    mockResultSet.setExpectedNextCalls(2);

    mockPreparedStatement.addResultSet(mockResultSet);
    mockPreparedStatement.addExpectedSetParameter(1, "closed");
    mockPreparedStatement.addExpectedSetParameter(2, "bar");
    mockPreparedStatement.setExpectedExecuteCalls(1);
    mockPreparedStatement.setExpectedCloseCalls(1);

    mockConnection.addExpectedPreparedStatementString("SELECT id FROM foo WHERE state = ? AND name = ?");
    mockConnection.addExpectedPreparedStatement(mockPreparedStatement);

    SQLProcessor sqlProcessor =
      new SQLProcessor("SELECT id FROM #table# WHERE state = |state| AND name = |name|");

    setAndExecute(sqlProcessor, mockConnectionSource);
    assertEquals("testval", sqlProcessor.getSingleResult());
    assertTrue(sqlProcessor.isSingleResultEqual("testval"));
    assertFalse(sqlProcessor.isSingleResultEqual("blahblah"));
    verifyAll();
  }

  public void testGetSingleResultNull()
  {
    mockResultSet.setupRows(new Object[0][0]);
    mockResultSet.setExpectedNextCalls(1);

    mockPreparedStatement.addResultSet(mockResultSet);
    mockPreparedStatement.addExpectedSetParameter(1, "closed");
    mockPreparedStatement.addExpectedSetParameter(2, "bar");
    mockPreparedStatement.setExpectedExecuteCalls(1);
    mockPreparedStatement.setExpectedCloseCalls(1);

    mockConnection.addExpectedPreparedStatementString("SELECT id FROM foo WHERE state = ? AND name = ?");
    mockConnection.addExpectedPreparedStatement(mockPreparedStatement);

    SQLProcessor sqlProcessor = new SQLProcessor("SELECT id FROM #table# WHERE state = |state| AND name = |name|");

    setAndExecute(sqlProcessor, mockConnectionSource);
    assertNull(sqlProcessor.getSingleResult());
    assertFalse(sqlProcessor.isSingleResultEqual("testval"));
    verifyAll();
  }

  public void testParameterNotSet() throws SystemException
  {
    mockPreparedStatement = new _MockPreparedStatement();
    mockPreparedStatement.addExpectedSetParameter(1, "bar");
    mockPreparedStatement.setExpectedExecuteCalls(0);
    mockPreparedStatement.setExpectedCloseCalls(1);
    mockConnection.addExpectedPreparedStatement(mockPreparedStatement);

    try
    {
      SQLProcessor sqlProcessor =
        new SQLProcessor("SELECT id, job FROM #table# WHERE state = |state| AND name = |name|")
        {
          protected void process(ResultSet resultSet) throws SQLException
          {
            resultSet.close();
          }
        };

      sqlProcessor.set("table", "foo");
      sqlProcessor.set("state", "bar");

      sqlProcessor.execute(mockConnectionSource);
      assertTrue("Exception expected", false);
    }
    catch (IllegalArgumentException e)
    {
      assertEquals("The parameter |name| was not set\n" +
        "SQL Description: <No description>\n" +
        "SQL: SELECT id, job FROM #table# WHERE state = |state| AND name = |name|", e.getMessage());
    }

    mockPreparedStatement.verify();
    mockConnection.verify();
    mockConnectionSource.verify();
  }

  public void testSetupAndIterator()
  {
    class BeanTester
    {
      public BeanTester(String state)
      {
        this.state = state;
      }

      public String getState()
      {
        return state;
      }

      private String state;
    }
    List beanList = new LinkedList();
    beanList.add(new BeanTester("closed"));
    beanList.add(new BeanTester("open"));

    mockPreparedStatement.addExpectedSetParameter(1, "closed");
    mockPreparedStatement.addExpectedSetParameter(1, "open");
    mockPreparedStatement.addExpectedSetParameter(2, "bar");
    mockPreparedStatement.setupUpdateCount(1);
    mockPreparedStatement.setExpectedExecuteCalls(2);

    mockConnection.addExpectedPreparedStatementString("UPDATE foo SET state = ? WHERE name = ?");
    mockConnection.addExpectedPreparedStatement(mockPreparedStatement);

    SQLProcessor sqlProcessor =
      new SQLProcessor("UPDATE #table# SET state = |state| WHERE name = |name|")
      {
        protected boolean setUp()
        {
          return i++ < 2;
        }

        int i = 0;
      };

    sqlProcessor.setBean(beanList.iterator());
    sqlProcessor.set("table", "foo");
    sqlProcessor.set("name", "bar");
    sqlProcessor.execute(mockConnectionSource);

    mockPreparedStatement.verify();
    mockConnection.verify();
    mockConnectionSource.verify();
  }

  public void testSetNull()
  {
    mockPreparedStatement.addExpectedSetParameter(1, null);
    mockPreparedStatement.addExpectedSetParameter(2, "bar");
    mockPreparedStatement.setExpectedExecuteCalls(1);
    mockPreparedStatement.setExpectedCloseCalls(1);
    mockPreparedStatement.setupUpdateCount(1);

    mockConnection.addExpectedPreparedStatementString("UPDATE foo SET state = ? WHERE name = ?");
    mockConnection.addExpectedPreparedStatement(mockPreparedStatement);

    SQLProcessor sqlProcessor =
      new SQLProcessor("UPDATE #table# SET state = |state| WHERE name = |name|");


    sqlProcessor.set("table", "foo");
    sqlProcessor.setNull("state", Types.CHAR);
    sqlProcessor.set("name", "bar");

    sqlProcessor.execute(mockConnectionSource);

    mockPreparedStatement.verify();
    mockConnection.verify();
    mockConnectionSource.verify();
  }

  public void testChainingParameterEvaluator()
  {
    mockPreparedStatement.addResultSet(mockResultSet);
    mockPreparedStatement.addExpectedSetParameter(1, "closed");
    mockPreparedStatement.addExpectedSetParameter(2, "bar");
    mockPreparedStatement.setExpectedExecuteCalls(1);
    mockPreparedStatement.setExpectedCloseCalls(1);

    mockConnection.addExpectedPreparedStatementString("SELECT id, job FROM foo WHERE state = ? AND name = ?");
    mockConnection.addExpectedPreparedStatement(mockPreparedStatement);

    ParameterEvaluator testingEvaluator = new ParameterEvaluator()
    {
      public Object getParameterValue(String parameter, Object suggestedValue)
      {
        if(suggestedValue==null)
        {
          return "closed";
        }
        else
        {
          return suggestedValue;
        }
      }
    };

    SQLProcessor sqlProcessor = new SQLProcessor("SELECT id, job FROM #table# WHERE state = |state| AND name = |name|");
    sqlProcessor.addEvaluator(testingEvaluator);
    sqlProcessor.set("table", "foo");
    sqlProcessor.set("name", "bar");

    sqlProcessor.execute(mockConnectionSource);
  }

  public class Bean
  {
    private String state;
    private String name;
    private int distance;

    public Bean()
    {
      this("closed","bar",1);
    }

    public Bean(String state, String name, int distance)
    {
      this.state = state;
      this.name = name;
      this.distance = distance;
    }

    public String getState()
    {
      return state;
    }

    public String getName()
    {
      return name;
    }

    public int getDistance()
    {
      return distance;
    }
  };

  public void testBeanShellParameterEvaluator()
  {
    mockPreparedStatement.addResultSet(mockResultSet);
    mockPreparedStatement.addExpectedSetParameter(1, "closed");
    mockPreparedStatement.addExpectedSetParameter(2, "bar");
    mockPreparedStatement.setExpectedExecuteCalls(1);
    mockPreparedStatement.setExpectedCloseCalls(1);

    mockConnection.addExpectedPreparedStatementString("SELECT id, job FROM foo WHERE state = ? AND name = ?");
    mockConnection.addExpectedPreparedStatement(mockPreparedStatement);

    MultiBeanSQLProcessor sqlProcessor = new MultiBeanSQLProcessor("testing","SELECT id, job FROM #table# WHERE state = |dbdbean.getState()| AND name = |dbdbean.getName()|");
    sqlProcessor.set("dbdbean", new Bean());
    sqlProcessor.set("table","foo");

    sqlProcessor.execute(mockConnectionSource);
  }

  public void testBeanShellParameterEvaluatorWithNull()
  {
    mockPreparedStatement.addResultSet(mockResultSet);
    mockPreparedStatement.addExpectedSetParameter(1, null);
    mockPreparedStatement.addExpectedSetParameter(2, null);
    mockPreparedStatement.addExpectedSetParameter(3, null);
    mockPreparedStatement.setExpectedExecuteCalls(1);
    mockPreparedStatement.setExpectedCloseCalls(1);

    mockConnection.addExpectedPreparedStatementString("SELECT id, job FROM foo WHERE state = ? AND name = ? AND foo=?");
    mockConnection.addExpectedPreparedStatement(mockPreparedStatement);

    final boolean[] hit=new boolean[]{false,false,false};
    ParameterEvaluator mockParameterEvaluator = new ParameterEvaluator()
    {
      public Object getParameterValue(String parameter, Object suggestedValue)
      {
        if(parameter.equals("dbdbean.state;CHAR"))
        {
          assertEquals("should be char",Types.CHAR,((SQLNull)suggestedValue).getType());
          hit[0]=true;
        }
        if(parameter.equals("dbdbean.name"))
        {
          assertEquals("should be other",Types.OTHER,((SQLNull)suggestedValue).getType());
          hit[1]=true;
        }
        if(parameter.equals("null;JAVA_OBJECT"))
        {
          assertEquals("should be int",Types.JAVA_OBJECT,((SQLNull)suggestedValue).getType());
          hit[2]=true;
        }
        return suggestedValue;
      }
    };

    MultiBeanSQLProcessor sqlProcessor = new MultiBeanSQLProcessor("testing","SELECT id, job FROM #table# WHERE state = |dbdbean.state;CHAR| AND name = |dbdbean.name| AND foo=|null;JAVA_OBJECT|");
    sqlProcessor.addEvaluator(mockParameterEvaluator);
    sqlProcessor.set("dbdbean", new Bean(null,null,5));
    sqlProcessor.set("table","foo");

    sqlProcessor.execute(mockConnectionSource);

    assertTrue("all were hit",hit[0]&&hit[1]&&hit[2]);

    mockPreparedStatement.verify();
    mockConnection.verify();
  }

  public void testBeanShellParameterEvaluatorWithIterator()
  {
    MockMultiRowResultSet res1 = createMockResultSet();
    mockPreparedStatement.addResultSet(res1);

    MockMultiRowResultSet res2 = createMockResultSet();
    mockPreparedStatement.addResultSet(res2);

    MockMultiRowResultSet res3 = createMockResultSet();
    mockPreparedStatement.addResultSet(res3);

    mockPreparedStatement.addExpectedSetParameter(1, "closed");
    mockPreparedStatement.addExpectedSetParameter(2, "STEVE");
    mockPreparedStatement.addExpectedSetParameter(2, "RICH");
    mockPreparedStatement.addExpectedSetParameter(2, "LESLIE");

    mockPreparedStatement.setExpectedExecuteCalls(3);
    mockPreparedStatement.setExpectedCloseCalls(1);

    mockConnection.addExpectedPreparedStatementString("SELECT id, job FROM foo WHERE state = ? AND name = ?");
    mockConnection.addExpectedPreparedStatement(mockPreparedStatement);

    java.util.List names = new LinkedList();

    names.add("Steve");
    names.add("Rich");
    names.add("Leslie");

    MultiBeanSQLProcessor sqlProcessor = new MultiBeanSQLProcessor("testing", "SELECT id, job FROM #table# WHERE state = |dbdbean.state| AND name = |name.toUpperCase()|");

    sqlProcessor.setIteratedBean("name", names.iterator());
    sqlProcessor.set("dbdbean", new Bean());
    sqlProcessor.set("table", "foo");

    sqlProcessor.execute(mockConnectionSource);

    res1.verify();
    res2.verify();
    res3.verify();
    mockPreparedStatement.verify();
    mockConnection.verify();
    mockConnectionSource.verify();
  }

  public void testBeanShellParameterEvaluatorWithEmptyIterator()
  {
    mockPreparedStatement.setExpectedExecuteCalls(0);
    mockPreparedStatement.setExpectedCloseCalls(1);

    mockConnection.addExpectedPreparedStatementString("SELECT id, job FROM foo WHERE state = ? AND name = ?");
    mockConnection.addExpectedPreparedStatement(mockPreparedStatement);

    java.util.List names = new LinkedList();

    MultiBeanSQLProcessor sqlProcessor = new MultiBeanSQLProcessor("testing", "SELECT id, job FROM #table# WHERE state = |dbdbean.state| AND name = |name.toUpperCase()|")
    {
      protected void process(ResultSet resultSet) throws SQLException
      {
        assertTrue("Should never be executed",false);
      }
    };

    sqlProcessor.setIteratedBean("name", names.iterator());
    sqlProcessor.set("dbdbean", new Bean());
    sqlProcessor.set("table", "foo");

    sqlProcessor.execute(mockConnectionSource);

    mockPreparedStatement.verify();
    mockConnection.verify();
    mockConnectionSource.verify();
  }

  public void testBeanShellLoggingCapability()
  {
    MockMultiRowResultSet res1 = createMockResultSet();
    mockPreparedStatement.addResultSet(res1);

    MockMultiRowResultSet res2 = createMockResultSet();
    mockPreparedStatement.addResultSet(res2);

    MockMultiRowResultSet res3 = createMockResultSet();
    mockPreparedStatement.addResultSet(res3);

    mockPreparedStatement.addExpectedSetParameter(1, "closed");
    mockPreparedStatement.addExpectedSetParameter(2, "STEVE");
    mockPreparedStatement.addExpectedSetParameter(2, "RICH");
    mockPreparedStatement.addExpectedSetParameter(2, "LESLIE");

    mockPreparedStatement.setExpectedExecuteCalls(3);
    mockPreparedStatement.setExpectedCloseCalls(1);

    mockConnection.addExpectedPreparedStatementString("SELECT id, job FROM foo WHERE state = ? AND name = ?");
    mockConnection.addExpectedPreparedStatement(mockPreparedStatement);

    java.util.List names = new LinkedList();

    names.add("Steve");
    names.add("Rich");
    names.add("Leslie");

    final List infos = new LinkedList();
    LoggingCapability mockLogger = new LoggingCapability()
    {
      public void logInfo(String information)
      {
        infos.add(information);
      }

      public void logWarning(String error, Throwable t)
      {
      }

      public void logError(String error, Throwable t)
      {
      }
    };

    MultiBeanSQLProcessor sqlProcessor = new MultiBeanSQLProcessor("testing", "SELECT id, job FROM #table# WHERE state = |dbdbean.state| AND name = |name.toUpperCase()|");
    sqlProcessor.setLoggingCapability(mockLogger);

    sqlProcessor.setIteratedBean("name", names.iterator());
    sqlProcessor.set("dbdbean", new Bean());
    sqlProcessor.set("table", "foo");

    sqlProcessor.execute(mockConnectionSource);

    assertEquals("Logged info messages",3,infos.size());
    assertEquals("Logged first message","testing: SELECT id, job FROM foo WHERE state = 'closed' AND name = 'STEVE'",infos.get(0));
    assertEquals("Logged first message","testing: SELECT id, job FROM foo WHERE state = 'closed' AND name = 'RICH'",infos.get(1));
    assertEquals("Logged first message","testing: SELECT id, job FROM foo WHERE state = 'closed' AND name = 'LESLIE'",infos.get(2));

    res1.verify();
    res2.verify();
    res3.verify();
    mockPreparedStatement.verify();
    mockConnection.verify();
    mockConnectionSource.verify();
  }

  public void testInsertedIdCapability()
  {
    mockPreparedStatement.addExpectedSetParameter(1, "closed");
    mockPreparedStatement.addExpectedSetParameter(2, "STEVE");
    mockPreparedStatement.addExpectedSetParameter(2, "RICH");
    mockPreparedStatement.addExpectedSetParameter(2, "LESLIE");

    mockPreparedStatement.setExpectedExecuteCalls(3);
    mockPreparedStatement.setExpectedCloseCalls(1);

    mockConnection.addExpectedPreparedStatementString("INSERT INTO foo VALUES (?, ?)");
    mockConnection.addExpectedPreparedStatement(mockPreparedStatement);

    java.util.List names = new LinkedList();
    names.add("Steve");
    names.add("Rich");
    names.add("Leslie");

    InsertedIdCapability mockInsertedIdCapability = new InsertedIdCapability()
    {
      int i = 0;
      public BigInteger fetchLastInsertedId(Connection conn)
      {
        return new BigInteger(String.valueOf(i++));
      }
    };

    MultiBeanSQLProcessor sqlProcessor = new MultiBeanSQLProcessor("testing", "INSERT INTO #table# VALUES (|dbdbean.state|, |name.toUpperCase()|)");
    sqlProcessor.setInsertedIdCapability(mockInsertedIdCapability);
    sqlProcessor.setIteratedBean("name", names.iterator());
    sqlProcessor.set("dbdbean", new Bean());
    sqlProcessor.set("table", "foo");

    sqlProcessor.execute(mockConnectionSource);

    assertEquals("Collected inserted ids",sqlProcessor.getInsertedIds().length,3);
    assertEquals("Last inserted id",sqlProcessor.getLastInsertedId(),new BigInteger("2"));

    mockPreparedStatement.verify();
    mockConnection.verify();
    mockConnectionSource.verify();
  }

  private void setAndExecute(SQLProcessor sqlProcessor, ConnectionSource connectionSource)
  {
    sqlProcessor.set("table", "foo");
    sqlProcessor.set("state", "closed");
    sqlProcessor.set("name", "bar");

    sqlProcessor.execute(connectionSource);
  }

  private void verifyAll()
  {
    mockResultSet.verify();
    mockPreparedStatement.verify();
    mockConnection.verify();
    mockConnectionSource.verify();
  }

  private MockMultiRowResultSet mockResultSet;
  private MockPreparedStatement mockPreparedStatement;
  private MockConnection mockConnection;
  private _MockConnectionSource mockConnectionSource;
}
