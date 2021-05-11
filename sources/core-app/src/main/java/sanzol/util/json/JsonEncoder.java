/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: January 2021
 *
 */
package sanzol.util.json;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

public final class JsonEncoder
{
	private static final int MAX_JSON_LENGTH = 32767;
	private static final int MAX_LEVELS = 2;

	private static final boolean TRUNCATE_MAPS = true;
	private static final boolean TRUNCATE_COLLECTIONS = true;
	private static final boolean TRUNCATE_ARRAYS = true;

	private JsonEncoder()
	{
		// Hide constructor
	}

	public static String encode(Object object)
	{
		int level = MAX_LEVELS;
		String jsonString = encode(object, level);

		while (jsonString != null && jsonString.length() > MAX_JSON_LENGTH)
		{
			if (level > 1)
			{
				level--;
				jsonString = encode(object, level);
			}
			else
			{
				jsonString = "{\"ERROR\":\"Object too large for encode\"}";
				break;
			}
		}

		if (isNullEmpyOrTruncated(jsonString))
			return null;
		else
			return jsonString;
	}

	public static String encode(Object object, int level)
	{
		StringBuilder builder = new StringBuilder();
		encode(object, builder, level);
		return builder.toString();
	}

	public static void encode(Object object, StringBuilder builder, int level)
	{
		if (object == null)
		{
			builder.append("null");
		}
		else if (object instanceof Character)
		{
			builder.append('"').append(object).append('"');
		}
		else if (object instanceof CharSequence)
		{
			builder.append('"').append(EscapeUtils.escapeJS(((CharSequence) object).toString(), false)).append('"');
		}
		else if (object instanceof Temporal)
		{
			DateTimeFormatter formatter;
			if (object instanceof LocalDate)
			{
				formatter = DateTimeFormatter.ISO_LOCAL_DATE;
			}
			else if (object instanceof LocalTime)
			{
				formatter = DateTimeFormatter.ISO_LOCAL_TIME;
			}
			else
			{
				formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
			}

			builder.append('"').append(formatter.format((Temporal) object)).append('"');
		}
		else if (object instanceof Number)
		{
			if (object instanceof Double || object instanceof Float || object instanceof BigDecimal)
			{
				builder.append(String.format(Locale.ENGLISH, "%f", object));
			}
			else
			{
				builder.append(object.toString());
			}
		}
		else if (object instanceof Boolean)
		{
			builder.append(object.toString());
		}
		else if (object instanceof Map)
		{
			encodeMap((Map<?, ?>) object, builder, level - 1);
		}
		else if (object instanceof Collection)
		{
			encodeCollection((Collection<?>) object, builder, level - 1);
		}
		else if (object.getClass().isArray())
		{
			encodeArray(object, builder, level - 1);
		}
		else
		{
			encodeBean(object, builder, level - 1);
		}
	}

	private static void encodeMap(Map<?, ?> map, StringBuilder builder, int level)
	{
		if (TRUNCATE_MAPS)
		{
			builder.append("[\"...\"]");
		}
		else
		{
			StringBuilder sbMap = new StringBuilder();

			for (final Entry<?, ?> e : map.entrySet())
			{
				final Object itemValue = e.getValue();
				if (itemValue != null)
				{
					sbMap.append(sbMap.length() == 0 ? "" : ",").append(encode(itemValue, level));
				}
			}

			builder.append('[').append(sbMap).append(']');
		}
	}

	private static void encodeCollection(Collection<?> collection, StringBuilder builder, int level)
	{
		if (TRUNCATE_COLLECTIONS)
		{
			builder.append("[\"...\"]");
		}
		else
		{
			StringBuilder sbCollection = new StringBuilder();

			for (Object itemValue : collection)
			{
				sbCollection.append(sbCollection.length() == 0 ? "" : ",").append(encode(itemValue, level));
			}

			builder.append('[').append(sbCollection).append(']');
		}
	}

	private static void encodeArray(Object array, StringBuilder builder, int level)
	{
		if (TRUNCATE_ARRAYS)
		{
			builder.append("[\"...\"]");
		}
		else
		{
			StringBuilder sbArray = new StringBuilder();

			for (Object itemValue : (Object[]) array)
			{
				sbArray.append(sbArray.length() == 0 ? "" : ",").append(encode(itemValue, level));
			}

			builder.append('[').append(sbArray).append(']');
		}
	}

	private static void encodeBean(Object bean, StringBuilder builder, int level)
	{
		final int modifiers = Modifier.STATIC + Modifier.TRANSIENT;

		if (level < 0)
		{
			builder.append("\"...\"");
			return;
		}

		StringBuilder sbBean = new StringBuilder();

		Field[] fields = bean.getClass().getDeclaredFields();
		AccessibleObject.setAccessible(fields, true);
		for (Field element : fields)
		{
			String key = element.getName();
			Object value;
			try
			{
				value = element.get(bean);
			}
			catch (IllegalAccessException e)
			{
				throw new IllegalArgumentException(String.format("Could not get value of object '%s'", bean.getClass()), e);
			}

			if ((element.getModifiers() & modifiers) > 0)
			{
				continue;
			}

			sbBean.append(sbBean.length() == 0 ? "" : ",").append("\"").append(key).append("\":").append(encode(value, level));
		}

		builder.append('{').append(sbBean).append('}');
	}

	private static boolean isNullEmpyOrTruncated(String json)
	{
		return json.equalsIgnoreCase("null") || json.equalsIgnoreCase("[]") || json.equalsIgnoreCase("[\"...\"]");
	}

}
