/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: June 2020
 *
 */
package sanzol.util;

import java.text.DecimalFormat;

public final class Utils
{
	private Utils()
	{
		// Hide constructor
	}

	public static Integer toInteger(String s)
	{
		if (s == null || s.isEmpty())
		{
			return null;
		}

		Integer n = null;
		try
		{
			n = Integer.parseInt(s);
		}
		catch (Exception ex)
		{
		}
		return n;
	}

	public static Short toShort(String s)
	{
		if (s == null || s.isEmpty())
		{
			return null;
		}

		Short n = null;
		try
		{
			n = Short.parseShort(s);
		}
		catch (Exception ex)
		{
		}
		return n;
	}

	public static Long toLong(String s)
	{
		if (s == null || s.isEmpty())
		{
			return null;
		}

		Long n = null;
		try
		{
			n = Long.parseLong(s);
		}
		catch (Exception ex)
		{
		}
		return n;
	}

	public static Integer toInteger(Double d)
	{
		if (d == null)
			return null;
		return d.intValue();
	}

	public static String readableMemorySize(long size)
	{
		if (size <= 0)
		{
			return "0";
		}
		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}

}
