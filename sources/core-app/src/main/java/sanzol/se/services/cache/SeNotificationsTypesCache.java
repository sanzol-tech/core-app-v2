package sanzol.se.services.cache;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sanzol.app.config.HibernateUtil;
import sanzol.se.model.entities.SeNotificationType;
import sanzol.util.persistence.Query;

public class SeNotificationsTypesCache
{
	private static final Logger LOG = LoggerFactory.getLogger(SeNotificationsTypesCache.class);

	private static Map<Integer, SeNotificationType> map = getValues();

	public static void load()
	{
		LOG.info(SeNotificationsTypesCache.class.getName() + " load");
		map = getValues();
	}

	private static Map<Integer, SeNotificationType> getValues()
	{
		Map<Integer, SeNotificationType> map = new LinkedHashMap<Integer, SeNotificationType>();

		org.hibernate.Session hbSession = HibernateUtil.getSessionFactory().getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			Query q = new Query(SeNotificationType.class, "a");
			q.orderBy(q.asc("notificationTypeId"));

			@SuppressWarnings("unchecked")
			List<SeNotificationType> list = q.list(hbSession);
			if (list != null)
			{
				for (SeNotificationType seNotificationType : list)
				{
					map.put(seNotificationType.getNotificationTypeId(), seNotificationType);
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

	public static List<SeNotificationType> getSeNotificationsTypes()
	{
		List<SeNotificationType> list = new ArrayList<SeNotificationType>(map.values());
		return list;
	}

	public static SeNotificationType getSeNotificationType(Integer notificationTypeId)
	{
		if (notificationTypeId == null)
			return null;

		SeNotificationType seNotificationType = map.get(notificationTypeId);
		return seNotificationType;
	}

}
