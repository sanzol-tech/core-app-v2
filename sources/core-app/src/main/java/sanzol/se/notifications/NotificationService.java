/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: January 2021
 *
 */
package sanzol.se.notifications;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sanzol.app.config.HibernateUtil;

public final class NotificationService
{
	private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);

	private Vector<NotificationSubscriber> subscribers;

	private static NotificationService notificationService;

	public static synchronized NotificationService getInstance()
	{
		if (notificationService == null)
		{
			notificationService = new NotificationService();
		}
		return notificationService;
	}

	private NotificationService()
	{
		subscribers = new Vector<NotificationSubscriber>();
	}

	public synchronized void addSuscriber(NotificationSubscriber subscriber)
	{
		if (subscriber == null)
		{
			throw new IllegalArgumentException("subscriber is NULL");
		}
		if (!subscribers.contains(subscriber))
		{
			LOG.debug("--- NOTIFICATIONS.SUBSCRIBE --- Key: {}", subscriber.getUserId());
			subscribers.addElement(subscriber);

			Integer unreadCount = getNotificationCount(subscriber.getUserId());
			subscriber.setUnreadCount(unreadCount);
		}
	}

	public synchronized void removeSuscriber(NotificationSubscriber subscriber)
	{
		LOG.debug("--- NOTIFICATIONS.UNSUBSCRIBE --- Key: {}", subscriber.getUserId());
		subscribers.removeElement(subscriber);
	}

	public synchronized void removeAllSuscribers(Integer userId)
	{
		subscribers.clear();
		for (int i = 0; i < subscribers.size(); i++)
		{
			if (userId.equals(subscribers.get(i).getUserId()))
			{
				subscribers.remove(i);
			}
		}
	}

	public synchronized void removeAllSuscribers()
	{
		subscribers.clear();
	}

	public void notifySubscribers(int userId)
	{
		LOG.debug("--- NOTIFICATIONS.NOTIFY --- userId: {}", userId);

		Integer unreadCount = getNotificationCount(userId);
		for (NotificationSubscriber s : subscribers)
		{
			if (s.getUserId().intValue() == userId)
			{
				s.setUnreadCount(unreadCount);
			}
		}
	}

	public void notifySubscribers()
	{
		LOG.debug("--- NOTIFICATIONS.NOTIFY ---");

		Map<Integer, Integer> map = getNotificationCount();

		for (NotificationSubscriber s : subscribers)
		{
			if (map.containsKey(s.getUserId()))
			{
				s.setUnreadCount(map.get(s.getUserId()));
			}
			else
			{
				s.setUnreadCount(0);
			}
		}
	}

	private Integer getNotificationCount(int userId)
	{
		Integer unreadCount = 0;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			String hql = "select count(*) as unreadCount"
					   + " from SeNotificationUser"
					   + " where readedDate is null"
					   + " and deletedDate is null"
					   + " and seUser.userId = :userId";

			org.hibernate.query.Query<Number> query = hbSession.createQuery(hql, Number.class);
			query.setParameter("userId", userId);

			Number number = (Number) query.uniqueResult();
			unreadCount = number.intValue();

			tx.commit();
		}
		catch (Exception e)
		{
			LOG.error("Error getting unread notifications", e);
			if (tx != null && tx.getStatus().canRollback())
			{
				tx.rollback();
			}
		}
		finally
		{
			hbSession.close();
		}

		return unreadCount;
	}

	private Map<Integer, Integer> getNotificationCount()
	{
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			String hql = "select seUser.userId, count(*) as unreadCount"
					   + " from SeNotificationUser"
					   + " where readedDate is null and deletedDate is null"
					   + " group by seUser.userId";

			org.hibernate.query.Query<Object[]> query = hbSession.createQuery(hql, Object[].class);

			List<Object[]> list = query.list();
			for (Object[] row : list)
			{
				Integer userId = ((Number) row[0]).intValue();
				Integer unreadCount = ((Number) row[1]).intValue();

				map.put(userId, unreadCount);
			}

			tx.commit();
		}
		catch (Exception e)
		{
			LOG.error("Error getting unread notifications", e);
			if (tx != null && tx.getStatus().canRollback())
			{
				tx.rollback();
			}
		}
		finally
		{
			hbSession.close();
		}

		return map;
	}

}
