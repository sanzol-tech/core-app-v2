package sanzol.se.web.controllers;

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
import sanzol.se.model.entities.SeError;
import sanzol.se.services.SeErrorsService;
import sanzol.util.DateRangeUtils;

@Named
@ViewScoped
public class SeErrorsController implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final String THIS_PAGE = "seErrors";

	private List<SelectItem> lstDateRanges;
	private String dateRange;

	private List<SeError> lstSeErrors;
	private SeError seError;

	private String jsonText;

	public List<SeError> getLstSeErrors()
	{
		return lstSeErrors;
	}

	public SeError getSeError()
	{
		return seError;
	}

	public void setSeError(SeError seError)
	{
		this.seError = seError;
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

	public String getJsonText()
	{
		return jsonText;
	}

	public void setJsonText(String jsonText)
	{
		this.jsonText = jsonText;
	}

	public void pageLoad()
	{
		HttpServletRequest request = FacesUtils.getRequest();
		if (!SecurityUtils.isGranted(request, Permissions.PERMISSION_SE_ERRORS, Permissions.LEVEL_READ_ONLY))
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
		lstDateRanges = DateRangeUtils.getRanges(12, 10, 20, 30);
		dateRange = lstDateRanges.get(0).getValue().toString();
	}

	private void loadGrid(RequestContext context)
	{
		DateRangeUtils.Period period = DateRangeUtils.getPeriod(dateRange);
		LocalDateTime _dateFrom = period.getBegin();
		LocalDateTime _dateTo = period.getEnd();

		SeErrorsService service = new SeErrorsService(context);
		lstSeErrors = service.getSeErrors(_dateFrom, _dateTo);
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

}
