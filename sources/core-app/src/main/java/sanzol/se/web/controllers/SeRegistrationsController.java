package sanzol.se.web.controllers;

import static sanzol.app.config.I18nPreference.getI18nString;

import java.io.Serializable;
import java.util.List;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.omnifaces.cdi.ViewScoped;

import sanzol.app.config.Permissions;
import sanzol.se.audit.AuditEvents;
import sanzol.se.audit.AuditService;
import sanzol.se.commons.FacesUtils;
import sanzol.se.commons.SecurityUtils;
import sanzol.se.context.RequestContext;
import sanzol.se.model.entities.SeRegistration;
import sanzol.se.model.entities.SeRegistrationState;
import sanzol.se.services.SeRegistrationsService;
import sanzol.se.services.cache.SeRegistrationsStatesCache;
import sanzol.util.PoiUtils;

@Named
@ViewScoped
public class SeRegistrationsController implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final String THIS_PAGE = "seRegistrations";

	private static final String MESSAGE_NO_RECORD_SELECTED = getI18nString("message.noRecordSelected");

	private List<SeRegistration> lstSeRegistrations;
	private SeRegistration seRegistration;

	private List<SeRegistrationState> lstSeRegistrationState;
	private Integer registrationStateId;
	private boolean onlyNotExpired = true;

	private boolean displayMode = true;
	private boolean editMode = false;

	public List<SeRegistration> getLstSeRegistrations()
	{
		return lstSeRegistrations;
	}

	public SeRegistration getSeRegistration()
	{
		return seRegistration;
	}

	public void setSeRegistration(SeRegistration seRegistration)
	{
		this.seRegistration = seRegistration;
	}

	public Integer getRegistrationStateId()
	{
		return registrationStateId;
	}

	public void setRegistrationStateId(Integer registrationStateId)
	{
		this.registrationStateId = registrationStateId;
	}

	public boolean isOnlyNotExpired()
	{
		return onlyNotExpired;
	}

	public void setOnlyNotExpired(boolean onlyNotExpired)
	{
		this.onlyNotExpired = onlyNotExpired;
	}

	public List<SeRegistrationState> getLstSeRegistrationState()
	{
		return lstSeRegistrationState;
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
		if (!SecurityUtils.isGranted(request, Permissions.PERMISSION_SE_REGISTRATIONS, Permissions.LEVEL_READ_ONLY))
		{
			SecurityUtils.redirectAccessDenied(request, FacesUtils.getResponse());
			return;
		}

		RequestContext context = RequestContext.createContext(request);
		try
		{
			loadSeRegistrationsStates();

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
		SeRegistrationsService service = new SeRegistrationsService(context);
		lstSeRegistrations = service.getSeRegistrations(registrationStateId, onlyNotExpired);
	}

	private void loadSeRegistrationsStates()
	{
		lstSeRegistrationState = SeRegistrationsStatesCache.getSeRegistrationsStates();

		/*

		1 - todo
		2 - invitaciones
		3 - user request
		4 - pendientes de aprobacion
		5 - finalizados / expirados

		*/

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

	public void edit()
	{
		if (seRegistration == null || seRegistration.getRegistrationId() == null)
		{
			FacesUtils.addMessageFatal(MESSAGE_NO_RECORD_SELECTED);
			return;
		}

		RequestContext context = RequestContext.createContext();
		try
		{
			SeRegistrationsService service = new SeRegistrationsService(context);
			seRegistration = service.getSeRegistration(seRegistration.getRegistrationId());
			if (context.hasErrorOrFatal())
			{
				return;
			}

			displayMode = false;
			editMode = true;

			// ----- Audit -----------------------------------------------------------------------------------------
			AuditService.auditSelect(context, THIS_PAGE, seRegistration.getRegistrationId().toString(), null, null);
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

	public void cancel()
	{
		seRegistration = null;

		displayMode = true;
		editMode = false;
	}

	public void authorize()
	{
		RequestContext context = RequestContext.createContext();
		try
		{
			SeRegistrationsService service = new SeRegistrationsService(context);
			service.authorizationSuccessful(seRegistration);
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

	public void revoke()
	{
		RequestContext context = RequestContext.createContext();
		try
		{
			SeRegistrationsService service = new SeRegistrationsService(context);
			service.authorizationRevoked(seRegistration);
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

	public void delete()
	{
		if (seRegistration == null)
		{
			FacesUtils.addMessageFatal(MESSAGE_NO_RECORD_SELECTED);
			return;
		}

		RequestContext context = RequestContext.createContext();
		try
		{
			SeRegistrationsService service = new SeRegistrationsService(context);
			service.delSeRegistration(seRegistration);
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

	public void postProcessXLSX(Object document)
	{
		RequestContext context = RequestContext.createContext();
		try
		{
			PoiUtils.postProcessXLS(document);

			AuditService.auditNavigation(context, AuditEvents.EXPORT_XLSX, THIS_PAGE, null, null, null);
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

	public void postProcessCSV(Object document)
	{
		RequestContext context = RequestContext.createContext();
		try
		{
			AuditService.auditNavigation(context, AuditEvents.EXPORT_CSV, THIS_PAGE, null, null, null);
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

	// ------------------------------------------------------------------------------------------------------------------------

	public static String getRowStyleClass(SeRegistration seRegistration)
	{
		if (seRegistration.getRegistrationStateId().equals(SeRegistrationsStatesCache.AUTHORIZATION_PENDING))
		{
			return "text-bold";
		}
		if (seRegistration.getRegistrationStateId().equals(SeRegistrationsStatesCache.AUTHORIZATION_REVOKED))
		{
			return "text-through";
		}
		if (seRegistration.isExpired())
		{
			return "text-through";
		}
		return null;
	}

}
