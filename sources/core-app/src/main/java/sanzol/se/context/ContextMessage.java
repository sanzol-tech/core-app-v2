/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: June 2020
 *
 */
package sanzol.se.context;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import sanzol.util.DateTimeUtils;
import sanzol.util.ExceptionUtils;

public final class ContextMessage implements Serializable
{
	private static final long serialVersionUID = 1L;

	private LocalDateTime datetime;
	private int level;
	private String method;
	private String message;
	private Exception exception;

	public ContextMessage(int level, String method, String message)
	{
		this.datetime = DateTimeUtils.now();
		this.level = level;
		this.method = method;
		this.message = message;
	}

	public ContextMessage(String method, Exception ex)
	{
		this.datetime = DateTimeUtils.now();
		this.level = ContextLogger.LEVEL_FATAL;
		this.method = method;
		this.message = ExceptionUtils.getMessage(ex);
		this.exception = ex;
	}

	public LocalDateTime getDatetime()
	{
		return datetime;
	}

	public void setDatetime(LocalDateTime datetime)
	{
		this.datetime = datetime;
	}

	public int getLevel()
	{
		return level;
	}

	public void setLevel(int level)
	{
		this.level = level;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public String getMethod()
	{
		return method;
	}

	public void setMethod(String method)
	{
		this.method = method;
	}

	public Exception getException()
	{
		return exception;
	}

	public void setException(Exception exception)
	{
		this.exception = exception;
	}

	public String getLevelName()
	{
		return ContextLogger.levelToString(level);
	}

	@Override
	public String toString()
	{
		DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
		return String.format("{\"datetime\":\"%s\",level\":\"%s\",method\":\"%s\",message\":\"%s\"}", formatter.format(datetime), ContextLogger.levelToString(level), method, message);
	}

}
