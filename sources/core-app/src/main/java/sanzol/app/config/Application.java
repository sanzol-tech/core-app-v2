/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: June 2020
 *
 */
package sanzol.app.config;

import java.time.ZonedDateTime;

import javax.servlet.ServletContext;

import sanzol.se.audit.AuditQueueService;
import sanzol.se.error.ErrorQueueService;
import sanzol.se.license.LicenseService;
import sanzol.se.services.cache.CacheEntities;

public final class Application
{
	private static ZonedDateTime startUpTime;
	private static String webappPath;

	public static ZonedDateTime getStartUpTime()
	{
		return startUpTime;
	}

	public static String getWebappPath()
	{
		return webappPath;
	}

	public static void init(ServletContext servletContext)
	{
		startUpTime = ZonedDateTime.now();
		webappPath = servletContext.getRealPath("/");

		AppProperties.load();
		I18nPreference.config();
		LicenseService.init();
		Version.load();

		AuditQueueService.init();
		ErrorQueueService.init();

		CacheEntities.load();

	}

}
