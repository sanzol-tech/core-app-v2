package sanzol.se.services.cache;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sanzol.app.config.HibernateUtil;
import sanzol.se.model.entities.SeEmailTemplate;
import sanzol.util.persistence.Query;

public class SeEmailTemplatesCache
{
	private static final Logger LOG = LoggerFactory.getLogger(SeEmailTemplatesCache.class);

	public static final int TEMPLATE_USER_WELCOME = 1;

	public static final int TEMPLATE_PASSWORD_RESET = 2;
	public static final int TEMPLATE_PASSWORD_RECOVERY = 3;
	public static final int TEMPLATE_PASSWORD_CHANGED = 4;

	public static final int TEMPLATE_SIGN_UP_INVITATION = 5;
	public static final int TEMPLATE_SIGN_UP_EMAIL_VALIDATION = 6;
	public static final int TEMPLATE_SIGN_UP_AURORIZATION_PENDING = 7;
	public static final int TEMPLATE_SIGN_UP_AURORIZATION_SUCCESSFUL = 8;
	public static final int TEMPLATE_SIGN_UP_AURORIZATION_REVOKED = 9;
	public static final int TEMPLATE_SIGN_UP_USER_CREATED = 10;

	private static Map<Integer, SeEmailTemplate> map = getValues();

	public static void load()
	{
		LOG.info(SeEmailTemplatesCache.class.getName() + " load");
		map = getValues();
	}

	private static Map<Integer, SeEmailTemplate> getValues()
	{
		Map<Integer, SeEmailTemplate> map = new LinkedHashMap<Integer, SeEmailTemplate>();

		org.hibernate.Session hbSession = HibernateUtil.getSessionFactory().getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			Query q = new Query(SeEmailTemplate.class, "a");
			q.orderBy(q.asc("emailTemplateId"));

			@SuppressWarnings("unchecked")
			List<SeEmailTemplate> list = q.list(hbSession);
			if (list != null)
			{
				for (SeEmailTemplate seEmailTemplate : list)
				{
					map.put(seEmailTemplate.getEmailTemplateId(), seEmailTemplate);
				}
			}

			tx.commit();
		}
		catch (Exception e)
		{
			LOG.error("Error getting values", e);
			if (tx != null && tx.getStatus().canRollback())
			{
				tx.rollback();
			}
		}
		finally
		{
			hbSession.close();
		}

		return map;
	}

	public static List<SeEmailTemplate> getSeEmailTemplates()
	{
		List<SeEmailTemplate> list = new ArrayList<SeEmailTemplate>(map.values());
		return list;
	}

	public static SeEmailTemplate getSeEmailTemplate(Integer emailTemplateId)
	{
		if (emailTemplateId == null)
			return null;

		SeEmailTemplate seEmailTemplate = map.get(emailTemplateId);
		return seEmailTemplate;
	}

}
