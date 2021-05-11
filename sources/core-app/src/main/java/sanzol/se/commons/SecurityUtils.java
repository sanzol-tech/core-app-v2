/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: June 2020
 *
 */
package sanzol.se.commons;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.omnifaces.util.Servlets;

import sanzol.se.sessions.ActiveSessions;
import sanzol.se.sessions.AuthSession;

public final class SecurityUtils
{
	private SecurityUtils()
	{
		// Hide constructor
	}

	public static boolean isGranted(int permission, int plevel)
	{
		HttpServletRequest request = FacesUtils.getRequest();
		return isGranted(request, permission, plevel);
	}

	public static boolean isGranted(HttpServletRequest request, int permission, int level)
	{
		AuthSession authSession = ActiveSessions.getAuthSession(request);
		return authSession.isGranted(permission, level);
	}

	public static void redirectAccessDenied(HttpServletRequest request, HttpServletResponse response)
	{
		Servlets.facesRedirect(request, response, "errors/denied.xhtml");
	}

}
