/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: January 2021
 *
 */
package sanzol.se.web.filters;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.omnifaces.filter.HttpFilter;
import org.omnifaces.util.Servlets;

import sanzol.se.license.LicenseService;
import sanzol.se.sessions.ActiveSessions;

public class SecurityFilter extends HttpFilter
{

	@Override
	public void doFilter(HttpServletRequest request, HttpServletResponse response, HttpSession session, FilterChain chain) throws ServletException, IOException
	{
		String redirectURL = "";

		String path = request.getRequestURI().substring(request.getContextPath().length());
		if (path.startsWith("/site/"))
		{
			if (session == null || ActiveSessions.getAuthSession(session.getId()) == null)
			{
				// redirectURL = "login/login.xhtml";
				redirectURL = "errors/expired.xhtml";
			}
			else if (LicenseService.getValidity() != LicenseService.Validity.VALID)
			{
				if (LicenseService.getExpiredAction().contains(LicenseService.ExpiredAction.DENIED))
				{
					redirectURL = "errors/license.xhtml";
				}
			}
		}

		if (redirectURL.isEmpty())
		{
			chain.doFilter(request, response);
		}
		else
		{
			Servlets.facesRedirect(request, response, redirectURL);
		}

	}

}
