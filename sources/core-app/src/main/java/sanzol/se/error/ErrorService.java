/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: January 2021
 *
 */
package sanzol.se.error;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sanzol.app.config.Version;
import sanzol.se.context.ContextMessage;
import sanzol.se.context.RequestContext;
import sanzol.se.model.entities.SeError;
import sanzol.se.model.entities.SeUser;
import sanzol.se.sessions.ActiveSessions;
import sanzol.se.sessions.AuthSession;
import sanzol.util.DateTimeUtils;
import sanzol.util.ExceptionUtils;

public class ErrorService
{
	private static final Logger LOG = LoggerFactory.getLogger(ErrorService.class);

	private static String HOSTNAME;
	static
	{
		try
		{
			HOSTNAME = InetAddress.getLocalHost().getHostName();
		}
		catch (UnknownHostException e)
		{
			LOG.error(ExceptionUtils.getMessage(e));
			HOSTNAME = "unknown";
		}
	}

	private static BlockingQueue<SeError> queue = ErrorQueueService.getQueue();

	// -----------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------

	public static void registerError(HttpServletRequest request, Throwable ex)
	{
		try
		{
			String sessionId = request.getSession().getId();
			AuthSession authSession = ActiveSessions.getAuthSession(sessionId);
			SeUser seUser = authSession.getSeUser();
			String path = request.getRequestURI().substring(request.getContextPath().length());
			String detail = "method: " + request.getMethod();

			SeError seError = new SeError();

			seError.setErrorDate(DateTimeUtils.now());
			seError.setSoftVersion(Version.getMySoftVersion());
			seError.setServerName(HOSTNAME);

			seError.setSeUser(seUser);

			seError.setContext(path);
			seError.setContextDetail(detail);

			seError.setExClass(ex.getClass().getSimpleName());
			seError.setExMessage(ex.getMessage());
			seError.setExStackTrace(ExceptionUtils.getStackTrace(ex));

			if (seError != null)
			{
				queue.put(seError);
			}
		}
		catch (Exception e)
		{
			LOG.error(ExceptionUtils.getMessage(e));
		}
	}

	public static void registerError(RequestContext context)
	{
		try
		{
			SeError seError = createError(context);
			if (seError != null)
			{
				queue.put(seError);
			}
		}
		catch (InterruptedException e)
		{
			LOG.error(ExceptionUtils.getMessage(e));
		}
	}

	public static SeError createError(RequestContext context)
	{
		if (context == null || context.getAuthSession() == null || !context.getMsgLogger().hasFatal())
		{
			return null;
		}

		SeError seError = new SeError();

		seError.setErrorDate(DateTimeUtils.now());
		seError.setSoftVersion(Version.getMySoftVersion());
		seError.setServerName(HOSTNAME);

		seError.setSeUser(context.getSeUser());

		HttpServletRequest request = context.getHttpServletRequest();
		if (request != null)
		{
			String path = request.getRequestURI().substring(request.getContextPath().length());
			String detail = "method: " + request.getMethod();
			seError.setContext(path);
			seError.setContextDetail(detail);
		}
		else
		{
			seError.setContext(context.getCallingMethod());
		}

		ContextMessage contextMessage = context.getMsgLogger().getLastFatal();
		if (contextMessage.getException() != null)
		{
			Exception ex = contextMessage.getException();
			seError.setExClass(ex.getClass().getSimpleName());
			seError.setExMessage(ex.getMessage());
			seError.setExStackTrace(ExceptionUtils.getStackTrace(ex));
		}
		else
		{
			seError.setExClass(context.getCallingMethod());
			seError.setExMessage(contextMessage.getMessage());
		}

		return seError;
	}

}