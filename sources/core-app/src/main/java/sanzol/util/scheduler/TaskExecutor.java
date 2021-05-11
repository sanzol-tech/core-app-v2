/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: August 2020
 *
 */
package sanzol.util.scheduler;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import sanzol.app.config.I18nPreference;
import sanzol.util.ExceptionUtils;

public class TaskExecutor
{
	private static final Logger LOG = LoggerFactory.getLogger(TaskExecutor.class);

	private ScheduledExecutorService executorService;

	private ITask task;

	private CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
	private CronParser parser = new CronParser(cronDefinition);
	private Cron cron;
	private ExecutionTime executionTime;

	public ITask getTask()
	{
		return task;
	}

	public String getCronDescription()
	{
		CronDescriptor cronDescriptor = CronDescriptor.instance(I18nPreference.getLocale());
		return cronDescriptor.describe(cron);
	}

	public TaskExecutor(ITask task, String cronExpression)
	{
		// Create a new ThreadFactory for name the class
		class NamedThreadFactory implements ThreadFactory
		{
			public Thread newThread(Runnable r)
			{
				return new Thread(r, task.getClass().getSimpleName());
			}
		}

		executorService = Executors.newScheduledThreadPool(1, new NamedThreadFactory());

		this.task = task;

		cron = parser.parse(cronExpression);
		executionTime = ExecutionTime.forCron(cron);

		LOG.info("Init Task - " + task.getClass().getSimpleName() + " - " + getCronDescription());
	}

	public void startExecution()
	{
		Runnable taskWrapper = new Runnable()
		{
			@Override
			public void run()
			{
				task.execute();
				startExecution();
			}
		};

		ZonedDateTime now = ZonedDateTime.now();
		Optional<Duration> timeToNextExecution = executionTime.timeToNextExecution(now);
		long millisDelay = timeToNextExecution.get().toMillis();

		executorService.schedule(taskWrapper, millisDelay, TimeUnit.MILLISECONDS);
	}

	public void stop()
	{
		LOG.info(String.format("Stopping %s ...", task.getClass().getSimpleName()));

		task.stop();

		executorService.shutdown();
		try
		{
			executorService.awaitTermination(1, TimeUnit.MINUTES);
		}
		catch (InterruptedException e)
		{
			LOG.error(ExceptionUtils.getMessage(e));
		}
	}

}
