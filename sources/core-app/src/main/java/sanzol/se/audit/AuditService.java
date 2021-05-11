/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: January 2021
 *
 */
package sanzol.se.audit;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sanzol.app.config.AppProperties;
import sanzol.app.config.Version;
import sanzol.se.context.RequestContext;
import sanzol.se.model.entities.SeAudit;
import sanzol.util.DateTimeUtils;
import sanzol.util.ExceptionUtils;

public class AuditService
{
	private static final Logger LOG = LoggerFactory.getLogger(AuditService.class);

	private static String HOSTNAME;
	static
	{
		try
		{
			HOSTNAME = InetAddress.getLocalHost().getHostName();
		}
		catch (UnknownHostException e)
		{
			LOG.error(ExceptionUtils.getMessage(e));
			HOSTNAME = "unknown";
		}
	}

	private static boolean AUDIT_LOGIN = AppProperties.PROP_AUDIT_LOGIN.getBooleanValue();
	private static boolean AUDIT_TRANSACTION = AppProperties.PROP_AUDIT_TRANSACTION.getBooleanValue();
	private static boolean AUDIT_NAVIGATION = AppProperties.PROP_AUDIT_NAVIGATION.getBooleanValue();

	private static BlockingQueue<SeAudit> queue = AuditQueueService.getQueue();

	// -----------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------

	public static void auditLogin(org.hibernate.Session hbSession, RequestContext context, String eventName, String detail)
	{
		SeAudit seAudit = createAuditLogin(context, eventName, detail);
		if (seAudit != null)
		{
			hbSession.save(seAudit);
		}
	}

	public static void auditLogin(RequestContext context, String eventName, String detail)
	{
		try
		{
			SeAudit seAudit = createAuditLogin(context, eventName, detail);
			if (seAudit != null)
			{
				queue.put(seAudit);
			}
		}
		catch (InterruptedException e)
		{
			LOG.error(ExceptionUtils.getMessage(e));
		}
	}

	public static SeAudit createAuditLogin(RequestContext context, String eventName, String detail)
	{
		if (!AUDIT_LOGIN || context == null || context.getAuthSession() == null)
		{
			return null;
		}

		SeAudit seAudit = new SeAudit();

		seAudit.setAuditDate(DateTimeUtils.now());
		seAudit.setSeSoftVersion(Version.getMySoftVersion());
		seAudit.setServerName(HOSTNAME);

		seAudit.setSeUser(context.getAuthSession().getSeUser());
		seAudit.setEventDate(context.getCreationDate());
		seAudit.setContext(context.getCallingMethod());
		seAudit.setSessionId(context.getAuthSession().getSessionId());
		seAudit.setIpAddress(context.getAuthSession().getRemoteAddress());
		seAudit.setUseragent(context.getAuthSession().getUserAgent());

		seAudit.setEventName(eventName);
		seAudit.setDetail(detail);

		return seAudit;
	}

	// -----------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------

	public static void auditInsert(org.hibernate.Session hbSession, RequestContext context, String entityName, String entityId, String parentId, String jsonNewEntity)
	{
		auditTransaction(context, AuditEvents.INSERT, entityName, entityId, parentId, null, jsonNewEntity, null);
	}

	public static void auditUpdate(org.hibernate.Session hbSession, RequestContext context, String entityName, String entityId, String parentId, String jsonOldEntity, String jsonNewEntity)
	{
		auditTransaction(context, AuditEvents.UPDATE, entityName, entityId, parentId, jsonOldEntity, jsonNewEntity, null);
	}

	public static void auditDelete(org.hibernate.Session hbSession, RequestContext context, String entityName, String entityId, String parentId, String jsonOldEntity)
	{
		auditTransaction(context, AuditEvents.DELETE, entityName, entityId, parentId, jsonOldEntity, null, null);
	}

	public static void auditTransaction(org.hibernate.Session hbSession, RequestContext context, String eventName, String entityName, String entityId, String parentId, String detail)
	{
		auditTransaction(hbSession, context, eventName, entityName, entityId, parentId, null, null, detail);
	}

	public static void auditTransaction(RequestContext context, String eventName, String entityName, String entityId, String parentId, String detail)
	{
		auditTransaction(context, eventName, entityName, entityId, parentId, null, null, detail);
	}

	// -----------------------------------------------------------------------------------------------------

	public static void auditTransaction(org.hibernate.Session hbSession, RequestContext context, String eventName, String entityName, String entityId, String parentId, String jsonOldEntity, String jsonNewEntity, String detail)
	{
		SeAudit seAudit = createAuditTransaction(context, eventName, entityName, entityId, parentId, jsonOldEntity, jsonNewEntity, detail);
		if (seAudit != null)
		{
			hbSession.save(seAudit);
		}
	}

	public static void auditTransaction(RequestContext context, String eventName, String entityName, String entityId, String parentId, String jsonOldEntity, String jsonNewEntity, String detail)
	{
		try
		{
			SeAudit seAudit = createAuditTransaction(context, eventName, entityName, entityId, parentId, jsonOldEntity, jsonNewEntity, detail);
			if (seAudit != null)
			{
				queue.put(seAudit);
			}
		}
		catch (InterruptedException e)
		{
			LOG.error(ExceptionUtils.getMessage(e));
		}
	}

	public static SeAudit createAuditTransaction(RequestContext context, String eventName, String entityName, String entityId, String parentId, String jsonOldEntity, String jsonNewEntity, String detail)
	{
		if (!AUDIT_TRANSACTION || context == null || context.getAuthSession() == null)
		{
			return null;
		}

		SeAudit seAudit = new SeAudit();

		seAudit.setAuditDate(DateTimeUtils.now());
		seAudit.setSeSoftVersion(Version.getMySoftVersion());
		seAudit.setServerName(HOSTNAME);

		seAudit.setSeUser(context.getAuthSession().getSeUser());
		seAudit.setEventDate(context.getCreationDate());
		seAudit.setContext(context.getCallingMethod());
		seAudit.setSessionId(context.getAuthSession().getSessionId());
		seAudit.setIpAddress(context.getAuthSession().getRemoteAddress());
		seAudit.setUseragent(context.getAuthSession().getUserAgent());

		seAudit.setEventName(eventName);
		seAudit.setEntityName(entityName);
		seAudit.setEntityId(entityId);
		seAudit.setParentId(parentId);
		seAudit.setOldEntity(jsonOldEntity);
		seAudit.setNewEntity(jsonNewEntity);
		seAudit.setDetail(detail);

		return seAudit;
	}

	// -----------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------

	public static void auditPageLoad(RequestContext context, String entityName, String detail)
	{
		auditNavigation(context, AuditEvents.PAGE_LOAD, entityName, null, null, detail);
	}

	public static void auditSelect(RequestContext context, String entityName, String entityId, String parentId, String detail)
	{
		auditNavigation(context, AuditEvents.SELECT, entityName, entityId, parentId, detail);
	}

	// -----------------------------------------------------------------------------------------------------

	public static void auditNavigation(org.hibernate.Session hbSession, RequestContext context, String eventName, String entityName, String entityId, String parentId, String detail)
	{
		SeAudit seAudit = createAuditNavigation(context, eventName, entityName, entityId, parentId, detail);
		if (seAudit != null)
		{
			hbSession.save(seAudit);
		}
	}

	public static void auditNavigation(RequestContext context, String eventName, String entityName, String entityId, String parentId, String detail)
	{
		try
		{
			SeAudit seAudit = createAuditNavigation(context, eventName, entityName, entityId, parentId, detail);
			if (seAudit != null)
			{
				queue.put(seAudit);
			}
		}
		catch (InterruptedException e)
		{
			LOG.error(ExceptionUtils.getMessage(e));
		}
	}

	public static SeAudit createAuditNavigation(RequestContext context, String eventName, String entityName, String entityId, String parentId, String detail)
	{
		if (!AUDIT_NAVIGATION || context == null || context.getAuthSession() == null)
		{
			return null;
		}

		SeAudit seAudit = new SeAudit();

		seAudit.setAuditDate(DateTimeUtils.now());
		seAudit.setSeSoftVersion(Version.getMySoftVersion());
		seAudit.setServerName(HOSTNAME);

		seAudit.setSeUser(context.getAuthSession().getSeUser());
		seAudit.setEventDate(context.getCreationDate());
		seAudit.setContext(context.getCallingMethod());
		seAudit.setSessionId(context.getAuthSession().getSessionId());
		seAudit.setIpAddress(context.getAuthSession().getRemoteAddress());
		seAudit.setUseragent(context.getAuthSession().getUserAgent());

		seAudit.setEventName(eventName);
		seAudit.setEntityName(entityName);
		seAudit.setEntityId(entityId);
		seAudit.setParentId(parentId);
		seAudit.setDetail(detail);

		return seAudit;
	}

}