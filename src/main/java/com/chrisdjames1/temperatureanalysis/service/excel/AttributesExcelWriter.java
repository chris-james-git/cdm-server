package com.chrisdjames1.temperatureanalysis.service.excel;

import com.chrisdjames1.temperatureanalysis.model.cdm.dataaccesslayer.CdmAttribute;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;
import java.util.stream.Collectors;

public class AttributesExcelWriter {

    @NonNull
    private final Sheet sheet;
    @NonNull
    private final List<CdmAttribute> attributes;
    private int rowCount;

    @Getter
    private XSSFFont headerFont;
    @Getter
    private XSSFFont dataFont;
    @Getter
    private CellStyle attrHeaderStyle;
    @Getter
    private CellStyle attrStyle;

    @Builder
    public AttributesExcelWriter(@NonNull XSSFWorkbook workbook, @NonNull Sheet sheet,
            @NonNull List<CdmAttribute> attributes, int rowCount) {
        this.sheet = sheet;
        this.attributes = attributes;
        this.rowCount = rowCount;

        attrHeaderStyle = workbook.createCellStyle();
        attrHeaderStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        attrHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerFont = workbook.createFont();
        headerFont.setFontName("Arial");
        headerFont.setFontHeightInPoints((short) 11);
        headerFont.setBold(true);
        attrHeaderStyle.setFont(headerFont);

        attrStyle = workbook.createCellStyle();
        attrStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        attrStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        dataFont = workbook.createFont();
        dataFont.setFontName("Arial");
        dataFont.setFontHeightInPoints((short) 11);
        dataFont.setBold(false);
        attrStyle.setFont(dataFont);
    }

    public int write() {

        Row attrTitleRow = sheet.createRow(rowCount++);
        Cell attrTitleCell = attrTitleRow.createCell(0);
        attrTitleCell.setCellValue("Attributes");
        attrTitleCell.setCellStyle(attrHeaderStyle);

        Row attrHeaderRow = sheet.createRow(rowCount++);
        Cell attrHeaderCell = attrHeaderRow.createCell(0);
        attrHeaderCell.setCellValue("Name");
        attrHeaderCell.setCellStyle(attrHeaderStyle);
        attrHeaderCell = attrHeaderRow.createCell(1);
        attrHeaderCell.setCellValue("Data Type");
        attrHeaderCell.setCellStyle(attrHeaderStyle);
        attrHeaderCell = attrHeaderRow.createCell(2);
        attrHeaderCell.setCellValue("Value");
        attrHeaderCell.setCellStyle(attrHeaderStyle);

        for (CdmAttribute attr : attributes) {

            Row attrRow = sheet.createRow(rowCount++);
            Cell attrCell = attrRow.createCell(0);
            attrCell.setCellValue(attr.getName());
            attrCell.setCellStyle(attrStyle);
            attrCell = attrRow.createCell(1);
            attrCell.setCellValue(attr.getDataType().toString());
            attrCell.setCellStyle(attrStyle);
            attrCell = attrRow.createCell(2);
            attrCell.setCellValue(
                    attr.getValues().stream().map(Object::toString).collect(Collectors.joining(", ")));
            attrCell.setCellStyle(attrStyle);
        }

        return rowCount;
    }
}
