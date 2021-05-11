package sanzol.se.services;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import sanzol.app.config.HibernateUtil;
import sanzol.se.audit.AuditService;
import sanzol.se.commons.BaseService;
import sanzol.se.context.RequestContext;
import sanzol.se.model.entities.SeNotification;
import sanzol.se.model.entities.SeNotificationUser;
import sanzol.se.notifications.NotificationService;
import sanzol.util.DateTimeUtils;
import sanzol.util.json.JsonEncoder;
import sanzol.util.persistence.Query;

public class SeNotificationsUsersService extends BaseService
{
	public SeNotificationsUsersService(RequestContext context)
	{
		super(context);
	}

	@SuppressWarnings("unchecked")
	public List<SeNotificationUser> getSeNotificationsUsers(Integer notificationId, Integer userId, LocalDateTime dateFrom, LocalDateTime dateTo, Boolean isReaded, Boolean isDeleted)
	{
		List<SeNotificationUser> list = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			Query q = new Query(SeNotificationUser.class, "a");
			q.alias(SeNotification.class, "seNotification");
			q.where(
					q.eq("seNotification.notificationId", notificationId),
					q.eq("seUser.userId", userId),
					q.ge("seNotification.notificationDate", dateFrom),
					q.le("seNotification.notificationDate", dateTo)
				);

			q.orderBy(q.desc("notificationUserId"));
			list = q.list(hbSession);

			tx.commit();
		}
		catch (Exception e)
		{
			context.getMsgLogger().addMessage(e);
			if (tx != null && tx.getStatus().canRollback())
			{
				tx.rollback();
			}
		}
		finally
		{
			hbSession.close();
		}

		return list;
	}

	public SeNotificationUser getSeNotificationUser(int notificationUserId)
	{
		SeNotificationUser seNotificationUser = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			seNotificationUser = (SeNotificationUser) hbSession.get(SeNotificationUser.class, notificationUserId);

			seNotificationUser.setReadedDate(DateTimeUtils.now());
			hbSession.update(seNotificationUser);
			hbSession.flush();

			tx.commit();
		}
		catch (Exception e)
		{
			context.getMsgLogger().addMessage(e);
			if (tx != null && tx.getStatus().canRollback())
			{
				tx.rollback();
			}
		}
		finally
		{
			hbSession.close();
		}

		// ----- NotificationService -----
		if (!context.hasErrorOrFatal() && seNotificationUser != null)
		{
			NotificationService.getInstance().notifySubscribers(seNotificationUser.getSeUser().getUserId());
		}

		return seNotificationUser;
	}

	public void setReaded(int notificationUserId)
	{
		SeNotificationUser seNotificationUser = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			seNotificationUser = (SeNotificationUser) hbSession.get(SeNotificationUser.class, notificationUserId);

			seNotificationUser.setReadedDate(DateTimeUtils.now());
			hbSession.update(seNotificationUser);
			hbSession.flush();

			// ----- Audit -----------------------------------------------------------------------------------------
			String detail = JsonEncoder.encode(seNotificationUser);
			AuditService.auditTransaction(hbSession, context, "SET_READED", SeNotificationUser.class.getSimpleName(), seNotificationUser.getNotificationUserId().toString(), null, detail);
			hbSession.flush();

			tx.commit();
		}
		catch (Exception e)
		{
			context.getMsgLogger().addMessage(e);
			if (tx != null && tx.getStatus().canRollback())
			{
				tx.rollback();
			}
		}
		finally
		{
			hbSession.close();
		}

		// ----- NotificationService -----
		if (!context.hasErrorOrFatal() && seNotificationUser != null)
		{
			NotificationService.getInstance().notifySubscribers(seNotificationUser.getSeUser().getUserId());
		}

	}

	public void setDeleted(int notificationUserId)
	{
		SeNotificationUser seNotificationUser = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			seNotificationUser = (SeNotificationUser) hbSession.get(SeNotificationUser.class, notificationUserId);

			seNotificationUser.setDeletedDate(DateTimeUtils.now());
			hbSession.update(seNotificationUser);
			hbSession.flush();

			// ----- Audit -----------------------------------------------------------------------------------------
			String detail = JsonEncoder.encode(seNotificationUser);
			AuditService.auditTransaction(hbSession, context, "SET_DELETED", SeNotificationUser.class.getSimpleName(), seNotificationUser.getNotificationUserId().toString(), null, detail);
			hbSession.flush();

			tx.commit();
		}
		catch (Exception e)
		{
			context.getMsgLogger().addMessage(e);
			if (tx != null && tx.getStatus().canRollback())
			{
				tx.rollback();
			}
		}
		finally
		{
			hbSession.close();
		}

		// ----- NotificationService -----
		if (!context.hasErrorOrFatal() && seNotificationUser != null)
		{
			NotificationService.getInstance().notifySubscribers(seNotificationUser.getSeUser().getUserId());
		}

	}

}
