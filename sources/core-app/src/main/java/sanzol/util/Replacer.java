/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: May 2021
 *
 */
package sanzol.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class Replacer
{
	private Map<String, String> tokens = new HashMap<String, String>();

	public static Replacer create()
	{
		return new Replacer();
	}

	public Replacer add(String k, String v)
	{
		if (!StringUtils.isAlpha(k))
		{
			throw new IllegalArgumentException("Key name must only contain letters");
		}
		tokens.put(k, v);
		return this;
	}

	public String replace(String text)
	{
		String patternString = "\\{(" + String.join("|", tokens.keySet()) + ")\\}";
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(text);
		StringBuffer sb = new StringBuffer();
		while (matcher.find())
		{
			matcher.appendReplacement(sb, tokens.get(matcher.group(1)));
		}
		matcher.appendTail(sb);
		return sb.toString();
	}
}
