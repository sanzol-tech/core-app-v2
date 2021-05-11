package sanzol.se.web.controllers;

import java.io.Serializable;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.omnifaces.cdi.ViewScoped;

import sanzol.app.config.Permissions;
import sanzol.se.audit.AuditService;
import sanzol.se.commons.FacesUtils;
import sanzol.se.commons.SecurityUtils;
import sanzol.se.context.RequestContext;
import sanzol.se.model.SeServerInfo;
import sanzol.se.services.SeServerInfoService;

@Named
@ViewScoped
public class SeServerInfoController implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final String THIS_PAGE = "seServerInfo";

	public List<SeServerInfo> getLstInfoEnvironment()
	{
		return SeServerInfoService.getInstance().getLstInfoEnvironment();
	}

	public List<SeServerInfo> getLstInfoDB()
	{
		return SeServerInfoService.getInstance().getLstInfoDB();
	}

	public List<SeServerInfo> getLstInfoTime()
	{
		return SeServerInfoService.getInstance().getLstInfoTime();
	}

	public List<SeServerInfo> getLstInfoCPU()
	{
		return SeServerInfoService.getInstance().getLstInfoCPU();
	}

	public List<SeServerInfo> getLstInfoMem()
	{
		return SeServerInfoService.getInstance().getLstInfoMem();
	}

	public List<SeServerInfo> getLstInfoDisk()
	{
		return SeServerInfoService.getInstance().getLstInfoDisk();
	}

	public List<SeServerInfo> getLstInfoNet()
	{
		return SeServerInfoService.getInstance().getLstInfoNet();
	}

	public void pageLoad()
	{
		HttpServletRequest request = FacesUtils.getRequest();
		if (!SecurityUtils.isGranted(request, Permissions.PERMISSION_SE_SERVER_INFO, Permissions.LEVEL_READ_ONLY))
		{
			SecurityUtils.redirectAccessDenied(request, FacesUtils.getResponse());
			return;
		}

		RequestContext context = RequestContext.createContext(request);
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
		SeServerInfoService.getInstance().refresh();
	}

	public void refresh()
	{
		RequestContext context = RequestContext.createContext();
		try
		{
			load();
		}
		catch (Exception e)
		{
			context.getMsgLogger().addMessage(e);
			FacesContext.getCurrentInstance().getPartialViewContext().getEvalScripts().add("PF('poll').stop();");
		}
		finally
		{
			context.close();
		}
	}

}
