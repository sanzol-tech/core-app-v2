package sanzol.se.services;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import sanzol.app.config.HibernateUtil;
import sanzol.se.commons.BaseService;
import sanzol.se.context.RequestContext;
import sanzol.se.model.entities.SeSoftVersion;
import sanzol.util.DateTimeUtils;
import sanzol.util.persistence.Query;

public class SeSoftVersionsService extends BaseService
{
	public SeSoftVersionsService(RequestContext context)
	{
		super(context);
	}

	@SuppressWarnings("unchecked")
	public List<SeSoftVersion> getSeSoftVersions()
	{
		List<SeSoftVersion> list = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			Query q = new Query(SeSoftVersion.class, "s");
			q.orderBy(q.desc("s.softVersionId"));
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

	public SeSoftVersion getSeSoftVersion(int softVersionId)
	{
		SeSoftVersion seSoftVersion = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();
			seSoftVersion = (SeSoftVersion) hbSession.get(SeSoftVersion.class, softVersionId);
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

		return seSoftVersion;
	}

	public SeSoftVersion VerifySoftVersion(int major, int minor, int patch, String fase, String detail)
	{
		SeSoftVersion seSoftVersion = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			Query q = new Query(SeSoftVersion.class, "s");
			q.where(
					q.eq("s.major", major),
					q.eq("s.minor", minor),
					q.eq("s.patch", patch),
					q.eq("s.fase", fase)
				);

			seSoftVersion = (SeSoftVersion) q.uniqueResult(hbSession);

			if (seSoftVersion == null)
			{
				seSoftVersion = new SeSoftVersion();

				seSoftVersion.setMajor(major);
				seSoftVersion.setMinor(minor);
				seSoftVersion.setPatch(patch);
				seSoftVersion.setFase(fase);
				seSoftVersion.setFirstStartup(DateTimeUtils.now());
				seSoftVersion.setDetail(detail);

				hbSession.save(seSoftVersion);
				hbSession.flush();
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

		return seSoftVersion;
	}

}
