package sanzol.se.web.controllers;

import static sanzol.app.config.I18nPreference.getI18nString;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.omnifaces.cdi.ViewScoped;

import sanzol.app.config.Permissions;
import sanzol.se.audit.AuditService;
import sanzol.se.commons.FacesUtils;
import sanzol.se.commons.SecurityUtils;
import sanzol.se.context.RequestContext;
import sanzol.se.model.entities.SeNotificationUser;
import sanzol.se.services.SeNotificationsUsersService;
import sanzol.util.DateRangeUtils;

@Named
@ViewScoped
public class SeNotificationsInboxController implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final String THIS_PAGE = "seNotificationsInbox";

	private static final String MESSAGE_NO_RECORD_SELECTED = getI18nString("message.noRecordSelected");

	private List<SelectItem> lstDateRanges;
	private String dateRange;

	private List<SeNotificationUser> lstSeNotificationsUsers;
	private SeNotificationUser seNotificationUser;

	public List<SeNotificationUser> getLstSeNotificationsUsers()
	{
		return lstSeNotificationsUsers;
	}

	public SeNotificationUser getSeNotificationUser()
	{
		return seNotificationUser;
	}

	public void setSeNotificationUser(SeNotificationUser seNotificationUser)
	{
		this.seNotificationUser = seNotificationUser;
	}

	public String getDateRange()
	{
		return dateRange;
	}

	public void setDateRange(String dateRange)
	{
		this.dateRange = dateRange;
	}

	public List<SelectItem> getLstDateRanges()
	{
		return lstDateRanges;
	}

	public void pageLoad()
	{
		HttpServletRequest request = FacesUtils.getRequest();
		if (!SecurityUtils.isGranted(request, Permissions.PERMISSION_SE_NOTIFICATIONS_INBOX, Permissions.LEVEL_READ_ONLY))
		{
			SecurityUtils.redirectAccessDenied(request, FacesUtils.getResponse());
			return;
		}

		RequestContext context = RequestContext.createContext(request);
		try
		{
			loadDateRanges();

			loadGrid(context);

			// ----- Audit -----------------------------------------------------------------------------------------
			if (!context.hasErrorOrFatal())
			{
				AuditService.auditPageLoad(context, THIS_PAGE, null);
			}
		}
		catch (Exception e)
		{
			context.getMsgLogger().addMessage(e);
		}
		finally
		{
			context.close();
		}
	}

	private void loadDateRanges()
	{
		lstDateRanges = DateRangeUtils.getRanges(12, 60, 120, 180);
		dateRange = lstDateRanges.get(0).getValue().toString();
	}

	private void loadGrid(RequestContext context)
	{
		DateRangeUtils.Period period = DateRangeUtils.getPeriod(dateRange);
		LocalDateTime _dateFrom = period.getBegin();
		LocalDateTime _dateTo = period.getEnd();

		SeNotificationsUsersService service = new SeNotificationsUsersService(context);
		lstSeNotificationsUsers = service.getSeNotificationsUsers(null, context.getSeUser().getUserId(), _dateFrom, _dateTo, null, false);
	}

	public void refresh()
	{
		RequestContext context = RequestContext.createContext();
		try
		{
			loadGrid(context);
		}
		catch (Exception e)
		{
			context.getMsgLogger().addMessage(e);
		}
		finally
		{
			context.close();
		}
	}

	public void setReaded(Integer notificationUserId)
	{
		if (notificationUserId == null)
		{
			FacesUtils.addMessageFatal(MESSAGE_NO_RECORD_SELECTED);
			return;
		}

		RequestContext context = RequestContext.createContext();
		try
		{
			SeNotificationsUsersService service = new SeNotificationsUsersService(context);
			service.setReaded(notificationUserId);
			if (context.hasErrorOrFatal())
			{
				return;
			}

			loadGrid(context);
		}
		catch (Exception e)
		{
			context.getMsgLogger().addMessage(e);
		}
		finally
		{
			context.close();
		}
	}

	public void delete()
	{
		if (seNotificationUser == null)
		{
			FacesUtils.addMessageFatal(MESSAGE_NO_RECORD_SELECTED);
			return;
		}

		RequestContext context = RequestContext.createContext();
		try
		{
			SeNotificationsUsersService service = new SeNotificationsUsersService(context);
			service.setDeleted(seNotificationUser.getNotificationUserId());
			if (context.hasErrorOrFatal())
			{
				return;
			}

			loadGrid(context);
		}
		catch (Exception e)
		{
			context.getMsgLogger().addMessage(e);
		}
		finally
		{
			context.close();
		}
	}

}
