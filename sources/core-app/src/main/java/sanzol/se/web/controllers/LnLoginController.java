package sanzol.se.web.controllers;

import static sanzol.app.config.I18nPreference.getI18nString;

import java.io.Serializable;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import sanzol.se.audit.AuditEvents;
import sanzol.se.audit.AuditService;
import sanzol.se.commons.FacesUtils;
import sanzol.se.context.RequestContext;
import sanzol.se.model.entities.SeUser;
import sanzol.se.services.LnLoginService;
import sanzol.se.sessions.ActiveSessions;
import sanzol.se.sessions.AuthSession;

@Named
@RequestScoped
public class LnLoginController implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final String MESSAGE_MAX_SESSIONS_EXCEEDED = getI18nString("login.message.maxSessionsExceeded");
	private static final String MESSAGE_FAILED_AUTHENTICATION = getI18nString("login.message.failedAuthentication");

	private String deviceDetail;
	private String username;
	private String password;

	public String getDeviceDetail()
	{
		return deviceDetail;
	}

	public void setDeviceDetail(String deviceDetail)
	{
		this.deviceDetail = deviceDetail;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String authenticate()
	{
		RequestContext context = RequestContext.createContext();
		try
		{
			LnLoginService service = new LnLoginService(context);

			AuthSession authSession = service.authenticate(username, password);
			if (context.hasErrorOrFatal())
			{
				return null;
			}

			if (authSession != null)
			{
				SeUser user = authSession.getSeUser();

				if (user.isIsKillPrevSession())
				{
					ActiveSessions.killSessions(user);
				}
				else
				{
					int numSessionsActive = ActiveSessions.getSessionsByUser(user).size();
					if (user.getMaxSessions() > 0 && user.getMaxSessions() <= numSessionsActive)
					{
						FacesUtils.addMessageWarn(MESSAGE_MAX_SESSIONS_EXCEEDED.replace("{max}", String.valueOf(user.getMaxSessions())));
						password = null;
						return null;
					}
				}

				authSession.setRemoteAddress(context.getHttpServletRequest().getRemoteAddr());
				authSession.setUserAgent(context.getHttpServletRequest().getHeader("user-agent"));

				FacesUtils.invalidateSession();
				HttpSession httpSession = FacesUtils.getSession(true);
				if (user.getMaxInactiveInterval() > 0)
				{
					httpSession.setMaxInactiveInterval(user.getMaxInactiveInterval());
				}

				authSession.setHttpSession(httpSession);
				context.setAuthSession(authSession);
				ActiveSessions.add(authSession);

				// ----- Audit -----------------------------------------------------------------------------------------
				AuditService.auditLogin(context, AuditEvents.LOGIN, deviceDetail);

				if (authSession.isPasswordExpired())
					return "/site/sePasswordChange?faces-redirect=true";
				else
					return "/site/loggedIndex?faces-redirect=true";
			}
			else
			{
				FacesUtils.addMessageError(MESSAGE_FAILED_AUTHENTICATION);
				password = null;
			}

		}
		catch (Exception e)
		{
			context.getMsgLogger().addMessage(e);
			password = null;
		}
		finally
		{
			context.close();
		}

		return null;
	}

}
