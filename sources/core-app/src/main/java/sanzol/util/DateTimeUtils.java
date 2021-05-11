/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: June 2020
 *
 */
package sanzol.util;

import static sanzol.app.config.I18nPreference.getI18nString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;

import sanzol.app.config.I18nPreference;

public class DateTimeUtils
{
	private static final String TXT_YEARS = getI18nString("label.time.years");
	private static final String TXT_MONTHS = getI18nString("label.time.months");
	private static final String TXT_DAYS = getI18nString("label.time.days");
	private static final String TXT_HOURS = getI18nString("label.time.hours");
	private static final String TXT_MIN = getI18nString("label.time.min");
	private static final String TXT_SEC = getI18nString("label.time.sec");
	private static final String TXT_MSECS = getI18nString("label.time.msecs");

	private static final long ONE_MILL = 1;
	private static final long ONE_SECOND = ONE_MILL * 1000;
	private static final long ONE_MINUTE = ONE_SECOND * 60;
	private static final long ONE_HOUR = ONE_MINUTE * 60;
	private static final long ONE_DAY = ONE_HOUR * 24;

	public static String getAge(LocalDate date)
	{
		return getAge(date, false);
	}

	public static String getAge(LocalDate bornDate, boolean viewLabelYear)
	{
		if (bornDate == null)
		{
			return null;
		}

		LocalDate now = today();

		int years = Period.between(bornDate, now).getYears();
		if (years != 0)
		{
			if (viewLabelYear)
				return String.format("%d %s", years, TXT_YEARS);
			else
				return String.valueOf(years);
		}
		else
		{
			int months = Period.between(bornDate, now).getMonths();
			if (months != 0)
			{
				return String.format("%d %s", months, TXT_MONTHS);
			}
			else
			{
				int days = Period.between(bornDate, now).getDays();
				return String.format("%d %s", days, TXT_DAYS);
			}
		}
	}

	public static String readableDateDiff(Temporal startDate, Temporal endDate)
	{
		if (startDate == null || endDate == null)
		{
			return null;
		}

		return millisToReadable(ChronoUnit.MILLIS.between(startDate, endDate));
	}

	public static String readableDateDiff(long startDate, long endDate)
	{
		return millisToReadable(endDate - startDate);
	}

	public static String millisToReadable(long millis)
	{
		long days = millis / ONE_DAY;
		long rest = millis % ONE_DAY;

		long hours = rest / ONE_HOUR;
		rest = rest % ONE_HOUR;

		long minutes = rest / ONE_MINUTE;
		rest = rest % ONE_MINUTE;

		long seconds = rest / ONE_SECOND;
		rest = rest % ONE_SECOND;

		int l = 0;
		String text = "";
		if (days > 0 && l < 2)
		{
			text += (text.isEmpty() ? "" : ", ") + days + " " + TXT_DAYS;
			l++;
		}
		if (hours > 0 && l < 2)
		{
			text += (text.isEmpty() ? "" : ", ") + hours + " " + TXT_HOURS;
			l++;
		}
		if (minutes > 0 && l < 2)
		{
			text += (text.isEmpty() ? "" : ", ") + minutes + " " + TXT_MIN;
			l++;
		}
		if (seconds > 0 && l < 2)
		{
			text += (text.isEmpty() ? "" : ", ") + seconds + " " + TXT_SEC;
			l++;
		}
		if (rest > 0 && l < 2)
		{
			text += (text.isEmpty() ? "" : ", ") + rest + " " + TXT_MSECS;
			l++;
		}

		return text;
	}

	public static long diffInMinutes(LocalDateTime d1, LocalDateTime d2)
	{
		return ChronoUnit.MINUTES.between(d2, d1);
	}

	public static long diffInDays(LocalDateTime d1, LocalDateTime d2)
	{
		return ChronoUnit.DAYS.between(d2, d1);
	}

	public static LocalDateTime min(LocalDateTime d1, LocalDateTime d2)
	{
		if (d1 == null && d2 == null)
			return null;
		if (d1 == null)
			return d2;
		if (d2 == null)
			return d1;
		return d1.isBefore(d2) ? d1 : d2;
	}

	public static LocalDateTime max(LocalDateTime d1, LocalDateTime d2)
	{
		if (d1 == null && d2 == null)
			return null;
		if (d1 == null)
			return d2;
		if (d2 == null)
			return d1;
		return d1.isAfter(d2) ? d1 : d2;
	}

	public static LocalDateTime max(LocalDateTime... list)
	{
		LocalDateTime max = null;
		if (list != null)
		{
			for (LocalDateTime d : list)
			{
				if (d == null)
				{
					continue;
				}
				if (max == null)
				{
					max = d;
					continue;
				}
				if (max.isBefore(d))
				{
					max = d;
				}
			}
		}
		return max;
	}

	public static boolean between(LocalDateTime d, LocalDateTime from, LocalDateTime to)
	{
		return !d.isBefore(from) && !d.isAfter(to);
	}

	public static boolean between(LocalDate d, LocalDate from, LocalDate to)
	{
		return !d.isBefore(from) && !d.isAfter(to);
	}

	public static LocalDateTime now()
	{
		return LocalDateTime.now(I18nPreference.getZoneId());
	}

	public static LocalDate today()
	{
		return LocalDate.now(I18nPreference.getZoneId());
	}

}
