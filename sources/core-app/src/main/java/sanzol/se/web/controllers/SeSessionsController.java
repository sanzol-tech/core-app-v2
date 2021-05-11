package sanzol.se.web.controllers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.omnifaces.cdi.ViewScoped;

import sanzol.app.config.Permissions;
import sanzol.se.audit.AuditEvents;
import sanzol.se.audit.AuditService;
import sanzol.se.commons.FacesUtils;
import sanzol.se.commons.SecurityUtils;
import sanzol.se.context.RequestContext;
import sanzol.se.sessions.ActiveSessions;
import sanzol.se.sessions.AuthSession;
import sanzol.util.json.JsonBuilder;

@Named
@ViewScoped
public class SeSessionsController implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final String THIS_PAGE = "seSessions";

	private List<AuthSession> lstAuthSession;
	private AuthSession authSession;

	public List<AuthSession> getLstAuthSession()
	{
		return lstAuthSession;
	}

	public AuthSession getAuthSession()
	{
		return authSession;
	}

	public void setAuthSession(AuthSession authSession)
	{
		this.authSession = authSession;
	}

	public void pageLoad()
	{
		HttpServletRequest request = FacesUtils.getRequest();
		if (!SecurityUtils.isGranted(request, Permissions.PERMISSION_SE_SESSIONS, Permissions.LEVEL_READ_ONLY))
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
		Map<String, AuthSession> mapSeSessions = ActiveSessions.getMapSessions();
		lstAuthSession = new ArrayList<AuthSession>(mapSeSessions.values());
	}

	public void killSession()
	{
		RequestContext context = RequestContext.createContext();
		try
		{
			HttpSession httpSession = ActiveSessions.getMapSessions().get(authSession.getHttpSession().getId()).getHttpSession();
			httpSession.invalidate();

			loadGrid(context);

			// ----- Audit -----------------------------------------------------------------------------------------
			String detail = JsonBuilder.create(null).add("username", authSession.getSeUser().getUsername()).toString();
			AuditService.auditTransaction(context, AuditEvents.KILL_SESSION, THIS_PAGE, null, authSession.getSeUser().getUserId().toString(), detail);
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
