/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: April 2021
 *
 */
package sanzol.util.validator;

import java.time.LocalDate;
import java.time.LocalDateTime;

import sanzol.app.config.I18nPreference;

public abstract class Validator
{
	protected static String getString(String i18nMessage)
	{
		if (i18nMessage.startsWith("{") && i18nMessage.endsWith("}"))
		{
			String key = i18nMessage.substring(1, i18nMessage.length() - 1);
			return I18nPreference.getI18nString(key);
		}
		else
		{
			return i18nMessage;
		}
	}

	protected static boolean between(LocalDateTime d, LocalDateTime from, LocalDateTime to)
	{
		return !d.isBefore(from) && !d.isAfter(to);
	}

	protected static boolean between(LocalDate d, LocalDate from, LocalDate to)
	{
		return !d.isBefore(from) && !d.isAfter(to);
	}

	protected static boolean between(int value, int min, int max)
	{
		return value >= min && value <= max;
	}

	protected static boolean between(long value, long min, long max)
	{
		return value >= min && value <= max;
	}

	protected static boolean between(double value, double min, double max)
	{
		return value >= min && value <= max;
	}

}
