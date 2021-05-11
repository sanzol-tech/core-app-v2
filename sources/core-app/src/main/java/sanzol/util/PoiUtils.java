/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: June 2020
 *
 */
package sanzol.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public final class PoiUtils
{

	private PoiUtils()
	{
		// Hide constructor
	}

	public static void postProcessXLS(Object document)
	{
		XSSFWorkbook wb = (XSSFWorkbook) document;
		XSSFSheet sheet = wb.getSheetAt(0);
		XSSFRow header = sheet.getRow(0);

		XSSFFont font = wb.createFont();
		font.setColor(IndexedColors.WHITE.getIndex());
		font.setBold(true);

		XSSFCellStyle cellStyle = wb.createCellStyle();
		cellStyle.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		cellStyle.setFont(font);

		for (int i = 0; i < header.getPhysicalNumberOfCells(); i++)
		{
			sheet.autoSizeColumn(i);

			XSSFCell cell = header.getCell(i);
			cell.setCellStyle(cellStyle);
		}
	}

	public static Row createRow(XSSFSheet sheet, int rownum, XSSFCellStyle cellStyle, Object... values)
	{
		Row row = sheet.createRow(rownum);

		for (int i = 0; i < values.length; i++)
		{
			Object item = values[i];

			Cell cell = row.createCell(i);

			if (item instanceof String)
			{
				cell.setCellValue((String) item);
				setStyle(cell, cellStyle);
			}
			else if (item instanceof Integer)
			{
				cell.setCellValue((Integer) item);
				setStyle(cell, cellStyle);
			}
			else if (item instanceof Long)
			{
				cell.setCellValue((Long) item);
				setStyle(cell, cellStyle);
			}
			else if (item instanceof Double)
			{
				cell.setCellValue((Double) item);
				setDoubleStyle(sheet.getWorkbook(), cell, cellStyle);
			}
		}

		return row;
	}

	private static void setStyle(Cell cell, XSSFCellStyle cellStyle)
	{
		if (cellStyle != null)
		{
			cell.setCellStyle(cellStyle);
		}
	}

	private static void setDoubleStyle(XSSFWorkbook workbook, Cell cell, XSSFCellStyle cellStyle)
	{
		if (cellStyle == null)
		{
			cellStyle = workbook.createCellStyle();
		}
		cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));

		cell.setCellStyle(cellStyle);
	}

}
