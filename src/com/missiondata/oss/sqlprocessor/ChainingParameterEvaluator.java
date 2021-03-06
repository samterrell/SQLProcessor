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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ChainingParameterEvaluator implements ParameterEvaluator
{
  private List chainedEvaluators = new LinkedList();

  public void addEvaluator(ParameterEvaluator evaluator)
  {
    if(evaluator!=null)
    {
      chainedEvaluators.add(evaluator);
    }
  }

  public Object getParameterValue(String parameter, Object suggestedValue)
  {
    Object retVal = suggestedValue;
    for (Iterator i = chainedEvaluators.iterator(); i.hasNext();)
    {
      ParameterEvaluator evaluator = (ParameterEvaluator) i.next();
      retVal = evaluator.getParameterValue(parameter, retVal);
    }

    return retVal;
  }
}
