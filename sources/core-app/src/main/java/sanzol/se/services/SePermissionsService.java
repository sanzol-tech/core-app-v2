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
import sanzol.se.model.entities.SePermission;
import sanzol.util.json.JsonEncoder;
import sanzol.util.persistence.Query;

public class SePermissionsService extends BaseService
{
	private static final String MESSAGE_CONTAINS_DEPENDENCIES = getI18nString("message.recordContainsDependencies");

	public SePermissionsService(RequestContext context)
	{
		super(context);
	}

	@SuppressWarnings("unchecked")
	public List<SePermission> getSePermissions(Boolean isActive)
	{
		List<SePermission> list = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			Query q = new Query(SePermission.class, "p");
			q.where(q.eq("p.isActive", isActive));
			q.orderBy(q.asc("p.category"), q.asc("p.permissionId"));
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

	public SePermission getSePermission(int permissionId)
	{
		SePermission sePermission = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();
			sePermission = (SePermission) hbSession.get(SePermission.class, permissionId);
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

		return sePermission;
	}

	public void addSePermission(SePermission sePermission)
	{
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			hbSession.save(sePermission);
			hbSession.flush();

			// ----- Audit -----------------------------------------------------------------------------------------
			String jsonNewEntity = JsonEncoder.encode(sePermission);
			AuditService.auditInsert(hbSession, context, SePermission.class.getSimpleName(), sePermission.getPermissionId().toString(), null, jsonNewEntity);
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
	}

	public void setSePermission(SePermission sePermission)
	{
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			SePermission oldEntity = (SePermission) hbSession.get(SePermission.class, sePermission.getPermissionId());
			hbSession.evict(oldEntity);

			hbSession.update(sePermission);
			hbSession.flush();

			// ----- Audit -----------------------------------------------------------------------------------------
			String jsonOldEntity = JsonEncoder.encode(oldEntity);
			String jsonNewEntity = JsonEncoder.encode(sePermission);
			AuditService.auditUpdate(hbSession, context, SePermission.class.getSimpleName(), sePermission.getPermissionId().toString(), null, jsonOldEntity, jsonNewEntity);
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
	}

	public void delSePermission(SePermission sePermission)
	{
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			hbSession.delete(sePermission);
			hbSession.flush();

			// ----- Audit -----------------------------------------------------------------------------------------
			String jsonOldEntity = JsonEncoder.encode(sePermission);
			AuditService.auditDelete(hbSession, context, SePermission.class.getSimpleName(), sePermission.getPermissionId().toString(), null, jsonOldEntity);
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
