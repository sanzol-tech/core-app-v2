/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: Aplil 2021
 *
 */
package sanzol.util;

public final class CaseUtils
{

	private CaseUtils()
	{
		// Hide constructor
	}

	public static String trim(String str)
	{
		if (str == null || str.isBlank())
		{
			return null;
		}
		return str.strip().replaceAll("\\s+", " ");
	}

	public static String lowerTrim(String str)
	{
		if (str == null || str.isBlank())
		{
			return null;
		}
		return str.strip().toLowerCase().replaceAll("\\s+", " ");
	}

	public static String upperTrim(String str)
	{
		if (str == null || str.isBlank())
		{
			return null;
		}
		return str.strip().toUpperCase().replaceAll("\\s+", " ");
	}

	public static boolean equals(String s1, String s2)
	{
		return lowerTrim(s1).equals(lowerTrim(s2));
	}

}
