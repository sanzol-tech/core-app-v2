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
import sanzol.se.model.entities.SePermission;
import sanzol.se.services.SePermissionsService;
import sanzol.util.PoiUtils;
import sanzol.util.validator.SelectValidator;
import sanzol.util.validator.StringValidator;
import sanzol.util.validator.ValidationDisplay;

@Named
@ViewScoped
public class SePermissionsController implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final String THIS_PAGE = "sePermissions";

	private static final String MESSAGE_NO_RECORD_SELECTED = getI18nString("message.noRecordSelected");

	private List<SePermission> lstSePermissions;
	private SePermission sePermission;

	private Boolean onlyActive = null;

	private boolean displayMode = true;
	private boolean editMode = false;

	public List<SePermission> getLstSePermissions()
	{
		return lstSePermissions;
	}

	public SePermission getSePermission()
	{
		return sePermission;
	}

	public void setSePermission(SePermission sePermission)
	{
		this.sePermission = sePermission;
	}

	public Boolean getOnlyActive()
	{
		return onlyActive;
	}

	public void setOnlyActive(Boolean onlyActive)
	{
		this.onlyActive = onlyActive;
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
		if (!SecurityUtils.isGranted(request, Permissions.PERMISSION_SE_PERMISSIONS, Permissions.LEVEL_READ_ONLY))
		{
			SecurityUtils.redirectAccessDenied(request, FacesUtils.getResponse());
			return;
		}

		RequestContext context = RequestContext.createContext(request);
		try
		{
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
		SePermissionsService service = new SePermissionsService(context);
		lstSePermissions = service.getSePermissions(onlyActive);
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

	public void add()
	{
		sePermission = new SePermission();
		sePermission.setIsUserDefined(true);
		sePermission.setIsActive(true);

		displayMode = false;
		editMode = true;
	}

	public void edit()
	{
		if (sePermission == null || sePermission.getPermissionId() == null)
		{
			FacesUtils.addMessageFatal(MESSAGE_NO_RECORD_SELECTED);
			return;
		}

		RequestContext context = RequestContext.createContext();
		try
		{
			SePermissionsService service = new SePermissionsService(context);
			sePermission = service.getSePermission(sePermission.getPermissionId());
			if (context.hasErrorOrFatal())
			{
				return;
			}

			displayMode = false;
			editMode = true;

			// ----- Audit -----------------------------------------------------------------------------------------
			AuditService.auditSelect(context, THIS_PAGE, sePermission.getPermissionId().toString(), null, null);
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

	public void undoEdit()
	{
		if (sePermission.getPermissionId() == null)
			add();
		else
			edit();
	}

	public void cancel()
	{
		sePermission = null;

		displayMode = true;
		editMode = false;
	}

	public boolean validate()
	{
		return ValidationDisplay.isValid(
				StringValidator.validate("name", "{sePermissions.field.name}", sePermission.getName(), null, null, true),
				SelectValidator.validate("userLevel", "{sePermissions.field.userLevel}", sePermission.getUserLevel(), -1)
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

			SePermissionsService service = new SePermissionsService(context);
			if (sePermission.getPermissionId() == null)
			{
				service.addSePermission(sePermission);
			}
			else
			{
				service.setSePermission(sePermission);
			}
			if (context.hasErrorOrFatal())
			{
				return;
			}

			sePermission = null;

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
		if (sePermission == null)
		{
			FacesUtils.addMessageFatal(MESSAGE_NO_RECORD_SELECTED);
			return;
		}

		RequestContext context = RequestContext.createContext();
		try
		{
			SePermissionsService service = new SePermissionsService(context);
			service.delSePermission(sePermission);
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

}
