package sanzol.se.services;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sanzol.app.config.HibernateUtil;
import sanzol.se.commons.BaseService;
import sanzol.se.context.RequestContext;
import sanzol.se.model.entities.SeError;
import sanzol.util.ExceptionUtils;
import sanzol.util.persistence.Query;

public class SeErrorsService extends BaseService
{
	private static final Logger LOG = LoggerFactory.getLogger(SeErrorsService.class);

	public SeErrorsService(RequestContext context)
	{
		super(context);
	}

	public List<SeError> getSeErrors()
	{
		return getSeErrors(null, null);
	}

	@SuppressWarnings("unchecked")
	public List<SeError> getSeErrors(LocalDateTime dateFrom, LocalDateTime dateTo)
	{
		List<SeError> list = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			Query q = new Query(SeError.class, "e");
			q.where(
					q.ge("e.errorDate", dateFrom),
					q.le("e.errorDate", dateTo)
				);
			q.orderBy(q.desc("e.errorId"));
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

	public SeError getSeError(int errorId)
	{
		SeError seError = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();
			seError = (SeError) hbSession.get(SeError.class, errorId);
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

		return seError;
	}

	public static void addSeError(SeError seError)
	{
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			hbSession.save(seError);
			hbSession.flush();

			tx.commit();
		}
		catch (Exception e)
		{
			LOG.error(ExceptionUtils.getMessage(e));
			if (tx != null && tx.getStatus().canRollback())
			{
				tx.rollback();
			}
		}
		finally
		{
			hbSession.close();
		}

	}

}
