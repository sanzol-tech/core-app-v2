/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: January 2021
 *
 */
package sanzol.se.audit;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sanzol.app.config.HibernateUtil;
import sanzol.se.model.entities.SeAudit;
import sanzol.util.ExceptionUtils;

public class AuditQueueService implements Runnable
{
	private static final Logger LOG = LoggerFactory.getLogger(AuditQueueService.class);

	private static final int MAX_INSERTS_TRANSACTION = 10;
	private static final int QUEUE_CAPACITY = 5000;

	private static BlockingQueue<SeAudit> queue;

	public static BlockingQueue<SeAudit> getQueue()
	{
		return queue;
	}

	public static void init()
	{
		// Create Queue
		queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);

		// Start Listener
		AuditQueueService consumer = new AuditQueueService();

		Thread thread = new Thread(consumer);
		thread.setName("AuditQueueService");
		thread.start();
	}

	public void run()
	{
		try
		{
			while (true)
			{
				int count = 0;
				while (queue.size() > 0 && count < MAX_INSERTS_TRANSACTION)
				{
					SeAudit seAudit = queue.take();
					insert(seAudit);

					count++;
				}

				if (count > 0)
				{
					executeBatch();
				}

				Thread.sleep(1000);
			}
		}
		catch (InterruptedException e)
		{
			LOG.error(ExceptionUtils.getMessage(e));
		}
	}

	private void insert(SeAudit seAudit)
	{
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			hbSession.save(seAudit);
			hbSession.flush();

			tx.commit();
		}
		catch (Exception e)
		{
			LOG.error(ExceptionUtils.getMessage(e));
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

	private void executeBatch()
	{
		// TODO: Not implemented
	}

}