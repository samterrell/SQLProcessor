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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.HashSet;
import java.sql.ResultSet;

/**
 * @author Darren Day
 */
public class ProxyRestrictingResultSet implements InvocationHandler
{
  public static interface RestrictedResultSet extends ResultSet{}

  private static Set restrictedMethods = new HashSet();
  static
  {
    restrictedMethods.add("next");
    restrictedMethods.add("previous");
    restrictedMethods.add("close");
  }

  private ResultSet impl;

  public static RestrictedResultSet restrict(ResultSet resultSetToRestrict)
  {
    return (RestrictedResultSet) Proxy.newProxyInstance
    (
      ProxyRestrictingResultSet.RestrictedResultSet.class.getClassLoader(),
      new Class[]{ProxyRestrictingResultSet.RestrictedResultSet.class},
      new ProxyRestrictingResultSet(resultSetToRestrict)
    );
  }

  protected ProxyRestrictingResultSet(ResultSet impl)
  {
    this.impl = impl;
  }

  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
  {
    if(restrictedMethods.contains(method.getName()))
    {
      throw new UnsupportedOperationException("ResultSet."+method.getName()+"() cannot be called on this result set");
    }
    return method.invoke(impl,args);
  }
}
