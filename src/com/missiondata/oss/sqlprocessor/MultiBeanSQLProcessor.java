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

import java.util.Iterator;
import java.sql.Types;

/**
 * Support for multiple beans, a named bean iterator, and
 * <a href="http://www.beanshell.org">BeanShell</a> syntax in
 * the parameter value substitutions
 *
 * @author Darren Day
 */
public class MultiBeanSQLProcessor extends AbstractSQLProcessorBase
{
  private String iteratedBeanName;
  private Iterator beanIterator;
  boolean executedOnce=false;

  public MultiBeanSQLProcessor(String description, String sqlText)
  {
    super(description, sqlText);
    addEvaluator(parameterEvaluator);
  }

  public void setIteratedBean(String key,Iterator iterator)
  {
    iteratedBeanName = key;
    beanIterator = iterator;
  }

  public void set(String key,Object value)
  {
    if(taggedSQL.isSubstitutionKey(key))
    {
      taggedSQL.setSubstitution(key,value);
    }

    parameterEvaluator.set(key,value);
  }

  protected boolean isSetUp()
  {
    if(beanIterator!=null)
    {
      Object nextBean;
      if(beanIterator.hasNext())
      {
        nextBean = beanIterator.next();
        set(iteratedBeanName,nextBean);

        return setUp();
      }
      else
      {
        return false;
      }
    }
    else if (!executedOnce)
    {
      executedOnce=true;
      return setUp();
    }
    else
    {
      return super.isSetUp();
    }
  }

  private BeanShellParameterEvaluator parameterEvaluator = new BeanShellParameterEvaluator();

  private class BeanShellParameterEvaluator implements ParameterEvaluator
  {
    private bsh.Interpreter interpreter = new bsh.Interpreter();

    public BeanShellParameterEvaluator()
    {
      set("__nullFilter",new NullTypeFilter());
    }

    public void set(String key, Object value)
    {
      try
      {
        interpreter.set(key,value);
      }
      catch (bsh.EvalError evalError)
      {
        throw new SystemException("could not set in context",evalError);
      }
    }

    public Object getParameterValue(String parameter, Object suggestedValue)
    {
      Object val = null;
      try
      {
        val = interpreter.eval(parameter);
        if(val==null)
        {
          val = interpreter.eval("__nullFilter.nullFor("+parameter+")");
        }
      }
      catch (bsh.EvalError evalError)
      {
        throw new SystemException("could not evaluate",evalError);
      }
      return val;
    }
  }

  public static class NullTypeFilter
  {
    public SQLNull nullFor(Object o) { return new SQLNull(Types.OTHER); };
    public SQLNull nullFor(String s) { return new SQLNull(Types.CHAR); };
    public SQLNull nullFor(java.math.BigDecimal b) { return new SQLNull(Types.NUMERIC); };
    public SQLNull nullFor(Boolean b) { return new SQLNull(Types.BIT); };
    public SQLNull nullFor(Byte b) { return new SQLNull(Types.TINYINT); };
    public SQLNull nullFor(Short s) { return new SQLNull(Types.SMALLINT); };
    public SQLNull nullFor(Integer i) { return new SQLNull(Types.INTEGER); };
    public SQLNull nullFor(Long l) { return new SQLNull(Types.BIGINT); };
    public SQLNull nullFor(Float f) { return new SQLNull(Types.REAL); };
    public SQLNull nullFor(Double d) { return new SQLNull(Types.DOUBLE); };
    public SQLNull nullFor(byte[] b) { return new SQLNull(Types.LONGVARBINARY); };
    public SQLNull nullFor(java.util.Date d) { return new SQLNull(Types.DATE); };
  }
}


