/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: June 2020
 *
 */
package sanzol.app.config;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import sanzol.se.license.LicenseService;
import sanzol.util.properties.PropertiesDB;
import sanzol.util.properties.PropertiesFile;
import sanzol.util.properties.PropertiesLicense;
import sanzol.util.properties.Property;
import sanzol.util.properties.Property.PropertyType;
import sanzol.util.properties.Property.SourceType;

public final class AppProperties
{
	private static Map<String, Property> lstProperties = new LinkedHashMap<String, Property>();

	public static Property PROP_CONFIG_LOCATION = add(SourceType.CONST, false, "config_location", null, PropertyType.STRING, null);

	public static Property PROP_BASE_ENGINE = add(SourceType.FILE, false, "base.engine", null, PropertyType.STRING, null);
	public static Property PROP_BASE_HIBERNATE_CONFIG = add(SourceType.FILE, false, "base.hibernate-config", null, PropertyType.STRING, null);
	public static Property PROP_BASE_URL = add(SourceType.FILE, false, "base.url", null, PropertyType.STRING, null);
	public static Property PROP_BASE_CATALOG = add(SourceType.FILE, false, "base.catalog", null, PropertyType.STRING, null);
	public static Property PROP_BASE_SCHEMA = add(SourceType.FILE, false, "base.schema", null, PropertyType.STRING, null);
	public static Property PROP_BASE_USERNAME = add(SourceType.FILE, true, "base.username", null, PropertyType.STRING, null);
	public static Property PROP_BASE_PASSWORD = add(SourceType.FILE, true, "base.password", null, PropertyType.STRING, null);

	public static Property PROP_WORKSPACE_HOME = add(SourceType.FILE, false, "workspace_path", getDefaultPath(), PropertyType.STRING, null);

	public static Property PROP_PASSWORD_POLICY_MIN_LENGTH = add(SourceType.DB, false, "password.policy.minLength", "6", PropertyType.INTEGER, null);
	public static Property PROP_PASSWORD_POLICY_MIN_LOWERCASE = add(SourceType.DB, false, "password.policy.minLowercase", "2", PropertyType.INTEGER, null);
	public static Property PROP_PASSWORD_POLICY_MIN_UPPERCASE = add(SourceType.DB, false, "password.policy.minUppercase", "1", PropertyType.INTEGER, null);
	public static Property PROP_PASSWORD_POLICY_MIN_NUMBERS = add(SourceType.DB, false, "password.policy.minNumbers", "1", PropertyType.INTEGER, null);
	public static Property PROP_PASSWORD_POLICY_MIN_SYMBOLS = add(SourceType.DB, false, "password.policy.minSymbols", "1", PropertyType.INTEGER, null);
	public static Property PROP_PASSWORD_EXPIRATION = add(SourceType.DB, false, "password.expiration", "24", PropertyType.INTEGER, null).withDetail("months");
	public static Property PROP_LOGIN_MAX_INCORRECT_ATTEMPS = add(SourceType.DB, false, "login.maxIncorrectAttempts", "-1", PropertyType.INTEGER, null);

	public static Property PROP_LOCAL_LANGUAGE = add(SourceType.DB, false, "i18n.locale.language", "en", PropertyType.STRING, "en;es");
	public static Property PROP_LOCAL_COUNTRY = add(SourceType.DB, false, "i18n.locale.country", "US", PropertyType.STRING, "AR;US");
	public static Property PROP_TIME_ZONE = add(SourceType.DB, false, "i18n.timezone", "America/Buenos_Aires", PropertyType.STRING, null);

	public static Property PROP_AUDIT_LOGIN = add(SourceType.DB, false, "audit.login", "true", PropertyType.BOOLEAN, null);
	public static Property PROP_AUDIT_TRANSACTION = add(SourceType.DB, false, "audit.transaction", "true", PropertyType.BOOLEAN, null);
	public static Property PROP_AUDIT_NAVIGATION = add(SourceType.DB, false, "audit.navigation", "true", PropertyType.BOOLEAN, null);

	public static Property PROP_NTP_SERVER = add(SourceType.DB, false, "ntp.server", "time.afip.gov.ar", PropertyType.STRING, null);

	public static Property PROP_MAIL_OUT_SERVER = add(SourceType.DB, false, "mail.out.server", "imap.domain", PropertyType.STRING, null);
	public static Property PROP_MAIL_OUT_PORT = add(SourceType.DB, false, "mail.out.port", "465", PropertyType.STRING, null);
	public static Property PROP_MAIL_OUT_USERNAME = add(SourceType.DB, false, "mail.out.username", "username@domain", PropertyType.STRING, null);
	public static Property PROP_MAIL_OUT_PASSWORD = add(SourceType.DB, true, "mail.out.password", "secret", PropertyType.STRING, null);
	public static Property PROP_MAIL_OUT_ENALED_SSL = add(SourceType.DB, false, "mail.out.enaled_ssl", "true", PropertyType.STRING, null);
	public static Property PROP_MAIL_OUT_ADDRESS = add(SourceType.DB, false, "mail.out.address", "username@domain", PropertyType.STRING, null);
	public static Property PROP_MAIL_OUT_PERSON = add(SourceType.DB, false, "mail.out.person", "username", PropertyType.STRING, null);

	// license properties
	public static Property PROP_LICENSE_PRODUCT = add(SourceType.LICENSE, true, "license.product", null, PropertyType.STRING, null);
	public static Property PROP_LICENSE_ID = add(SourceType.LICENSE, false, "license.id", null, PropertyType.INTEGER, null);
	public static Property PROP_LICENSE_FINGERPRINT = add(SourceType.LICENSE, true, "license.fingerprint", null, PropertyType.STRING, null);
	public static Property PROP_LICENSE_CLIENT = add(SourceType.LICENSE, false, "license.client", null, PropertyType.STRING, null);
	public static Property PROP_LICENSE_CLIENT_CUIT = add(SourceType.LICENSE, false, "license.client_cuit", null, PropertyType.LONG, null);
	public static Property PROP_LICENSE_FROM = add(SourceType.LICENSE, false, "license.date_from", null, PropertyType.DATETIME, null);
	public static Property PROP_LICENSE_TO = add(SourceType.LICENSE, false, "license.date_to", null, PropertyType.DATETIME, null);
	public static Property PROP_LICENSE_MODE = add(SourceType.LICENSE, true, "license.validate_mode", LicenseService.ValidateMode.FULL.name(), PropertyType.STRING, null);
	public static Property PROP_LICENSE_EXPIRED_ACTION = add(SourceType.LICENSE, true, "license.expired_action", String.valueOf(LicenseService.ExpiredAction.DENIED.name()), PropertyType.STRING, null);

	public static Map<String, Property> getLstProperties()
	{
		return lstProperties;
	}

	public static Property add(SourceType sourceType, boolean hidden, String name, String value, PropertyType propertyType, String setValues)
	{
		Property porperty = new Property(sourceType, hidden, name, value, propertyType, setValues);
		lstProperties.put(name, porperty);
		return porperty;
	}

	public static void load()
	{
		PropertiesFile.load(lstProperties);
		PropertiesDB.load(lstProperties);
		PropertiesLicense.load(lstProperties);
	}

	private static String getDefaultPath()
	{
		return System.getProperty("user.home") + File.separator + Version.getVendor() + File.separator + Version.getProduct();
	}

}
