package sanzol.se.web.controllers;

import static sanzol.app.config.I18nPreference.getI18nString;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.omnifaces.cdi.ViewScoped;

import sanzol.app.config.Permissions;
import sanzol.se.audit.AuditService;
import sanzol.se.commons.FacesUtils;
import sanzol.se.commons.SecurityUtils;
import sanzol.se.context.RequestContext;
import sanzol.se.model.entities.SeNotification;
import sanzol.se.model.entities.SeNotificationType;
import sanzol.se.model.entities.SeNotificationUser;
import sanzol.se.services.SeNotificationsService;
import sanzol.se.services.SeNotificationsUsersService;
import sanzol.se.services.cache.SeNotificationsTypesCache;
import sanzol.util.DateTimeUtils;
import sanzol.util.validator.SelectValidator;
import sanzol.util.validator.StringValidator;
import sanzol.util.validator.ValidationDisplay;

@Named
@ViewScoped
public class SeNotificationsSentController implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final String THIS_PAGE = "seNotificationsSent";

	private static final String MESSAGE_NO_RECORD_SELECTED = getI18nString("message.noRecordSelected");

	private List<SeNotification> lstSeNotifications;
	private SeNotification seNotification;

	private LocalDate dateFrom = DateTimeUtils.today().minusMonths(1);
	private LocalDate dateTo = null;

	private List<SeNotificationType> lstSeNotificationType;
	private Integer fltNotificationTypeId;
	private Integer notificationTypeId;

	private List<SeNotificationUser> lstSeNotificationUsers;

	private boolean displayMode = true;
	private boolean editMode = false;

	public List<SeNotification> getLstSeNotifications()
	{
		return lstSeNotifications;
	}

	public SeNotification getSeNotification()
	{
		return seNotification;
	}

	public void setSeNotification(SeNotification seNotification)
	{
		this.seNotification = seNotification;
	}

	public LocalDate getDateFrom()
	{
		return dateFrom;
	}

	public void setDateFrom(LocalDate dateFrom)
	{
		this.dateFrom = dateFrom;
	}

	public LocalDate getDateTo()
	{
		return dateTo;
	}

	public void setDateTo(LocalDate dateTo)
	{
		this.dateTo = dateTo;
	}

	public List<SeNotificationType> getLstSeNotificationType()
	{
		return lstSeNotificationType;
	}

	public Integer getFltNotificationTypeId()
	{
		return fltNotificationTypeId;
	}

	public void setFltNotificationTypeId(Integer fltNotificationTypeId)
	{
		this.fltNotificationTypeId = fltNotificationTypeId;
	}

	public Integer getNotificationTypeId()
	{
		return notificationTypeId;
	}

	public void setNotificationTypeId(Integer notificationTypeId)
	{
		this.notificationTypeId = notificationTypeId;
	}

	public List<SeNotificationUser> getLstSeNotificationUsers()
	{
		return lstSeNotificationUsers;
	}

	public boolean isDisplayMode()
	{
		return displayMode;
	}

	public void setDisplayMode(boolean displayMode)
	{
		this.displayMode = displayMode;
	}

	public boolean isEditMode()
	{
		return editMode;
	}

	public void setEditMode(boolean editMode)
	{
		this.editMode = editMode;
	}

	public void pageLoad()
	{
		HttpServletRequest request = FacesUtils.getRequest();
		if (!SecurityUtils.isGranted(request, Permissions.PERMISSION_SE_NOTIFICATIONS_SENT, Permissions.LEVEL_READ_ONLY))
		{
			SecurityUtils.redirectAccessDenied(request, FacesUtils.getResponse());
			return;
		}

		RequestContext context = RequestContext.createContext(request);
		try
		{
			loadSeNotificationsTypes(context);

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

	private void loadGrid(RequestContext context)
	{
		LocalDateTime _dateFrom = null;
		if (dateFrom != null)
		{
			_dateFrom = dateFrom.atStartOfDay();
		}
		LocalDateTime _dateTo = null;
		if (dateTo != null)
		{
			_dateTo = dateTo.atTime(LocalTime.MAX);
		}

		SeNotificationsService service = new SeNotificationsService(context);
		lstSeNotifications = service.getSeNotifications(fltNotificationTypeId, _dateFrom, _dateTo);
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

	public void loadUsers(SeNotification seNotification)
	{
		RequestContext context = RequestContext.createContext();
		try
		{
			SeNotificationsUsersService service = new SeNotificationsUsersService(context);
			lstSeNotificationUsers = service.getSeNotificationsUsers(seNotification.getNotificationId(), null, null, null, null, null);
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

	private void loadSeNotificationsTypes(RequestContext context)
	{
		lstSeNotificationType = SeNotificationsTypesCache.getSeNotificationsTypes();
	}

	public void add()
	{
		seNotification = new SeNotification();

		notificationTypeId = null;

		displayMode = false;
		editMode = true;
	}

	public void cancel()
	{
		notificationTypeId = null;

		seNotification = null;

		displayMode = true;
		editMode = false;
	}

	private static SeNotificationType search(List<SeNotificationType> list, Integer id)
	{
		if (id != null && list != null && !list.isEmpty())
		{
			for (SeNotificationType entity : list)
			{
				if (id.equals(entity.getNotificationTypeId()))
				{
					return entity;
				}
			}
		}
		return null;
	}

	public boolean validate()
	{
		return ValidationDisplay.isValid(
				SelectValidator.validate("seNotificationType", "{seNotificationsSent.field.seNotificationType}", notificationTypeId),
				StringValidator.validate("message", "{seNotificationsSent.field.message}", seNotification.getMessage(), null, null, true)
			);
	}

	public void save()
	{
		RequestContext context = RequestContext.createContext();
		try
		{
			if (!validate())
			{
				return;
			}

			seNotification.setSeNotificationType(search(lstSeNotificationType, notificationTypeId));

			SeNotificationsService service = new SeNotificationsService(context);
			if (seNotification.getNotificationId() == null)
			{
				service.addSeNotification(seNotification);
			}
			else
			{
				// service.setSeNotification(seNotification);
				context.getMsgLogger().addMessageError("No es posible actualizar una notificación");
			}
			if (context.hasErrorOrFatal())
			{
				return;
			}

			notificationTypeId = null;

			seNotification = null;

			displayMode = true;
			editMode = false;

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
		if (seNotification == null)
		{
			FacesUtils.addMessageFatal(MESSAGE_NO_RECORD_SELECTED);
			return;
		}

		RequestContext context = RequestContext.createContext();
		try
		{
			SeNotificationsService service = new SeNotificationsService(context);
			service.delSeNotification(seNotification);
			if (context.hasErrorOrFatal())
			{
				return;
			}

			displayMode = true;
			editMode = false;

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
