package sanzol.se.services;

import static sanzol.app.config.I18nPreference.getI18nString;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;

import sanzol.app.config.HibernateUtil;
import sanzol.se.audit.AuditService;
import sanzol.se.commons.BaseService;
import sanzol.se.context.RequestContext;
import sanzol.se.model.entities.SeNotification;
import sanzol.se.model.entities.SeNotificationUser;
import sanzol.se.model.entities.SeUser;
import sanzol.se.notifications.NotificationService;
import sanzol.util.DateTimeUtils;
import sanzol.util.json.JsonEncoder;
import sanzol.util.persistence.Query;

public class SeNotificationsService extends BaseService
{
	private static final String MESSAGE_CONTAINS_DEPENDENCIES = getI18nString("message.recordContainsDependencies");

	public SeNotificationsService(RequestContext context)
	{
		super(context);
	}

	@SuppressWarnings("unchecked")
	public List<SeNotification> getSeNotifications(Integer notificationTypeId, LocalDateTime dateFrom, LocalDateTime dateTo)
	{
		List<SeNotification> list = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			Query q = new Query(SeNotification.class, "a");

			q.where(
					q.eq("seNotificationType.notificationTypeId", notificationTypeId),
					q.ge("notificationDate", dateFrom),
					q.le("notificationDate", dateTo)
				);

			q.orderBy(q.desc("notificationId"));
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

	public SeNotification getSeNotification(int notificationId)
	{
		SeNotification seNotification = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();
			seNotification = (SeNotification) hbSession.get(SeNotification.class, notificationId);
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

		return seNotification;
	}

	public void addSeNotification(SeNotification seNotification)
	{
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			Query q = new Query(SeUser.class, "u");
			q.where(
					q.eq("seAccount.accountId", null),
					q.eq("isActive", true)
				);

			@SuppressWarnings("unchecked")
			List<SeUser> lstSeUsers = q.list(hbSession);

			if (lstSeUsers != null && !lstSeUsers.isEmpty())
			{
				seNotification.setNotificationDate(DateTimeUtils.now());
				hbSession.save(seNotification);
				hbSession.flush();

				for (SeUser seUser : lstSeUsers)
				{
					SeNotificationUser seNotificationUser = new SeNotificationUser();
					seNotificationUser.setSeNotification(seNotification);
					seNotificationUser.setSeUser(seUser);
					hbSession.save(seNotificationUser);
				}
				hbSession.flush();
			}

			// ----- Audit -----------------------------------------------------------------------------------------
			String jsonNewEntity = JsonEncoder.encode(seNotification);
			AuditService.auditInsert(hbSession, context, SeNotification.class.getSimpleName(), seNotification.getNotificationId().toString(), null, jsonNewEntity);
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

		// ----- NotificationService ---------------------------------------------------------------------------
		if (!context.hasErrorOrFatal())
		{
			NotificationService.getInstance().notifySubscribers();
		}

	}

	public void delSeNotification(SeNotification seNotification)
	{
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			// -----------------------------------------------------------------------------------------------------
			hbSession.createQuery("delete SeNotificationUser where seNotification.notificationId = :notificationId")
				.setParameter("notificationId", seNotification.getNotificationId())
				.executeUpdate();
			hbSession.flush();

			// -----------------------------------------------------------------------------------------------------
			hbSession.delete(seNotification);
			hbSession.flush();

			// ----- Audit -----------------------------------------------------------------------------------------
			String jsonOldEntity = JsonEncoder.encode(seNotification);
			AuditService.auditDelete(hbSession, context, SeNotification.class.getSimpleName(), seNotification.getNotificationId().toString(), null, jsonOldEntity);
			hbSession.flush();

			tx.commit();

		}
		catch (Exception e)
		{
			if (e.getCause() instanceof ConstraintViolationException)
			{
				context.getMsgLogger().addMessageError(MESSAGE_CONTAINS_DEPENDENCIES);
			}
			else
			{
				context.getMsgLogger().addMessage(e);
			}

			if (tx != null && tx.getStatus().canRollback())
			{
				tx.rollback();
			}
		}
		finally
		{
			hbSession.close();
		}

		// ----- NotificationService ---------------------------------------------------------------------------
		if (!context.hasErrorOrFatal())
		{
			NotificationService.getInstance().notifySubscribers();
		}

	}

}
