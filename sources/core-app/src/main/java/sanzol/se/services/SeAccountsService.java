package sanzol.se.services;

import static sanzol.app.config.I18nPreference.getI18nString;

import java.util.List;
import java.util.StringJoiner;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;

import sanzol.app.config.HibernateUtil;
import sanzol.se.audit.AuditService;
import sanzol.se.commons.BaseService;
import sanzol.se.context.RequestContext;
import sanzol.se.model.entities.SeAccount;
import sanzol.se.model.entities.SeUser;
import sanzol.util.json.JsonEncoder;
import sanzol.util.persistence.Query;

public class SeAccountsService extends BaseService
{
	private static final String THE_ENTITY = getI18nString("seAccounts.label.theEntity");
	private static final String MESSAGE_ALREADY_EXISTS = getI18nString("seAccounts.message.alreadyExists");
	private static final String MESSAGE_CONTAINS_DEPENDENCIES = getI18nString("message.recordContainsDependencies");
	private static final String MESSAGE_CONTAINS_DEPENDENCIES_DET = getI18nString("message.recordContainsDependencies.detailed");

	public SeAccountsService(RequestContext context)
	{
		super(context);
	}

	@SuppressWarnings("unchecked")
	public List<SeAccount> getSeAccounts(Boolean isActive)
	{
		List<SeAccount> list = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			Query q = new Query(SeAccount.class, "a");

			q.where(q.eq("isActive", isActive));

			q.orderBy(q.asc("accountId"));
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

	public SeAccount getSeAccount(int accountId)
	{
		SeAccount seAccount = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();
			seAccount = (SeAccount) hbSession.get(SeAccount.class, accountId);
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

		return seAccount;
	}

	public void addSeAccount(SeAccount seAccount)
	{
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			// Validate uniques
			// -------------------------------------------------------------------------------
			if (unique(hbSession, seAccount))
			{
				context.getMsgLogger().addMessageError(MESSAGE_ALREADY_EXISTS);
				tx.rollback();
				return;
			}

			hbSession.save(seAccount);
			hbSession.flush();

			// ----- Audit -----------------------------------------------------------------------------------------
			String jsonNewEntity = JsonEncoder.encode(seAccount);
			AuditService.auditInsert(hbSession, context, SeAccount.class.getSimpleName(), seAccount.getAccountId().toString(), null, jsonNewEntity);
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

	public void setSeAccount(SeAccount seAccount)
	{
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			// Validate uniques
			// -------------------------------------------------------------------------------
			if (unique(hbSession, seAccount))
			{
				context.getMsgLogger().addMessageError(MESSAGE_ALREADY_EXISTS);
				tx.rollback();
				return;
			}

			SeAccount oldEntity = (SeAccount) hbSession.get(SeAccount.class, seAccount.getAccountId());
			hbSession.evict(oldEntity);

			hbSession.update(seAccount);
			hbSession.flush();

			// ----- Audit -----------------------------------------------------------------------------------------
			String jsonOldEntity = JsonEncoder.encode(oldEntity);
			String jsonNewEntity = JsonEncoder.encode(seAccount);
			AuditService.auditUpdate(hbSession, context, SeAccount.class.getSimpleName(), seAccount.getAccountId().toString(), null, jsonOldEntity, jsonNewEntity);
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

	public void delSeAccount(SeAccount seAccount)
	{
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			// Validate dependencies
			// -----------------------------------------------------------------------------------------------------
			String dependencies = dependencies(hbSession, seAccount);
			if (dependencies != null)
			{
				context.getMsgLogger().addMessageError(MESSAGE_CONTAINS_DEPENDENCIES_DET.replace("{theEntity}", THE_ENTITY).replace("{dependencies}", dependencies));
				tx.rollback();
				return;
			}

			hbSession.delete(seAccount);
			hbSession.flush();

			// ----- Audit -----------------------------------------------------------------------------------------
			String jsonOldEntity = JsonEncoder.encode(seAccount);
			AuditService.auditDelete(hbSession, context, SeAccount.class.getSimpleName(), seAccount.getAccountId().toString(), null, jsonOldEntity);
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

	private static boolean unique(org.hibernate.Session hbSession, SeAccount seAccount)
	{
		SeAccount _seAccount;

		if (seAccount.getAccountId() == null)
		{
			Query q = new Query(SeAccount.class, "a");
			q.where(q.eq("name", seAccount.getName()));
			_seAccount = (SeAccount) q.uniqueResult(hbSession);
		}
		else
		{
			Query q = new Query(SeAccount.class, "a");
			q.where(
					q.eq("name", seAccount.getName()),
					q.ne("accountId", seAccount.getAccountId())
				);
			_seAccount = (SeAccount) q.uniqueResult(hbSession);
		}

		return (_seAccount != null);
	}

	private static String dependencies(org.hibernate.Session hbSession, SeAccount seAccount)
	{
		StringJoiner joiner = new StringJoiner(",");

		Query q = new Query(SeUser.class, "a");
		q.where(q.eq("seAccount.accountId", seAccount.getAccountId()));
		SeUser _seUser = (SeUser) q.firstResult(hbSession);
		if (_seUser != null)
			joiner.add("seUser");

		if (joiner.length() > 0)
			return joiner.toString();
		else
			return null;
	}

}
