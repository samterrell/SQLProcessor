package com.missiondata.oss.sqlprocessor;

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

import com.missiondata.oss.sqlprocessor.LoggingCapability;

public class JavaJDKLoggingCapability implements LoggingCapability
{
  java.util.logging.Logger logger = java.util.logging.Logger.getLogger("sqlprocessor");
  public void logInfo(String information)
  {
    logger.info(information);
  }

  public void logWarning(String error, Throwable t)
  {
    logger.log(java.util.logging.Level.WARNING,error,t);
  }

  public void logError(String error, Throwable t)
  {
    logger.log(java.util.logging.Level.SEVERE,error,t);
  }
}
