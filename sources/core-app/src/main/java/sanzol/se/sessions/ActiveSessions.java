/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: January 2021
 *
 */
package sanzol.se.sessions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import sanzol.se.model.entities.SeUser;

public final class ActiveSessions
{
	private static Map<String, AuthSession> mapAuthSessions = new LinkedHashMap<String, AuthSession>();

	public static Map<String, AuthSession> getMapSessions()
	{
		return Collections.unmodifiableMap(mapAuthSessions);
	}

	public static void add(AuthSession authSession)
	{
		mapAuthSessions.put(authSession.getHttpSession().getId(), authSession);
	}

	public static void remove(HttpSession httpSession)
	{
		mapAuthSessions.remove(httpSession.getId());
	}

	public static AuthSession getAuthSession(HttpServletRequest request)
	{
		return mapAuthSessions.get(request.getSession().getId());
	}

	public static AuthSession getAuthSession(String sessionId)
	{
		return mapAuthSessions.get(sessionId);
	}

	public static SeUser getSeUser(HttpServletRequest request)
	{
		AuthSession authSession = getAuthSession(request);
		if (authSession != null)
		{
			return authSession.getSeUser();
		}

		return null;
	}

	public static List<AuthSession> getSessionsByUser(SeUser user)
	{
		List<AuthSession> lst = new ArrayList<>();

		for (Map.Entry<String, AuthSession> entry : mapAuthSessions.entrySet())
		{
			AuthSession authSession = entry.getValue();
			SeUser seUser = authSession.getSeUser();
			if (seUser.getUserId().equals(user.getUserId()))
			{
				lst.add(authSession);
			}
		}

		return lst;
	}

	public static void killSessions(SeUser user)
	{
		for (Map.Entry<String, AuthSession> entry : mapAuthSessions.entrySet())
		{
			AuthSession authSession = entry.getValue();
			SeUser seUser = authSession.getSeUser();
			if (seUser.getUserId().equals(user.getUserId()))
			{
				authSession.getHttpSession().invalidate();
			}
		}
	}

}
