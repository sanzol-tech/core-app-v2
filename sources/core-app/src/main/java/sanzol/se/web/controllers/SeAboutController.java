package sanzol.se.web.controllers;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.omnifaces.cdi.ViewScoped;

import sanzol.app.config.AppProperties;
import sanzol.app.config.Version;
import sanzol.se.audit.AuditService;
import sanzol.se.commons.FacesUtils;
import sanzol.se.context.RequestContext;
import sanzol.se.license.LicenseService;
import sanzol.se.model.SeServerInfo;
import sanzol.se.services.SeServerInfoService;

@Named
@ViewScoped
public class SeAboutController implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final String THIS_PAGE = "seAbout";

	public String getVersion()
	{
		return Version.getVersion();
	}

	public String getTitle()
	{
		return Version.getTitle();
	}

	public Integer getLicenseId()
	{
		return AppProperties.PROP_LICENSE_ID.getIntegerValue();
	}

	public String getFingerprint()
	{
		return LicenseService.getServerFingerprint();
	}

	public String getClient()
	{
		return AppProperties.PROP_LICENSE_CLIENT.getValue();
	}

	public LocalDateTime getDateFrom()
	{
		return AppProperties.PROP_LICENSE_FROM.getLocalDateTimeValue();
	}

	public LocalDateTime getDateTo()
	{
		return AppProperties.PROP_LICENSE_TO.getLocalDateTimeValue();
	}

	public String getValidity()
	{
		return LicenseService.getValidity().name();
	}

	public List<SeServerInfo> getLstInfoVersions()
	{
		return SeServerInfoService.getInstance().getLstInfoVersions();
	}

	public void pageLoad()
	{
		HttpServletRequest req = FacesUtils.getRequest();

		RequestContext context = RequestContext.createContext(req);
		try
		{
			load();

			// ----- Audit -----------------------------------------------------------------------------------------
			if (!context.hasErrorOrFatal())
			{
				AuditService.auditPageLoad(context, THIS_PAGE, null);
			}
		}
		catch (Exception e)
		{
			context.getMsgLogger().addMessage(e);
		}
		finally
		{
			context.close();
		}
	}

	private void load()
	{
		SeServerInfoService.getInstance();
	}

}
