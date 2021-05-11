/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: January 2021
 *
 */
package sanzol.se.context;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import sanzol.se.commons.ControllerUtils;
import sanzol.se.commons.FacesUtils;
import sanzol.se.error.ErrorService;
import sanzol.se.model.entities.SeUser;
import sanzol.se.sessions.ActiveSessions;
import sanzol.se.sessions.AuthSession;
import sanzol.util.DateTimeUtils;

public final class RequestContext implements Serializable, AutoCloseable
{
	private static final long serialVersionUID = 1L;

	private LocalDateTime creationDate;
	private LocalDateTime closeDate;
	private String callingMethod;
	private HttpServletRequest httpServletRequest;
	private AuthSession authSession;
	private ContextLogger msgLogger;
	private boolean showAndRegisterErrors;

	public static RequestContext createContext()
	{
		HttpServletRequest request = FacesUtils.getRequest();
		return createContext(request, 3);
	}

	public static RequestContext createContext(HttpServletRequest request)
	{
		return createContext(request, 3);
	}

	private static RequestContext createContext(HttpServletRequest request, int stackTrace)
	{
		RequestContext context = new RequestContext();

		context.creationDate = DateTimeUtils.now();
		context.closeDate = null;

		// -------------------------------------------------------------------------------------------------
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		context.callingMethod = String.format("%s %s", stackTraceElements[stackTrace].getClassName(), stackTraceElements[stackTrace].getMethodName());

		// -------------------------------------------------------------------------------------------------
		if (request != null)
		{
			context.httpServletRequest = request;

			HttpSession httpSession = request.getSession();
			context.authSession = ActiveSessions.getAuthSession(httpSession.getId());
		}

		// -------------------------------------------------------------------------------------------------
		context.msgLogger = new ContextLogger();

		// -------------------------------------------------------------------------------------------------
		context.showAndRegisterErrors = (request != null);

		return context;
	}

	public RequestContext clone(boolean _showAndRegisterErrors)
	{
		RequestContext context = new RequestContext();
		context.creationDate = creationDate;
		context.closeDate = null;
		context.callingMethod = callingMethod;
		context.httpServletRequest = httpServletRequest;
		context.authSession = authSession;
		context.msgLogger = new ContextLogger();
		context.showAndRegisterErrors = _showAndRegisterErrors;
		return context;
	}

	@Override
	public void close()
	{
		this.closeDate = DateTimeUtils.now();

		if (showAndRegisterErrors)
		{
			if (msgLogger.hasFatal())
			{
				ErrorService.registerError(this);
			}

			ControllerUtils.showErrors(this);
		}
	}

	// ------------------------------------------------------------------------------------------------

	public String getServletURL()
	{
		HttpServletRequest req = httpServletRequest;
		return req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + req.getContextPath();
	}

	public SeUser getSeUser()
	{
		return ActiveSessions.getSeUser(httpServletRequest);
	}

	public boolean hasErrorOrFatal()
	{
		return msgLogger.hasErrorOrFatal();
	}

	// ------------------------------------------------------------------------------------------------

	public LocalDateTime getCreationDate()
	{
		return creationDate;
	}

	public LocalDateTime getCloseDate()
	{
		return closeDate;
	}

	public String getCallingMethod()
	{
		return callingMethod;
	}

	public HttpServletRequest getHttpServletRequest()
	{
		return httpServletRequest;
	}

	public AuthSession getAuthSession()
	{
		return authSession;
	}

	public void setAuthSession(AuthSession authSession)
	{
		this.authSession = authSession;
	}

	public ContextLogger getMsgLogger()
	{
		return msgLogger;
	}

}
