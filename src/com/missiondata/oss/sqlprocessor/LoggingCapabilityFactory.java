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

public class LoggingCapabilityFactory
{
  static boolean HAS_LOG4J;
  static
  {
    try
    {
      Class.forName("org.apache.log4j.Logger");
      HAS_LOG4J=true;
    }
    catch (ClassNotFoundException e)
    {
      HAS_LOG4J=false;
    }
  }

  static boolean HAS_JDKLOGGER;
  static
  {
    try
    {
      Class.forName("java.util.logging.Logger");
      HAS_JDKLOGGER=true;
    }
    catch (ClassNotFoundException e)
    {
      HAS_JDKLOGGER=false;
    }
  }

  public static LoggingCapability getLoggingCapability(String loggerName)
  {
    LoggingCapability loggingImpl;
    if(HAS_LOG4J)
    {
      loggingImpl = new Log4jLoggingCapability();
    }
    else if(HAS_JDKLOGGER)
    {
      loggingImpl = new JavaJDKLoggingCapability();
    }
    else
    {
      loggingImpl = NULL_LOGGING;
    }

    return loggingImpl;
  }

  private static LoggingCapability NULL_LOGGING = new LoggingCapability()
  {
    public void logInfo(String information)
    {
    }

    public void logWarning(String error, Throwable t)
    {
    }

    public void logError(String error, Throwable t)
    {
    }
  };
}
