package sanzol.se.services;

import static sanzol.app.config.I18nPreference.getI18nString;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;

import org.apache.commons.codec.DecoderException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import sanzol.app.config.HibernateUtil;
import sanzol.se.audit.AuditEvents;
import sanzol.se.audit.AuditService;
import sanzol.se.commons.BaseService;
import sanzol.se.context.RequestContext;
import sanzol.se.model.entities.SeEmailTemplate;
import sanzol.se.model.entities.SePassword;
import sanzol.se.model.entities.SeUser;
import sanzol.se.services.cache.SeEmailTemplatesCache;
import sanzol.util.DateTimeUtils;
import sanzol.util.Replacer;
import sanzol.util.json.JsonBuilder;
import sanzol.util.persistence.Query;
import sanzol.util.security.PasswordUtils;
import sanzol.util.validator.PasswordValidator;

public class SePasswordService extends BaseService
{
	private static final String MESSAGE_PASSWORD_CURRENT_INCORRECT = getI18nString("sePasswordChange.message.currentIncorrect");

	public SePasswordService(RequestContext context)
	{
		super(context);
	}

	// ---------------------------------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------------

	public void changePassword(SeUser seUser, String oldPassword, String newPassword)
	{
		if (seUser == null)
		{
			throw new IllegalArgumentException("seUser cannot be null");
		}

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			changePassword(hbSession, seUser, oldPassword, newPassword);
			if (context.hasErrorOrFatal())
			{
				tx.rollback();
				return;
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

		return;
	}

	private void changePassword(org.hibernate.Session hbSession, SeUser seUser, String oldPassword, String newPassword) throws NoSuchAlgorithmException, InvalidKeySpecException, DecoderException
	{

		// -------- Get current password -------------------------------------------------------------------------
		Query query = new Query(SePassword.class, "p");
		query.where(
				query.eq("p.seUser", seUser),
				query.isNull("p.dateTo")
			);
		SePassword sePasswordStored = (SePassword) query.uniqueResult(hbSession);

		// -------------------------------------------------------------------------------------------------------
		if (sePasswordStored == null || !PasswordUtils.validatePassword(seUser.getUsername(), oldPassword, sePasswordStored.getPassword()))
		{
			context.getMsgLogger().addMessageError(MESSAGE_PASSWORD_CURRENT_INCORRECT);
			return;
		}

		LocalDateTime now = DateTimeUtils.now();

		// -------- Update current password ----------------------------------------------------------------------
		sePasswordStored.setDateTo(now);
		hbSession.update(sePasswordStored);
		hbSession.flush();

		// -------- Add new password -----------------------------------------------------------------------------
		SePassword sePassword = new SePassword();
		sePassword.setSeUser(seUser);
		sePassword.setDateFrom(now);
		sePassword.setPassword(PasswordUtils.hashPassword(seUser.getUsername(), newPassword));

		hbSession.save(sePassword);
		hbSession.flush();

		// -------- Audit ----------------------------------------------------------------------------------------
		String detail = JsonBuilder.create(null).add("username", seUser.getUsername()).toString();
		AuditService.auditTransaction(hbSession, context, AuditEvents.PASSWORD_CHANGED, SePassword.class.getSimpleName(), sePassword.getPasswordId().toString(), seUser.getUserId().toString(), detail);
		hbSession.flush();

		// -------- Send email -----------------------------------------------------------------------------------
		if (seUser.getEmail() != null && !seUser.getEmail().isBlank())
		{
			String to = seUser.getFormattedEmail();
			SeEmailTemplate seEmailTemplate = SeEmailTemplatesCache.getSeEmailTemplate(SeEmailTemplatesCache.TEMPLATE_PASSWORD_CHANGED);
			Replacer replacer = Replacer.create()
						.add("name", seUser.getNameAlt())
						.add("username", seUser.getUsername());
			SeEmailService.create().withTemplate(to, seEmailTemplate, replacer).sendAsync();
		}
	}

	// ---------------------------------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------------

	public String reset(SeUser seUser)
	{
		if (seUser == null)
		{
			throw new IllegalArgumentException("seUser cannot be null");
		}

		String newPassword = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();
			newPassword = reset(hbSession, seUser);
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

		return newPassword;
	}

	private String reset(org.hibernate.Session hbSession, SeUser seUser) throws NoSuchAlgorithmException, InvalidKeySpecException
	{
		// -------- Get current password -------------------------------------------------------------------------
		Query query = new Query(SePassword.class, "p");
		query.where(
				query.eq("p.seUser", seUser),
				query.isNull("p.dateTo")
			);
		SePassword sePasswordStored = (SePassword) query.uniqueResult(hbSession);

		// -------------------------------------------------------------------------------------------------------
		LocalDateTime now = DateTimeUtils.now();

		// -------- Update current password ----------------------------------------------------------------------
		if (sePasswordStored != null)
		{
			sePasswordStored.setDateTo(now);

			hbSession.update(sePasswordStored);
			hbSession.flush();
		}

		// -------- Generate random password ---------------------------------------------------------------------
		String newPassword = PasswordValidator.generatePassword();

		// -------- Add new password -----------------------------------------------------------------------------
		SePassword sePassword = new SePassword();
		sePassword.setSeUser(seUser);
		sePassword.setDateFrom(now);
		sePassword.setPassword(PasswordUtils.hashPassword(seUser.getUsername(), newPassword));
		sePassword.setIsTemporal(true);

		hbSession.save(sePassword);
		hbSession.flush();

		// -------- Audit ----------------------------------------------------------------------------------------
		String detail = JsonBuilder.create(null).add("username", seUser.getUsername()).toString();
		AuditService.auditTransaction(hbSession, context, AuditEvents.PASSWORD_RESETED, SePassword.class.getSimpleName(), sePassword.getPasswordId().toString(), seUser.getUserId().toString(), detail);
		hbSession.flush();

		return newPassword;
	}

}
