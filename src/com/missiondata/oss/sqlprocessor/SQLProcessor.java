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


import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Types;
import java.util.*;

/**
 * sqlprocessor is a facade for parts of the JDBC API.  It hides much of the complexity
 * and danger of dealing directly with the JDBC API.  The best way to show this is with
 * an example:
 * Without sqlprocessor
 * <pre>
 * PreparedStatement statement = null;
 * try
 * {
 *   statement = user.getConnection().prepareStatement("select id from foo where name = ? and state = ?");
 *   statement.setString( 1, "bar" );
 *   statement.setString( 2, "paid" );
 *   ResultSet resultSet = statement.executeQuery();
 *   while( resultSet.next() )
 *   {
 *     processId( resultSet.getString( "id" );
 *   }
 * }
 * catch( SQLException e )
 * {
 *   throw SystemException( e );
 * }
 * finally
 * {
 *   DataUtil.closeStatement( statement );
 * }
 * </pre>
 *
 * With sqlprocessor:
 * <pre>
 * sqlprocessor sqlProcessor =
 *   new sqlprocessor( user.getDefaultSource(), "select id from foo where name = |name| and state = |state|" )
 *   {
 *     protected void process( ResultSet resultSet )
 *     {
 *       processId( resultSet.getString( "id" ) );
 *     }
 *   };
 * sqlProcessor.set( "name", "bar" );
 * sqlProcessor.set( "state", "paid" );
 * sqlProcessor.execute();;
 * </pre>

 * @author Leslie Hensley
 */
public class SQLProcessor extends AbstractSQLProcessorBase
{
  /**
   * @param description       the description is included in the messages of exceptions and
   *                          log messages generated by this class
   * @param sqlText           the text of the sql that will executed. A word surrounded by '#'
   *                          in the string indicates a substitution variable, and a word
   *                          surrounded by '|' indicates a parameter variables.  Parameter
   *                          variables can be used anywhere a '?' can be used in the JDBC API.
   *                          Substitution parameters can be used anywhere in the string but
   *                          are less efficient than parameter variables.
   */
  public SQLProcessor(String description, String sqlText)
  {
    super(description,sqlText);
    addEvaluator(new InternalDefaultParameterEvaluator());
    setBean(singletonIterator(new Object()));
  }

  /**
   * @param sqlText           the text of the sql that will executed. A word surrounded by '#'
   *                          in the string indicates a substitution variable, and a word
   *                          surrounded by '|' indicates a parameter variables.  Parameter
   *                          variables can be used anywhere a '?' can be used in the JDBC API.
   *                          Substitution parameters can be used anywhere in the string but
   *                          are less efficient than parameter variables.
   */
  public SQLProcessor(String sqlText)
  {
    this("<No description>", sqlText);
  }

  /**
   * Sets the value of a substitution variable or a parameter variable in the
   * string that was passed to the constructor.  Note that in the case of a
   * conflict in the setting of a parameter variable via this method and
   * <code>setBean</code> this method takes precedence.
   *
   * @ param key      the word that was between '#'s or '|'s in the sqlText
   * @ param value    what variable will be replaced with
   */
  public void set(String key, Object value)
  {
    if (taggedSQL.isParameterKey(key))
    {
      parameterValues.put(key, value);
    }
    else
    {
      taggedSQL.setSubstitution(key, value);
    }
  }

  public void setBigDecimal(String key, BigDecimal decimal, int scale, int roundingMode)
  {
    BigDecimal scaledDecimal = null;

    if (decimal != null)
    {
      scaledDecimal = decimal.setScale(scale, roundingMode);
      set(key, scaledDecimal);
    }
    else
    {
      setNull(key, Types.DECIMAL);
    }
  }

  public boolean hasKey(String key)
  {
    return taggedSQL.isKey(key);
  }

  public void setNull(String key, int type)
  {
    parameterValues.put(key, new SQLNull(type));
  }

  /**
   * A convenience method that wraps <code>value</value> in an <code>Integer</code>
   * and calls the base set method.
   */
  public void set(String key, int value)
  {
    set(key, new Integer(value));
  }

  /**
   * A convenience method that wraps <code>value</value> in an <code>Character</code>
   * and calls the base set method.
   */
  public void set(String key, char value)
  {
    set(key, new String(new char[]{value}));
  }

  /**
   * A convenience method that wraps <code>value</value> in a <code>Double</code>
   * and calls the base set method.
   */
  public void set(String key, double value)
  {
    set(key, new Double(value));
  }

  /**
   * Convenience method to set a possibly null value without having to check if the value is null.
   * @param key Key to set
   * @param value
   * @param sqlType SQL type used for a call to setNull if the value is null.
   */
  public void setNullable(String key, Object value, int sqlType)
  {
    if (value != null)
    {
      set(key, value);
    }
    else
    {
      setNull(key, sqlType);
    }
  }

  /**
   * Maps accessors in the form of <code>getX</code> on <code>bean</code> to
   * parameter variables.  For example if <code>bean</code> has a method
   * named <code>getName</code> the equivilent of
   * <code>set("name", bean.getName())</code> will be performed.
   *
   * @param bean a object with methods of the form <code>getX</code>
   */
  public void setBean(Object bean)
  {
    this.beanIterator = singletonIterator(bean);
  }

  private Iterator singletonIterator(Object bean)
  {
    List list = new ArrayList(1);
    list.add(bean);
    return list.iterator();
  }

  /**
   * Iterates over the bean objects in beanIterator and executes the query
   * once for each item in the iterator.  Note that if <code>setUp</code>
   * is overridden then the return value from <code>setUp</code> takes
   * precedence when the determination is made how many times to execute
   * the query.
   *
   * @param beanIterator
   */
  public void setBean(Iterator beanIterator)
  {
    this.beanIterator = beanIterator;
  }

  protected boolean isSetUp()
  {
    Iterator beanIt = getBeanIterator();

    if (beanIt.hasNext())
    {
      setCurrentBean(beanIt.next());
      return setUp();
    }
    else
    {
      return super.isSetUp();
    }
  }

  /**
   * Runs the update, insert or query that was specified in the constructor.
   *
   * @param connectionSource  the ConnectionSource for the database that the sql will act on
   */
  public int execute(ConnectionSource connectionSource) throws SQLSystemException
  {
    bean = EMPTYBEAN;
    return super.execute(connectionSource);
  }

  /**
   * Get last id's long value representation. If no results
   * were encountered during the sqlprocessor's execution
   * then a -1 is returned.
   */
  public long getLastInsertedIdValue()
  {
    long lastVal = -1;
    BigInteger last = getLastInsertedId();
    if (last != null)
    {
      lastVal = last.longValue();
    }
    return lastVal;
  }

  protected boolean isEmptyBean()
  {
    return this.bean instanceof EmptyBean;
  }

  protected Object currentBean()
  {
    return this.bean;
  }

  protected void setCurrentBean(Object bean)
  {
    this.bean = bean;
  }

  protected Iterator getBeanIterator()
  {
    return beanIterator;
  }

  protected Object getParameterValue(String key)
  {
    return parameterValues.get(key);
  }

  protected boolean hasParameterKey(String key)
  {
    return parameterValues.containsKey(key);
  }

  public void setNullType(String[] params, int type)
  {
    for (int i = 0; i < params.length; i++)
    {
      setNullType(params[i], type);
    }
  }

  public void setNullType(String key, int type)
  {
    beanToNullType.put(key, new SQLNull(type));
  }

  public void setDefaultNullType(Class theClass, int type)
  {
    classToNullType.put(theClass.toString(), new SQLNull(type));
  }

  protected SQLNull convertBeanToNullType(String parameter)
  {
    return (SQLNull)beanToNullType.get(parameter);
  }

  protected SQLNull convertClassToNullType(Class ret)
  {
    return (SQLNull)classToNullType.get(ret.toString());
  }

  private Object bean = EMPTYBEAN;
  private Iterator beanIterator;

  private Map parameterValues = new HashMap();
  private Map beanToNullType = new HashMap();
  private Map classToNullType = new HashMap();

  private static class EmptyBean extends Object
  {
  };
  protected static final EmptyBean EMPTYBEAN = new EmptyBean();

  private class InternalDefaultParameterEvaluator implements ParameterEvaluator
  {
    public Object getParameterValue(String parameter, Object suggestedValue) throws IllegalArgumentException
    {
      Object value = parameterValues.get(parameter);
      if (value == null)
      {
        String attemptedMethod = createGetterNameFromKey(parameter);

        try
        {
          Method method = null;
          try
          {
            method = SQLProcessor.this.bean.getClass().getMethod(attemptedMethod, null);
            value = method.invoke(SQLProcessor.this.bean, null);
          }
          catch (NoSuchMethodException e)
          {
          }
          if (value == null)
          {
            value = beanToNullType.get(parameter);
          }
          if (value == null && method != null)
          {
            Class ret = method.getReturnType();
            if (ret != null)
            {
              value = SQLProcessor.this.convertClassToNullType(ret);
            }
          }
        }
        catch (Exception e)
        {
          return null;
        }
      }
      return value;
    }

    protected String createGetterNameFromKey(String key)
    {
      String attemptedMethod = "get" + key.substring(0, 1).toUpperCase() + key.substring(1);
      return attemptedMethod;
    }
  }

}