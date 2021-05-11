/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: June 2020
 *
 */
package sanzol.se.web.listeners;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sanzol.se.sessions.ActiveSessions;

@WebListener
public class SessionListener implements HttpSessionListener
{
	private static final Logger LOG = LoggerFactory.getLogger(SessionListener.class);

	@Override
	public void sessionCreated(HttpSessionEvent event)
	{
		HttpSession session = event.getSession();
		LOG.debug("Session created : " + session.getId());
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent event)
	{
		HttpSession session = event.getSession();
		LOG.debug("Session destroyed : " + session.getId());

		ActiveSessions.remove(session);
	}

}
