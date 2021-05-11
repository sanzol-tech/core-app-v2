/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: June 2020
 *
 */
package sanzol.se.commons;

import sanzol.se.context.ContextLogger;
import sanzol.se.context.ContextMessage;
import sanzol.se.context.RequestContext;

public final class ControllerUtils
{

	public static boolean showErrors(RequestContext context)
	{
		boolean result = false;

		for (ContextMessage msg : context.getMsgLogger().getLstMessages())
		{
			switch (msg.getLevel())
			{
			case ContextLogger.LEVEL_FATAL:
				FacesUtils.addMessageFatal(msg.getMessage());
				result = true;
				break;
			case ContextLogger.LEVEL_ERROR:
				FacesUtils.addMessageError(msg.getMessage());
				result = true;
				break;
			case ContextLogger.LEVEL_WARN:
				FacesUtils.addMessageWarn(msg.getMessage());
				result = true;
				break;
			}
		}

		return result;
	}

	public static void showMessages(ContextLogger contextLogger)
	{
		if (contextLogger != null)
		{
			for (ContextMessage msg : contextLogger.getLstMessages())
			{
				showMessage(msg);
			}
		}
	}

	public static void showMessage(ContextMessage msg)
	{
		switch (msg.getLevel())
		{
		case ContextLogger.LEVEL_FATAL:
			FacesUtils.addMessageFatal(msg.getMessage());
			break;
		case ContextLogger.LEVEL_ERROR:
			FacesUtils.addMessageError(msg.getMessage());
			break;
		case ContextLogger.LEVEL_WARN:
			FacesUtils.addMessageWarn(msg.getMessage());
			break;
		case ContextLogger.LEVEL_INFO:
			FacesUtils.addMessageInfo(msg.getMessage());
			break;
		default:
			FacesUtils.addMessageInfo(msg.getMessage());
			break;
		}
	}

}
