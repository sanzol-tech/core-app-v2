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
import sanzol.se.model.entities.SeAccount;
import sanzol.se.services.SeAccountsService;
import sanzol.util.CaseUtils;
import sanzol.util.PoiUtils;
import sanzol.util.validator.StringValidator;
import sanzol.util.validator.ValidationDisplay;

@Named
@ViewScoped
public class SeAccountsController implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final String THIS_PAGE = "seAccounts";

	private static final String MESSAGE_NO_RECORD_SELECTED = getI18nString("message.noRecordSelected");

	private List<SeAccount> lstSeAccounts;
	private SeAccount seAccount;

	private Boolean onlyActive = null;

	private boolean displayMode = true;
	private boolean editMode = false;

	public List<SeAccount> getLstSeAccounts()
	{
		return lstSeAccounts;
	}

	public SeAccount getSeAccount()
	{
		return seAccount;
	}

	public void setSeAccount(SeAccount seAccount)
	{
		this.seAccount = seAccount;
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
		if (!SecurityUtils.isGranted(request, Permissions.PERMISSION_SE_ACCOUNTS, Permissions.LEVEL_READ_ONLY))
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
		SeAccountsService service = new SeAccountsService(context);
		lstSeAccounts = service.getSeAccounts(onlyActive);
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
		seAccount = new SeAccount();
		seAccount.setIsActive(true);

		displayMode = false;
		editMode = true;
	}

	public void edit()
	{
		if (seAccount == null || seAccount.getAccountId() == null)
		{
			FacesUtils.addMessageFatal(MESSAGE_NO_RECORD_SELECTED);
			return;
		}

		RequestContext context = RequestContext.createContext();
		try
		{
			SeAccountsService service = new SeAccountsService(context);
			seAccount = service.getSeAccount(seAccount.getAccountId());
			if (context.hasErrorOrFatal())
			{
				return;
			}

			displayMode = false;
			editMode = true;

			// ----- Audit -----------------------------------------------------------------------------------------
			AuditService.auditSelect(context, THIS_PAGE, seAccount.getAccountId().toString(), null, null);
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
		if (seAccount.getAccountId() == null)
			add();
		else
			edit();
	}

	public void cancel()
	{
		seAccount = null;

		displayMode = true;
		editMode = false;
	}

	public void normalize()
	{
		seAccount.setName(CaseUtils.trim(seAccount.getName()));
	}

	public boolean validate()
	{
		return ValidationDisplay.isValid(
				StringValidator.validate("name", "{seAccounts.field.name}", seAccount.getName(), null, null, true)
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

			SeAccountsService service = new SeAccountsService(context);
			if (seAccount.getAccountId() == null)
			{
				service.addSeAccount(seAccount);
			}
			else
			{
				service.setSeAccount(seAccount);
			}
			if (context.hasErrorOrFatal())
			{
				return;
			}

			seAccount = null;

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
		if (seAccount == null)
		{
			FacesUtils.addMessageFatal(MESSAGE_NO_RECORD_SELECTED);
			return;
		}

		RequestContext context = RequestContext.createContext();
		try
		{
			SeAccountsService service = new SeAccountsService(context);
			service.delSeAccount(seAccount);
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
