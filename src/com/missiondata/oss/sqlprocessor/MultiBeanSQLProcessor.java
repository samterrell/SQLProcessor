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

import bsh.EvalError;

import java.util.Iterator;

public class MultiBeanSQLProcessor extends AbstractSQLProcessorBase
{
  private String iteratedBeanName;
  private Iterator beanIterator;
  boolean executedOnce=false;

  public MultiBeanSQLProcessor(String description, String sqlText)
  {
    super(description, sqlText);
  }

  protected Object getValue(String key)
  {
    return parameterEvaluator.getParameterValue(key);
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

  private BeanShellParameterEvaluator parameterEvaluator = new BeanShellParameterEvaluator();

  private class BeanShellParameterEvaluator implements ParameterEvaluator
  {
    private bsh.Interpreter interpreter = new bsh.Interpreter();

    public void set(String key, Object value)
    {
      try
      {
        interpreter.set(key,value);
      }
      catch (EvalError evalError)
      {
        // todo catch this
        evalError.printStackTrace();  //To change body of catch statement use Options | File Templates.
      }
    }

    public Object getParameterValue(String parameter)
    {
      Object val = null;
      try
      {
        val = interpreter.eval(parameter);
      }
      catch (bsh.EvalError evalError)
      {
        // todo catch this
        evalError.printStackTrace();  //To change body of catch statement use Options | File Templates.
      }
      return val;
    }
  }

}
