/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: June 2020
 *
 */
package sanzol.se.web.listeners;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import sanzol.app.config.Application;

@WebListener
public class ApplicationListener implements ServletContextListener
{
	@Override
	public void contextInitialized(ServletContextEvent event)
	{
		Application.init(event.getServletContext());
	}

	@Override
	public void contextDestroyed(ServletContextEvent event)
	{
		//
	}

}