package sanzol.se.services;

import static sanzol.app.config.I18nPreference.getI18nString;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;

import sanzol.app.config.HibernateUtil;
import sanzol.se.audit.AuditService;
import sanzol.se.commons.BaseService;
import sanzol.se.context.RequestContext;
import sanzol.se.model.entities.SeWhitelist;
import sanzol.util.json.JsonEncoder;
import sanzol.util.persistence.Query;

public class SeWhitelistService extends BaseService
{
	private static final String MESSAGE_CONTAINS_DEPENDENCIES = getI18nString("message.recordContainsDependencies");

	public SeWhitelistService(RequestContext context)
	{
		super(context);
	}

	@SuppressWarnings("unchecked")
	public List<SeWhitelist> getSeWhiteList()
	{
		List<SeWhitelist> list = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			Query q = new Query(SeWhitelist.class, "a");
			q.orderBy(q.asc("lastname"), q.asc("firstname"));
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

	public SeWhitelist getSeWhiteList(int whiteListId)
	{
		SeWhitelist seWhitelist = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();
			seWhitelist = (SeWhitelist) hbSession.get(SeWhitelist.class, whiteListId);
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

		return seWhitelist;
	}

	public void addSeWhiteList(SeWhitelist seWhitelist, boolean invite)
	{
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			hbSession.save(seWhitelist);
			hbSession.flush();

			// ----- Audit -----------------------------------------------------------------------------------------
			String jsonNewEntity = JsonEncoder.encode(seWhitelist);
			AuditService.auditInsert(hbSession, context, SeWhitelist.class.getSimpleName(), seWhitelist.getWhitelistId().toString(), null, jsonNewEntity);
			hbSession.flush();

			if (invite)
			{
				SeRegistrationsService seRegistrationsService = new SeRegistrationsService(context);
				seRegistrationsService.addInvitation(hbSession, seWhitelist);
			}

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
	}

	public void setSeWhiteList(SeWhitelist seWhitelist, boolean invite)
	{
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			SeWhitelist oldEntity = (SeWhitelist) hbSession.get(SeWhitelist.class, seWhitelist.getWhitelistId());
			hbSession.evict(oldEntity);

			hbSession.update(seWhitelist);
			hbSession.flush();

			// ----- Audit -----------------------------------------------------------------------------------------
			String jsonOldEntity = JsonEncoder.encode(oldEntity);
			String jsonNewEntity = JsonEncoder.encode(seWhitelist);
			AuditService.auditUpdate(hbSession, context, SeWhitelist.class.getSimpleName(), seWhitelist.getWhitelistId().toString(), null, jsonOldEntity, jsonNewEntity);
			hbSession.flush();

			if (invite)
			{
				SeRegistrationsService seRegistrationsService = new SeRegistrationsService(context);
				seRegistrationsService.addInvitation(hbSession, seWhitelist);
			}

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
	}

	public void delSeWhiteList(SeWhitelist seWhitelist)
	{
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			hbSession.delete(seWhitelist);
			hbSession.flush();

			// ----- Audit -----------------------------------------------------------------------------------------
			String jsonOldEntity = JsonEncoder.encode(seWhitelist);
			AuditService.auditDelete(hbSession, context, SeWhitelist.class.getSimpleName(), seWhitelist.getWhitelistId().toString(), null, jsonOldEntity);
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
	}

}
