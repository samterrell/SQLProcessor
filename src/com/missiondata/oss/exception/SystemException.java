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
package com.missiondata.oss.exception;

import java.io.*;

/**
 * @author Steven Yelton
 * @author Leslie Hensley
 */
public class SystemException extends RuntimeException
{
  public SystemException()
  {
    super();
  }

  public SystemException(String s)
  {
    super(s);
  }

  public SystemException(String s, Throwable e)
  {
    super(s + "\nNested exception message: " + e.getMessage());
    this.e = e;
  }

  public SystemException(Throwable e)
  {
    super("Nested exception message: " + e.getMessage());
    this.e = e;
  }

  public String toString()
  {
    if (e == null)
    {
      return super.toString();
    }
    else
    {
      return getClass().getName() + " ( " + e.getClass().getName() + " ): " + getMessage();
    }
  }

  public void printStackTrace()
  {
    if (e == null)
    {
      super.printStackTrace();
    }
    else
    {
      printStackTrace(System.err);
    }
  }

  public void printStackTrace(PrintStream s)
  {
    if (e == null)
    {
      super.printStackTrace(s);
    }
    else
    {
      printStackTrace(new PrintWriter(new OutputStreamWriter(s)));
    }

  }

  public void printStackTrace(PrintWriter s)
  {
    if (e == null)
    {
      super.printStackTrace(s);
    }
    else
    {
      try
      {
        s.println(getMessage());
        super.printStackTrace(s);
        s.println("Nested stack Trace:");
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        stringWriter.close();
        BufferedReader reader = new BufferedReader(new StringReader(stringWriter.toString()));
        String line;
        while ((line = reader.readLine()) != null)
        {
          s.println("    " + line);
        }
        s.flush();
      }
      catch (IOException ie)
      {
        System.err.println(ie.toString() + "\nFailed printing exception: " + toString());
      }
    }
  }

  private Throwable e;
}