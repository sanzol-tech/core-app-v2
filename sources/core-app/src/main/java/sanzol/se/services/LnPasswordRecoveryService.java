package sanzol.se.services;

import static sanzol.app.config.I18nPreference.getI18nString;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import sanzol.app.config.HibernateUtil;
import sanzol.se.commons.BaseService;
import sanzol.se.context.RequestContext;
import sanzol.se.model.entities.SeEmailTemplate;
import sanzol.se.model.entities.SePassword;
import sanzol.se.model.entities.SePasswordRecovery;
import sanzol.se.model.entities.SeUser;
import sanzol.se.services.cache.SeEmailTemplatesCache;
import sanzol.util.DateTimeUtils;
import sanzol.util.Replacer;
import sanzol.util.i18n.DateFormatter;
import sanzol.util.persistence.Query;
import sanzol.util.security.PasswordUtils;

public class LnPasswordRecoveryService extends BaseService
{
	private static final String MESSAGE_EMAIL_NOT_FOUND = getI18nString("passwordRecovery.message.email.notFound");
	private static final String MESSAGE_USER_LOCKED = getI18nString("passwordRecovery.message.userLocked");
	private static final String MESSAGE_WAIT_TO_REQUEST = getI18nString("passwordRecovery.message.waitToRequest");
	private static final String MESSAGE_VALIDATION_CODE_INVALID = getI18nString("passwordRecovery.message.validationCode.invalid");

	public static final long SECONDS_TO_RETRY = TimeUnit.MINUTES.toSeconds(10);
	public static final long SECONDS_EXPIRATION_PROCESS = TimeUnit.HOURS.toSeconds(2);

	public static final int VALIDATION_CODE_SIZE = 16;

	public LnPasswordRecoveryService(RequestContext context)
	{
		super(context);
	}

	public void addSePasswordRecovery(String email)
	{
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			// -------- Search user by email -------------------------------------------------------------------------
			Query q1 = new Query(SeUser.class, "u");
			q1.where(
					q1.eq("email", email),
					q1.eq("isActive", true)
				);
			SeUser seUser = (SeUser) q1.firstResult(hbSession);

			if (seUser == null)
			{
				context.getMsgLogger().addMessageError(MESSAGE_EMAIL_NOT_FOUND);
				return;
			}
			else if (seUser.isIsLocked())
			{
				context.getMsgLogger().addMessageError(MESSAGE_USER_LOCKED);
				return;
			}

			// -------- Is there a valid code ? ----------------------------------------------------------------------
			LocalDateTime expirationDate = DateTimeUtils.now().minusSeconds(SECONDS_TO_RETRY);

			Query q2 = new Query(SePasswordRecovery.class, "a");
			q2.where(
					q2.eq("seUser", seUser),
					q2.gt("requestDate", expirationDate),
					q2.isNull("processDate")
				);
			SePasswordRecovery _sePasswordRecovery = (SePasswordRecovery) q2.firstResult(hbSession);

			if (_sePasswordRecovery != null)
			{
				String dateDiff = DateTimeUtils.readableDateDiff(DateTimeUtils.now(), _sePasswordRecovery.getRequestDate().plusSeconds(SECONDS_TO_RETRY));
				context.getMsgLogger().addMessageError(MESSAGE_WAIT_TO_REQUEST.replace("{time}", dateDiff));
				return;
			}

			// -------- Add new sePasswordRecovery -------------------------------------------------------------------
			SePasswordRecovery sePasswordRecovery = new SePasswordRecovery();
			sePasswordRecovery.setSeUser(seUser);
			sePasswordRecovery.setRequestDate(DateTimeUtils.now());
			sePasswordRecovery.setExpirationDate(DateTimeUtils.now().plusSeconds(SECONDS_EXPIRATION_PROCESS));
			sePasswordRecovery.setValidationCode(RandomStringUtils.randomAlphanumeric(16).toUpperCase());
			sePasswordRecovery.setIpAddress(context.getHttpServletRequest().getRemoteAddr());
			sePasswordRecovery.setUseragent(context.getHttpServletRequest().getHeader("user-agent"));

			hbSession.save(sePasswordRecovery);
			hbSession.flush();

			// -------- Send email -----------------------------------------------------------------------------------
			if (sePasswordRecovery.getSeUser().getEmail() != null && !sePasswordRecovery.getSeUser().getEmail().isBlank())
			{
				String to = sePasswordRecovery.getSeUser().getFormattedEmail();
				SeEmailTemplate seEmailTemplate = SeEmailTemplatesCache.getSeEmailTemplate(SeEmailTemplatesCache.TEMPLATE_PASSWORD_RECOVERY);
				Replacer replacer = Replacer.create()
					.add("servletURL", context.getServletURL())
					.add("validationCode", sePasswordRecovery.getValidationCode())
					.add("expiration", DateFormatter.getFormatterDateTimeShort().format(sePasswordRecovery.getExpirationDate()));
				SeEmailService.create().withTemplate(seEmailTemplate, replacer).send(to);
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

	public SePasswordRecovery checkValidationCode(String validationCode)
	{
		SePasswordRecovery sePasswordRecovery = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			Query q = new Query(SePasswordRecovery.class, "r");
			q.where(
					q.eq("validationCode", validationCode),
					q.gt("expirationDate", DateTimeUtils.now()),
					q.isNull("processDate")
				);

			sePasswordRecovery = (SePasswordRecovery) q.firstResult(hbSession);

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

		return sePasswordRecovery;
	}

	public SePassword updateSePassword(String validationCode, String newPassword)
	{
		SePassword sePassword = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			sePassword = updateSePassword(hbSession, validationCode, newPassword);
			if (sePassword == null)
			{
				tx.rollback();
				return null;
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

		return sePassword;
	}

	private SePassword updateSePassword(org.hibernate.Session hbSession, String validationCode, String newPassword) throws Exception
	{
		LocalDateTime now = DateTimeUtils.now();

		// -------- Get recovery code ----------------------------------------------------------------------------
		Query query1 = new Query(SePasswordRecovery.class, "r");
		query1.where(
				query1.eq("validationCode", validationCode),
				query1.gt("expirationDate", now),
				query1.isNull("processDate")
			);
		SePasswordRecovery sePasswordRecovery = (SePasswordRecovery) query1.uniqueResult(hbSession);

		if (sePasswordRecovery == null)
		{
			context.getMsgLogger().addMessageError(MESSAGE_VALIDATION_CODE_INVALID);
			return null;
		}

		// -------- Get current password -------------------------------------------------------------------------
		Query query2 = new Query(SePassword.class, "p");
		query2.where(
				query2.eq("seUser", sePasswordRecovery.getSeUser()),
				query2.isNull("dateTo")
			);
		SePassword sePasswordStored = (SePassword) query2.uniqueResult(hbSession);

		if (sePasswordStored == null)
		{
			throw new Exception(SePassword.class.getSimpleName() + " record does not exist for this user");
		}

		// -------- Update current password ----------------------------------------------------------------------
		sePasswordStored.setDateTo(now);
		hbSession.update(sePasswordStored);
		hbSession.flush();

		// -------- Add new password -----------------------------------------------------------------------------
		SePassword sePassword = new SePassword();
		sePassword.setSeUser(sePasswordRecovery.getSeUser());
		sePassword.setDateFrom(now);
		sePassword.setPassword(PasswordUtils.hashPassword(sePasswordRecovery.getSeUser().getUsername(), newPassword));
		hbSession.save(sePassword);
		hbSession.flush();

		// -------- Update SePasswordRecovery --------------------------------------------------------------------
		sePasswordRecovery.setProcessDate(now);
		hbSession.update(sePasswordRecovery);
		hbSession.flush();

		// -------- Send email -----------------------------------------------------------------------------------
		if (sePasswordRecovery.getSeUser().getEmail() != null && !sePasswordRecovery.getSeUser().getEmail().isBlank())
		{
			String to = sePasswordRecovery.getSeUser().getFormattedEmail();
			SeEmailTemplate seEmailTemplate = SeEmailTemplatesCache.getSeEmailTemplate(SeEmailTemplatesCache.TEMPLATE_PASSWORD_CHANGED);
			Replacer replacer = Replacer.create()
				.add("name", sePasswordRecovery.getSeUser().getNameAlt())
				.add("username", sePasswordRecovery.getSeUser().getUsername());
			SeEmailService.create().withTemplate(seEmailTemplate, replacer).send(to);
		}

		return sePassword;
	}

}
