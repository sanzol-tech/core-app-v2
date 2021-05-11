package sanzol.se.services;

import static sanzol.app.config.I18nPreference.getI18nString;

import java.util.List;
import java.util.StringJoiner;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import sanzol.app.config.HibernateUtil;
import sanzol.se.audit.AuditService;
import sanzol.se.commons.BaseService;
import sanzol.se.context.RequestContext;
import sanzol.se.model.entities.SeSector;
import sanzol.se.model.entities.SeSectorUser;
import sanzol.se.model.entities.SeUser;
import sanzol.util.json.JsonEncoder;
import sanzol.util.persistence.Query;

public class SeSectorsService extends BaseService
{
	private static final String THE_ENTITY = getI18nString("seSectors.label.theEntity");
	private static final String MESSAGE_ALREADY_EXISTS = getI18nString("seSectors.message.alreadyExists");
	private static final String MESSAGE_CANNOT_DEPEND_INFERIOR = getI18nString("seSectors.message.cannotDependInferior");
	private static final String MESSAGE_CONTAINS_DEPENDENCIES = getI18nString("message.recordContainsDependencies");
	private static final String MESSAGE_CONTAINS_DEPENDENCIES_DET = getI18nString("message.recordContainsDependencies.detailed");

	public SeSectorsService(RequestContext context)
	{
		super(context);
	}

	public List<SeSector> getSeSectors(Boolean isActive)
	{
		return getSeSectors(null, isActive);
	}

	@SuppressWarnings("unchecked")
	public List<SeSector> getSeSectors(Integer excludeSectorId, Boolean isActive)
	{
		List<SeSector> list = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			Query q = new Query();
			q.select("sec")
				.from(q.alias(SeSector.class, "sec"))
				.leftJoin("sec.seSectorPrev", "ant")
				.where(
						q.ne("sec.sectorId", excludeSectorId),
						q.eq("sec.isActive", isActive)
					)
				.orderBy(
						q.asc("coalesce(ant.name, sec.name)"),
						q.asc("coalesce(sec.seSectorPrev.sectorId, -1)"),
						q.asc("sec.name")
					);

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

	public TreeNode getTreeSectores()
	{
		TreeNode root = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			SeSector sectorNull = null;
			DefaultTreeNode node = new DefaultTreeNode(sectorNull);
			root = getTreeSectores(hbSession, node);

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

		return root;
	}

	private TreeNode getTreeSectores(org.hibernate.Session hbSession, TreeNode root)
	{
		Query q = new Query(SeSector.class, "s");

		SeSector sectorRoot = (SeSector) root.getData();
		if (sectorRoot == null)
		{
			q.where(q.isNull("s.seSectorPrev"));
		}
		else
		{
			q.where(q.eq("s.seSectorPrev.sectorId", sectorRoot.getSectorId()));
		}

		q.orderBy(q.asc("s.name"));

		@SuppressWarnings("unchecked")
		List<SeSector> list = q.list(hbSession);

		if (list != null && !list.isEmpty())
		{
			for (SeSector sector : list)
			{
				TreeNode current = new DefaultTreeNode(sector, null);
				current.setExpanded(true);

				TreeNode node = getTreeSectores(hbSession, current);

				root.getChildren().add(node);
			}
		}

		return root;
	}

	public SeSector getSeSector(int sectorId)
	{
		SeSector seSector = null;

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			seSector = (SeSector) hbSession.get(SeSector.class, sectorId);

			if (seSector != null)
			{
				Hibernate.initialize(seSector.getLstSeSectorUsers());
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

		return seSector;
	}

	private boolean validateTree(org.hibernate.Session hbSession, SeSector sector)
	{
		SeSector seSectorPrev = sector.getSeSectorPrev();

		while (seSectorPrev != null && seSectorPrev.getSeSectorPrev() != null)
		{
			if (sector.getSectorId().equals(seSectorPrev.getSeSectorPrev().getSectorId()))
			{
				return false;
			}

			seSectorPrev = (SeSector) hbSession.get(SeSector.class, seSectorPrev.getSeSectorPrev().getSectorId());
		}

		return true;
	}

	public void addSeSector(SeSector seSector, List<Integer> lstUserIdSel)
	{
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			// Validate uniques
			// -------------------------------------------------------------------------------
			if (unique(hbSession, seSector))
			{
				context.getMsgLogger().addMessageError(MESSAGE_ALREADY_EXISTS);
				tx.rollback();
				return;
			}

			hbSession.save(seSector);
			hbSession.flush();

			// -----------------------------------------------------------------------------------------------------
			for (Integer userId : lstUserIdSel)
			{
				SeUser seUser = (SeUser) hbSession.get(SeUser.class, userId);
				SeSectorUser sectorUser = new SeSectorUser();
				sectorUser.setSeSector(seSector);
				sectorUser.setSeUser(seUser);
				hbSession.save(sectorUser);
			}
			hbSession.flush();

			// ----- Audit -----------------------------------------------------------------------------------------
			String jsonNewEntity = JsonEncoder.encode(seSector);
			AuditService.auditInsert(hbSession, context, SeSector.class.getSimpleName(), seSector.getSectorId().toString(), null, jsonNewEntity);
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

	public void setSeSector(SeSector seSector, List<Integer> lstUserIdSel)
	{
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			if (validateTree(hbSession, seSector))
			{
				tx = hbSession.beginTransaction();

				// Validate uniques
				// -------------------------------------------------------------------------------
				if (unique(hbSession, seSector))
				{
					context.getMsgLogger().addMessageError(MESSAGE_ALREADY_EXISTS);
					tx.rollback();
					return;
				}

				SeSector oldEntity = (SeSector) hbSession.get(SeSector.class, seSector.getSectorId());
				hbSession.evict(oldEntity);

				hbSession.update(seSector);
				hbSession.flush();

				// -----------------------------------------------------------------------------------------------------
				hbSession.createQuery("delete SeSectorUser where seSector.sectorId = :sectorId")
					.setParameter("sectorId", seSector.getSectorId())
					.executeUpdate();
				hbSession.flush();

				for (Integer userId : lstUserIdSel)
				{
					SeUser seUser = (SeUser) hbSession.get(SeUser.class, userId);
					SeSectorUser sectorUser = new SeSectorUser();
					sectorUser.setSeSector(seSector);
					sectorUser.setSeUser(seUser);
					hbSession.save(sectorUser);
				}
				hbSession.flush();

				// ----- Audit -----------------------------------------------------------------------------------------
				String jsonOldEntity = JsonEncoder.encode(oldEntity);
				String jsonNewEntity = JsonEncoder.encode(seSector);
				AuditService.auditUpdate(hbSession, context, SeSector.class.getSimpleName(), seSector.getSectorId().toString(), null, jsonOldEntity, jsonNewEntity);
				hbSession.flush();

				tx.commit();
			}
			else
			{
				context.getMsgLogger().addMessageError(MESSAGE_CANNOT_DEPEND_INFERIOR);
			}
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

	public void delSeSector(SeSector seSector)
	{
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			// Validate dependencies
			// -----------------------------------------------------------------------------------------------------
			String dependencies = dependencies(hbSession, seSector);
			if (dependencies != null)
			{
				context.getMsgLogger().addMessageError(MESSAGE_CONTAINS_DEPENDENCIES_DET.replace("{theEntity}", THE_ENTITY).replace("{dependencies}", dependencies));
				tx.rollback();
				return;
			}

			hbSession.delete(seSector);
			hbSession.flush();

			// ----- Audit -----------------------------------------------------------------------------------------
			String jsonOldEntity = JsonEncoder.encode(seSector);
			AuditService.auditDelete(hbSession, context, SeSector.class.getSimpleName(), seSector.getSectorId().toString(), null, jsonOldEntity);
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

	private static boolean unique(org.hibernate.Session hbSession, SeSector seSector)
	{
		SeSector _seSector;

		if (seSector.getSectorId() == null)
		{
			Query q = new Query(SeSector.class, "a");
			q.where(q.eq("name", seSector.getName()));
			_seSector = (SeSector) q.uniqueResult(hbSession);
		}
		else
		{
			Query q = new Query(SeSector.class, "a");
			q.where(
					q.eq("name", seSector.getName()),
					q.ne("sectorId", seSector.getSectorId())
				);
			_seSector = (SeSector) q.uniqueResult(hbSession);
		}

		return (_seSector != null);
	}

	private static String dependencies(org.hibernate.Session hbSession, SeSector seSector)
	{
		StringJoiner joiner = new StringJoiner(",");

		Query q = new Query(SeSectorUser.class, "a");
		q.where(q.eq("seSector.sectorId", seSector.getSectorId()));
		SeSectorUser _seSectorUser = (SeSectorUser) q.firstResult(hbSession);
		if (_seSectorUser != null)
			joiner.add("seSectorUser");

		if (joiner.length() > 0)
			return joiner.toString();
		else
			return null;
	}

}
