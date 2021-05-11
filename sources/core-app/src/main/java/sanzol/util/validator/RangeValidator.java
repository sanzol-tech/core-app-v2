/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: June 2020
 *
 */
package sanzol.util.validator;

import static sanzol.app.config.I18nPreference.getI18nString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import sanzol.util.DateTimeUtils;
import sanzol.util.i18n.DateFormatter;

public class RangeValidator extends Validator
{
	private static final String VALIDATION_NOTEMPTY_MESSAGE = getI18nString("validation.required.message");
	private static final String VALIDATION_NUMBER_MIN_MESSAGE = getI18nString("validation.number.min.message");
	private static final String VALIDATION_NUMBER_MAX_MESSAGE = getI18nString("validation.number.max.message");
	private static final String VALIDATION_NUMBER_RANGE_MESSAGE = getI18nString("validation.number.range.message");
	private static final String VALIDATION_DATE_MIN_MESSAGE = getI18nString("validation.date.min.message");
	private static final String VALIDATION_DATE_MAX_MESSAGE = getI18nString("validation.date.max.message");
	private static final String VALIDATION_DATE_RANGE_MESSAGE = getI18nString("validation.date.range.message");

	public static ValidationItem validate(String clientId, String field, LocalDateTime date, LocalDateTime min, LocalDateTime max, boolean isNotNull)
	{
		if (date == null)
		{
			if (isNotNull)
			{
				return new ValidationItem(VALIDATION_NOTEMPTY_MESSAGE.replace("{field}", getString(field)), clientId);
			}
			return ValidationItem.VALID();
		}

		DateTimeFormatter formatter = DateFormatter.getFormatterDateTime();

		if (min != null && max != null && !between(date, min, max))
		{
			return new ValidationItem(VALIDATION_DATE_RANGE_MESSAGE.replace("{field}", getString(field)).replace("{min}", formatter.format(min)).replace("{max}", formatter.format(max)), clientId);
		}
		if (min != null && date.isBefore(min))
		{
			return new ValidationItem(VALIDATION_DATE_MIN_MESSAGE.replace("{field}", getString(field)).replace("{min}", formatter.format(min)), clientId);
		}
		if (max != null && date.isAfter(max))
		{
			return new ValidationItem(VALIDATION_DATE_MAX_MESSAGE.replace("{field}", getString(field)).replace("{max}", formatter.format(max)), clientId);
		}
		return ValidationItem.VALID();
	}

	public static ValidationItem validate(String clientId, String field, LocalDate date, LocalDate min, LocalDate max, boolean isNotNull)
	{
		if (date == null)
		{
			if (isNotNull)
			{
				return new ValidationItem(VALIDATION_NOTEMPTY_MESSAGE.replace("{field}", getString(field)), clientId);
			}
			return ValidationItem.VALID();
		}

		DateTimeFormatter formatter = DateFormatter.getFormatterDate();

		if (min != null && max != null && !between(date, min, max))
		{
			return new ValidationItem(VALIDATION_DATE_RANGE_MESSAGE.replace("{field}", getString(field)).replace("{min}", formatter.format(min)).replace("{max}", formatter.format(max)), clientId);
		}
		if (min != null && date.isBefore(min))
		{
			return new ValidationItem(VALIDATION_DATE_MIN_MESSAGE.replace("{field}", getString(field)).replace("{min}", formatter.format(min)), clientId);
		}
		if (max != null && date.isAfter(max))
		{
			return new ValidationItem(VALIDATION_DATE_MAX_MESSAGE.replace("{field}", getString(field)).replace("{max}", formatter.format(max)), clientId);
		}
		return ValidationItem.VALID();
	}

	public static ValidationItem validateBirthDate(String clientId, String field, LocalDate date, Integer minAge, Integer maxAge, boolean isNotNull)
	{
		if (date == null)
		{
			if (isNotNull)
			{
				return new ValidationItem(VALIDATION_NOTEMPTY_MESSAGE.replace("{field}", getString(field)), clientId);
			}
			return ValidationItem.VALID();
		}

		LocalDate today = DateTimeUtils.today();
		LocalDate min = today.minusYears(maxAge == null ? 120 : maxAge);
		LocalDate max = today.minusYears(minAge == null ? 0 : minAge);

		DateTimeFormatter formatter = DateFormatter.getFormatterDate();

		if (min != null && max != null && !between(date, min, max))
		{
			return new ValidationItem(VALIDATION_DATE_RANGE_MESSAGE.replace("{field}", getString(field)).replace("{min}", formatter.format(min)).replace("{max}", formatter.format(max)), clientId);
		}
		if (min != null && date.isBefore(min))
		{
			return new ValidationItem(VALIDATION_DATE_MIN_MESSAGE.replace("{field}", getString(field)).replace("{min}", formatter.format(min)), clientId);
		}
		if (max != null && date.isAfter(max))
		{
			return new ValidationItem(VALIDATION_DATE_MAX_MESSAGE.replace("{field}", getString(field)).replace("{max}", formatter.format(max)), clientId);
		}
		return ValidationItem.VALID();
	}

//----------------------------------------------------------------------------------------------------------------------

	public static ValidationItem validate(String clientId, String field, Double value, Double min, Double max, boolean isNotNull)
	{
		if (value == null)
		{
			if (isNotNull)
			{
				return new ValidationItem(VALIDATION_NOTEMPTY_MESSAGE.replace("{field}", getString(field)), clientId);
			}
			return ValidationItem.VALID();
		}

		if (min != null && max != null && !between(value, min, max))
		{
			return new ValidationItem(VALIDATION_NUMBER_RANGE_MESSAGE.replace("{field}", getString(field)).replace("{min}", String.valueOf(min)).replace("{max}", String.valueOf(max)), clientId);
		}
		if (min != null && value.doubleValue() < min.doubleValue())
		{
			return new ValidationItem(VALIDATION_NUMBER_MIN_MESSAGE.replace("{field}", getString(field)).replace("{min}", String.valueOf(min)), clientId);
		}
		if (max != null && value.doubleValue() > max.doubleValue())
		{
			return new ValidationItem(VALIDATION_NUMBER_MAX_MESSAGE.replace("{field}", getString(field)).replace("{max}", String.valueOf(max)), clientId);
		}
		return ValidationItem.VALID();
	}

	public static ValidationItem validate(String clientId, String field, Long value, Long min, Long max, boolean isNotNull)
	{
		if (value == null)
		{
			if (isNotNull)
			{
				return new ValidationItem(VALIDATION_NOTEMPTY_MESSAGE.replace("{field}", getString(field)), clientId);
			}
			return ValidationItem.VALID();
		}

		if (min != null && max != null && !between(value, min, max))
		{
			return new ValidationItem(VALIDATION_NUMBER_RANGE_MESSAGE.replace("{field}", getString(field)).replace("{min}", String.valueOf(min)).replace("{max}", String.valueOf(max)), clientId);
		}
		if (min != null && value.longValue() < min.longValue())
		{
			return new ValidationItem(VALIDATION_NUMBER_MIN_MESSAGE.replace("{field}", getString(field)).replace("{min}", String.valueOf(min)), clientId);
		}
		if (max != null && value.longValue() > max.longValue())
		{
			return new ValidationItem(VALIDATION_NUMBER_MAX_MESSAGE.replace("{field}", getString(field)).replace("{max}", String.valueOf(max)), clientId);
		}
		return ValidationItem.VALID();
	}

	public static ValidationItem validate(String clientId, String field, Integer value, Integer min, Integer max, boolean isNotNull)
	{
		if (value == null)
		{
			if (isNotNull)
			{
				return new ValidationItem(VALIDATION_NOTEMPTY_MESSAGE.replace("{field}", getString(field)), clientId);
			}
			return ValidationItem.VALID();
		}

		if (min != null && max != null && !between(value, min, max))
		{
			return new ValidationItem(VALIDATION_NUMBER_RANGE_MESSAGE.replace("{field}", getString(field)).replace("{min}", String.valueOf(min)).replace("{max}", String.valueOf(max)), clientId);
		}
		if (min != null && value.intValue() < min.intValue())
		{
			return new ValidationItem(VALIDATION_NUMBER_MIN_MESSAGE.replace("{field}", getString(field)).replace("{min}", String.valueOf(min)), clientId);
		}
		if (max != null && value.intValue() > max.intValue())
		{
			return new ValidationItem(VALIDATION_NUMBER_MAX_MESSAGE.replace("{field}", getString(field)).replace("{max}", String.valueOf(max)), clientId);
		}
		return ValidationItem.VALID();
	}

}
