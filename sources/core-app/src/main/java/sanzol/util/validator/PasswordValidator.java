/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: June 2020
 *
 */
package sanzol.util.validator;

import static sanzol.app.config.I18nPreference.getI18nString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import sanzol.app.config.AppProperties;
import sanzol.app.config.I18nPreference;

public final class PasswordValidator extends Validator
{
	private static final String VALIDATION_PASSWORD_POLICY_MESSAGE = getI18nString("validation.password.policy.message");

	private static final int MIN_LENGTH = AppProperties.PROP_PASSWORD_POLICY_MIN_LENGTH.getIntegerValue();
	private static final int MIN_LOWERCASE = AppProperties.PROP_PASSWORD_POLICY_MIN_LOWERCASE.getIntegerValue();
	private static final int MIN_UPPERCASE = AppProperties.PROP_PASSWORD_POLICY_MIN_UPPERCASE.getIntegerValue();
	private static final int MIN_NUMBERS = AppProperties.PROP_PASSWORD_POLICY_MIN_NUMBERS.getIntegerValue();
	private static final int MIN_SYMBOLS = AppProperties.PROP_PASSWORD_POLICY_MIN_SYMBOLS.getIntegerValue();

	public static ValidationItem validatePassword(String clientId, String password)
	{
		boolean notCompliance =
			(password.length() < MIN_LENGTH) ||
			(password.length() - password.replaceAll("[a-z]+", "").length() < MIN_LOWERCASE) ||
			(password.length() - password.replaceAll("[A-Z]+", "").length() < MIN_UPPERCASE) ||
			(password.length() - password.replaceAll("[0-9]+", "").length() < MIN_NUMBERS) ||
			(password.length() - password.replaceAll("[^0-9a-zA-Z ]+", "").length() < MIN_SYMBOLS);

		if (notCompliance)
			return new ValidationItem(getPolicy(), clientId);
		else
			return ValidationItem.VALID();
	}

	public static String getPolicy()
	{
		String policy = "";

		if (MIN_LENGTH == 1)
			policy += "1 " + I18nPreference.getI18nString("label.pp.character");
		else if (MIN_LENGTH > 1)
			policy += MIN_LENGTH + " " + I18nPreference.getI18nString("label.pp.characters");

		if (MIN_LOWERCASE == 1)
			policy += policy.isEmpty() ? "" : "; " + "1 " + I18nPreference.getI18nString("label.pp.lowercase");
		else if (MIN_LOWERCASE > 1)
			policy += policy.isEmpty() ? "" : "; " + MIN_LOWERCASE + " " + I18nPreference.getI18nString("label.pp.lowercase.plural");

		if (MIN_UPPERCASE == 1)
			policy += policy.isEmpty() ? "" : "; " + "1 " + I18nPreference.getI18nString("label.pp.uppercase");
		else if (MIN_UPPERCASE > 1)
			policy += policy.isEmpty() ? "" : "; " + MIN_UPPERCASE + " " + I18nPreference.getI18nString("label.pp.uppercase.plural");

		if (MIN_NUMBERS == 1)
			policy += policy.isEmpty() ? "" : "; " + "1 " + I18nPreference.getI18nString("label.pp.number");
		else if (MIN_NUMBERS > 1)
			policy += policy.isEmpty() ? "" : "; " + MIN_NUMBERS + " " + I18nPreference.getI18nString("label.pp.numbers");

		if (MIN_SYMBOLS == 1)
			policy += policy.isEmpty() ? "" : "; " + "1 " + I18nPreference.getI18nString("label.pp.special");
		else if (MIN_SYMBOLS > 1)
			policy += policy.isEmpty() ? "" : "; " + MIN_SYMBOLS + " " + I18nPreference.getI18nString("label.pp.specials");

		if (!policy.isEmpty())
			return VALIDATION_PASSWORD_POLICY_MESSAGE.replace("{policy}", policy);
		else
			return "not password policy is enabled";
	}

	public static String generatePassword()
	{
		int min_length = Math.max(MIN_LENGTH, 10);
		int min_uppercase = Math.max(MIN_UPPERCASE, 2);
		int min_numbers = Math.max(MIN_NUMBERS, 2);
		int min_symbols = Math.max(MIN_SYMBOLS, 2);
		int min_lowercase = Math.max(MIN_LOWERCASE, min_length - min_uppercase - min_numbers - min_symbols);

		Random r = new Random();

		List<Character> lstChars = new ArrayList<Character>();

		for (int i = 0; i < min_uppercase; i++)
		{
			lstChars.add((char) (r.nextInt(26) + 'A'));
		}

		for (int i = 0; i < min_lowercase; i++)
		{
			lstChars.add((char) (r.nextInt(26) + 'a'));
		}

		for (int i = 0; i < min_numbers; i++)
		{
			lstChars.add((char) (r.nextInt(10) + '0'));
		}

		// char[] symbols = { '!', '#', '$', '%', '&', '(', ')', '*', '+', '-', '.', '/', ':', '<', '=', '>', '?', '@', '{', '|', '}' };
		char[] symbols = { '!', '#', '$', '%', '&', '+', '-', '.', ':', '=', '?', '@' };
		for (int i = 0; i < min_symbols; i++)
		{
			lstChars.add(symbols[r.nextInt(symbols.length)]);
		}

		Collections.shuffle(lstChars);
		String password = lstChars.stream().collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
		return password;
	}

}
