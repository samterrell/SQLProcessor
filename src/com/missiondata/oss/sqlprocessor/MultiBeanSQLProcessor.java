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
import java.util.StringTokenizer;
import java.sql.Types;

import bsh.EvalError;

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

  /**
   * Binds a bean name to the contencts of the iterator.
   * <p>
   * The processor iterates over the contents and
   * sets the <i>iteratedBeanName</i> to each member of the iterated
   * collection, and then executes the PreparedStatement.
   * <p>
   * Execution ends when the iterator is exhausted or when {@link #setUp} call
   * returns false
   *
   * @param iteratedBeanName bound with each individual member of the iterator
   * @param iterator source of values for the bean
   */
  public void setIteratedBean(String iteratedBeanName, Iterator iterator)
  {
    this.iteratedBeanName = iteratedBeanName;
    beanIterator = iterator;
  }

  /**
   *
   * @param parameter
   * @param value
   */
  public void set(String parameter,Object value)
  {
    if(taggedSQL.isSubstitutionKey(parameter))
    {
      taggedSQL.setSubstitution(parameter,value);
    }

    parameterEvaluator.set(parameter,value);
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
        Object[] parsed = parseParameterAndNull(parameter);
        val = interpreter.eval((String)parsed[0]);
        if(val==null)
        {
          val = parsed[1];
        }
      }
      catch (bsh.EvalError evalError)
      {
        throw new SystemException("could not evaluate",evalError);
      }
      return val;
    }

    private Object[] parseParameterAndNull(String parameter)
    {
      SQLNull retNull=new SQLNull(Types.OTHER);

      StringTokenizer stringTok = new StringTokenizer(parameter,";",false);
      int numTokens = stringTok.countTokens();
      if(numTokens>1)
      {
        StringBuffer pre = new StringBuffer();
        while(stringTok.hasMoreTokens())
        {
          numTokens--;
          String token = stringTok.nextToken();
          if(numTokens>0)
          {
            pre.append(token);
          }
          else
          {
            char[] ca = token.toCharArray();
            boolean isIdentifier=Character.isJavaIdentifierStart(ca[0]);
            for (int i = 1; i < ca.length && isIdentifier; i++)
            {
              isIdentifier = Character.isJavaIdentifierPart(ca[i]);
            }
            if(isIdentifier)
            {
              try
              {
                Integer typeVal = (Integer) interpreter.eval("java.sql.Types."+token);
                retNull = new SQLNull(typeVal.intValue());
                parameter = pre.toString();
              } catch (EvalError ignore){};

            }
          }
        }
      }

      return new Object[]{parameter,retNull};
    }

  }

}


