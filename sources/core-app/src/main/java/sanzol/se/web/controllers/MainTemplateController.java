package sanzol.se.web.controllers;

import java.io.Serializable;

import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import sanzol.app.config.MainMenu;
import sanzol.app.config.PermissionsUser;
import sanzol.app.config.Version;
import sanzol.se.audit.AuditEvents;
import sanzol.se.audit.AuditService;
import sanzol.se.commons.FacesUtils;
import sanzol.se.context.RequestContext;
import sanzol.se.model.entities.SeUser;
import sanzol.se.notifications.NotificationService;
import sanzol.se.notifications.NotificationSubscriber;
import sanzol.se.sessions.ActiveSessions;
import sanzol.se.sessions.AuthSession;
import sanzol.web.component.menu.RenderMenu;

@Named
@SessionScoped
public class MainTemplateController implements Serializable
{
	private static final long serialVersionUID = 1L;

	private MainMenu mainMenu = MainMenu.create();

	private SeUser user = null;
	private PermissionsUser permissionsUser = new PermissionsUser(null);

	private NotificationSubscriber notificationSubscriber;

	private String renderedMenu;

	private Integer menuIndex = 0;

	public Integer getMenuIndex()
	{
		return menuIndex;
	}

	public void setMenuIndex(Integer menuIndex)
	{
		this.menuIndex = menuIndex;
	}

	public SeUser getUser()
	{
		return user;
	}

	public PermissionsUser getPermissionsUser()
	{
		return permissionsUser;
	}

	public NotificationSubscriber getNotificationSubscriber()
	{
		return notificationSubscriber;
	}

	public String getRenderedMenu()
	{
		return renderedMenu;
	}

	public String getTitle()
	{
		return Version.getTitle();
	}

	public String getVendor()
	{
		return Version.getVendor();
	}

	public MainTemplateController()
	{
		HttpServletRequest req = FacesUtils.getRequest();

		if (req.getRequestedSessionId() != null && req.isRequestedSessionIdValid())
		{
			AuthSession authSession = ActiveSessions.getAuthSession(req);
			if (authSession != null)
			{
				user = authSession.getSeUser();
				permissionsUser = new PermissionsUser(authSession.getMapPermissions());

				RenderMenu renderMenu = new RenderMenu(authSession.getMapPermissions());
				renderedMenu = renderMenu.render(mainMenu.getMenuModel());

				notificationSubscriber = new NotificationSubscriber(user.getUserId());
				NotificationService.getInstance().addSuscriber(notificationSubscriber);
			}
		}
	}

	@PreDestroy
	public void preDestroy()
	{
		NotificationService.getInstance().removeSuscriber(notificationSubscriber);
	}

	public String logout()
	{
		RequestContext context = RequestContext.createContext();
		try
		{
			AuditService.auditLogin(context, AuditEvents.LOGOUT, null);
		}
		catch (Exception e)
		{
			context.getMsgLogger().addMessage(e);
		}
		finally
		{
			context.close();
		}

		FacesUtils.invalidateSession();
		return "/index?faces-redirect=true";
	}

}
