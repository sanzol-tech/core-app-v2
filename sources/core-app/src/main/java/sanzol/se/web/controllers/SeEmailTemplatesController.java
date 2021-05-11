package sanzol.se.web.controllers;

import java.io.Serializable;
import java.util.List;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.omnifaces.cdi.ViewScoped;

import sanzol.app.config.Permissions;
import sanzol.se.audit.AuditService;
import sanzol.se.commons.FacesUtils;
import sanzol.se.commons.SecurityUtils;
import sanzol.se.context.RequestContext;
import sanzol.se.model.entities.SeEmailTemplate;
import sanzol.se.services.SeEmailTemplatesService;
import sanzol.util.validator.StringValidator;
import sanzol.util.validator.ValidationDisplay;

@Named
@ViewScoped
public class SeEmailTemplatesController implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final String THIS_PAGE = "seEmailTemplates";

	private static final String TXT_NO_SELECTED_RECORD_FOUND = "No selected record found";

	private List<SeEmailTemplate> lstSeEmailTemplates;
	private SeEmailTemplate seEmailTemplate;

	private boolean displayMode = true;
	private boolean editMode = false;

	public List<SeEmailTemplate> getLstSeEmailTemplates()
	{
		return lstSeEmailTemplates;
	}

	public SeEmailTemplate getSeEmailTemplate()
	{
		return seEmailTemplate;
	}

	public void setSeEmailTemplate(SeEmailTemplate seEmailTemplate)
	{
		this.seEmailTemplate = seEmailTemplate;
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
		if (!SecurityUtils.isGranted(request, Permissions.PERMISSION_SE_EMAIL_TEMPLATES, Permissions.LEVEL_READ_ONLY))
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
		SeEmailTemplatesService service = new SeEmailTemplatesService(context);
		lstSeEmailTemplates = service.getSeEmailTemplates();
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
		if (seEmailTemplate == null || seEmailTemplate.getEmailTemplateId() == null)
		{
			FacesUtils.addMessageFatal(TXT_NO_SELECTED_RECORD_FOUND);
			return;
		}

		RequestContext context = RequestContext.createContext();
		try
		{
			SeEmailTemplatesService service = new SeEmailTemplatesService(context);
			seEmailTemplate = service.getSeEmailTemplate(seEmailTemplate.getEmailTemplateId());
			if (context.hasErrorOrFatal())
			{
				return;
			}

			displayMode = false;
			editMode = true;

			// ----- Audit -----------------------------------------------------------------------------------------
			AuditService.auditSelect(context, THIS_PAGE, seEmailTemplate.getEmailTemplateId().toString(), null, null);
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
		edit();
	}

	public void cancel()
	{
		seEmailTemplate = null;

		displayMode = true;
		editMode = false;
	}

	public boolean validate()
	{
		return ValidationDisplay.isValid(
				StringValidator.validate("subject", "{seEmailTemplates.field.subject}", seEmailTemplate.getSubject(), null, null, true),
				StringValidator.validate("plainBody", "{seEmailTemplates.field.plainBody}", seEmailTemplate.getPlainBody(), null, null, true)
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

			SeEmailTemplatesService service = new SeEmailTemplatesService(context);
			service.setSeEmailTemplate(seEmailTemplate);
			if (context.hasErrorOrFatal())
			{
				return;
			}

			seEmailTemplate = null;

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
