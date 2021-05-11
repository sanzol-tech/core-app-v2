package sanzol.se.web.controllers;

import static sanzol.app.config.I18nPreference.getI18nString;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.omnifaces.cdi.ViewScoped;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sanzol.app.config.Permissions;
import sanzol.se.audit.AuditService;
import sanzol.se.commons.FacesUtils;
import sanzol.se.commons.SecurityUtils;
import sanzol.se.context.RequestContext;
import sanzol.se.model.entities.SeSector;
import sanzol.se.model.entities.SeSectorUser;
import sanzol.se.model.entities.SeUser;
import sanzol.se.services.SeSectorsService;
import sanzol.se.services.SeUsersService;
import sanzol.util.CaseUtils;
import sanzol.util.DateTimeUtils;
import sanzol.util.ExceptionUtils;
import sanzol.util.PoiUtils;
import sanzol.util.validator.StringValidator;
import sanzol.util.validator.ValidationDisplay;

@Named
@ViewScoped
public class SeSectorsController implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final String THIS_PAGE = "seSectors";

	private static final String MESSAGE_NO_RECORD_SELECTED = getI18nString("message.noRecordSelected");

	private static final Logger LOG = LoggerFactory.getLogger(SeSectorsController.class);

	private TreeNode root;
	private SeSector seSector;

	private List<SeSector> lstSectorsPrev;
	private Integer sectorPrevId;

	private List<SeUser> lstSeUsers;
	private List<Integer> lstUserIdSel;

	private Boolean onlyActive = null;

	private boolean displayMode = true;
	private boolean editMode = false;

	public TreeNode getRoot()
	{
		return root;
	}

	public SeSector getSeSector()
	{
		return seSector;
	}

	public void setSeSector(SeSector seSector)
	{
		this.seSector = seSector;
	}

	public List<SeSector> getLstSectorsPrev()
	{
		return lstSectorsPrev;
	}

	public Integer getSectorPrevId()
	{
		return sectorPrevId;
	}

	public void setSectorPrevId(Integer sectorPrevId)
	{
		this.sectorPrevId = sectorPrevId;
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
		if (!SecurityUtils.isGranted(request, Permissions.PERMISSION_SE_SECTORS, Permissions.LEVEL_READ_ONLY))
		{
			SecurityUtils.redirectAccessDenied(request, FacesUtils.getResponse());
			return;
		}

		RequestContext context = RequestContext.createContext(request);
		try
		{
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

	private void loadSectores(RequestContext context, Integer excludeNSector)
	{
		SeSectorsService service = new SeSectorsService(context);
		lstSectorsPrev = service.getSeSectors(excludeNSector, true);

	}

	private void loadSeUsers(RequestContext context)
	{
		SeUsersService service = new SeUsersService(context);
		lstSeUsers = service.getSeUsers(null, null);
	}

	public void loadGrid(RequestContext context)
	{
		SeSectorsService service = new SeSectorsService(context);
		root = service.getTreeSectores();
	}

	private void loadSeUsersSel()
	{
		lstUserIdSel = new ArrayList<Integer>();
		for (SeSectorUser sectorUser : seSector.getLstSeSectorUsers())
		{
			lstUserIdSel.add(sectorUser.getSeUser().getUserId());
		}
	}

	public void expandAll()
	{
		for (TreeNode node : root.getChildren())
		{
			node.setExpanded(true);
		}
	}

	public void colapseAll()
	{
		for (TreeNode node : root.getChildren())
		{
			node.setExpanded(false);
		}
	}

	public void add()
	{
		RequestContext context = RequestContext.createContext();
		try
		{
			seSector = new SeSector();
			seSector.setIsActive(true);

			sectorPrevId = null;

			loadSectores(context, null);

			lstUserIdSel = new ArrayList<Integer>();

			displayMode = false;
			editMode = true;
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
		if (seSector == null || seSector.getSectorId() == null)
		{
			FacesUtils.addMessageFatal(MESSAGE_NO_RECORD_SELECTED);
			return;
		}

		RequestContext context = RequestContext.createContext();
		try
		{
			SeSectorsService service = new SeSectorsService(context);
			seSector = service.getSeSector(seSector.getSectorId());
			if (context.hasErrorOrFatal())
			{
				return;
			}

			sectorPrevId = seSector.getSeSectorPrev() != null ? seSector.getSeSectorPrev().getSectorId() : null;

			loadSectores(context, seSector.getSectorId());
			loadSeUsersSel();

			displayMode = false;
			editMode = true;

			// ----- Audit -----------------------------------------------------------------------------------------
			AuditService.auditSelect(context, THIS_PAGE, seSector.getSectorId().toString(), null, null);
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
		if (seSector.getSectorId() == null)
			add();
		else
			edit();
	}

	public void cancel()
	{
		sectorPrevId = null;

		seSector = null;

		displayMode = true;
		editMode = false;
	}

	private static SeSector search(List<SeSector> list, Integer id)
	{
		if (id != null && list != null && !list.isEmpty())
		{
			for (SeSector entity : list)
			{
				if (id.equals(entity.getSectorId()))
				{
					return entity;
				}
			}
		}
		return null;
	}

	public void normalize()
	{
		seSector.setName(CaseUtils.trim(seSector.getName()));
	}

	public boolean validate()
	{
		return ValidationDisplay.isValid(
				StringValidator.validate("name", "{seSectors.field.name}", seSector.getName(), null, null, true)
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

			seSector.setSeSectorPrev(search(lstSectorsPrev, sectorPrevId));

			SeSectorsService service = new SeSectorsService(context);

			if (seSector.getSectorId() == null)
			{
				service.addSeSector(seSector, lstUserIdSel);
			}
			else
			{
				service.setSeSector(seSector, lstUserIdSel);
			}

			if (context.hasErrorOrFatal())
			{
				return;
			}
			sectorPrevId = null;

			seSector = null;

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
		if (seSector == null)
		{
			FacesUtils.addMessageFatal(MESSAGE_NO_RECORD_SELECTED);
			return;
		}

		RequestContext context = RequestContext.createContext();
		try
		{
			SeSectorsService service = new SeSectorsService(context);
			service.delSeSector(seSector);
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

	// --------------------------------------------------------------
	// --------------------------------------------------------------

	public void exportExcel()
	{
		FacesContext fc = FacesContext.getCurrentInstance();
		ExternalContext externalContext = fc.getExternalContext();

		externalContext.setResponseContentType("application/vnd.ms-excel");
		externalContext.setResponseHeader("Expires", "0");
		externalContext.setResponseHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
		externalContext.setResponseHeader("Pragma", "public");
		externalContext.setResponseHeader("Content-disposition", "attachment; filename=" + getExportExcelFileName());
		externalContext.addResponseCookie(org.primefaces.util.Constants.DOWNLOAD_COOKIE, "true", new HashMap<String, Object>());

		// ------------------------------------------------------------

		try (XSSFWorkbook wb = new XSSFWorkbook())
		{
			XSSFSheet sheet = wb.createSheet("Sectores");

			// ------------------------------------------------------------
			XSSFFont font = wb.createFont();
			font.setColor(IndexedColors.WHITE.getIndex());
			font.setBold(true);

			XSSFCellStyle cellStyle = wb.createCellStyle();
			cellStyle.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
			cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			cellStyle.setFont(font);

			// ------------------------------------------------------------
			RowNumber rowNumber = new RowNumber();
			PoiUtils.createRow(sheet, rowNumber.getValue(), cellStyle, "Sector");
			rowNumber.increase();

			// ------------------------------------------------------------
			generateRows(root.getChildren(), sheet, rowNumber, 0);

			XSSFRow header = sheet.getRow(0);
			for (int i = 0; i < header.getPhysicalNumberOfCells(); i++)
			{
				sheet.autoSizeColumn(i);
			}

			try (OutputStream out = externalContext.getResponseOutputStream())
			{
				wb.write(out);
			}
			catch (IOException ex)
			{
				FacesUtils.addMessage(ex);
			}

		}
		catch (IOException e)
		{
			LOG.error(ExceptionUtils.getMessage(e));
		}

		try
		{
			externalContext.responseFlushBuffer();
		}
		catch (IOException e)
		{
			LOG.error(ExceptionUtils.getMessage(e));
		}

		fc.responseComplete();
	}

	class RowNumber
	{
		int value = 0;

		public int getValue()
		{
			return value;
		}

		public void increase()
		{
			value++;
		}
	}

	private void generateRows(List<TreeNode> lstTreeNodes, XSSFSheet sheet, RowNumber rowNumber, int level)
	{
		for (TreeNode node : lstTreeNodes)
		{
			SeSector seSector = (SeSector) node.getData();
			PoiUtils.createRow(sheet, rowNumber.getValue(), null, getRowValues(level, seSector.getName()));
			rowNumber.increase();

			if (node.getChildCount() > 0)
			{
				generateRows(node.getChildren(), sheet, rowNumber, level + 1);
			}
		}
	}

	private Object[] getRowValues(int level, String value)
	{
		List<String> lst = new ArrayList<String>();
		for (int i = 0; i < level; i++)
			lst.add("");
		lst.add(value);
		return lst.toArray(new Object[0]);
	}

	private static String getExportExcelFileName()
	{
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMdd");
		return "sectores_" + formatter.format(DateTimeUtils.now()) + ".xlsx";
	}

}
