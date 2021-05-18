package sanzol.se.services;

import static sanzol.app.config.I18nPreference.getI18nString;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.DecoderException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import sanzol.app.config.AppProperties;
import sanzol.app.config.HibernateUtil;
import sanzol.app.config.Permissions;
import sanzol.se.commons.BaseService;
import sanzol.se.context.RequestContext;
import sanzol.se.model.entities.SePassword;
import sanzol.se.model.entities.SePermission;
import sanzol.se.model.entities.SeUser;
import sanzol.se.sessions.AuthSession;
import sanzol.util.DateTimeUtils;
import sanzol.util.persistence.Query;
import sanzol.util.security.PasswordUtils;

public class LnLoginService extends BaseService
{
	private static final String MESSAGE_LOGIN_INCORRECT = getI18nString("login.message.incorrect");
	private static final String MESSAGE_LOGIN_USER_LOCKED = getI18nString("login.message.userLocked");

	private static final Integer PASSWORD_EXPIRATION_MONTHS = AppProperties.PROP_PASSWORD_EXPIRATION.getIntegerValue();
	private static final Integer MAX_INCORRECT_ATTEMPTS = AppProperties.PROP_LOGIN_MAX_INCORRECT_ATTEMPS.getIntegerValue();

	public LnLoginService(RequestContext context)
	{
		super(context);
	}

	public AuthSession authenticate(String username, String password)
	{
		AuthSession authSession = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();
			authSession = authenticate(hbSession, username, password);
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

		return authSession;
	}

	private AuthSession authenticate(org.hibernate.Session hbSession, String username, String password) throws NoSuchAlgorithmException, InvalidKeySpecException, DecoderException
	{
		// User
		Query queryUser = new Query(SeUser.class, "u");
		queryUser.where(queryUser.eq("u.username", username.toLowerCase()), queryUser.eq("u.isActive", true));
		SeUser seUser = (SeUser) queryUser.uniqueResult(hbSession);
		if (seUser == null)
		{
			context.getMsgLogger().addMessageError(MESSAGE_LOGIN_INCORRECT);
			return null;
		}

		if (seUser.isIsLocked())
		{
			context.getMsgLogger().addMessageError(MESSAGE_LOGIN_USER_LOCKED);
			return null;
		}

		// Password
		Query queryPass = new Query(SePassword.class, "p");
		queryPass.where(queryPass.eq("p.seUser", seUser), queryPass.isNull("p.dateTo"));
		SePassword sePassword = (SePassword) queryPass.uniqueResult(hbSession);

		if (sePassword == null || !PasswordUtils.validatePassword(seUser.getUsername(), password, sePassword.getPassword()))
		{
			seUser.setIncorrectAttempts(seUser.getIncorrectAttempts() + 1);
			if (MAX_INCORRECT_ATTEMPTS > 0 && seUser.getIncorrectAttempts() >= MAX_INCORRECT_ATTEMPTS)
			{
				seUser.setIsLocked(true);
			}
			hbSession.update(seUser);

			context.getMsgLogger().addMessageError("Usuario o clave incorrecta");
			return null;
		}

		boolean passwordExpired = passwordExpired(sePassword);

		// Permissions
		Map<Integer, Integer> mapPermissions = getPermissions(hbSession, seUser);

		// Sectors
		Set<Integer> lstSectors = getSectors(hbSession, seUser);

		// Authentication Ticket
		AuthSession authSession = new AuthSession();
		authSession.setSeUser(seUser);
		authSession.setMapPermissions(mapPermissions);
		authSession.setSectors(lstSectors);
		authSession.setPasswordExpired(passwordExpired);

		// Reset IncorrectAttempts and Update LastLogin
		seUser.setIncorrectAttempts(0);
		seUser.setLastLogin(DateTimeUtils.now());
		hbSession.update(seUser);

		return authSession;
	}

	private boolean passwordExpired(SePassword sePassword)
	{
		boolean isExpired = false;

		if (sePassword.isIsTemporal())
		{
			return true;
		}

		if (PASSWORD_EXPIRATION_MONTHS != null && PASSWORD_EXPIRATION_MONTHS > 0)
		{
			LocalDateTime expirationDate = sePassword.getDateFrom().plusMonths(PASSWORD_EXPIRATION_MONTHS);
			isExpired = DateTimeUtils.now().isAfter(expirationDate);
		}

		return isExpired;
	}

	private Map<Integer, Integer> getPermissions(org.hibernate.Session hbSession, SeUser user)
	{
		Map<Integer, Integer> mapPermissions = new HashMap<Integer, Integer>();

		String hql
			= "select pe.permissionId, max(rp.permissionLevel)"
			+ " from "
			+ "   SeRolePermission as rp,"
			+ "   SeRoleUser ru"
			+ " join rp.seRole ro"
			+ " join rp.sePermission pe"
			+ " join ru.seUser ur"
			+ " where rp.seRole = ru.seRole"
			+ "   and ur.userId = :userId"
			+ "   and ro.isActive = true"
			+ "   and pe.isActive = true"
			+ "   and pe.userLevel >= :userLevel"
			+ " group by pe.permissionId";

		@SuppressWarnings("unchecked")
		org.hibernate.query.Query<Object[]> sqlQuery = hbSession.createQuery(hql);

		sqlQuery
			.setParameter("userId", user.getUserId())
			.setParameter("userLevel", user.getUserLevel());

		List<Object[]> list = sqlQuery.list();

		for (Object[] pair : list)
		{
			int permissionId = (Integer) pair[0];
			int permissionLevel = (Integer) pair[1];
			if (permissionLevel != Permissions.LEVEL_DENIED)
			{
				mapPermissions.put(permissionId, permissionLevel);
			}
		}

		// -----------------------------------------------------------------------------------
		// -----------------------------------------------------------------------------------

		if (mapPermissions.containsKey(Permissions.PERMISSION_SE_ADMIN))
		{
			Query q = new Query(SePermission.class, "p");
			q.where(
					q.ne("p.permissionId", Permissions.PERMISSION_SE_ADMIN),
					q.ge("p.userLevel", user.getUserLevel())
				);
			q.orderBy(q.desc("p.permissionId"));

			@SuppressWarnings("unchecked")
			List<SePermission> lstPermissions = q.list(hbSession);

			for (SePermission permission : lstPermissions)
			{
				mapPermissions.put(permission.getPermissionId(), Permissions.LEVEL_ALLOW);
			}
		}

		return mapPermissions;
	}

	private Set<Integer> getSectors(org.hibernate.Session hbSession, SeUser seUser)
	{
		Set<Integer> lstSectors = new HashSet<Integer>();

		String hql = "select seSector.sectorId from SeSectorUser where seUser.userId = :userId";

		org.hibernate.query.Query<Integer> query = hbSession.createQuery(hql, Integer.class);
		query.setParameter("userId", seUser.getUserId());
		List<Integer> list = query.list();

		if (list != null && !list.isEmpty())
		{
			for (Integer item : list)
			{
				lstSectors.addAll(getSectors(hbSession, item));
			}
		}

		return lstSectors;
	}

	private Set<Integer> getSectors(org.hibernate.Session hbSession, Integer sectorId)
	{
		Set<Integer> lstSectors = new HashSet<Integer>();

		String hql;
		org.hibernate.query.Query<Integer> query;
		if (sectorId == null)
		{
			hql = "select sectorId from SeSector where seSectorPrev.sectorId is null and isActive = true";
			query = hbSession.createQuery(hql, Integer.class);
		}
		else
		{
			lstSectors.add(sectorId);
			hql = "select sectorId from SeSector where seSectorPrev.sectorId = :sectorPrevId and isActive = true";
			query = hbSession.createQuery(hql, Integer.class);
			query.setParameter("sectorPrevId", sectorId);
		}
		List<Integer> list = query.list();

		if (list != null && !list.isEmpty())
		{
			for (Integer item : list)
			{
				lstSectors.addAll(getSectors(hbSession, item));
			}
		}

		return lstSectors;
	}

}
