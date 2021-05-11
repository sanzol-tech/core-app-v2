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

public class PatternValidator extends Validator
{
	private static final String VALIDATION_NOTEMPTY_MESSAGE = getI18nString("validation.required.message");
	private static final String VALIDATION_PATTERN_MESSAGE = getI18nString("validation.pattern.message");
	private static final String VALIDATION_EMAIL_MESSAGE = getI18nString("validation.email.message");

	private static final String USERNAME_REGEX = "^[a-zA-Z0-9_-]{4,16}$";
	private static final Pattern USERNAME_PATTERN = Pattern.compile(USERNAME_REGEX);

	private static final String EMAIL_REGEX = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

	private static final String PHONE_REGEX = "^[+]*[(]{0,1}[0-9]{1,4}[)]{0,1}[-\\s\\./0-9]*$";
	private static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_REGEX);

	public static ValidationItem validateUsername(String clientId, String field, String value, boolean isNotNull)
	{
		if (value == null || value.isEmpty())
		{
			if (isNotNull)
			{
				return new ValidationItem(VALIDATION_NOTEMPTY_MESSAGE.replace("{field}", getString(field)), clientId);
			}
			return ValidationItem.VALID();
		}

		Matcher matcher = USERNAME_PATTERN.matcher(value);
		boolean isOK = matcher.matches();

		if (!isOK)
		{
			return new ValidationItem(VALIDATION_PATTERN_MESSAGE.replace("{field}", getString(field)), clientId);
		}
		return ValidationItem.VALID();
	}

	public static ValidationItem validateEmail(String clientId, String field, String value, boolean isNotNull)
	{
		if (value == null || value.isEmpty())
		{
			if (isNotNull)
			{
				return new ValidationItem(VALIDATION_NOTEMPTY_MESSAGE.replace("{field}", getString(field)), clientId);
			}
			return ValidationItem.VALID();
		}

		Matcher matcher = EMAIL_PATTERN.matcher(value);
		boolean isOK = matcher.matches();

		if (!isOK)
		{
			return new ValidationItem(VALIDATION_EMAIL_MESSAGE.replace("{field}", getString(field)), clientId);
		}
		return ValidationItem.VALID();
	}

	public static ValidationItem validatePhone(String clientId, String field, String value, boolean isNotNull)
	{
		if (value == null || value.isEmpty())
		{
			if (isNotNull)
			{
				return new ValidationItem(VALIDATION_NOTEMPTY_MESSAGE.replace("{field}", getString(field)), clientId);
			}
			return ValidationItem.VALID();
		}

		Matcher matcher = PHONE_PATTERN.matcher(value);
		boolean isOK = matcher.matches();

		if (!isOK)
		{
			return new ValidationItem(VALIDATION_PATTERN_MESSAGE.replace("{field}", getString(field)), clientId);
		}
		return ValidationItem.VALID();
	}

}
