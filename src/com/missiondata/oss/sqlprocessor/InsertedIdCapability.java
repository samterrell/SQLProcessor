package com.missiondata.oss.sqlprocessor;

/**
 * Implementations can be used to help the {@link AbstractSQLProcessorBase}
 * retrieve the last id resulting from an insert call.
 *
 * @see AbstractSQLProcessorBase#setInsertedIdCapability
 */
public interface InsertedIdCapability
{
  java.math.BigInteger fetchLastInsertedId(java.sql.Connection conn);
}
