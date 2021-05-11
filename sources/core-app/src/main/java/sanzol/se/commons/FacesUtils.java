/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: June 2020
 *
 */
package sanzol.se.commons;

import java.io.IOException;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import sanzol.util.ExceptionUtils;

public final class FacesUtils
{
	private FacesUtils()
	{
		// Hide constructor
	}

	// ------------------------------------------------------
	// Context
	// ------------------------------------------------------
	public static FacesContext getContext()
	{
		return FacesContext.getCurrentInstance();
	}

	public static ExternalContext getExternalContext()
	{
		return getContext().getExternalContext();
	}

	public static ServletContext getServletContext()
	{
		return (ServletContext) getContext().getExternalContext().getContext();
	}

	public static boolean isPostback()
	{
		return getContext().isPostback();
	}

	// ------------------------------------------------------
	// Session
	// ------------------------------------------------------
	public static HttpSession getSession(boolean create)
	{
		return (HttpSession) getExternalContext().getSession(create);
	}

	public static String getSessionId()
	{
		HttpSession session = getSession(false);
		return (session != null) ? session.getId() : null;
	}

	public static void invalidateSession()
	{
		getExternalContext().invalidateSession();
	}

	// ------------------------------------------------------
	// Request y Response
	// ------------------------------------------------------
	public static HttpServletRequest getRequest()
	{
		return (HttpServletRequest) getExternalContext().getRequest();
	}

	public static HttpServletResponse getResponse()
	{
		return (HttpServletResponse) getExternalContext().getResponse();
	}

	public static String getServletURL()
	{
		HttpServletRequest req = getRequest();
		return req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + req.getContextPath();
	}

	// ------------------------------------------------------
	// Redirect
	// ------------------------------------------------------
	public static void redirect(String url)
	{
		try
		{
			FacesUtils.getExternalContext().redirect(url);
		}
		catch (IOException e)
		{
			FacesUtils.addMessage(e);
		}
	}

	// ------------------------------------------------------
	// Messages
	// ------------------------------------------------------
	public static void addMessageInfo(String msg)
	{
		getContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
	}

	public static void addMessageWarn(String msg)
	{
		getContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, msg, null));
	}

	public static void addMessageError(String msg)
	{
		getContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
	}

	public static void addMessageFatal(String msg)
	{
		getContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, msg, null));
	}

	@Deprecated
	public static void addMessage(Exception e)
	{
		addMessageFatal(ExceptionUtils.getMessage(e));
	}

}
