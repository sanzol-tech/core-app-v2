/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: June 2020
 *
 */
package sanzol.util.validator;

import static sanzol.app.config.I18nPreference.getI18nString;

public class SelectValidator extends Validator
{
	private static final String VALIDATION_SELECT_MESSAGE = getI18nString("validation.select.message");

	public static ValidationItem validate(String clientId, String field, String value)
	{
		if (value == null || value.isBlank())
		{
			return new ValidationItem(VALIDATION_SELECT_MESSAGE.replace("{field}", getString(field)), clientId);
		}
		return ValidationItem.VALID();
	}

	public static ValidationItem validate(String clientId, String field, Integer value, int nullValue)
	{
		if (value == null || value == nullValue)
		{
			return new ValidationItem(VALIDATION_SELECT_MESSAGE.replace("{field}", getString(field)), clientId);
		}
		return ValidationItem.VALID();
	}

	public static ValidationItem validate(String clientId, String field, Object value)
	{
		if (value == null)
		{
			return new ValidationItem(VALIDATION_SELECT_MESSAGE.replace("{field}", getString(field)), clientId);
		}
		return ValidationItem.VALID();
	}

}
