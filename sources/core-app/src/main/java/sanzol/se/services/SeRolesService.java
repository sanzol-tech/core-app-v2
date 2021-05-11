package sanzol.se.services;

import static sanzol.app.config.I18nPreference.getI18nString;

import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;

import sanzol.app.config.HibernateUtil;
import sanzol.app.config.Permissions;
import sanzol.se.audit.AuditService;
import sanzol.se.commons.BaseService;
import sanzol.se.context.RequestContext;
import sanzol.se.model.SePermissionLevel;
import sanzol.se.model.entities.SeRole;
import sanzol.se.model.entities.SeRolePermission;
import sanzol.se.model.entities.SeRoleUser;
import sanzol.se.model.entities.SeUser;
import sanzol.util.json.JsonEncoder;
import sanzol.util.persistence.Query;

public class SeRolesService extends BaseService
{
	private static final String MESSAGE_ALREADY_EXISTS = getI18nString("seRoles.message.alreadyExists");
	private static final String MESSAGE_CONTAINS_DEPENDENCIES = getI18nString("message.recordContainsDependencies");

	public SeRolesService(RequestContext context)
	{
		super(context);
	}

	@SuppressWarnings("unchecked")
	public List<SeRole> getSeRoles(Boolean isActive)
	{
		List<SeRole> list = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			Query q = new Query(SeRole.class, "r");
			q.where(q.eq("r.isActive", isActive));
			q.orderBy(q.asc("r.name"));
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

	public SeRole getSeRole(int roleId)
	{
		SeRole seRole = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			seRole = (SeRole) hbSession.get(SeRole.class, roleId);

			if (seRole != null)
			{
				Hibernate.initialize(seRole.getLstSeRolesPermissions());
				Hibernate.initialize(seRole.getLstSeRolesUsers());
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

		return seRole;
	}

	public void addSeRole(SeRole seRole, List<SePermissionLevel> lstSePermissionLevel, List<Integer> lstUserIdSel)
	{
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			// Validate uniques
			// -------------------------------------------------------------------------------
			if (unique(hbSession, seRole))
			{
				context.getMsgLogger().addMessageError(MESSAGE_ALREADY_EXISTS);
				tx.rollback();
				return;
			}

			hbSession.save(seRole);
			hbSession.flush();

			// -----------------------------------------------------------------------------------------------------
			for (SePermissionLevel sePermissionLevel : lstSePermissionLevel)
			{
				SeRolePermission rolePermission = new SeRolePermission();
				rolePermission.setSeRole(seRole);
				rolePermission.setSePermission(sePermissionLevel.getSePermission());
				rolePermission.setPermissionLevel(sePermissionLevel.getLevel());
				hbSession.save(rolePermission);
			}
			hbSession.flush();

			// -----------------------------------------------------------------------------------------------------
			for (Integer userId : lstUserIdSel)
			{
				SeUser seUser = (SeUser) hbSession.get(SeUser.class, userId);
				SeRoleUser roleUser = new SeRoleUser();
				roleUser.setSeRole(seRole);
				roleUser.setSeUser(seUser);
				hbSession.save(roleUser);
			}
			hbSession.flush();

			// ----- Audit -----------------------------------------------------------------------------------------
			String jsonNewEntity = JsonEncoder.encode(seRole);
			AuditService.auditInsert(hbSession, context, SeRole.class.getSimpleName(), seRole.getRoleId().toString(), null, jsonNewEntity);
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

	public void setSeRole(SeRole seRole, List<SePermissionLevel> lstSePermissionLevel, List<Integer> lstUserIdSel)
	{
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			// Validate uniques
			// -------------------------------------------------------------------------------
			if (unique(hbSession, seRole))
			{
				context.getMsgLogger().addMessageError(MESSAGE_ALREADY_EXISTS);
				tx.rollback();
				return;
			}

			SeRole oldEntity = (SeRole) hbSession.get(SeRole.class, seRole.getRoleId());
			hbSession.evict(oldEntity);

			hbSession.update(seRole);
			hbSession.flush();

			// -----------------------------------------------------------------------------------------------------
			hbSession.createQuery("delete SeRolePermission where seRole.roleId = :roleId")
				.setParameter("roleId", seRole.getRoleId())
				.executeUpdate();
			hbSession.flush();

			for (SePermissionLevel sePermissionLevel : lstSePermissionLevel)
			{
				if (sePermissionLevel.getLevel() != Permissions.LEVEL_DENIED)
				{
					SeRolePermission rolePermission = new SeRolePermission();
					rolePermission.setSeRole(seRole);
					rolePermission.setSePermission(sePermissionLevel.getSePermission());
					rolePermission.setPermissionLevel(sePermissionLevel.getLevel());
					hbSession.save(rolePermission);
				}
			}
			hbSession.flush();

			// -----------------------------------------------------------------------------------------------------
			hbSession.createQuery("delete SeRoleUser where seRole.roleId = :roleId")
				.setParameter("roleId", seRole.getRoleId())
				.executeUpdate();
			hbSession.flush();

			for (Integer userId : lstUserIdSel)
			{
				SeUser seUser = (SeUser) hbSession.get(SeUser.class, userId);
				SeRoleUser roleUser = new SeRoleUser();
				roleUser.setSeRole(seRole);
				roleUser.setSeUser(seUser);
				hbSession.save(roleUser);
			}
			hbSession.flush();

			// ----- Audit -----------------------------------------------------------------------------------------
			String jsonOldEntity = JsonEncoder.encode(oldEntity);
			String jsonNewEntity = JsonEncoder.encode(seRole);
			AuditService.auditUpdate(hbSession, context, SeRole.class.getSimpleName(), seRole.getRoleId().toString(), null, jsonOldEntity, jsonNewEntity);
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

	public void delSeRole(SeRole seRole)
	{
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			// -----------------------------------------------------------------------------------------------------
			hbSession.createQuery("delete SeRolePermission where seRole.roleId = :roleId")
				.setParameter("roleId", seRole.getRoleId())
				.executeUpdate();
			hbSession.flush();

			hbSession.createQuery("delete SeRoleUser where seRole.roleId = :roleId")
				.setParameter("roleId", seRole.getRoleId())
				.executeUpdate();
			hbSession.flush();

			// -----------------------------------------------------------------------------------------------------
			hbSession.delete(seRole);
			hbSession.flush();

			// ----- Audit -----------------------------------------------------------------------------------------
			String jsonOldEntity = JsonEncoder.encode(seRole);
			AuditService.auditDelete(hbSession, context, SeRole.class.getSimpleName(), seRole.getRoleId().toString(), null, jsonOldEntity);
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

	private static boolean unique(org.hibernate.Session hbSession, SeRole seRole)
	{
		SeRole _seRole;

		if (seRole.getRoleId() == null)
		{
			Query q = new Query(SeRole.class, "a");
			q.where(q.eq("name", seRole.getName()));
			_seRole = (SeRole) q.uniqueResult(hbSession);
		}
		else
		{
			Query q = new Query(SeRole.class, "a");
			q.where(q.eq("name", seRole.getName()), q.ne("roleId", seRole.getRoleId()));
			_seRole = (SeRole) q.uniqueResult(hbSession);
		}

		return (_seRole != null);
	}

}
