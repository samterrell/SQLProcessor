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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Leslie Hensley
 */
public class _TaggedSQLTest extends TestCase
{
  public _TaggedSQLTest(String name)
  {
    super(name);
  }

  public void testGetPreparedString()
  {
    TaggedSQL taggedSQL = new TaggedSQL("UPDATE #table# SET state = |state| WHERE name = |name| and type = 'A'");
    taggedSQL.setSubstitution("table", "foo");
    assertEquals("UPDATE foo SET state = ? WHERE name = ? and type = 'A'", taggedSQL.getPreparedString());
  }

  public void testSetInvalidSubstitution()
  {
    try
    {
      TaggedSQL taggedSQL = new TaggedSQL("UPDATE #table# SET state = |state| WHERE name = |name|");
      taggedSQL.setSubstitution("notfound", "foo");
      assertTrue("Exception not thrown", false);
    }
    catch (IllegalArgumentException e)
    {
      assertEquals("notfound is not a substitution name", e.getMessage());
    }
  }

  public void testSubstitutionNotSet()
  {
    try
    {
      TaggedSQL taggedSQL = new TaggedSQL("UPDATE #table# SET state = |state| WHERE name = |name|");
      taggedSQL.getPreparedString();
      assertTrue("Exception not thrown", false);
    }
    catch (IllegalArgumentException e)
    {
      assertEquals("The substitution #table# was not set", e.getMessage());
    }
  }

  public void testUnbalancedPound()
  {
    try
    {
      TaggedSQL taggedSQL = new TaggedSQL("UPDATE #table SET state = |state| WHERE name = |name|");
      assertTrue("Exception not thrown", false);
    }
    catch (IllegalArgumentException e)
    {
      assertEquals("SQL text contains unbalanced # : UPDATE #table SET state = |state| WHERE name = |name|", e.getMessage());
    }
  }

  public void testUnbalancedPipe()
  {
    try
    {
      TaggedSQL taggedSQL = new TaggedSQL("UPDATE #table# SET state = |state WHERE name = |name|");
      assertTrue("Exception not thrown", false);
    }
    catch (IllegalArgumentException e)
    {
      assertEquals("SQL text contains unbalanced | : UPDATE #table# SET state = |state WHERE name = |name|", e.getMessage());
    }
  }

  public void testDuplicateKey()
  {
    try
    {
      TaggedSQL taggedSQL = new TaggedSQL("UPDATE #table# SET state = |table| WHERE name = |name|");
      assertTrue("Exception not thrown", false);
    }
    catch (IllegalArgumentException e)
    {
      assertEquals("Cannot use key for substitions and parameters : table", e.getMessage());
    }
    try
    {
      TaggedSQL taggedSQL = new TaggedSQL("UPDATE |table| SET state = #table# WHERE name = |name|");
      assertTrue("Exception not thrown", false);
    }
    catch (IllegalArgumentException e)
    {
      assertEquals("Cannot use key for substitions and parameters : table", e.getMessage());
    }
  }

  public void testRepeatedSubstitute()
  {
    TaggedSQL taggedSQL = new TaggedSQL("UPDATE table SET #column# = |value|, #column#2 = |value| WHERE #column# = '#column#'");
    taggedSQL.setSubstitution("column", "tony");
    assertEquals("UPDATE table SET tony = ?, tony2 = ? WHERE tony = 'tony'", taggedSQL.getPreparedString());
    assertEquals(taggedSQL.getParameterIndices("value").size(), 2);
    assertEquals(taggedSQL.getSubstitutionIndices("column").size(), 4);
  }

  public void testEscapedPound()
  {
    TaggedSQL taggedSQL = new TaggedSQL("UPDATE #table# SET state = |state| WHERE name = |name| and type = '##'");
    taggedSQL.setSubstitution("table", "foo");
    assertEquals("UPDATE foo SET state = ? WHERE name = ? and type = '#'", taggedSQL.getPreparedString());
  }

  public void testEscapedPipe()
  {
    TaggedSQL taggedSQL = new TaggedSQL("UPDATE #table# SET state = |state| WHERE name = |name| and type = '||'");
    taggedSQL.setSubstitution("table", "foo");
    assertEquals("UPDATE foo SET state = ? WHERE name = ? and type = '|'", taggedSQL.getPreparedString());
  }

  public void testIsSubstitutionKey()
  {
    TaggedSQL taggedSQL = new TaggedSQL("UPDATE #table# SET state = |state| WHERE name = |name|");
    assertTrue(taggedSQL.isSubstitutionKey("table"));
    assertTrue(!taggedSQL.isSubstitutionKey("state"));
    assertTrue(!taggedSQL.isSubstitutionKey("blah"));
  }

  public void testIsParameterKey()
  {
    TaggedSQL taggedSQL = new TaggedSQL("UPDATE #table# SET state = |state| WHERE name = |name|");
    assertTrue(taggedSQL.isParameterKey("state"));
    assertTrue(!taggedSQL.isParameterKey("table"));
    assertTrue(!taggedSQL.isParameterKey("blah"));
  }

  public void testGetParameterIndex()
  {
    TaggedSQL taggedSQL = new TaggedSQL("UPDATE #table# SET state = |state|, anotherState = |state| WHERE name = |name|");
    assertTrue(taggedSQL.getParameterIndices("state").contains(new Integer(1)));
    assertTrue(taggedSQL.getParameterIndices("state").contains(new Integer(2)));
    assertTrue(taggedSQL.getParameterIndices("name").contains(new Integer(3)));
  }

  public void testIsDirty()
  {
    TaggedSQL taggedSQL = new TaggedSQL("UPDATE #table# SET state = |state| WHERE name = |name|");
    taggedSQL.setSubstitution("table", "foo");
    assertTrue(taggedSQL.isDirty());
    taggedSQL.getPreparedString();
    assertTrue(!taggedSQL.isDirty());
    taggedSQL.setSubstitution("table", "blah");
    assertTrue(taggedSQL.isDirty());
  }

  public void testIsQuery()
  {
    TaggedSQL taggedSQL = new TaggedSQL("sElect * from table1");
    assertEquals("Expected query", taggedSQL.isQuery(), true);

    taggedSQL = new TaggedSQL("UPDATE table1 set a = 2");
    assertEquals("Expected !query", taggedSQL.isQuery(), false);

    taggedSQL = new TaggedSQL("INSERT INTO table1 set fooble=|a2|, column_with_aselect = 3");
    assertEquals("Expected !query", false, taggedSQL.isQuery());

    taggedSQL = new TaggedSQL("UPDATE table_select set b = 2");
    assertFalse("Expected !query", taggedSQL.isQuery());
  }

  public void testIsInsert()
  {
    TaggedSQL taggedSQL = new TaggedSQL("sElect * from table1");
    assertEquals("Expected query", false, taggedSQL.isInsert());

    taggedSQL = new TaggedSQL("UPDATE table1 set a_insert = 2");
    assertEquals("Expected !query", false, taggedSQL.isInsert());

    taggedSQL = new TaggedSQL("INSERT INTO table1 set fooble=|a2|, column_with_aselect = 3");
    assertEquals("Expected !query", true, taggedSQL.isInsert());
  }

  public void testGetParameterKeys()
  {
    Set expectedKeys = new HashSet();
    expectedKeys.add("state");
    expectedKeys.add("name");

    TaggedSQL taggedSQL = new TaggedSQL("UPDATE #table# SET state = |state| WHERE name = |name|");

    Set actualKeys = new HashSet();
    for (Iterator iterator = taggedSQL.getParameterKeys(); iterator.hasNext();)
    {
      actualKeys.add(iterator.next());
    }

    assertEquals(expectedKeys, actualKeys);
  }
}
