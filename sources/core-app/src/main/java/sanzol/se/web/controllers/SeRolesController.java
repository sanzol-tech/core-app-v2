package sanzol.se.web.controllers;

import static sanzol.app.config.I18nPreference.getI18nString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.omnifaces.cdi.ViewScoped;

import sanzol.app.config.Permissions;
import sanzol.se.audit.AuditService;
import sanzol.se.commons.FacesUtils;
import sanzol.se.commons.SecurityUtils;
import sanzol.se.context.RequestContext;
import sanzol.se.model.SePermissionLevel;
import sanzol.se.model.entities.SePermission;
import sanzol.se.model.entities.SeRole;
import sanzol.se.model.entities.SeRolePermission;
import sanzol.se.model.entities.SeRoleUser;
import sanzol.se.model.entities.SeUser;
import sanzol.se.services.SePermissionsService;
import sanzol.se.services.SeRolesService;
import sanzol.se.services.SeUsersService;
import sanzol.util.CaseUtils;
import sanzol.util.validator.StringValidator;
import sanzol.util.validator.ValidationDisplay;

@Named
@ViewScoped
public class SeRolesController implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final String THIS_PAGE = "seRoles";

	private static final String MESSAGE_NO_RECORD_SELECTED = getI18nString("message.noRecordSelected");

	private List<SeRole> lstSeRoles;
	private SeRole seRole;

	private List<SePermission> lstSePermissions;
	private List<SePermissionLevel> lstSePermissionLevel;

	private List<SeUser> lstSeUsers;
	private List<Integer> lstUserIdSel;

	private Boolean onlyActive = null;

	private boolean displayMode = true;
	private boolean editMode = false;

	public List<SeRole> getLstSeRoles()
	{
		return lstSeRoles;
	}

	public SeRole getSeRole()
	{
		return seRole;
	}

	public void setSeRole(SeRole seRole)
	{
		this.seRole = seRole;
	}

	public List<SePermissionLevel> getLstSePermissionLevel()
	{
		return lstSePermissionLevel;
	}

	public void setLstSePermissionLevel(List<SePermissionLevel> lstSePermissionLevel)
	{
		this.lstSePermissionLevel = lstSePermissionLevel;
	}

	public List<Integer> getLstUserIdSel()
	{
		return lstUserIdSel;
	}

	public void setLstUserIdSel(List<Integer> lstUserIdSel)
	{
		this.lstUserIdSel = lstUserIdSel;
	}

	public List<SeUser> getLstSeUsers()
	{
		return lstSeUsers;
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
		if (!SecurityUtils.isGranted(request, Permissions.PERMISSION_SE_ROLES, Permissions.LEVEL_READ_ONLY))
		{
			SecurityUtils.redirectAccessDenied(request, FacesUtils.getResponse());
			return;
		}

		RequestContext context = RequestContext.createContext(request);
		try
		{
			loadSePermissions(context);
			loadSeUsers(context);

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

	private void loadSePermissions(RequestContext context)
	{
		SePermissionsService service = new SePermissionsService(context);
		lstSePermissions = service.getSePermissions(null);
	}

	private void loadSeUsers(RequestContext context)
	{
		SeUsersService service = new SeUsersService(context);
		lstSeUsers = service.getSeUsers(null, null);
	}

	private void loadGrid(RequestContext context)
	{
		SeRolesService service = new SeRolesService(context);
		lstSeRoles = service.getSeRoles(onlyActive);
	}

	private void loadSePermissionLevel(boolean deniedAll)
	{
		lstSePermissionLevel = new ArrayList<SePermissionLevel>();
		for (SePermission permission : lstSePermissions)
		{
			SePermissionLevel sePermissionLevel = new SePermissionLevel();

			sePermissionLevel.setSePermission(permission);

			sePermissionLevel.setLevel(Permissions.LEVEL_DENIED);
			if (!deniedAll)
			{
				for (SeRolePermission seRolePermission : seRole.getLstSeRolesPermissions())
				{
					if (seRolePermission.getSePermission().getPermissionId().equals(permission.getPermissionId()))
					{
						sePermissionLevel.setLevel(seRolePermission.getPermissionLevel());
					}
				}
			}

			lstSePermissionLevel.add(sePermissionLevel);
		}
	}

	private void loadSeUsersSel()
	{
		lstUserIdSel = new ArrayList<Integer>();
		for (SeRoleUser roleUser : seRole.getLstSeRolesUsers())
		{
			lstUserIdSel.add(roleUser.getSeUser().getUserId());
		}
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
		seRole = new SeRole();
		seRole.setIsActive(true);

		loadSePermissionLevel(true);
		lstUserIdSel = new ArrayList<Integer>();

		displayMode = false;
		editMode = true;
	}

	public void edit()
	{
		if (seRole == null || seRole.getRoleId() == null)
		{
			FacesUtils.addMessageFatal(MESSAGE_NO_RECORD_SELECTED);
			return;
		}

		RequestContext context = RequestContext.createContext();
		try
		{
			SeRolesService service = new SeRolesService(context);
			seRole = service.getSeRole(seRole.getRoleId());
			if (context.hasErrorOrFatal())
			{
				return;
			}

			loadSePermissionLevel(false);
			loadSeUsersSel();

			displayMode = false;
			editMode = true;

			// ----- Audit -----------------------------------------------------------------------------------------
			AuditService.auditSelect(context, THIS_PAGE, seRole.getRoleId().toString(), null, null);
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
		if (seRole.getRoleId() == null)
			add();
		else
			edit();
	}

	public void cancel()
	{
		seRole = null;

		displayMode = true;
		editMode = false;
	}

	public void normalize()
	{
		seRole.setName(CaseUtils.trim(seRole.getName()));
	}

	public boolean validate()
	{
		return ValidationDisplay.isValid(
				StringValidator.validate("name", "{seRoles.field.name}", seRole.getName(), null, null, true)
			);
	}

	public void save()
	{
		RequestContext context = RequestContext.createContext();
		try
		{
			normalize();
			if (!validate())
			{
				return;
			}

			SeRolesService service = new SeRolesService(context);
			if (seRole.getRoleId() == null)
			{
				service.addSeRole(seRole, lstSePermissionLevel, lstUserIdSel);
			}
			else
			{
				service.setSeRole(seRole, lstSePermissionLevel, lstUserIdSel);
			}
			if (context.hasErrorOrFatal())
			{
				return;
			}
			seRole = null;

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
		if (seRole == null)
		{
			FacesUtils.addMessageFatal(MESSAGE_NO_RECORD_SELECTED);
			return;
		}

		RequestContext context = RequestContext.createContext();
		try
		{
			SeRolesService service = new SeRolesService(context);
			service.delSeRole(seRole);
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
