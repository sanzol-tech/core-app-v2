/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: June 2020
 *
 */
package sanzol.app.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sanzol.se.context.ContextLogger;
import sanzol.se.context.RequestContext;
import sanzol.se.model.entities.SeSoftVersion;
import sanzol.se.services.SeSoftVersionsService;
import sanzol.util.ExceptionUtils;

public final class Version
{
	private static final Logger LOG = LoggerFactory.getLogger(Version.class);

	public enum FaseType
	{
		SNAPSHOT, ALPHA, BETA, RC, GA
	}

	private static final String VENDOR = "sanzol";
	private static final String CLIENT = null;
	private static final String AUTHOR = "Fernando Elenberg Sanzol";
	private static final String PRODUCT = "core-app";

	private static final int VERSION_MAJOR = 1;
	private static final int VERSION_MINOR = 0;
	private static final int VERSION_PATCH = 0;
	private static final FaseType VERSION_FASE = FaseType.SNAPSHOT;
	private static final Integer VERSION_FASE_NUMBER = null;

	private static final String DETAIL = null;

	private static SeSoftVersion mySoftVersion = null;

	private Version()
	{
		// Hide constructor
	}

	public static void load()
	{
		if (mySoftVersion == null)
		{
			try (RequestContext context = RequestContext.createContext(null))
			{
				SeSoftVersionsService service = new SeSoftVersionsService(context);
				mySoftVersion = service.VerifySoftVersion(VERSION_MAJOR, VERSION_MINOR, VERSION_PATCH, getVersionFase(), DETAIL);

				ContextLogger contextLogger = context.getMsgLogger();
				if (contextLogger.hasErrorOrFatal())
				{
					LOG.error(contextLogger.getLastErrorOrFatal().getMessage());
				}
			}
			catch (Exception e)
			{
				LOG.error(ExceptionUtils.getMessage(e));
			}
		}
	}

	public static String getVendor()
	{
		return VENDOR;
	}

	public static String getClient()
	{
		return CLIENT;
	}

	public static String getAuthor()
	{
		return AUTHOR;
	}

	public static String getProduct()
	{
		return PRODUCT;
	}

	public static int getVersionMajor()
	{
		return VERSION_MAJOR;
	}

	public static int getVersionMinor()
	{
		return VERSION_MINOR;
	}

	public static String getVersionFase()
	{
		return VERSION_FASE.name() + (VERSION_FASE_NUMBER != null ? ("." + VERSION_FASE_NUMBER) : "");
	}

	public static String getVersion()
	{
		return VERSION_MAJOR + "." + VERSION_MINOR + "." + VERSION_PATCH + "-" + getVersionFase();
	}

	public static String getTitle()
	{
		return CLIENT != null ? CLIENT.toUpperCase() + "." + PRODUCT : PRODUCT;
	}

	public static SeSoftVersion getMySoftVersion()
	{
		return mySoftVersion;
	}

}
