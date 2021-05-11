/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: January 2021
 *
 */
package sanzol.se.web.filters;

import static org.omnifaces.exceptionhandler.FullAjaxExceptionHandler.getExceptionTypesToIgnoreInLogging;
import static org.omnifaces.util.Utils.isOneInstanceOf;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.omnifaces.filter.HttpFilter;

import sanzol.se.error.ErrorService;

public class ExceptionFilter extends HttpFilter
{
	private Class<? extends Throwable>[] exceptionTypesToIgnoreInLogging;

	@Override
	public void init() throws ServletException
	{
		exceptionTypesToIgnoreInLogging = getExceptionTypesToIgnoreInLogging(getServletContext());
	}

	@Override
	public void doFilter(HttpServletRequest request, HttpServletResponse response, HttpSession session, FilterChain chain) throws ServletException, IOException
	{
		try
		{
			chain.doFilter(request, response);
		}
		catch (FileNotFoundException ignore)
		{
			response.sendError(HttpServletResponse.SC_NOT_FOUND, request.getRequestURI());
		}
		catch (Throwable ex)
		{
			Throwable cause = ex instanceof ServletException ? ex.getCause() : ex;

			if (!isOneInstanceOf(cause.getClass(), exceptionTypesToIgnoreInLogging))
			{
				ErrorService.registerError(request, cause);
			}

			throw ex;
		}
	}

}