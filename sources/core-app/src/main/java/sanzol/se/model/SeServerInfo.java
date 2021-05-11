/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: June 2020
 *
 */
package sanzol.se.model;

import java.beans.Transient;
import java.io.Serializable;

public class SeServerInfo implements Serializable
{
	private static final long serialVersionUID = 1L;

	public enum ServerInfoStatus
	{
		OK, IMPORTANT, WARN, ERROR
	}

	private String key;
	private String value;
	private ServerInfoStatus status;

	public SeServerInfo(String key, String value)
	{
		this.key = key;
		this.value = value;
		this.status = ServerInfoStatus.OK;
	}

	public SeServerInfo(String key, String value, ServerInfoStatus status)
	{
		this.key = key;
		this.value = value;
		this.status = status;
	}

	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	public ServerInfoStatus getStatus()
	{
		return status;
	}

	public void setStatus(ServerInfoStatus status)
	{
		this.status = status;
	}

	@Override
	public String toString()
	{
		String separador = "";
		if (key != null && !key.isEmpty() && value != null && !value.isEmpty())
		{
			separador = " : ";
		}
		return key + separador + value;
	}

	@Transient
	public String getCssClass()
	{
		switch (status)
		{
		case IMPORTANT:
			return "infoserver-important";
		case WARN:
			return "infoserver-warn";
		case ERROR:
			return "infoserver-error";
		default:
			return "infoserver-ok";
		}

	}
}
