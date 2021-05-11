/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: June 2020
 *
 */
package sanzol.se.context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class ContextLogger implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final int LEVEL_ALL = 0;
	public static final int LEVEL_TRACE = 1;
	public static final int LEVEL_INFO = 2;
	public static final int LEVEL_WARN = 3;
	public static final int LEVEL_ERROR = 4;
	public static final int LEVEL_FATAL = 5;

	private List<ContextMessage> lstMessages = new ArrayList<ContextMessage>();
	private transient int[] count = { 0, 0, 0, 0, 0, 0 };

	public List<ContextMessage> getLstMessages()
	{
		return lstMessages;
	}

	public boolean hasErrorOrFatal()
	{
		return count[LEVEL_ERROR] > 0 || count[LEVEL_FATAL] > 0;
	}

	public boolean hasFatal()
	{
		return count[LEVEL_FATAL] > 0;
	}

	public boolean hasError()
	{
		return count[LEVEL_ERROR] > 0;
	}

	public boolean hasWarning()
	{
		return count[LEVEL_WARN] > 0;
	}

	public ContextMessage getLastErrorOrFatal()
	{
		for (int i = lstMessages.size() - 1; i >= 0; i--)
		{
			if (lstMessages.get(i).getLevel() >= LEVEL_ERROR)
			{
				return lstMessages.get(i);
			}
		}

		return null;
	}

	public ContextMessage getLastFatal()
	{
		for (int i = lstMessages.size() - 1; i >= 0; i--)
		{
			if (lstMessages.get(i).getLevel() >= LEVEL_FATAL)
			{
				return lstMessages.get(i);
			}
		}

		return null;
	}

	public ContextMessage getLastMessage(int level)
	{
		for (int i = lstMessages.size() - 1; i >= 0; i--)
		{
			if (lstMessages.get(i).getLevel() == level)
			{
				return lstMessages.get(i);
			}
		}

		return null;
	}

	// ------------------------------------------------------------------------

	public void clean()
	{
		lstMessages = new ArrayList<ContextMessage>();
		for (int i = 0; i < count.length; i++)
		{
			count[i] = 0;
		}
	}

	// ------------------------------------------------------------------------

	public void addMessageTrace(String message)
	{
		addMessage(LEVEL_TRACE, getCallingMethod(), message);
	}

	public void addMessageInfo(String message)
	{
		addMessage(LEVEL_INFO, getCallingMethod(), message);
	}

	public void addMessageWarning(String message)
	{
		addMessage(LEVEL_WARN, getCallingMethod(), message);
	}

	public void addMessageError(String message)
	{
		addMessage(LEVEL_ERROR, getCallingMethod(), message);
	}

	public void addMessageFatal(String message)
	{
		addMessage(LEVEL_FATAL, getCallingMethod(), message);
	}

	public void addMessage(int level, String method, String message)
	{
		ContextMessage logMessage = new ContextMessage(level, method, message);
		lstMessages.add(logMessage);
		count[level]++;
		count[LEVEL_ALL]++;
	}

	public void addMessage(Exception ex)
	{
		ContextMessage logMessage = new ContextMessage(getCallingMethod(), ex);
		lstMessages.add(logMessage);
		count[LEVEL_FATAL]++;
		count[LEVEL_ALL]++;
	}

	private static String getCallingMethod()
	{
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		return String.format("%s %s", stackTraceElements[3].getClassName(), stackTraceElements[3].getMethodName());
	}

	public static String levelToString(int level)
	{
		if (level == LEVEL_TRACE)
			return "TRACE";
		else if (level == LEVEL_INFO)
			return "INFO";
		else if (level == LEVEL_WARN)
			return "WARN";
		else if (level == LEVEL_ERROR)
			return "ERROR";
		else if (level == LEVEL_FATAL)
			return "FATAL";

		return null;
	}

	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		for (ContextMessage message : lstMessages)
		{
			sb.append(message.toString());
		}
		return sb.toString();
	}

}
