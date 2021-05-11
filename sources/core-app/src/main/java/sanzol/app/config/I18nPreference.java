/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: June 2020
 *
 */
package sanzol.app.config;

import java.time.ZoneId;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sanzol.util.i18n.Bundle;
import sanzol.util.i18n.DateFormatter;

public final class I18nPreference
{
	private static final Logger LOG = LoggerFactory.getLogger(I18nPreference.class);

	private static Locale locale = new Locale("es", "AR");
	private static ZoneId zoneId = ZoneId.of("America/Buenos_Aires");
	private static Bundle bundle = new Bundle(locale);

	public static Locale getLocale()
	{
		return locale;
	}

	public static void setLocale(Locale locale)
	{
		I18nPreference.locale = locale;
	}

	public static ZoneId getZoneId()
	{
		return zoneId;
	}

	public static void setZoneId(ZoneId zoneId)
	{
		I18nPreference.zoneId = zoneId;
	}

	public static Bundle getBundle()
	{
		return bundle;
	}

	public static void setBundle(Bundle bundle)
	{
		I18nPreference.bundle = bundle;
	}

	public static String getI18nString(String key)
	{
		return bundle.getString(key);
	}

	public static void config()
	{
		String languageStr = AppProperties.PROP_LOCAL_LANGUAGE.getValue();
		String countryStr = AppProperties.PROP_LOCAL_COUNTRY.getValue();
		locale = new Locale(languageStr, countryStr);

		String timeZoneStr = AppProperties.PROP_TIME_ZONE.getValue();
		if (ZoneId.getAvailableZoneIds().contains(timeZoneStr))
		{
			zoneId = ZoneId.of(timeZoneStr);
		}
		else
		{
			LOG.error("unknown ZoneId " + timeZoneStr);
		}

		bundle = new Bundle(locale);

		DateFormatter.setLocale(locale);
	}

}
