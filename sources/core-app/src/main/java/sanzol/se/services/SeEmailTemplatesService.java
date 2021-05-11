package sanzol.se.services;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import sanzol.app.config.HibernateUtil;
import sanzol.se.audit.AuditService;
import sanzol.se.commons.BaseService;
import sanzol.se.context.RequestContext;
import sanzol.se.model.entities.SeEmailTemplate;
import sanzol.util.json.JsonEncoder;
import sanzol.util.persistence.Query;

public class SeEmailTemplatesService extends BaseService
{

	public SeEmailTemplatesService(RequestContext context)
	{
		super(context);
	}

	@SuppressWarnings("unchecked")
	public List<SeEmailTemplate> getSeEmailTemplates()
	{
		List<SeEmailTemplate> list = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			Query q = new Query(SeEmailTemplate.class, "a");
			q.orderBy(q.asc("emailTemplateId"));
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

	public SeEmailTemplate getSeEmailTemplate(int emailTemplateId)
	{
		SeEmailTemplate seEmailTemplate = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();
			seEmailTemplate = (SeEmailTemplate) hbSession.get(SeEmailTemplate.class, emailTemplateId);
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

		return seEmailTemplate;
	}

	public void setSeEmailTemplate(SeEmailTemplate seEmailTemplate)
	{
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			SeEmailTemplate oldEntity = (SeEmailTemplate) hbSession.get(SeEmailTemplate.class, seEmailTemplate.getEmailTemplateId());
			hbSession.evict(oldEntity);

			hbSession.update(seEmailTemplate);
			hbSession.flush();

			// ----- Audit -----------------------------------------------------------------------------------------
			String jsonOldEntity = JsonEncoder.encode(oldEntity);
			String jsonNewEntity = JsonEncoder.encode(seEmailTemplate);
			AuditService.auditUpdate(hbSession, context, SeEmailTemplate.class.getSimpleName(), seEmailTemplate.getEmailTemplateId().toString(), null, jsonOldEntity, jsonNewEntity);
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
