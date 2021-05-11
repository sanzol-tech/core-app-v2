/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: June 2020
 *
 */
package sanzol.se.license;

import java.io.File;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sanzol.app.config.AppProperties;
import sanzol.app.config.Version;
import sanzol.util.ExceptionUtils;
import sanzol.util.NetUtils;
import sanzol.util.i18n.DateFormatter;

public class LicenseService
{
	private static final Logger LOG = LoggerFactory.getLogger(LicenseService.class);

	public enum Validity
	{
		INVALID, EXPIRED, VALID
	};

	public enum ValidateMode
	{
		FULL, ONLY_DATES
	};

	public enum ExpiredAction
	{
		NONE, WARN, DELAY, DENIED
	}

	private static final String WARN_MESSAGE = "License invalid or expired";

	private static String product;
	//private static Integer licenseId;
	private static String fingerprint;
	//private static String client;
	private static LocalDateTime dateFrom;
	private static LocalDateTime dateTo;
	private static ValidateMode validateMode;
	private static Set<ExpiredAction> expiredAction;
	private static long expireMillis;
	private static String serverFingerprint;
	private static Validity validity;

	public static String getWarnMessage()
	{
		return WARN_MESSAGE;
	}

	public static ValidateMode getValidateMode()
	{
		return validateMode;
	}

	public static Set<ExpiredAction> getExpiredAction()
	{
		return expiredAction;
	}

	public static long getExpireMillis()
	{
		return expireMillis;
	}

	public static String getServerFingerprint()
	{
		return serverFingerprint;
	}

	public static Validity getValidity()
	{
		return validity;
	}

// -----------------------------------------------------------------------------

	public static void init()
	{
		product = AppProperties.PROP_LICENSE_PRODUCT.getValue();
		// licenseId = Properties.PROP_LICENSE_ID.getIntegerValue();
		fingerprint = AppProperties.PROP_LICENSE_FINGERPRINT.getValue();
		// client = Properties.PROP_LICENSE_CLIENT.getValue();
		dateFrom = AppProperties.PROP_LICENSE_FROM.getLocalDateTimeValue();
		dateTo = AppProperties.PROP_LICENSE_TO.getLocalDateTimeValue();
		validateMode = toMode(AppProperties.PROP_LICENSE_MODE.getValue());
		expiredAction = toExpiredAction(AppProperties.PROP_LICENSE_EXPIRED_ACTION.getValue());
		expireMillis = toExpireMillis(dateFrom, dateTo);
		serverFingerprint = getFingerprint(AppProperties.PROP_BASE_ENGINE.getValue());

		validity = toValidity();

		logLicense(validity, serverFingerprint);

		if (validity == Validity.VALID)
		{
			new java.util.Timer().schedule(new java.util.TimerTask()
			{
				@Override
				public void run()
				{
					validity = Validity.EXPIRED;
					doExpiredAction();
				}
			}, expireMillis);
		}
		else
		{
			doExpiredAction();
		}
	}

	public static void logLicense(Validity validity, String serverFingerprint)
	{
		String _dateFrom = DateFormatter.getFormatterDate().format(dateFrom);
		String _dateTo = DateFormatter.getFormatterDate().format(dateTo);

		LOG.info("");
		LOG.info("/" + "-".repeat(50) + "\\");
		LOG.info("|" + StringUtils.center(" ___                __         ", 50) + "|");
		LOG.info("|" + StringUtils.center("(_  _ _   _   _/   (  _  _    /", 50) + "|");
		LOG.info("|" + StringUtils.center("/  (-/ /)(//)(/() __)(//)/_()( ", 50) + "|");
		LOG.info("|" + " ".repeat(50) + "|");
		LOG.info("|" + StringUtils.center("(o\\/o)", 50) + "|");
		LOG.info("|" + StringUtils.center("/)((  ))(\\", 50) + "|");
		LOG.info("|" + StringUtils.center("^  ^", 50) + "|");
		LOG.info("+" + "-".repeat(50) + "+");

		LOG.info("|" + StringUtils.rightPad(" " + Version.getTitle() + " " + Version.getVersion(), 50) + "|");
		LOG.info("+--" + "-".repeat(46) + "--+");

		LOG.info("|" + StringUtils.rightPad(" Fingerprint: " + serverFingerprint, 50) + "|");
		LOG.info("|" + StringUtils.rightPad(" License status: " + validity.name(), 50) + "|");
		LOG.info("|" + StringUtils.rightPad(" Validity: " + _dateFrom + " - " + _dateTo, 50) + "|");
		LOG.info("\\" + "-".repeat(50) + "/");
		LOG.info("");
	}

	private static ValidateMode toMode(String value)
	{
		for (ValidateMode e : ValidateMode.values())
		{
			if (e.name().equalsIgnoreCase(value.trim()))
			{
				return e;
			}
		}
		return ValidateMode.FULL;
	}

	private static Set<ExpiredAction> toExpiredAction(String value)
	{
		Set<ExpiredAction> expiredActions = new HashSet<ExpiredAction>();
		for (String item : value.split("[;,]"))
		{
			for (ExpiredAction e : ExpiredAction.values())
			{
				if (e.name().equalsIgnoreCase(item.trim()))
				{
					expiredActions.add(e);
					break;
				}
			}
		}
		return expiredActions;
	}

	private static long toExpireMillis(LocalDateTime dateFrom, LocalDateTime dateTo)
	{
		LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
		if (dateFrom == null || dateTo == null || dateTo.isBefore(dateFrom) || dateFrom.isAfter(now) || dateTo.isBefore(now))
		{
			return 0;
		}
		return now.until(dateTo, ChronoUnit.MILLIS);
	}

	private static Validity toValidity()
	{
		if (fingerprint == null || dateFrom == null || dateTo == null || expiredAction.isEmpty())
		{
			return Validity.INVALID;
		}

		String myProduct = Version.getVendor() + "." + Version.getProduct() + "_v" + Version.getVersionMajor();
		if (!myProduct.equals(product))
		{
			return Validity.INVALID;
		}

		if (validateMode == ValidateMode.FULL && !serverFingerprint.equals(fingerprint))
		{
			return Validity.INVALID;
		}

		if (expireMillis <= 0)
		{
			return Validity.EXPIRED;
		}

		return Validity.VALID;
	}

	private static void doExpiredAction()
	{
		//
	}

	public static void doActionIfNoValid()
	{
		if (validity != Validity.VALID && expiredAction.contains(ExpiredAction.DELAY))
		{
			try
			{
				Thread.sleep(LicenseService.getMillisToDelay());
			}
			catch (InterruptedException e)
			{
				LOG.error(ExceptionUtils.getMessage(e));
			}
		}
	}

	private static long getMillisToDelay()
	{
		if (validity == Validity.INVALID)
		{
			return 20000;
		}

		long days = ChronoUnit.DAYS.between(dateTo, LocalDateTime.now(ZoneId.of("UTC")));

		if (days > 180)
			return TimeUnit.SECONDS.toMillis(20);
		else if (days > 90)
			return TimeUnit.SECONDS.toMillis(15);
		else if (days > 45)
			return TimeUnit.SECONDS.toMillis(10);
		else if (days > 15)
			return TimeUnit.SECONDS.toMillis(5);
		else
			return 2500;
	}

// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------

	private static String getFingerprint(String... values)
	{
		File file = new File(System.getProperty("user.home"));
		String value
				= Version.getProduct() + "||"
				+ getHostName() + "||"
				+ NetUtils.getInfoForHash() + "||"
				+ System.getProperty("user.name") + "||"
				+ System.getProperty("os.arch") + "||"
				+ System.getProperty("os.name") + "||"
				+ System.getProperty("os.version") + "||"
				+ Runtime.getRuntime().availableProcessors() + "||"
				+ file.getTotalSpace();

		for (String item : values)
		{
			value += "||" + item;
		}

		return getHash(value);
	}

	private static String getHostName()
	{
		try
		{
			return InetAddress.getLocalHost().getHostName();
		}
		catch (UnknownHostException e)
		{
			return "unknown";
		}
	}

	private static String getHash(String value)
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(value.getBytes());
			byte[] bytes = md.digest();

			return toHex(bytes);
		}
		catch (Exception e)
		{
			return "ERR:" + e.getMessage();
		}
	}

	private static String toHex(byte[] array)
	{
		BigInteger bi = new BigInteger(1, array);
		String hex = bi.toString(16);
		int paddingLength = (array.length * 2) - hex.length();
		if (paddingLength > 0)
		{
			return String.format("%0" + paddingLength + "d", 0) + hex;
		}
		else
		{
			return hex;
		}
	}

}
