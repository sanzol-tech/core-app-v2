package sanzol.se.web.controllers;

import static sanzol.app.config.I18nPreference.getI18nString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.omnifaces.cdi.ViewScoped;
import org.primefaces.PrimeFaces;

import sanzol.app.config.Permissions;
import sanzol.se.audit.AuditEvents;
import sanzol.se.audit.AuditService;
import sanzol.se.commons.FacesUtils;
import sanzol.se.commons.SecurityUtils;
import sanzol.se.context.RequestContext;
import sanzol.se.model.entities.SeAccount;
import sanzol.se.model.entities.SeEmailTemplate;
import sanzol.se.model.entities.SeRole;
import sanzol.se.model.entities.SeRoleUser;
import sanzol.se.model.entities.SeSector;
import sanzol.se.model.entities.SeSectorUser;
import sanzol.se.model.entities.SeUser;
import sanzol.se.services.SeAccountsService;
import sanzol.se.services.SeEmailService;
import sanzol.se.services.SePasswordService;
import sanzol.se.services.SeRolesService;
import sanzol.se.services.SeSectorsService;
import sanzol.se.services.SeUsersService;
import sanzol.se.services.cache.SeEmailTemplatesCache;
import sanzol.util.CaseUtils;
import sanzol.util.PoiUtils;
import sanzol.util.Replacer;
import sanzol.util.validator.PasswordValidator;
import sanzol.util.validator.PatternValidator;
import sanzol.util.validator.RangeValidator;
import sanzol.util.validator.SelectValidator;
import sanzol.util.validator.StringValidator;
import sanzol.util.validator.ValidationDisplay;

@Named
@ViewScoped
public class SeUsersController implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final String THIS_PAGE = "seUsers";

	private static final String MESSAGE_NO_RECORD_SELECTED = getI18nString("message.noRecordSelected");

	private List<SeUser> lstSeUsers;
	private SeUser seUser;

	private List<SeAccount> lstSeAccount;
	private Integer fltAccountId;
	private Integer accountId;

	private List<SelectItem> lstSeSectors;
	private List<String> lstSeSectorsSel;

	private List<SelectItem> lstSeRoles;
	private List<String> lstSeRolesSel;

	private String password;
	private String newPassword;

	private Boolean onlyActive = null;

	private boolean displayMode = true;
	private boolean editMode = false;

	public SeUser getSeUser()
	{
		return seUser;
	}

	public void setSeUser(SeUser seUser)
	{
		this.seUser = seUser;
	}

	public List<SeAccount> getLstSeAccount()
	{
		return lstSeAccount;
	}

	public Integer getFltAccountId()
	{
		return fltAccountId;
	}

	public void setFltAccountId(Integer fltAccountId)
	{
		this.fltAccountId = fltAccountId;
	}

	public Integer getAccountId()
	{
		return accountId;
	}

	public void setAccountId(Integer accountId)
	{
		this.accountId = accountId;
	}

	public List<String> getLstSeSectorsSel()
	{
		return lstSeSectorsSel;
	}

	public void setLstSeSectorsSel(List<String> lstSeSectorsSel)
	{
		this.lstSeSectorsSel = lstSeSectorsSel;
	}

	public List<String> getLstSeRolesSel()
	{
		return lstSeRolesSel;
	}

	public void setLstSeRolesSel(List<String> lstSeRolesSel)
	{
		this.lstSeRolesSel = lstSeRolesSel;
	}

	public List<SelectItem> getLstSeSectors()
	{
		return lstSeSectors;
	}

	public List<SelectItem> getLstSeRoles()
	{
		return lstSeRoles;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getNewPassword()
	{
		return newPassword;
	}

	public void setNewPassword(String newPassword)
	{
		this.newPassword = newPassword;
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

	public List<SeUser> getLstSeUsers()
	{
		return lstSeUsers;
	}

	public String getPolicy()
	{
		return PasswordValidator.getPolicy();
	}

	public void pageLoad()
	{
		HttpServletRequest request = FacesUtils.getRequest();
		if (!SecurityUtils.isGranted(request, Permissions.PERMISSION_SE_USERS, Permissions.LEVEL_READ_ONLY))
		{
			SecurityUtils.redirectAccessDenied(request, FacesUtils.getResponse());
			return;
		}

		RequestContext context = RequestContext.createContext(request);
		try
		{
			loadSeAccounts(context);
			loadSeSectors(context);
			loadSeRoles(context);

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
		SeUsersService service = new SeUsersService(context);
		lstSeUsers = service.getSeUsers(fltAccountId, onlyActive);
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

	private void loadSeAccounts(RequestContext context)
	{
		SeAccountsService service = new SeAccountsService(context);
		lstSeAccount = service.getSeAccounts(null);
	}

	private void loadSeSectors(RequestContext context)
	{
		SeSectorsService service = new SeSectorsService(context);
		List<SeSector> lst = service.getSeSectors(null);

		lstSeSectors = new ArrayList<>();
		for (SeSector item : lst)
		{
			lstSeSectors.add(new SelectItem(item.getSectorId().toString(), item.getName()));
		}
	}

	private void loadSeRoles(RequestContext context)
	{
		SeRolesService service = new SeRolesService(context);
		List<SeRole> lst = service.getSeRoles(null);

		lstSeRoles = new ArrayList<>();
		for (SeRole item : lst)
		{
			lstSeRoles.add(new SelectItem(item.getRoleId().toString(), item.getName()));
		}
	}

	private void loadSeSectorsSel()
	{
		lstSeSectorsSel = new ArrayList<String>();
		for (SeSectorUser roleUser : seUser.getLstSeSectorsUser())
		{
			lstSeSectorsSel.add(roleUser.getSeSector().getSectorId().toString());
		}
	}

	private void loadSeRolesSel()
	{
		lstSeRolesSel = new ArrayList<String>();
		for (SeRoleUser roleUser : seUser.getLstSeRolesUser())
		{
			lstSeRolesSel.add(roleUser.getSeRole().getRoleId().toString());
		}
	}

	public void add()
	{
		seUser = new SeUser();
		seUser.setMaxSessions(-1);
		seUser.setMaxInactiveInterval(-1);
		seUser.setIncorrectAttempts(0);
		seUser.setIsActive(true);

		password = "";

		lstSeSectorsSel = new ArrayList<String>();
		lstSeRolesSel = new ArrayList<String>();

		displayMode = false;
		editMode = true;
	}

	public void edit()
	{
		if (seUser == null || seUser.getUserId() == null)
		{
			FacesUtils.addMessageFatal(MESSAGE_NO_RECORD_SELECTED);
			return;
		}

		RequestContext context = RequestContext.createContext();
		try
		{
			SeUsersService service = new SeUsersService(context);
			seUser = service.getSeUser(seUser.getUserId());
			if (context.hasErrorOrFatal())
			{
				return;
			}

			loadSeSectorsSel();
			loadSeRolesSel();

			accountId = seUser.getSeAccount() != null ? seUser.getSeAccount().getAccountId() : null;

			password = "";

			displayMode = false;
			editMode = true;

			// ----- Audit -----------------------------------------------------------------------------------------
			AuditService.auditSelect(context, THIS_PAGE, seUser.getUserId().toString(), null, null);
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
		if (seUser.getUserId() == null)
			add();
		else
			edit();
	}

	public void cancel()
	{
		seUser = null;
		accountId = null;
		password = "";

		displayMode = true;
		editMode = false;
	}

	private static SeAccount search(List<SeAccount> list, Integer id)
	{
		if (id != null && list != null && !list.isEmpty())
		{
			for (SeAccount entity : list)
			{
				if (id.equals(entity.getAccountId()))
				{
					return entity;
				}
			}
		}
		return null;
	}

	public void normalize()
	{
		seUser.setUsername(CaseUtils.lowerTrim(seUser.getUsername()));
		seUser.setLastname(CaseUtils.trim(seUser.getLastname()));
		seUser.setFirstname(CaseUtils.trim(seUser.getFirstname()));
		seUser.setEmail(CaseUtils.lowerTrim(seUser.getEmail()));
	}

	public boolean validate()
	{
		return ValidationDisplay.isValid(
				StringValidator.validate("username", "{seUsers.field.username}", seUser.getUsername(), null, null, true),
				StringValidator.validate("lastname", "{seUsers.field.lastname}", seUser.getLastname(), null, null, true),
				StringValidator.validate("firstname", "{seUsers.field.firstname}", seUser.getFirstname(), null, null, true),
				PatternValidator.validateEmail("email", "{seUsers.field.email}", seUser.getEmail(), false),
				PatternValidator.validatePhone("cellphone", "{seUsers.field.cellphone}", seUser.getCellphone(), false),
				RangeValidator.validate("maxSessions", "{seUsers.field.maxSessions}", seUser.getMaxSessions(), -1, 99, true),
				RangeValidator.validate("maxInactiveInterval", "{seUsers.field.maxInactiveInterval}", seUser.getMaxInactiveInterval(), -1, 86400, true),
				SelectValidator.validate("userLevel", "{seUsers.field.userLevel}", seUser.getUserLevel(), -1)
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

			seUser.setSeAccount(search(lstSeAccount, accountId));

			SeUsersService service = new SeUsersService(context);

			boolean isNew = seUser.getUserId() == null;

			if (isNew)
			{
				password = PasswordValidator.generatePassword();
				service.addSeUser(seUser, password, lstSeSectorsSel, lstSeRolesSel);
			}
			else
			{
				service.setSeUser(seUser, lstSeSectorsSel, lstSeRolesSel);
			}

			if (context.hasErrorOrFatal())
			{
				return;
			}

			// seUser = null;

			displayMode = true;
			editMode = false;

			loadGrid(context);

			if (isNew)
			{
				PrimeFaces.current().executeScript("PF('sbInfoUserCreated').show();");
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

	public void delete()
	{
		if (seUser == null)
		{
			FacesUtils.addMessageFatal(MESSAGE_NO_RECORD_SELECTED);
			return;
		}

		RequestContext context = RequestContext.createContext();
		try
		{
			SeUsersService service = new SeUsersService(context);
			service.delSeUser(seUser);
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

	public void unlock()
	{
		if (seUser == null)
		{
			FacesUtils.addMessageFatal(MESSAGE_NO_RECORD_SELECTED);
			return;
		}

		RequestContext context = RequestContext.createContext();
		try
		{
			SeUsersService service = new SeUsersService(context);
			service.unlockUser(seUser);
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

	public void resetPassword()
	{
		if (seUser == null)
		{
			FacesUtils.addMessageFatal(MESSAGE_NO_RECORD_SELECTED);
			return;
		}

		RequestContext context = RequestContext.createContext();
		try
		{
			SePasswordService service = new SePasswordService(context);
			newPassword = service.reset(seUser);
		}
		catch (Exception e)
		{
			context.getMsgLogger().addMessage(e);
			newPassword = "";
		}
		finally
		{
			context.close();
		}
	}

	public void generatePassword()
	{
		password = PasswordValidator.generatePassword();
	}

	public void sendWelcome()
	{
		if (seUser.getEmail() != null && !seUser.getEmail().isBlank())
		{
			String to = seUser.getFormattedEmail();
			SeEmailTemplate seEmailTemplate = SeEmailTemplatesCache.getSeEmailTemplate(SeEmailTemplatesCache.TEMPLATE_USER_WELCOME);
			Replacer replacer = Replacer.create()
				.add("name", seUser.getNameAlt())
				.add("username", seUser.getUsername())
				.add("password", newPassword);
			SeEmailService.create().withTemplate(seEmailTemplate, replacer).sendAsync(to);
		}
	}

	public void sendPassword()
	{
		if (seUser.getEmail() != null && !seUser.getEmail().isBlank())
		{
			String to = seUser.getFormattedEmail();
			SeEmailTemplate seEmailTemplate = SeEmailTemplatesCache.getSeEmailTemplate(SeEmailTemplatesCache.TEMPLATE_PASSWORD_RESET);
			Replacer replacer = Replacer.create()
				.add("name", seUser.getNameAlt())
				.add("username", seUser.getUsername())
				.add("password", newPassword);
			SeEmailService.create().withTemplate(seEmailTemplate, replacer).sendAsync(to);
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
