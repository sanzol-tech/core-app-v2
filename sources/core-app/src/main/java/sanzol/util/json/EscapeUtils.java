/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: January 2021
 *
 */
package sanzol.util.json;

public final class EscapeUtils
{
	private static final int UNICODE_3_BYTES = 0xfff;
	private static final int UNICODE_2_BYTES = 0xff;
	private static final int UNICODE_1_BYTE = 0xf;
	private static final int UNICODE_END_PRINTABLE_ASCII = 0x7f;
	private static final int UNICODE_BEGIN_PRINTABLE_ASCII = 0x20;

	private EscapeUtils()
	{
		// Hide constructor
	}

	public static String escapeJS(String string, boolean escapeSingleQuote)
	{
		if (string == null)
		{
			return null;
		}

		StringBuilder builder = new StringBuilder(string.length());

		for (char c : string.toCharArray())
		{
			if (c > UNICODE_3_BYTES)
			{
				builder.append("\\u").append(Integer.toHexString(c));
			}
			else if (c > UNICODE_2_BYTES)
			{
				builder.append("\\u0").append(Integer.toHexString(c));
			}
			else if (c > UNICODE_END_PRINTABLE_ASCII)
			{
				builder.append("\\u00").append(Integer.toHexString(c));
			}
			else if (c < UNICODE_BEGIN_PRINTABLE_ASCII)
			{
				escapeJSControlCharacter(builder, c);
			}
			else
			{
				escapeJSASCIICharacter(builder, c, escapeSingleQuote);
			}
		}

		return builder.toString();
	}

	private static void escapeJSControlCharacter(StringBuilder builder, char c)
	{
		switch (c)
		{
		case '\b':
			builder.append('\\').append('b');
			break;
		case '\n':
			builder.append('\\').append('n');
			break;
		case '\t':
			builder.append('\\').append('t');
			break;
		case '\f':
			builder.append('\\').append('f');
			break;
		case '\r':
			builder.append('\\').append('r');
			break;
		default:
			if (c > UNICODE_1_BYTE)
			{
				builder.append("\\u00").append(Integer.toHexString(c));
			}
			else
			{
				builder.append("\\u000").append(Integer.toHexString(c));
			}

			break;
		}
	}

	private static void escapeJSASCIICharacter(StringBuilder builder, char c, boolean escapeSingleQuote)
	{
		switch (c)
		{
		case '\'':
			if (escapeSingleQuote)
			{
				builder.append('\\');
			}
			builder.append('\'');
			break;
		case '"':
			builder.append('\\').append('"');
			break;
		case '\\':
			builder.append('\\').append('\\');
			break;
		case '/':
			builder.append('\\').append('/');
			break;
		default:
			builder.append(c);
			break;
		}
	}
}
