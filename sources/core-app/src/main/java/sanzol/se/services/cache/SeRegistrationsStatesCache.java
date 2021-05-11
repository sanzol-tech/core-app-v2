package sanzol.se.services.cache;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sanzol.app.config.HibernateUtil;
import sanzol.se.model.entities.SeRegistrationState;
import sanzol.util.persistence.Query;

public class SeRegistrationsStatesCache
{
	private static final Logger LOG = LoggerFactory.getLogger(SeRegistrationsStatesCache.class.getName());

	public static final int INVITED = 1;
	public static final int USER_REQUESTED = 2;
	public static final int AUTHORIZATION_PENDING = 3;
	public static final int AUTHORIZATION_SUCCESSFUL = 4;
	public static final int AUTHORIZATION_REVOKED = 5;
	public static final int USER_CREATED = 6;

	private static Map<Integer, SeRegistrationState> map = getValues();

	public static void load()
	{
		LOG.info(SeRegistrationsStatesCache.class.getName() + " load");
		map = getValues();
	}

	private static Map<Integer, SeRegistrationState> getValues()
	{
		Map<Integer, SeRegistrationState> map = new LinkedHashMap<Integer, SeRegistrationState>();

		org.hibernate.Session hbSession = HibernateUtil.getSessionFactory().getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			Query q = new Query(SeRegistrationState.class, "a");
			q.orderBy(q.asc("registrationStateId"));

			@SuppressWarnings("unchecked")
			List<SeRegistrationState> list = q.list(hbSession);
			if (list != null)
			{
				for (SeRegistrationState seRegistrationState : list)
				{
					map.put(seRegistrationState.getRegistrationStateId(), seRegistrationState);
				}
			}

			tx.commit();
		}
		catch (Exception e)
		{
			LOG.error("Error getting values", e);
			if (tx != null)
			{
				tx.rollback();
			}
		}

		return map;
	}

	public static List<SeRegistrationState> getSeRegistrationsStates()
	{
		List<SeRegistrationState> list = new ArrayList<SeRegistrationState>(map.values());
		return list;
	}

	public static SeRegistrationState getSeRegistrationState(Integer registrationStateId)
	{
		if (registrationStateId == null)
			return null;

		SeRegistrationState seRegistrationState = map.get(registrationStateId);
		return seRegistrationState;
	}

}
