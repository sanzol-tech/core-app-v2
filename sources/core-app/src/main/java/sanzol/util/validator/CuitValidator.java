/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: June 2020
 *
 */
package sanzol.util.validator;

import static sanzol.app.config.I18nPreference.getI18nString;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CuitValidator extends Validator
{
	private static final String VALIDATION_NOTEMPTY_MESSAGE = getI18nString("validation.required.message");
	private static final String VALIDATION_PATTERN_MESSAGE = getI18nString("validation.pattern.message");

	private static final String CUIT_REGEX = "\\b(20|23|24|27|30|33|34)(\\-)?[0-9]{8}(\\-)?[0-9]";
	private static final Pattern CUIT_PATTERN = Pattern.compile(CUIT_REGEX);

	public static ValidationItem validateCuit(String clientId, String field, String cuit, boolean isNotNull)
	{
		if (cuit == null || cuit.isEmpty())
		{
			if (isNotNull)
			{
				return new ValidationItem(VALIDATION_NOTEMPTY_MESSAGE.replace("{field}", getString(field)), clientId);
			}
			return ValidationItem.VALID();
		}

		Matcher matcher = CUIT_PATTERN.matcher(cuit);
		boolean isOK = matcher.matches();

		if (!isOK)
		{
			return new ValidationItem(VALIDATION_PATTERN_MESSAGE.replace("{field}", getString(field)), clientId);
		}

		if (!verificatorCuit(cuit.replace("-", "")))
		{
			return new ValidationItem(VALIDATION_PATTERN_MESSAGE.replace("{field}", getString(field)), clientId);
		}

		return ValidationItem.VALID();
	}

	private static boolean verificatorCuit(String cuit)
	{
		int verificator = Integer.parseInt(cuit.substring(10));

		int[] factors = new int[] { 5, 4, 3, 2, 7, 6, 5, 4, 3, 2 };

		int v1 = Integer.parseInt(cuit.substring(0, 1)) * factors[0];
		for (int i = 1; i != 10; i++)
		{
			v1 += Integer.parseInt(cuit.substring(i, i + 1)) * factors[i];
		}

		int v2 = v1 % 11;
		int v3 = 11 - v2;

		if (v3 == 11)
		{
			v3 = 0;
		}
		else
		{
			if (v3 == 10)
			{
				v3 = 9;
			}
		}

		return v3 == verificator;
	}

}
