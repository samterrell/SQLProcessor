/*
 * Copyright (c) 2003 Mission Data.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL MISSION DATA BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.missiondata.oss.sqlprocessor;

import com.missiondata.oss.exception.SystemException;

import java.sql.SQLException;

/**
 * @author Mission Data
 */
public class SQLSystemException extends SystemException
{
  public SQLSystemException(String message, SQLException exception)
  {
    super(message, exception);
    this.exception = exception;
  }

  public boolean isDuplicateKeyViolation()
  {
    return getErrorCode() == Constants.SQL_DUPLICATE_ERROR_CODE_1 || getErrorCode() == Constants.SQL_DUPLICATE_ERROR_CODE_2;
  }

  public boolean isForeignKeyViolation()
  {
    return getErrorCode() == Constants.FOREIGN_KEY_VIOLATION;
  }

  public int getErrorCode()
  {
    return exception.getErrorCode();
  }

  public String getSQLState()
  {
    return exception.getSQLState();
  }

  SQLException exception;
}
