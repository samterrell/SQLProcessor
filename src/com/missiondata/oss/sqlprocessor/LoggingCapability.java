package com.missiondata.oss.sqlprocessor;

/**
 * Can be used the by an {@link AbstractSQLProcessorBase} for logging.
 *
 * @see AbstractSQLProcessorBase#setLoggingCapability
 */
public interface LoggingCapability
{
  void logInfo(String information);
  void logWarning(String error, Throwable t);
  void logError(String error, Throwable t);
}
