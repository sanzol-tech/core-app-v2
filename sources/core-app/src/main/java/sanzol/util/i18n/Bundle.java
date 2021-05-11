/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: March 2021
 *
 */
package sanzol.util.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bundle
{
	private static final Logger LOG = LoggerFactory.getLogger(Bundle.class);

	private Locale locale;

	private ResourceBundle bundleCore;
	private ResourceBundle bundleApp;

	public Locale getLocale()
	{
		return locale;
	}

	public ResourceBundle getBundleCore()
	{
		return bundleCore;
	}

	public ResourceBundle getBundleApp()
	{
		return bundleApp;
	}

	public Bundle(Locale locale)
	{
		this.locale = locale;

		bundleCore = ResourceBundle.getBundle("bundle/core", locale);
		bundleApp = ResourceBundle.getBundle("bundle/app", locale);
	}

	public String getString(String key)
	{
		if (bundleCore.containsKey(key))
		{
			return bundleCore.getString(key);
		}
		else if (bundleApp.containsKey(key))
		{
			return bundleApp.getString(key);
		}
		else
		{
			LOG.warn("key {} not found in ResourceBundle", key);
			return key;
		}
	}

}
