/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: January 2021
 *
 */
package sanzol.se.sessions;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import javax.persistence.Transient;
import javax.servlet.http.HttpSession;

import sanzol.se.model.entities.SeUser;
import sanzol.util.DateTimeUtils;
import sanzol.util.UserAgentUtils;

public class AuthSession implements Serializable
{
	private static final long serialVersionUID = 1L;

	private SeUser seUser;
	private boolean passwordExpired;

	private Map<Integer, Integer> mapPermissions;
	private Set<Integer> sectors;

	private HttpSession httpSession;

	private String remoteAddress;
	private String userAgent;

	public SeUser getSeUser()
	{
		return seUser;
	}

	public void setSeUser(SeUser seUser)
	{
		this.seUser = seUser;
	}

	public boolean isPasswordExpired()
	{
		return passwordExpired;
	}

	public void setPasswordExpired(boolean passwordExpired)
	{
		this.passwordExpired = passwordExpired;
	}

	public Map<Integer, Integer> getMapPermissions()
	{
		return mapPermissions;
	}

	public void setMapPermissions(Map<Integer, Integer> mapPermissions)
	{
		this.mapPermissions = mapPermissions;
	}

	public Set<Integer> getSectors()
	{
		return sectors;
	}

	public void setSectors(Set<Integer> sectors)
	{
		this.sectors = sectors;
	}

	public HttpSession getHttpSession()
	{
		return httpSession;
	}

	public void setHttpSession(HttpSession httpSession)
	{
		this.httpSession = httpSession;
	}

	public String getRemoteAddress()
	{
		return remoteAddress;
	}

	public void setRemoteAddress(String remoteAddress)
	{
		this.remoteAddress = remoteAddress;
	}

	public String getUserAgent()
	{
		return userAgent;
	}

	public void setUserAgent(String userAgent)
	{
		this.userAgent = userAgent;
	}

	// ------------------------------------------------------------------------------------------------

	@Transient
	public String getSessionId()
	{
		return httpSession.getId();
	}

	@Transient
	public String getCreationTime()
	{
		return DateTimeUtils.readableDateDiff(httpSession.getCreationTime(), System.currentTimeMillis());
	}

	@Transient
	public String getLastAccessedTime()
	{
		return DateTimeUtils.readableDateDiff(httpSession.getLastAccessedTime(), System.currentTimeMillis());
	}

	@Transient
	public String getBrowser()
	{
		return UserAgentUtils.getInstance().getBrowser(userAgent);
	}

	@Transient
	public String getOperatingSystem()
	{
		return UserAgentUtils.getInstance().getOperatingSystem(userAgent);
	}

	// ------------------------------------------------------------------------------------------------

	public boolean isGranted(int permission, int level)
	{
		return mapPermissions != null && mapPermissions.containsKey(permission) && mapPermissions.get(permission) >= level;
	}

}
