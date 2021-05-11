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
import sanzol.se.model.entities.SeWhitelist;
import sanzol.se.services.SeWhitelistService;
import sanzol.util.CaseUtils;
import sanzol.util.PoiUtils;
import sanzol.util.validator.PatternValidator;
import sanzol.util.validator.RangeValidator;
import sanzol.util.validator.StringValidator;
import sanzol.util.validator.ValidationDisplay;

@Named
@ViewScoped
public class SeWhitelistController implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final String THIS_PAGE = "seWhitelist";

	private static final String MESSAGE_NO_RECORD_SELECTED = getI18nString("message.noRecordSelected");
	private static final String MESSAGE_INVITATIED_SUCCESSFULLY = getI18nString("seWhiteList.message.invitedSuccessfully");

	private List<SeWhitelist> lstSeWhiteList;
	private SeWhitelist seWhiteList;

	private Boolean onlyActive = null;

	private boolean displayMode = true;
	private boolean editMode = false;

	public List<SeWhitelist> getLstSeWhiteList()
	{
		return lstSeWhiteList;
	}

	public SeWhitelist getSeWhiteList()
	{
		return seWhiteList;
	}

	public void setSeWhiteList(SeWhitelist seWhiteList)
	{
		this.seWhiteList = seWhiteList;
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
		if (!SecurityUtils.isGranted(request, Permissions.PERMISSION_SE_WHITELIST, Permissions.LEVEL_READ_ONLY))
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
		SeWhitelistService service = new SeWhitelistService(context);
		lstSeWhiteList = service.getSeWhiteList();
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
		seWhiteList = new SeWhitelist();

		displayMode = false;
		editMode = true;
	}

	public void edit()
	{
		if (seWhiteList == null || seWhiteList.getWhitelistId() == null)
		{
			FacesUtils.addMessageFatal(MESSAGE_NO_RECORD_SELECTED);
			return;
		}

		RequestContext context = RequestContext.createContext();
		try
		{
			SeWhitelistService service = new SeWhitelistService(context);
			seWhiteList = service.getSeWhiteList(seWhiteList.getWhitelistId());
			if (context.hasErrorOrFatal())
			{
				return;
			}

			displayMode = false;
			editMode = true;

			// ----- Audit -----------------------------------------------------------------------------------------
			AuditService.auditSelect(context, THIS_PAGE, seWhiteList.getWhitelistId().toString(), null, null);
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
		if (seWhiteList.getWhitelistId() == null)
			add();
		else
			edit();
	}

	public void cancel()
	{
		seWhiteList = null;

		displayMode = true;
		editMode = false;
	}

	public void normalize()
	{
		seWhiteList.setLastname(CaseUtils.trim(seWhiteList.getLastname()));
		seWhiteList.setFirstname(CaseUtils.trim(seWhiteList.getFirstname()));
		seWhiteList.setGender("MF".indexOf(seWhiteList.getGender()) >= 0 ? seWhiteList.getGender() : null);
		seWhiteList.setGender("M".equals(seWhiteList.getGender()) || "F".equals(seWhiteList.getGender()) ? seWhiteList.getGender() : null);
		seWhiteList.setDocumentId(CaseUtils.upperTrim(seWhiteList.getDocumentId()));
		seWhiteList.setEmail(CaseUtils.lowerTrim(seWhiteList.getEmail()));
		seWhiteList.setCellphone(CaseUtils.trim(seWhiteList.getCellphone()));
	}

	public boolean validate()
	{
		return ValidationDisplay.isValid(
				StringValidator.validate("lastname", "{seWhiteList.field.lastname}", seWhiteList.getLastname(), null, null, true),
				StringValidator.validate("firstname", "{seWhiteList.field.firstname}", seWhiteList.getFirstname(), null, null, true),
				RangeValidator.validateBirthDate("birthDate", "{seWhiteList.field.birthDate}", seWhiteList.getBirthDate(), 0, 120, false),
				StringValidator.validateAlphanumeric("documentId", "{seWhiteList.field.documentId}", seWhiteList.getDocumentId(), 5, 10, false),
				PatternValidator.validateEmail("email", "{seWhiteList.field.email}", seWhiteList.getEmail(), true),
				PatternValidator.validatePhone("cellphone", "{userRegistration.field.cellphone}", seWhiteList.getCellphone(), false)
			);
	}

	public void save()
	{
		saveAndInvite(false);
	}

	public void invite()
	{
		saveAndInvite(true);
	}

	public void saveAndInvite(boolean invite)
	{
		RequestContext context = RequestContext.createContext();
		try
		{
			normalize();
			if (!validate())
			{
				return;
			}

			SeWhitelistService service = new SeWhitelistService(context);
			if (seWhiteList.getWhitelistId() == null)
			{
				service.addSeWhiteList(seWhiteList, invite);
			}
			else
			{
				service.setSeWhiteList(seWhiteList, invite);
			}
			if (context.hasErrorOrFatal())
			{
				return;
			}

			seWhiteList = null;

			displayMode = true;
			editMode = false;

			loadGrid(context);

			if (invite)
			{
				FacesUtils.addMessageInfo(MESSAGE_INVITATIED_SUCCESSFULLY);
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
		if (seWhiteList == null)
		{
			FacesUtils.addMessageFatal(MESSAGE_NO_RECORD_SELECTED);
			return;
		}

		RequestContext context = RequestContext.createContext();
		try
		{
			SeWhitelistService service = new SeWhitelistService(context);
			service.delSeWhiteList(seWhiteList);
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
