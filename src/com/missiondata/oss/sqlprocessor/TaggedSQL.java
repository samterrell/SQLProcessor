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

import java.util.*;

/**
 * @author Leslie Hensley
 */
public class TaggedSQL
{
  public TaggedSQL(String sqlText)
  {
    parseString(sqlText);
  }

  public boolean isQuery()
  {
    return ((String) parsedString.get(0)).trim().toUpperCase().startsWith("SELECT");
  }

  public boolean isInsert()
  {
    return ((String) parsedString.get(0)).trim().toUpperCase().startsWith("INSERT");
  }

  public void setSubstitution(String key, Object value)
  {
    if (isSubstitutionKey(key))
    {
      substitutionValues.put(key, value);
      dirty = true;
    }
    else
    {
      throw new IllegalArgumentException(key + " is not a substitution name");
    }
  }


  public String getPreparedStringForLogging()
  {

    Iterator keys = substitutionIndices.keySet().iterator();
    List parsedString = new LinkedList(this.parsedString);

    while (keys.hasNext())
    {
      String key = (String) keys.next();
      String subval = null;

      if (substitutionValues.containsKey(key))
      {
        subval = "" + substitutionValues.get(key);
      }
      else
      {
        subval = "!SUB{" + key + "}!";
      }

      for (Iterator i = getSubstitutionIndices(key).iterator(); i.hasNext();)
      {
        int idx = ((Integer) i.next()).intValue();
        parsedString.set(idx, subval);
      }
    }


    StringBuffer statementText = new StringBuffer();
    Iterator iterator = parsedString.iterator();

    while (iterator.hasNext())
    {
      statementText.append(iterator.next());
    }

    return statementText.toString();
  }


  public String getPreparedString()
  {
    Iterator keys = substitutionIndices.keySet().iterator();
    while (keys.hasNext())
    {
      String key = (String) keys.next();
      if (substitutionValues.containsKey(key))
      {
        for (Iterator i = getSubstitutionIndices(key).iterator(); i.hasNext();)
        {
          int idx = ((Integer) i.next()).intValue();
          parsedString.set(idx, substitutionValues.get(key));
        }
      }
      else
      {
        throw new IllegalArgumentException("The substitution #" + key + "# was not set");
      }
    }

    StringBuffer statementText = new StringBuffer();
    Iterator iterator = parsedString.iterator();
    while (iterator.hasNext())
    {
      statementText.append(iterator.next());
    }
    dirty = false;
    return statementText.toString();
  }

  public Iterator getParameterKeys()
  {
    return parameterIndices.keySet().iterator();
  }

  public boolean isSubstitutionKey(String key)
  {
    return substitutionIndices.containsKey(key);
  }

  public boolean isParameterKey(String key)
  {
    return parameterIndices.containsKey(key);
  }

  public boolean isKey(String key)
  {
    return isSubstitutionKey(key) || isParameterKey(key);
  }

  public List getParameterIndices(String key)
  {
    return ((List) parameterIndices.get(key));
  }


  public List getSubstitutionIndices(String key)
  {
    return ((List) substitutionIndices.get(key));
  }

  public String getParameterKey(int index)
  {
    for (Iterator i = parameterIndices.entrySet().iterator(); i.hasNext();)
    {
      Map.Entry entry = (Map.Entry) i.next();
      List list = (List) entry.getValue();
      if (list.contains(new Integer(index)))
      {
        return (String) entry.getKey();
      }
    }
    throw new IllegalArgumentException("Index " + index + " not found.");
  }

  public boolean isDirty()
  {
    return dirty;
  }

  private void parseString(String sqlText)
  {
    // convert |var| to ?
    int paramCount = 1;
    int lastIndex = 0;
    int newIndex;
    StringBuffer convertedString = new StringBuffer();
    while ((newIndex = sqlText.indexOf("|", lastIndex)) != -1)
    {
      if (newIndex == sqlText.length() - 1)
      {
        throw new IllegalArgumentException("SQL text contains unbalanced | : " + sqlText);
      }

      // skip if the | is escaped
      if (sqlText.substring(newIndex + 1, newIndex + 2).equals("|"))
      {
        convertedString.append(sqlText.substring(lastIndex, newIndex + 1));
        lastIndex = newIndex + 2;
        continue;
      }
      convertedString.append(sqlText.substring(lastIndex, newIndex));
      convertedString.append("?");

      lastIndex = newIndex + 1;
      newIndex = sqlText.indexOf("|", lastIndex);
      if (newIndex == -1)
      {
        throw new IllegalArgumentException("SQL text contains unbalanced | : " + sqlText);
      }

      if (isSubstitutionKey(sqlText.substring(lastIndex, newIndex)))

      {
        throw new IllegalArgumentException("Cannot use key for substitions and parameters : " + sqlText.substring(lastIndex, newIndex));
      }
      else
      {
        addParameterIndex(sqlText.substring(lastIndex, newIndex), paramCount++);
      }
      lastIndex = newIndex + 1;
    }
    convertedString.append(sqlText.substring(lastIndex, sqlText.length()));

    // How can this be done with only one pass through the string?
    //   Is it worth using regex on this?

    // parse #
    int settingCount = 1;
    String passTwo = convertedString.toString();
    lastIndex = 0;
    while ((newIndex = passTwo.indexOf("#", lastIndex)) != -1)
    {
      // skip if the # is escaped
      if (passTwo.substring(newIndex + 1, newIndex + 2).equals("#"))
      {
        parsedString.add(passTwo.substring(lastIndex, newIndex + 1));
        lastIndex = newIndex + 2;
        settingCount++;
        continue;
      }
      parsedString.add(passTwo.substring(lastIndex, newIndex));

      lastIndex = newIndex + 1;
      newIndex = passTwo.indexOf("#", lastIndex);
      if (newIndex == -1)
      {
        throw new IllegalArgumentException("SQL text contains unbalanced # : " + sqlText);
      }


      if (isParameterKey(passTwo.substring(lastIndex, newIndex)))
      {
        throw new IllegalArgumentException("Cannot use key for substitions and parameters : " + passTwo.substring(lastIndex, newIndex));
      }
      addSubstitutionIndex(passTwo.substring(lastIndex, newIndex), settingCount);

      settingCount += 2;
      parsedString.add(passTwo.substring(lastIndex - 1, newIndex + 1));
      lastIndex = newIndex + 1;
    }
    parsedString.add(passTwo.substring(lastIndex, passTwo.length()));
  }


  private void addParameterIndex(String key, int position)
  {
    addAnEntry(parameterIndices, key, position);
  }


  private void addSubstitutionIndex(String key, int position)
  {
    addAnEntry(substitutionIndices, key, position);
  }


  private void addAnEntry(Map map, String key, int position)
  {
    List entries = (List) map.get(key);

    if (entries == null)
    {
      entries = new LinkedList();
      map.put(key, entries);
    }

    entries.add(new Integer(position));
  }

  private boolean dirty = true;
  private List parsedString = new ArrayList();

  private Map parameterIndices = new HashMap();
  private Map substitutionIndices = new HashMap();
  private Map substitutionValues = new HashMap();
}
