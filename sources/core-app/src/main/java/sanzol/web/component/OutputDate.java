/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: February 2021
 *
 */
package sanzol.web.component;

import static sanzol.app.config.I18nPreference.getI18nString;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import sanzol.util.DateTimeUtils;
import sanzol.util.i18n.DateFormatter;

@FacesComponent(createTag = true, tagName = "outputDate", namespace = "http://sanzol.com.ar/ui")
public class OutputDate extends UIOutput
{
	private static final String TXT_TODAY = getI18nString("label.time.today");

	@Override
	public String getFamily()
	{
		return "sanzol";
	}

	@Override
	public void encodeEnd(FacesContext context) throws IOException
	{
		String result = "";

		Object value = getAttributes().get("value");
		String prefix = (String) getAttributes().get("prefix");
		String format = (String) getAttributes().get("format");

		if (value == null)
		{
			result = "";
		}
		else if (value instanceof Temporal)
		{
			if (format == null || format.isBlank())
			{
				result = autoFormat((Temporal) value, true, true);
			}
			else
			{
				result = DateFormatter.getFormatter(format).format((Temporal) value);
			}

			if (prefix != null && !prefix.isBlank())
			{
				result = prefix + " " + result;
			}
		}
		else
		{
			throw new IllegalArgumentException("Unsupported data type");
		}

		ResponseWriter writer = context.getResponseWriter();
		writer.startElement("span", this);
		if (getAttributes().containsKey("styleClass"))
		{
			writer.writeAttribute("class", getAttributes().get("styleClass"), "styleClass");
		}
		if (getAttributes().containsKey("style"))
		{
			writer.writeAttribute("style", getAttributes().get("style"), "style");
		}

		writer.write(result);

		writer.endElement("span");
	}

	public static String autoFormat(Temporal value, boolean shortTime, boolean replaceToday)
	{
		if (value == null)
		{
			return "";
		}

		else if (value instanceof LocalDateTime)
		{
			LocalDateTime datetime = (LocalDateTime) value;

			if (datetime.truncatedTo(ChronoUnit.DAYS).equals(datetime))
			{
				return DateFormatter.getFormatterDate().format(datetime);
			}
			else if (datetime.toLocalDate().equals(DateTimeUtils.today()))
			{
				if (shortTime)
					return TXT_TODAY + " " + DateFormatter.getFormatterTimeShort().format(datetime);
				else
					return TXT_TODAY + " " + DateFormatter.getFormatterTime().format(datetime);
			}
			else
			{
				if (shortTime)
					return DateFormatter.getFormatterDateTimeShort().format(datetime);
				else
					return DateFormatter.getFormatterDateTime().format(datetime);
			}
		}

		else if (value instanceof LocalDate)
		{
			if (value.equals(DateTimeUtils.today()))
				return TXT_TODAY;
			else
				return DateFormatter.getFormatterDate().format(value);
		}

		else if (value instanceof LocalTime)
		{
			if (shortTime)
				return DateFormatter.getFormatterTimeShort().format(value);
			else
				return DateFormatter.getFormatterTime().format(value);
		}

		return value.toString();
	}

}