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

/**
 *
 */
public interface ParameterEvaluator
{
  /**
   * Given a parameter, generate an Object value that will be used to satisfy
   * either a text substition or a prepared statement's paremeter value.
   * <p>
   * Note that a null is specified by returning an {@link SQLNull} instance.
   * <p>
   * Returning the Java <em>null</em> indicates and error condiation: this evaluator was unable
   * to return a valid parameter value. The SQLProcessor will not continue execution
   * of the prepared statment.
   * <p>
   *
   *
   * @see SQLNull
   * @param processor the current SQLProcessor needing parameter substitution
   * @param parameter the text between || that requires transformation
   * @return object value, an SQLNull instance,
   */
  Object getParameterValue(String parameter, Object suggestedValue);
}
