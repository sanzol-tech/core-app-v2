/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: February 2021
 *
 */
package sanzol.util;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;

public class UserAgentUtils
{
	private static UserAgentUtils userAgentUtils;

	private static UserAgentAnalyzer uaa;

	private UserAgentUtils()
	{
		uaa = UserAgentAnalyzer
				.newBuilder()
				.hideMatcherLoadStats()
				.withCache(8192)
				.withField(UserAgent.AGENT_NAME_VERSION_MAJOR)
				.withField(UserAgent.OPERATING_SYSTEM_NAME_VERSION)
				.build();
	}

	public static synchronized UserAgentUtils getInstance()
	{
		if (userAgentUtils == null)
		{
			userAgentUtils = new UserAgentUtils();
		}
		return userAgentUtils;
	}

	public String getBrowser(String userAgent)
	{
		UserAgent uAgent = uaa.parse(userAgent);
		return uAgent.getValue(UserAgent.AGENT_NAME_VERSION_MAJOR);
	}

	public String getOperatingSystem(String userAgent)
	{
		UserAgent uAgent = uaa.parse(userAgent);
		return uAgent.getValue(UserAgent.OPERATING_SYSTEM_NAME_VERSION);
	}

}
