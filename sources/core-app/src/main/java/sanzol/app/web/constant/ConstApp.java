/**
 * 
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: June 2020
 * 
 */
package sanzol.app.web.constant;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import sanzol.app.config.AppProperties;
import sanzol.app.config.I18nPreference;
import sanzol.app.config.Version;
import sanzol.se.license.LicenseService;

@Named
@ApplicationScoped
public class ConstApp
{
	public String getVendor()
	{
		return Version.getVendor();
	}

	public String getClient()
	{
		return AppProperties.PROP_LICENSE_CLIENT.getValue();
	}

	public String getProduct()
	{
		return Version.getProduct();
	}

	public String getTitle()
	{
		return Version.getTitle();
	}

	public String getLocale()
	{
		return I18nPreference.getLocale().toString();
	}

	public String getFingerprint()
	{
		return LicenseService.getServerFingerprint();
	}

}
