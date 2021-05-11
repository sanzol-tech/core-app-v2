/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: January 2021
 *
 */
package sanzol.util.json;

import java.util.Locale;

public final class JsonBuilder
{
	private StringBuilder json;
	private String title;

	private JsonBuilder(String title)
	{
		json = new StringBuilder();
		this.title = title;
	}

	public static JsonBuilder create(String title)
	{
		JsonBuilder p = new JsonBuilder(title);
		return p;
	}

	public JsonBuilder add(String key, Object object)
	{
		if (key == null)
		{
			throw new IllegalArgumentException("Null key");
		}

		String value = JsonEncoder.encode(object);

		if (json.length() > 0)
			json.append(",");

		json.append("\"").append(key).append("\":").append(value);

		return this;
	}

	public JsonBuilder addDecimal(String key, Number value, int roundDigits)
	{
		if (key == null)
		{
			throw new IllegalArgumentException("Null key");
		}

		if (json.length() > 0)
			json.append(",");

		json.append("\"").append(key).append("\":").append(String.format(Locale.ENGLISH, "%." + roundDigits + "f", value));

		return this;
	}

	@Override
	public String toString()
	{
		if (json.length() == 0)
		{
			return "";
		}
		else
		{
			if (title != null && !title.isBlank())
			{
				json.insert(0, "{\"" + title + "\":{");
				json.append("}}");
			}
			else
			{
				json.insert(0, "{");
				json.append("}");
			}

			return json.toString();
		}
	}

}
