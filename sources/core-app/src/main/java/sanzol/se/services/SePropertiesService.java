package sanzol.se.services;

import java.util.List;

import org.apache.commons.lang3.SerializationUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import sanzol.app.config.HibernateUtil;
import sanzol.se.audit.AuditService;
import sanzol.se.commons.BaseService;
import sanzol.se.context.RequestContext;
import sanzol.se.model.entities.SeProperty;
import sanzol.util.json.JsonEncoder;
import sanzol.util.persistence.Query;
import sanzol.util.properties.Property;

public class SePropertiesService extends BaseService
{
	public SePropertiesService(RequestContext context)
	{
		super(context);
	}

	@SuppressWarnings("unchecked")
	public List<SeProperty> getSeProperties()
	{
		List<SeProperty> list = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			Query q = new Query(SeProperty.class, "p");
			q.orderBy(q.asc("p.propertyId"));
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

	public SeProperty getSeProperty(int propertyId)
	{
		SeProperty seProperty = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();
			seProperty = (SeProperty) hbSession.get(SeProperty.class, propertyId);
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

		return seProperty;
	}

	public void setSeProperty(Property property)
	{
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			Query q = new Query(SeProperty.class, "p");
			q.where(q.eq("p.name", property.getName()));
			SeProperty oldEntity = (SeProperty) q.uniqueResult(hbSession);

			SeProperty seProperty;
			if (oldEntity != null)
			{
				seProperty = SerializationUtils.clone(oldEntity);
				hbSession.evict(oldEntity);

				seProperty.setEncryptValue(property.getValue());

				hbSession.update(seProperty);
			}
			else
			{
				seProperty = new SeProperty();

				seProperty.setName(property.getName());
				seProperty.setDetail(property.getDetail());
				seProperty.setEncryptValue(property.getValue());

				hbSession.save(seProperty);
			}
			hbSession.flush();

			// ----- Audit -----------------------------------------------------------------------------------------
			String jsonOldEntity = JsonEncoder.encode(oldEntity);
			String jsonNewEntity = JsonEncoder.encode(seProperty);
			AuditService.auditUpdate(hbSession, context, SeProperty.class.getSimpleName(), seProperty.getPropertyId().toString(), null, jsonOldEntity, jsonNewEntity);
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
