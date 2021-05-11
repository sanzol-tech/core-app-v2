/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: January 2021
 *
 */
package sanzol.se.notifications;

import java.io.Serializable;

public class NotificationSubscriber implements Serializable
{
	private static final long serialVersionUID = 1L;

	private Integer userId;
	private Integer unreadCount;

	public NotificationSubscriber(Integer userId)
	{
		this.userId = userId;
	}

	public Integer getUserId()
	{
		return userId;
	}

	public void setUserId(Integer userId)
	{
		this.userId = userId;
	}

	public Integer getUnreadCount()
	{
		return unreadCount;
	}

	public void setUnreadCount(Integer unreadCount)
	{
		this.unreadCount = unreadCount;
	}

}
