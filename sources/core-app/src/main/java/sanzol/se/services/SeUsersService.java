package sanzol.se.services;

import static sanzol.app.config.I18nPreference.getI18nString;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.StringJoiner;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;

import sanzol.app.config.HibernateUtil;
import sanzol.se.audit.AuditEvents;
import sanzol.se.audit.AuditService;
import sanzol.se.commons.BaseService;
import sanzol.se.context.RequestContext;
import sanzol.se.model.entities.SeAudit;
import sanzol.se.model.entities.SeEmailTemplate;
import sanzol.se.model.entities.SeNotificationUser;
import sanzol.se.model.entities.SePassword;
import sanzol.se.model.entities.SeRole;
import sanzol.se.model.entities.SeRoleUser;
import sanzol.se.model.entities.SeSector;
import sanzol.se.model.entities.SeSectorUser;
import sanzol.se.model.entities.SeUser;
import sanzol.se.services.cache.SeEmailTemplatesCache;
import sanzol.util.DateTimeUtils;
import sanzol.util.Replacer;
import sanzol.util.json.JsonBuilder;
import sanzol.util.json.JsonEncoder;
import sanzol.util.persistence.Query;
import sanzol.util.security.PasswordUtils;

public class SeUsersService extends BaseService
{
	private static final String THE_ENTITY = getI18nString("seUsers.label.theEntity");
	private static final String MESSAGE_USERNAME_ALREADY_EXISTS = getI18nString("seUsers.message.usernameExists");
	private static final String MESSAGE_EMAIL_ALREADY_EXISTS = getI18nString("seUsers.message.emailExists");
	private static final String MESSAGE_CONTAINS_DEPENDENCIES = getI18nString("message.recordContainsDependencies");
	private static final String MESSAGE_CONTAINS_DEPENDENCIES_DET = getI18nString("message.recordContainsDependencies.detailed");

	public SeUsersService(RequestContext context)
	{
		super(context);
	}

	@SuppressWarnings("unchecked")
	public List<SeUser> getSeUsers(Integer accountId, Boolean isActive)
	{
		List<SeUser> list = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			Query q = new Query(SeUser.class, "u");
			q.where(
					q.eq("seAccount.accountId", accountId),
					q.eq("isActive", isActive)
				);
			q.orderBy(q.asc("u.username"));
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

	public SeUser getSeUser(int userId)
	{
		SeUser seUser = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			seUser = (SeUser) hbSession.get(SeUser.class, userId);

			if (seUser != null)
			{
				Hibernate.initialize(seUser.getLstSeSectorsUser());
				Hibernate.initialize(seUser.getLstSeRolesUser());
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

		return seUser;
	}

	public void addSeUser(SeUser seUser, String password, List<String> lstSeSectorsSel, List<String> lstSeRolesSel)
	{
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			// seUser.setUsername(seUser.getUsername().trim().toLowerCase());
			// seUser.setEmail(seUser.getEmail().isBlank() ? null : seUser.getEmail().trim().toLowerCase());

			// Validate uniques
			// -------------------------------------------------------------------------------
			String uqErrorMessage = unique(hbSession, seUser);
			if (uqErrorMessage != null)
			{
				context.getMsgLogger().addMessageError(uqErrorMessage);
				tx.rollback();
				return;
			}

			// -------------------------------------------------------------------------------
			hbSession.save(seUser);
			hbSession.flush();

			// -----------------------------------------------------------------------------------------------------
			createPassword(hbSession, seUser, password, true);

			// -----------------------------------------------------------------------------------------------------
			for (String value : lstSeSectorsSel)
			{
				SeSector sector = (SeSector) hbSession.get(SeSector.class, Integer.valueOf(value));
				SeSectorUser sectorUser = new SeSectorUser();
				sectorUser.setSeSector(sector);
				sectorUser.setSeUser(seUser);
				hbSession.save(sectorUser);
			}
			hbSession.flush();

			// -----------------------------------------------------------------------------------------------------
			for (String value : lstSeRolesSel)
			{
				SeRole seRole = (SeRole) hbSession.get(SeRole.class, Integer.valueOf(value));
				SeRoleUser roleUser = new SeRoleUser();
				roleUser.setSeRole(seRole);
				roleUser.setSeUser(seUser);
				hbSession.save(roleUser);
			}
			hbSession.flush();

			// -------- Audit ----------------------------------------------------------------------------------------
			String jsonNewEntity = JsonEncoder.encode(seUser);
			AuditService.auditInsert(hbSession, context, SeUser.class.getSimpleName(), seUser.getUserId().toString(), null, jsonNewEntity);
			hbSession.flush();

			// -------- Send email -----------------------------------------------------------------------------------
			if (seUser.getEmail() != null && !seUser.getEmail().isBlank())
			{
				String to = seUser.getEmail();
				SeEmailTemplate seEmailTemplate = SeEmailTemplatesCache.getSeEmailTemplate(SeEmailTemplatesCache.TEMPLATE_USER_WELCOME);
				Replacer replacer = Replacer.create()
					.add("name", seUser.getNameAlt())
					.add("username", seUser.getUsername())
					.add("password", password);
				SeEmailService.create().withTemplate(seEmailTemplate, replacer).sendAsync(to);
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

	private static SePassword createPassword(org.hibernate.Session hbSession, SeUser seUser, String password, boolean isTemporal) throws NoSuchAlgorithmException, InvalidKeySpecException
	{
		SePassword sePassword = new SePassword();
		sePassword.setSeUser(seUser);
		sePassword.setDateFrom(DateTimeUtils.now());
		sePassword.setPassword(PasswordUtils.hashPassword(seUser.getUsername(), password));
		sePassword.setIsTemporal(isTemporal);

		hbSession.save(sePassword);
		hbSession.flush();

		return sePassword;
	}

	public void setSeUser(SeUser seUser, List<String> lstSeSectorsSel, List<String> lstSeRolesSel)
	{
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			// seUser.setUsername(seUser.getUsername().trim().toLowerCase());
			// seUser.setEmail(seUser.getEmail().isBlank() ? null : seUser.getEmail().trim().toLowerCase());

			// Validate uniques
			// -------------------------------------------------------------------------------
			String uqErrorMessage = unique(hbSession, seUser);
			if (uqErrorMessage != null)
			{
				context.getMsgLogger().addMessageError(uqErrorMessage);
				tx.rollback();
				return;
			}

			// -------------------------------------------------------------------------------
			SeUser oldEntity = (SeUser) hbSession.get(SeUser.class, seUser.getUserId());
			hbSession.evict(oldEntity);

			// -------------------------------------------------------------------------------
			hbSession.update(seUser);
			hbSession.flush();

			// -----------------------------------------------------------------------------------------------------
			hbSession.createQuery("delete SeSectorUser where seUser.userId = :userId")
				.setParameter("userId", seUser.getUserId())
				.executeUpdate();
			hbSession.flush();

			for (String value : lstSeSectorsSel)
			{
				SeSector seSector = (SeSector) hbSession.get(SeSector.class, Integer.valueOf(value));
				SeSectorUser sectorUser = new SeSectorUser();
				sectorUser.setSeSector(seSector);
				sectorUser.setSeUser(seUser);
				hbSession.save(sectorUser);
			}
			hbSession.flush();

			// -----------------------------------------------------------------------------------------------------
			hbSession.createQuery("delete SeRoleUser where seUser.userId = :userId")
				.setParameter("userId", seUser.getUserId())
				.executeUpdate();
			hbSession.flush();

			for (String value : lstSeRolesSel)
			{
				SeRole seRole = (SeRole) hbSession.get(SeRole.class, Integer.valueOf(value));
				SeRoleUser roleUser = new SeRoleUser();
				roleUser.setSeRole(seRole);
				roleUser.setSeUser(seUser);
				hbSession.save(roleUser);
			}
			hbSession.flush();

			// ----- Audit -----------------------------------------------------------------------------------------
			String jsonOldEntity = JsonEncoder.encode(oldEntity);
			String jsonNewEntity = JsonEncoder.encode(seUser);
			AuditService.auditUpdate(hbSession, context, SeUser.class.getSimpleName(), seUser.getUserId().toString(), null, jsonOldEntity, jsonNewEntity);
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

	public void delSeUser(SeUser seUser)
	{
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			// Validate dependencies
			// -----------------------------------------------------------------------------------------------------
			String dependencies = dependencies(hbSession, seUser);
			if (dependencies != null)
			{
				context.getMsgLogger().addMessageError(MESSAGE_CONTAINS_DEPENDENCIES_DET.replace("{theEntity}", THE_ENTITY).replace("{dependencies}", dependencies));
				tx.rollback();
				return;
			}

			// -----------------------------------------------------------------------------------------------------
			hbSession.createQuery("delete SePassword where seUser.userId = :userId")
				.setParameter("userId", seUser.getUserId())
				.executeUpdate();
			hbSession.flush();

			hbSession.createQuery("delete SeSectorUser where seUser.userId = :userId")
				.setParameter("userId", seUser.getUserId())
				.executeUpdate();
			hbSession.flush();

			hbSession.createQuery("delete SeRoleUser where seUser.userId = :userId")
				.setParameter("userId", seUser.getUserId())
				.executeUpdate();
			hbSession.flush();

			hbSession.createQuery("delete SeError where seUser.userId = :userId")
				.setParameter("userId", seUser.getUserId())
				.executeUpdate();
			hbSession.flush();

			// -----------------------------------------------------------------------------------------------------
			hbSession.delete(seUser);
			hbSession.flush();

			// ----- Audit -----------------------------------------------------------------------------------------
			String jsonOldEntity = JsonEncoder.encode(seUser);
			AuditService.auditDelete(hbSession, context, SeUser.class.getSimpleName(), seUser.getUserId().toString(), null, jsonOldEntity);
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

	private String unique(org.hibernate.Session hbSession, SeUser seUser)
	{
		String errorMessage = null;

		SeUser _seUser;
		if (seUser.getUserId() == null)
		{
			Query q = new Query(SeUser.class, "a");
			q.where(q.OR(q.eq("username", seUser.getUsername()), q.eq("email", seUser.getEmail())));
			_seUser = (SeUser) q.firstResult(hbSession);
		}
		else
		{
			Query q = new Query(SeUser.class, "a");
			q.where(
					q.OR(q.eq("username", seUser.getUsername()),
						 q.eq("email", seUser.getEmail())),
					q.ne("userId", seUser.getUserId())
				);
			_seUser = (SeUser) q.firstResult(hbSession);
		}

		if (_seUser != null)
		{
			if (_seUser.getUsername().equalsIgnoreCase(seUser.getUsername()))
				errorMessage = MESSAGE_USERNAME_ALREADY_EXISTS;
			else if (_seUser.getEmail().equalsIgnoreCase(seUser.getEmail()))
				errorMessage = MESSAGE_EMAIL_ALREADY_EXISTS;
			else
				errorMessage = "The record contains a unique field that already exists";
		}

		return errorMessage;
	}

	private static String dependencies(org.hibernate.Session hbSession, SeUser seUser)
	{
		StringJoiner joiner = new StringJoiner(",");

		Query q1 = new Query(SeNotificationUser.class, "a");
		q1.where(q1.eq("seUser.userId", seUser.getUserId()));
		SeNotificationUser _seNotificationUser = (SeNotificationUser) q1.firstResult(hbSession);
		if (_seNotificationUser != null)
			joiner.add("seNotificationUser");

		Query q2 = new Query(SeAudit.class, "a");
		q2.where(q2.eq("seUser.userId", seUser.getUserId()));
		SeAudit _seAudit = (SeAudit) q2.firstResult(hbSession);
		if (_seAudit != null)
			joiner.add("seAudit");

		if (joiner.length() > 0)
			return joiner.toString();
		else
			return null;
	}

	public void unlockUser(SeUser seUser)
	{
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			hbSession.createQuery("update SeUser set incorrectAttempts = 0, isLocked = false where userId = :userId")
				.setParameter("userId", seUser.getUserId())
				.executeUpdate();
			hbSession.flush();

			// ----- Audit -----------------------------------------------------------------------------------------
			String detail = JsonBuilder.create(null).add("username", seUser.getUsername()).toString();
			AuditService.auditTransaction(hbSession, context, AuditEvents.UNLOCK_USER, SeUser.class.getSimpleName(), seUser.getUserId().toString(), null, detail);
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

}
