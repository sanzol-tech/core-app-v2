/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: February 2021
 *
 */
package sanzol.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class ExceptionUtils
{
	public static String getMessage(Exception e)
	{
		if (e.getClass().getName().equals("java.lang.Exception"))
		{
			return e.getMessage();
		}
		else
		{
			return e.getClass().getName() + " : " + e.getMessage();
		}
	}

	public static String getSimpleMessage(Exception e)
	{
		if (e.getClass().getName().equals("java.lang.Exception"))
		{
			return e.getMessage();
		}
		else
		{
			return e.getClass().getName();
		}
	}

	public static String getStackTrace(final Throwable throwable)
	{
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw, true);
		throwable.printStackTrace(pw);
		return sw.getBuffer().toString();
	}

}
