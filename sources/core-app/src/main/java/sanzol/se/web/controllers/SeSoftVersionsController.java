package sanzol.se.web.controllers;

import java.io.Serializable;
import java.util.List;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.omnifaces.cdi.ViewScoped;

import sanzol.app.config.Permissions;
import sanzol.se.audit.AuditService;
import sanzol.se.commons.FacesUtils;
import sanzol.se.commons.SecurityUtils;
import sanzol.se.context.RequestContext;
import sanzol.se.model.entities.SeSoftVersion;
import sanzol.se.services.SeSoftVersionsService;

@Named
@ViewScoped
public class SeSoftVersionsController implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final String THIS_PAGE = "seSoftVersions";

	private List<SeSoftVersion> lstSeSoftVersions;
	private SeSoftVersion seSoftVersion;

	public List<SeSoftVersion> getLstSeSoftVersions()
	{
		return lstSeSoftVersions;
	}

	public void setLstSeSoftVersions(List<SeSoftVersion> lstSeSoftVersions)
	{
		this.lstSeSoftVersions = lstSeSoftVersions;
	}

	public SeSoftVersion getSeSoftVersion()
	{
		return seSoftVersion;
	}

	public void setSeSoftVersion(SeSoftVersion seSoftVersion)
	{
		this.seSoftVersion = seSoftVersion;
	}

	public void pageLoad()
	{
		HttpServletRequest request = FacesUtils.getRequest();
		if (!SecurityUtils.isGranted(request, Permissions.PERMISSION_SE_SOFT_VERSIONS, Permissions.LEVEL_READ_ONLY))
		{
			SecurityUtils.redirectAccessDenied(request, FacesUtils.getResponse());
			return;
		}

		RequestContext context = RequestContext.createContext(request);
		try
		{
			loadGrid(context);

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

	private void loadGrid(RequestContext context)
	{
		SeSoftVersionsService service = new SeSoftVersionsService(context);
		lstSeSoftVersions = service.getSeSoftVersions();
	}

	public void refresh()
	{
		RequestContext context = RequestContext.createContext();
		try
		{
			loadGrid(context);
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

}
