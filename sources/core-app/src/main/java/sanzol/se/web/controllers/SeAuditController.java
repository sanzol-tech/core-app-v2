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
import sanzol.se.model.entities.SeAudit;
import sanzol.se.model.entities.SeUser;
import sanzol.se.services.SeAuditService;
import sanzol.util.DateRangeUtils;

@Named
@ViewScoped
public class SeAuditController implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final String THIS_PAGE = "seAudit";

	private List<SelectItem> lstDateRanges;
	private String dateRange;

	private List<SeAudit> lstSeAudit;
	private List<SeAudit> lstFilteredSeAudit;

	private SeUser seUser;

	private String jsonText;

	public List<SeAudit> getLstSeAudit()
	{
		return lstSeAudit;
	}

	public List<SeAudit> getLstFilteredSeAudit()
	{
		return lstFilteredSeAudit;
	}

	public void setLstFilteredSeAudit(List<SeAudit> lstFilteredSeAudit)
	{
		this.lstFilteredSeAudit = lstFilteredSeAudit;
	}

	public SeUser getSeUser()
	{
		return seUser;
	}

	public void setSeUser(SeUser seUser)
	{
		this.seUser = seUser;
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
		if (!SecurityUtils.isGranted(request, Permissions.PERMISSION_SE_AUDIT, Permissions.LEVEL_READ_ONLY))
		{
			SecurityUtils.redirectAccessDenied(request, FacesUtils.getResponse());
			return;
		}

		RequestContext context = RequestContext.createContext(request);
		try
		{
			if (SecurityUtils.isGranted(Permissions.PERMISSION_SE_AUDIT_ALL, Permissions.LEVEL_READ_ONLY))
			{
				seUser = null;
			}
			else
			{
				seUser = context.getSeUser();
			}

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

		SeAuditService service = new SeAuditService(context);
		lstSeAudit = service.getSeAudit(seUser, _dateFrom, _dateTo);
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
