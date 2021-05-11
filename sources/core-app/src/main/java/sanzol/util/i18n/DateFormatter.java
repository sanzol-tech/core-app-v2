/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: March 2021
 *
 */
package sanzol.util.i18n;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateFormatter
{
	public static final int COUNTRY_US = 0;
	public static final int COUNTRY_AR = 1;

	public static final String[] ZONED_DATE_TIME = { "yyyy/MM/dd HH:mm:ss zzz", "dd/MM/yyyy HH:mm:ss zzz" };
	public static final String[] DATE_TIME = { "yyyy/MM/dd HH:mm:ss", "dd/MM/yyyy HH:mm:ss" };
	public static final String[] DATE_TIME_SHORT = { "yyyy/MM/dd HH:mm", "dd/MM/yyyy HH:mm" };
	public static final String[] DATE = { "yyyy/MM/dd", "dd/MM/yyyy" };
	public static final String[] TIME = { "HH:mm:ss", "HH:mm:ss" };
	public static final String[] TIME_SHORT = { "HH:mm", "HH:mm" };
	public static final String[] PERIOD = { "yyyy MMMM", "MMMM yyyy" };
	public static final String[] PERIOD_SHORT = { "yyyy-MM", "yyyy-MM" };

	private static Locale locale = new Locale("es", "AR");
	private static int country = getCountryIndex(locale);

	public static void setLocale(Locale locale)
	{
		DateFormatter.locale = locale;
		DateFormatter.country = getCountryIndex(locale);
	}

	public static DateTimeFormatter getFormatter(String name)
	{
		return getFormatter(name, country);
	}

	public static int getCountryIndex(Locale locale)
	{
		return ("AR".equalsIgnoreCase(locale.getCountry()) ? COUNTRY_AR : COUNTRY_US);
	}

	public static DateTimeFormatter getFormatter(String name, int country)
	{
		switch (name)
		{
		case "ZONED_DATE_TIME":
			return DateTimeFormatter.ofPattern(ZONED_DATE_TIME[country]);
		case "DATE_TIME":
			return DateTimeFormatter.ofPattern(DATE_TIME[country]);
		case "DATE_TIME_SHORT":
			return DateTimeFormatter.ofPattern(DATE_TIME_SHORT[country]);
		case "DATE":
			return DateTimeFormatter.ofPattern(DATE[country]);
		case "TIME":
			return DateTimeFormatter.ofPattern(TIME[country]);
		case "TIME_SHORT":
			return DateTimeFormatter.ofPattern(TIME_SHORT[country]);
		case "PERIOD":
			return DateTimeFormatter.ofPattern(PERIOD[country]);
		case "PERIOD_SHORT":
			return DateTimeFormatter.ofPattern(PERIOD_SHORT[country]);
		default:
			throw new IllegalArgumentException("unknown mask");
		}
	}

	public static String getMaskZonedDateTime(Locale locale)
	{
		return ZONED_DATE_TIME[getCountryIndex(locale)];
	}

	public static String getMaskZonedDateTime()
	{
		return ZONED_DATE_TIME[country];
	}

	public static DateTimeFormatter getFormatterZonedDateTime(Locale locale)
	{
		return DateTimeFormatter.ofPattern(ZONED_DATE_TIME[getCountryIndex(locale)]);
	}

	public static DateTimeFormatter getFormatterZonedDateTime()
	{
		return DateTimeFormatter.ofPattern(ZONED_DATE_TIME[country]);
	}

	public static String getMaskDateTime(Locale locale)
	{
		return DATE_TIME[getCountryIndex(locale)];
	}

	public static String getMaskDateTime()
	{
		return DATE_TIME[country];
	}

	public static DateTimeFormatter getFormatterDateTime(Locale locale)
	{
		return DateTimeFormatter.ofPattern(DATE_TIME[getCountryIndex(locale)]);
	}

	public static DateTimeFormatter getFormatterDateTime()
	{
		return DateTimeFormatter.ofPattern(DATE_TIME[country]);
	}

	public static String getMaskDateTimeShort(Locale locale)
	{
		return DATE_TIME_SHORT[getCountryIndex(locale)];
	}

	public static String getMaskDateTimeShort()
	{
		return DATE_TIME_SHORT[country];
	}

	public static DateTimeFormatter getFormatterDateTimeShort(Locale locale)
	{
		return DateTimeFormatter.ofPattern(DATE_TIME_SHORT[getCountryIndex(locale)]);
	}

	public static DateTimeFormatter getFormatterDateTimeShort()
	{
		return DateTimeFormatter.ofPattern(DATE_TIME_SHORT[country]);
	}

	public static String getMaskDate(Locale locale)
	{
		return DATE[getCountryIndex(locale)];
	}

	public static String getMaskDate()
	{
		return DATE[country];
	}

	public static DateTimeFormatter getFormatterDate(Locale locale)
	{
		return DateTimeFormatter.ofPattern(DATE[getCountryIndex(locale)]);
	}

	public static DateTimeFormatter getFormatterDate()
	{
		return DateTimeFormatter.ofPattern(DATE[country]);
	}

	public static DateTimeFormatter getFormatterTime(Locale locale)
	{
		return DateTimeFormatter.ofPattern(TIME[getCountryIndex(locale)]);
	}

	public static DateTimeFormatter getFormatterTime()
	{
		return DateTimeFormatter.ofPattern(TIME[country]);
	}

	public static String getMaskTimeShort(Locale locale)
	{
		return TIME_SHORT[getCountryIndex(locale)];
	}

	public static String getMaskTimeShort()
	{
		return TIME_SHORT[country];
	}

	public static DateTimeFormatter getFormatterTimeShort(Locale locale)
	{
		return DateTimeFormatter.ofPattern(TIME_SHORT[getCountryIndex(locale)]);
	}

	public static DateTimeFormatter getFormatterTimeShort()
	{
		return DateTimeFormatter.ofPattern(TIME_SHORT[country]);
	}

	public static String getMaskPeriod(Locale locale)
	{
		return PERIOD[getCountryIndex(locale)];
	}

	public static String getMaskPeriod()
	{
		return PERIOD[country];
	}

	public static DateTimeFormatter getFormatterPeriod(Locale locale)
	{
		return DateTimeFormatter.ofPattern(PERIOD[getCountryIndex(locale)], locale);
	}

	public static DateTimeFormatter getFormatterPeriod()
	{
		return DateTimeFormatter.ofPattern(PERIOD[country], locale);
	}

	public static String getMaskPeriodShort(Locale locale)
	{
		return PERIOD_SHORT[getCountryIndex(locale)];
	}

	public static String getMaskPeriodShort()
	{
		return PERIOD_SHORT[country];
	}

	public static DateTimeFormatter getFormatterPeriodShort(Locale locale)
	{
		return DateTimeFormatter.ofPattern(PERIOD_SHORT[getCountryIndex(locale)]);
	}

	public static DateTimeFormatter getFormatterPeriodShort()
	{
		return DateTimeFormatter.ofPattern(PERIOD_SHORT[country]);
	}

}
