package sanzol.se.services;

import static sanzol.app.config.I18nPreference.getI18nString;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;

import com.itextpdf.text.DocumentException;

import sanzol.app.config.HibernateUtil;
import sanzol.se.audit.AuditService;
import sanzol.se.commons.BaseService;
import sanzol.se.context.RequestContext;
import sanzol.se.model.entities.SeEmailTemplate;
import sanzol.se.model.entities.SePassword;
import sanzol.se.model.entities.SeRegistration;
import sanzol.se.model.entities.SeRegistrationEvent;
import sanzol.se.model.entities.SeRegistrationState;
import sanzol.se.model.entities.SeUser;
import sanzol.se.model.entities.SeWhitelist;
import sanzol.se.services.cache.SeEmailTemplatesCache;
import sanzol.se.services.cache.SeRegistrationsStatesCache;
import sanzol.util.CaseUtils;
import sanzol.util.DateTimeUtils;
import sanzol.util.Replacer;
import sanzol.util.i18n.DateFormatter;
import sanzol.util.json.JsonEncoder;
import sanzol.util.persistence.Query;
import sanzol.util.security.PasswordUtils;

public class SeRegistrationsService extends BaseService
{
	private static final String MESSAGE_CONTAINS_DEPENDENCIES = getI18nString("message.recordContainsDependencies");

	private static final String MESSAGE_REGISTRATION_EXISTS = getI18nString("userRegistration.message.alreadyExists");
	private static final String MESSAGE_REGISTRATION_EXISTS_WAIT = getI18nString("userRegistration.message.alreadyExistsAndWait");
	private static final String MESSAGE_USERNAME_ALREADY_EXISTS = getI18nString("userRegistration.message.usernameExists");
	private static final String MESSAGE_EMAIL_ALREADY_EXISTS = getI18nString("userRegistration.message.emailExists");
	private static final String MESSAGE_NOT_IN_AUTHORIZATION_PENDING = getI18nString("userRegistration.message.notInAuthPending");

	private static final int USER_LEVEL_LOW = 3;

	public static final long SECONDS_TO_RETRY = TimeUnit.MINUTES.toSeconds(10);
	public static final long SECONDS_EXPIRATION_INVITATION = TimeUnit.DAYS.toSeconds(14);
	public static final long SECONDS_EXPIRATION_PROCESS = TimeUnit.HOURS.toSeconds(4);

	public static final int VALIDATION_CODE_SIZE = 16;

	public SeRegistrationsService(RequestContext context)
	{
		super(context);
	}

	@SuppressWarnings("unchecked")
	public List<SeRegistration> getSeRegistrations(Integer registrationStateId, boolean onlyNotExpired)
	{
		List<SeRegistration> list = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			Query q = new Query(SeRegistration.class, "a");
			q.where(
					q.eq("lastEvent.seRegistrationState.registrationStateId", registrationStateId),
					onlyNotExpired ? q.OR(q.isNull("lastEvent.expirationDate"), q.gt("lastEvent.expirationDate", DateTimeUtils.now())) : null
				);
			q.orderBy(q.desc("registrationId"));
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

	public SeRegistration getSeRegistration(int registrationId)
	{
		SeRegistration seRegistration = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();
			seRegistration = (SeRegistration) hbSession.get(SeRegistration.class, registrationId);

			if (seRegistration != null)
			{
				Hibernate.initialize(seRegistration.getLstSeRegistrationsEvents());
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

		return seRegistration;
	}

	public void delSeRegistration(SeRegistration seRegistration)
	{
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			// -----------------------------------------------------------------------------------------------------
			hbSession.createQuery("update SeRegistration set firstEvent = null, lastEvent = null where registrationId = :registrationId")
					.setParameter("registrationId", seRegistration.getRegistrationId())
					.executeUpdate();
			hbSession.flush();

			hbSession.createQuery("update SeRegistrationEvent set prevEvent = null where seRegistration.registrationId = :registrationId")
					.setParameter("registrationId", seRegistration.getRegistrationId())
					.executeUpdate();
			hbSession.flush();

			hbSession.createQuery("delete SeRegistrationEvent where seRegistration.registrationId = :registrationId")
					.setParameter("registrationId", seRegistration.getRegistrationId())
					.executeUpdate();
			hbSession.flush();

			// -----------------------------------------------------------------------------------------------------
			hbSession.delete(seRegistration);
			hbSession.flush();

			// ----- Audit -----------------------------------------------------------------------------------------
			String jsonOldEntity = JsonEncoder.encode(seRegistration);
			AuditService.auditDelete(hbSession, context, SeRegistration.class.getSimpleName(), seRegistration.getRegistrationId().toString(), null, jsonOldEntity);
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

	// -------- Registration events -------------------------------------------------------------------------------------------

	public void addInvitation(SeWhitelist seWhitelist)
	{
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			addInvitation(hbSession, seWhitelist);

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

	public void addInvitation(Session hbSession, SeWhitelist seWhitelist) throws IOException, DocumentException
	{
		LocalDateTime now = DateTimeUtils.now();

		// -------- Is there another request for registration ? --------------------------------------------------
		if (existsRegistration(hbSession, null, seWhitelist.getEmail()))
		{
			return;
		}

		// -------- User already exists ? ------------------------------------------------------------------------
		if (existsUser(hbSession, null, seWhitelist.getEmail()))
		{
			return;
		}

		// -------- Create SeRegistration --------------------------------------------------------------------
		SeRegistrationState seRegistrationState = SeRegistrationsStatesCache.getSeRegistrationState(SeRegistrationsStatesCache.INVITED);
		LocalDateTime expirationDate = now.plusSeconds(SECONDS_EXPIRATION_INVITATION);

		SeRegistration seRegistration = toSeRegistration(seWhitelist);
		seRegistration.setValidationCode(RandomStringUtils.randomAlphanumeric(VALIDATION_CODE_SIZE).toUpperCase());
		hbSession.save(seRegistration);
		hbSession.flush();

		SeRegistrationEvent seRegistrationEvent = new SeRegistrationEvent();
		seRegistrationEvent.setPrevEvent(seRegistration.getLastEvent());
		seRegistrationEvent.setSeRegistration(seRegistration);
		seRegistrationEvent.setSeRegistrationState(seRegistrationState);
		seRegistrationEvent.setEventDate(now);
		seRegistrationEvent.setExpirationDate(expirationDate);
		if (context.getAuthSession() == null)
		{
			seRegistrationEvent.setIpAddress(context.getHttpServletRequest().getRemoteAddr());
			seRegistrationEvent.setUseragent(context.getHttpServletRequest().getHeader("user-agent"));
		}
		hbSession.save(seRegistrationEvent);
		hbSession.flush();

		seRegistration.setFirstEvent(seRegistrationEvent);
		seRegistration.setLastEvent(seRegistrationEvent);
		hbSession.update(seRegistrationEvent);
		hbSession.flush();

		// ----- Audit -----------------------------------------------------------------------------------------
		String jsonEntity = JsonEncoder.encode(seRegistration);
		AuditService.auditTransaction(hbSession, context, "SIGN_UP_INVITATION", SeRegistration.class.getSimpleName(), seRegistration.getRegistrationId().toString(), null, jsonEntity);
		hbSession.flush();

		// -------- Send email -----------------------------------------------------------------------------------
		if (seRegistration.getEmail() != null && !seRegistration.getEmail().isBlank())
		{
			String to = seRegistration.getFormattedEmail();
			SeEmailTemplate seEmailTemplate = SeEmailTemplatesCache.getSeEmailTemplate(SeEmailTemplatesCache.TEMPLATE_SIGN_UP_INVITATION);
			Replacer replacer = Replacer.create()
						.add("name", seRegistration.getFullNameAlt())
						.add("servletURL", context.getServletURL())
						.add("validationCode", seRegistration.getValidationCode())
						.add("expiration", DateFormatter.getFormatterDateTimeShort().format(expirationDate));
			SeEmailService.create().withTemplate(seEmailTemplate, replacer).send(to);
		}

	}

	private SeRegistration toSeRegistration(SeWhitelist seWhitelist)
	{
		SeRegistration seRegistration = new SeRegistration();
		seRegistration.setLastname(seWhitelist.getLastname());
		seRegistration.setFirstname(seWhitelist.getFirstname());
		seRegistration.setBirthDate(seWhitelist.getBirthDate());
		seRegistration.setGender(seWhitelist.getGender());
		seRegistration.setDocumentId(seWhitelist.getDocumentId());
		seRegistration.setEmail(seWhitelist.getEmail());
		seRegistration.setCellphone(seWhitelist.getCellphone());
		return seRegistration;
	}

	public void addUserRequest(SeRegistration seRegistration)
	{
		LocalDateTime now = DateTimeUtils.now();

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			// -------- Is there another request for registration ? --------------------------------------------------
			if (existsRegistration(hbSession, null, seRegistration.getEmail()))
			{
				return;
			}

			// -------- User already exists ? ------------------------------------------------------------------------
			if (existsUser(hbSession, null, seRegistration.getEmail()))
			{
				return;
			}

			// -------- Create SeRegistration --------------------------------------------------------------------
			SeRegistrationState seRegistrationState = SeRegistrationsStatesCache.getSeRegistrationState(SeRegistrationsStatesCache.USER_REQUESTED);
			LocalDateTime expirationDate = now.plusSeconds(SECONDS_EXPIRATION_PROCESS);

			seRegistration.setValidationCode(RandomStringUtils.randomAlphanumeric(VALIDATION_CODE_SIZE).toUpperCase());
			hbSession.save(seRegistration);
			hbSession.flush();

			SeRegistrationEvent seRegistrationEvent = new SeRegistrationEvent();
			seRegistrationEvent.setPrevEvent(seRegistration.getLastEvent());
			seRegistrationEvent.setSeRegistration(seRegistration);
			seRegistrationEvent.setSeRegistrationState(seRegistrationState);
			seRegistrationEvent.setEventDate(now);
			seRegistrationEvent.setExpirationDate(expirationDate);
			if (context.getAuthSession() == null)
			{
				seRegistrationEvent.setIpAddress(context.getHttpServletRequest().getRemoteAddr());
				seRegistrationEvent.setUseragent(context.getHttpServletRequest().getHeader("user-agent"));
			}
			hbSession.save(seRegistrationEvent);
			hbSession.flush();

			seRegistration.setFirstEvent(seRegistrationEvent);
			seRegistration.setLastEvent(seRegistrationEvent);
			hbSession.update(seRegistrationEvent);
			hbSession.flush();

			// -------- Send email -----------------------------------------------------------------------------------
			if (seRegistration.getEmail() != null && !seRegistration.getEmail().isBlank())
			{
				String to = seRegistration.getFormattedEmail();
				SeEmailTemplate seEmailTemplate = SeEmailTemplatesCache.getSeEmailTemplate(SeEmailTemplatesCache.TEMPLATE_SIGN_UP_EMAIL_VALIDATION);
				Replacer replacer = Replacer.create()
							.add("name", seRegistration.getFullNameAlt())
							.add("servletURL", context.getServletURL())
							.add("validationCode", seRegistration.getValidationCode())
							.add("expiration", DateFormatter.getFormatterDateTimeShort().format(expirationDate));
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

	private boolean existsRegistration(Session hbSession, String username, String email)
	{
		Query q = new Query(SeRegistration.class, "a");
		q.where(
				q.OR(q.eq("username", username), q.eq("email", email)),
				q.ne("lastEvent.seRegistrationState.registrationStateId", SeRegistrationsStatesCache.USER_CREATED),
				q.ne("lastEvent.seRegistrationState.registrationStateId", SeRegistrationsStatesCache.AUTHORIZATION_REVOKED),
				q.OR(q.isNull("lastEvent.expirationDate"), q.gt("lastEvent.expirationDate", DateTimeUtils.now()))
			);
		SeRegistration seRegistration = (SeRegistration) q.firstResult(hbSession);

		if (seRegistration != null)
		{
			String dateDiff = DateTimeUtils.readableDateDiff(DateTimeUtils.now(), seRegistration.getLastEvent().getExpirationDate());
			if (dateDiff == null)
				context.getMsgLogger().addMessageError(
						MESSAGE_REGISTRATION_EXISTS
							.replace("{state}", seRegistration.getLastEvent().getSeRegistrationState().getName())
					);
			else
				context.getMsgLogger().addMessageError(
						MESSAGE_REGISTRATION_EXISTS_WAIT
							.replace("{state}", seRegistration.getLastEvent().getSeRegistrationState().getName())
							.replace("{time}", dateDiff)
					);
			return true;
		}
		else
		{
			return false;
		}
	}

	private boolean existsUser(Session hbSession, String username, String email)
	{
		Query q = new Query(SeUser.class, "a");
		q.where(
				q.OR(q.eq("username", username), q.eq("email", email))
			);
		SeUser _seUser = (SeUser) q.firstResult(hbSession);

		if (_seUser != null)
		{
			if (_seUser.getUsername().equalsIgnoreCase(username))
			{
				context.getMsgLogger().addMessageError(MESSAGE_USERNAME_ALREADY_EXISTS);
			}
			else if (_seUser.getEmail().equalsIgnoreCase(email))
			{
				context.getMsgLogger().addMessageError(MESSAGE_EMAIL_ALREADY_EXISTS);
			}
			return true;
		}
		else
		{
			return false;
		}
	}

	public SeRegistration checkValidationCode(String validationCode)
	{
		SeRegistration seRegistration = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			Query q = new Query(SeRegistration.class, "a");
			q.where(
					q.eq("validationCode", validationCode),
					q.OR(q.eq("lastEvent.seRegistrationState.registrationStateId", SeRegistrationsStatesCache.INVITED),
						 q.eq("lastEvent.seRegistrationState.registrationStateId", SeRegistrationsStatesCache.USER_REQUESTED),
						 q.eq("lastEvent.seRegistrationState.registrationStateId", SeRegistrationsStatesCache.AUTHORIZATION_SUCCESSFUL)),
					q.OR(q.isNull("lastEvent.expirationDate"), q.gt("lastEvent.expirationDate", DateTimeUtils.now()))
				);

			seRegistration = (SeRegistration) q.firstResult(hbSession);

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

		return seRegistration;
	}

	public Boolean checkWhitelist(SeRegistration seRegistration)
	{
		Boolean canContinue = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			// -------- Update registration --------------------------------------------------------------------------
			hbSession.update(seRegistration);
			hbSession.flush();

			// -------- Search in whitelist --------------------------------------------------------------------------
			Query q = new Query(SeWhitelist.class, "a");
			q.where(q.eq("email", CaseUtils.lowerTrim(seRegistration.getEmail())));
			SeWhitelist seWhiteList = (SeWhitelist) q.firstResult(hbSession);
			boolean inWhitelist =
						(seWhiteList != null) &&
						CaseUtils.equals(seWhiteList.getLastname(), seRegistration.getLastname()) &&
						CaseUtils.equals(seWhiteList.getFirstname(), seRegistration.getFirstname());

			if (inWhitelist)
			{
				canContinue = true;
			}
			else
			{
				// -------- Add registration event -----------------------------------------------------------------------
				addSeRegistrationEvent(hbSession, seRegistration, SeRegistrationsStatesCache.AUTHORIZATION_PENDING, null);

				// -------- Send email -----------------------------------------------------------------------------------
				if (seRegistration.getEmail() != null && !seRegistration.getEmail().isBlank())
				{
					String to = seRegistration.getFormattedEmail();
					SeEmailTemplate seEmailTemplate = SeEmailTemplatesCache.getSeEmailTemplate(SeEmailTemplatesCache.TEMPLATE_SIGN_UP_AURORIZATION_PENDING);
					Replacer replacer = Replacer.create()
								.add("name", seRegistration.getFullNameAlt())
								.add("username", seRegistration.getUsername());
					SeEmailService.create().withTemplate(seEmailTemplate, replacer).sendAsync(to);
				}

				canContinue = false;
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

		return canContinue;
	}

	public void authorizationSuccessful(SeRegistration seRegistration)
	{
		if (!seRegistration.getRegistrationStateId().equals(SeRegistrationsStatesCache.AUTHORIZATION_PENDING))
		{
			context.getMsgLogger().addMessageError(MESSAGE_NOT_IN_AUTHORIZATION_PENDING);
			return;
		}

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			LocalDateTime expirationDate = DateTimeUtils.now().plusSeconds(SECONDS_EXPIRATION_PROCESS);

			addSeRegistrationEvent(hbSession, seRegistration, SeRegistrationsStatesCache.AUTHORIZATION_SUCCESSFUL, expirationDate);

			// -------- Send email -----------------------------------------------------------------------------------
			if (seRegistration.getEmail() != null && !seRegistration.getEmail().isBlank())
			{
				String to = seRegistration.getFormattedEmail();
				SeEmailTemplate seEmailTemplate = SeEmailTemplatesCache.getSeEmailTemplate(SeEmailTemplatesCache.TEMPLATE_SIGN_UP_AURORIZATION_SUCCESSFUL);
				Replacer replacer = Replacer.create()
							.add("name", seRegistration.getFullNameAlt())
							.add("servletURL", context.getServletURL())
							.add("validationCode", seRegistration.getValidationCode())
							.add("expiration", DateFormatter.getFormatterDateTimeShort().format(expirationDate));
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

	public void authorizationRevoked(SeRegistration seRegistration)
	{
		if (!seRegistration.getRegistrationStateId().equals(SeRegistrationsStatesCache.AUTHORIZATION_PENDING))
		{
			context.getMsgLogger().addMessageError(MESSAGE_NOT_IN_AUTHORIZATION_PENDING);
			return;
		}

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			addSeRegistrationEvent(hbSession, seRegistration, SeRegistrationsStatesCache.AUTHORIZATION_REVOKED, null);

			// -------- Send email -----------------------------------------------------------------------------------
			if (seRegistration.getEmail() != null && !seRegistration.getEmail().isBlank())
			{
				String to = seRegistration.getFormattedEmail();
				SeEmailTemplate seEmailTemplate = SeEmailTemplatesCache.getSeEmailTemplate(SeEmailTemplatesCache.TEMPLATE_SIGN_UP_AURORIZATION_REVOKED);
				Replacer replacer = Replacer.create()
							.add("name", seRegistration.getFullNameAlt())
							.add("username", seRegistration.getUsername());
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

	public void createUser(SeRegistration seRegistration, String password)
	{
		LocalDateTime now = DateTimeUtils.now();

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			// -------- Create user ----------------------------------------------------------------------------------
			SeUser seUser = new SeUser();
			seUser.setUsername(seRegistration.getUsername());
			seUser.setLastname(seRegistration.getLastname());
			seUser.setFirstname(seRegistration.getFirstname());
			seUser.setEmail(seRegistration.getEmail());
			seUser.setMaxSessions(-1);
			seUser.setMaxInactiveInterval(-1);
			seUser.setIncorrectAttempts(0);
			seUser.setUserLevel(USER_LEVEL_LOW);
			seUser.setIsActive(true);

			hbSession.save(seUser);
			hbSession.flush();

			// -------- Create password ------------------------------------------------------------------------------
			SePassword sePassword = new SePassword();
			sePassword.setSeUser(seUser);
			sePassword.setDateFrom(now);
			sePassword.setPassword(PasswordUtils.hashPassword(seUser.getUsername(), password));
			sePassword.setIsTemporal(false);

			hbSession.save(sePassword);
			hbSession.flush();

			// -------- Update registration --------------------------------------------------------------------------
			seRegistration.setSeUser(seUser);
			hbSession.update(seRegistration);
			hbSession.flush();

			// -------- Add registration event -----------------------------------------------------------------------
			addSeRegistrationEvent(hbSession, seRegistration, SeRegistrationsStatesCache.USER_CREATED, null);

			// -------- Send email -----------------------------------------------------------------------------------
			if (seRegistration.getEmail() != null && !seRegistration.getEmail().isBlank())
			{
				String to = seRegistration.getFormattedEmail();
				SeEmailTemplate seEmailTemplate = SeEmailTemplatesCache.getSeEmailTemplate(SeEmailTemplatesCache.TEMPLATE_SIGN_UP_USER_CREATED);
				Replacer replacer = Replacer.create()
							.add("name", seRegistration.getFullNameAlt())
							.add("username", seRegistration.getUsername());
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

	private void addSeRegistrationEvent(Session hbSession, SeRegistration seRegistration, int registrationStateId, LocalDateTime expirationDate)
	{
		SeRegistrationState seRegistrationState = SeRegistrationsStatesCache.getSeRegistrationState(registrationStateId);

		SeRegistrationEvent seRegistrationEvent = new SeRegistrationEvent();
		seRegistrationEvent.setPrevEvent(seRegistration.getLastEvent());
		seRegistrationEvent.setSeRegistration(seRegistration);
		seRegistrationEvent.setSeRegistrationState(seRegistrationState);
		seRegistrationEvent.setEventDate(DateTimeUtils.now());
		seRegistrationEvent.setExpirationDate(expirationDate);
		if (context.getAuthSession() == null)
		{
			seRegistrationEvent.setIpAddress(context.getHttpServletRequest().getRemoteAddr());
			seRegistrationEvent.setUseragent(context.getHttpServletRequest().getHeader("user-agent"));
		}
		hbSession.save(seRegistrationEvent);
		hbSession.flush();

		seRegistration.setLastEvent(seRegistrationEvent);
		hbSession.update(seRegistration);
		hbSession.flush();
	}

}
