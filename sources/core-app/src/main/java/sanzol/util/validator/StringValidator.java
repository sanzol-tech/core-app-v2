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

public class StringValidator extends Validator
{
	private static final String VALIDATION_NOTEMPTY_MESSAGE = getI18nString("validation.required.message");
	private static final String VALIDATION_MINLENGTH_MESSAGE = getI18nString("validation.minLength.message");
	private static final String VALIDATION_MAXLENGTH_MESSAGE = getI18nString("validation.maxLength.message");
	private static final String VALIDATION_LENGTH_MESSAGE = getI18nString("validation.length.message");
	private static final String VALIDATION_PATTERN_MESSAGE = getI18nString("validation.pattern.message");

	private static final String ALPHANUMERIC_REGEX = "[A-Za-z0-9]*";
	private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile(ALPHANUMERIC_REGEX);

	public static ValidationItem validate(String clientId, String field, String value, Integer min, Integer max, boolean isNotNull)
	{
		if (value == null || value.isBlank())
		{
			if (isNotNull)
			{
				return new ValidationItem(VALIDATION_NOTEMPTY_MESSAGE.replace("{field}", getString(field)), clientId);
			}
			return ValidationItem.VALID();
		}

		if (min != null && max != null && !between(value.length(), min, max))
		{
			return new ValidationItem(VALIDATION_LENGTH_MESSAGE.replace("{field}", getString(field)).replace("{min}", String.valueOf(min)).replace("{max}", String.valueOf(max)), clientId);
		}
		if (min != null && value.length() < min)
		{
			return new ValidationItem(VALIDATION_MINLENGTH_MESSAGE.replace("{field}", getString(field)).replace("{min}", String.valueOf(min)), clientId);
		}
		if (max != null && value.length() > max)
		{
			return new ValidationItem(VALIDATION_MAXLENGTH_MESSAGE.replace("{field}", getString(field)).replace("{max}", String.valueOf(max)), clientId);
		}
		return ValidationItem.VALID();
	}

	public static ValidationItem validateAlphanumeric(String clientId, String field, String value, Integer min, Integer max, boolean isNotNull)
	{
		if (value == null || value.isBlank())
		{
			if (isNotNull)
			{
				return new ValidationItem(VALIDATION_NOTEMPTY_MESSAGE.replace("{field}", getString(field)), clientId);
			}
			return ValidationItem.VALID();
		}

		if (min != null && max != null && !between(value.length(), min, max))
		{
			return new ValidationItem(VALIDATION_LENGTH_MESSAGE.replace("{field}", getString(field)).replace("{min}", String.valueOf(min)).replace("{max}", String.valueOf(max)), clientId);
		}
		if (min != null && value.length() < min)
		{
			return new ValidationItem(VALIDATION_MINLENGTH_MESSAGE.replace("{field}", getString(field)).replace("{min}", String.valueOf(min)), clientId);
		}
		if (max != null && value.length() > max)
		{
			return new ValidationItem(VALIDATION_MAXLENGTH_MESSAGE.replace("{field}", getString(field)).replace("{max}", String.valueOf(max)), clientId);
		}

		Matcher matcher = ALPHANUMERIC_PATTERN.matcher(value);
		boolean isOK = matcher.matches();

		if (!isOK)
		{
			return new ValidationItem(VALIDATION_PATTERN_MESSAGE.replace("{field}", getString(field)), clientId);
		}
		return ValidationItem.VALID();
	}
}
