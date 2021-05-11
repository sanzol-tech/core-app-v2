/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: February 2021
 *
 */
package sanzol.util;

import static sanzol.app.config.I18nPreference.getI18nString;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import sanzol.util.i18n.DateFormatter;

public final class DateRangeUtils
{
	private static final String TXT_DAYS = getI18nString("label.time.days");
	private static final String TXT_LAST_PLURAL = getI18nString("label.last.plural");

	private DateRangeUtils()
	{
		// Hide constructor
	}

	public static List<SelectItem> getRanges(Integer maxPeriods, int... lastDays)
	{
		final DateTimeFormatter formatterValue = DateTimeFormatter.ofPattern("yyyyMM");
		final DateTimeFormatter formatterLabel = DateFormatter.getFormatterPeriod();
		final LocalDate today = DateTimeUtils.today();

		List<SelectItem> list = new ArrayList<SelectItem>();

		if (lastDays != null)
		{
			for (int days : lastDays)
			{
				list.add(new SelectItem("L" + days + "D", TXT_LAST_PLURAL + " " + days + " " + TXT_DAYS));
			}
		}

		if (maxPeriods != null)
		{
			for (int i = 0; i < maxPeriods; i++)
			{
				LocalDate date = today.minusMonths(i);
				list.add(new SelectItem(formatterValue.format(date), formatterLabel.format(date)));
			}
		}

		return list;
	}

	public static class Period implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private LocalDateTime begin;

		public LocalDateTime getBegin()
		{
			return begin;
		}

		public void setBegin(LocalDateTime begin)
		{
			this.begin = begin;
		}

		private LocalDateTime end;

		public LocalDateTime getEnd()
		{
			return end;
		}

		public void setEnd(LocalDateTime end)
		{
			this.end = end;
		}

		public Period(LocalDateTime begin, LocalDateTime end)
		{
			this.begin = begin;
			this.end = end;
		}
	}

	public static Period getPeriod(String range)
	{
		LocalDateTime begin = null;
		LocalDateTime end = null;

		if (range != null && !range.isEmpty())
		{
			if (range.length() > 2 && range.startsWith("L") && range.endsWith("D"))
			{
				String strDays = range.substring(1, range.length() - 1);
				Long days = Utils.toLong(strDays);
				LocalDate today = DateTimeUtils.today();
				end = today.atTime(LocalTime.MAX);
				begin = today.minusDays(days).atStartOfDay();
			}
			else if (range.length() == 6)
			{
				final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
				LocalDate temp = LocalDate.parse(range + "01", formatter);
				begin = temp.atStartOfDay();
				end = temp.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);
			}
		}

		return new Period(begin, end);
	}

}
