/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: January 2021
 *
 */
package sanzol.se.error;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sanzol.app.config.HibernateUtil;
import sanzol.se.model.entities.SeError;
import sanzol.util.ExceptionUtils;

public class ErrorQueueService implements Runnable
{
	private static final Logger LOG = LoggerFactory.getLogger(ErrorQueueService.class);

	private static final int MAX_INSERTS_TRANSACTION = 10;
	private static final int QUEUE_CAPACITY = 5000;

	private static BlockingQueue<SeError> queue;

	public static BlockingQueue<SeError> getQueue()
	{
		return queue;
	}

	public static void init()
	{
		// Create Queue
		queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);

		// Start Listener
		ErrorQueueService consumer = new ErrorQueueService();

		Thread thread = new Thread(consumer);
		thread.setName("ErrorQueueService");
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
					SeError seError = queue.take();
					insert(seError);

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

	private void insert(SeError seError)
	{
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			hbSession.save(seError);
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