package sanzol.se.web.controllers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.omnifaces.cdi.ViewScoped;

import sanzol.app.config.AppProperties;
import sanzol.app.config.Permissions;
import sanzol.se.audit.AuditService;
import sanzol.se.commons.FacesUtils;
import sanzol.se.commons.SecurityUtils;
import sanzol.se.context.RequestContext;
import sanzol.se.services.SePropertiesService;
import sanzol.util.properties.Property;

@Named
@ViewScoped
public class SePropertiesController implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final String THIS_PAGE = "seProperties";

	private List<Property> lstPropertiesFiltered;

	private Property property;
	private String value;
	private List<String> lstValues;

	private boolean displayMode = true;
	private boolean editMode = false;

	public List<Property> getLstProperties()
	{
		return new ArrayList<Property>(AppProperties.getLstProperties().values());
	}

	public List<Property> getLstPropertiesFiltered()
	{
		return lstPropertiesFiltered;
	}

	public void setLstPropertiesFiltered(List<Property> lstPropertiesFiltered)
	{
		this.lstPropertiesFiltered = lstPropertiesFiltered;
	}

	public Property getProperty()
	{
		return property;
	}

	public void setProperty(Property property)
	{
		this.property = property;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	public List<String> getLstValues()
	{
		return lstValues;
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
		if (!SecurityUtils.isGranted(request, Permissions.PERMISSION_SE_PROPERTIES, Permissions.LEVEL_READ_ONLY))
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
		//
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
		value = property.getValue();

		if (property.getSetValues() != null && !property.getSetValues().isEmpty())
		{
			lstValues = Arrays.asList(property.getSetValues().split("\\s*;\\s*"));
		}

		displayMode = false;
		editMode = true;
	}

	public void undoEdit()
	{
		value = AppProperties.getLstProperties().get(property.getName()).getValue();
	}

	public void cancel()
	{
		property = null;
		lstValues = null;

		displayMode = true;
		editMode = false;
	}

	public boolean validate()
	{
		return true;
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

			property.setValue(value);

			SePropertiesService service = new SePropertiesService(context);
			service.setSeProperty(property);
			if (context.hasErrorOrFatal())
			{
				return;
			}

			property = null;

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
