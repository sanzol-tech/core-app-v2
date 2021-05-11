package sanzol.se.services;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import sanzol.app.config.HibernateUtil;
import sanzol.se.commons.BaseService;
import sanzol.se.context.RequestContext;
import sanzol.se.model.entities.SeAudit;
import sanzol.se.model.entities.SeUser;
import sanzol.util.persistence.Query;

public class SeAuditService extends BaseService
{
	public SeAuditService(RequestContext context)
	{
		super(context);
	}

	@SuppressWarnings("unchecked")
	public List<SeAudit> getSeAudit(SeUser seUser, LocalDateTime dateFrom, LocalDateTime dateTo)
	{
		List<SeAudit> list = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			Query q = new Query(SeAudit.class, "a");
			q.where(
					q.eq("a.seUser", seUser),
					q.ge("a.auditDate", dateFrom),
					q.le("a.auditDate", dateTo)
				);
			q.orderBy(q.desc("a.auditId"));
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

	public SeAudit getSeAudit(int auditId)
	{
		SeAudit seAudit = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();
			seAudit = (SeAudit) hbSession.get(SeAudit.class, auditId);
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

		return seAudit;
	}

}
