package com.chrisdjames1.temperatureanalysis.service.excel;

import com.chrisdjames1.temperatureanalysis.model.cdm.dataaccesslayer.CdmAttribute;
import lombok.Builder;
import lombok.NonNull;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class AttributesExcelWriter extends ExcelWriter {
    @NonNull
    private final List<CdmAttribute> attributes;

    @Builder
    public AttributesExcelWriter(@NonNull XSSFWorkbook workbook, @NonNull Sheet sheet,
            @NonNull List<CdmAttribute> attributes, int rowCount, @Nullable XSSFFont headerFont,
            @Nullable XSSFFont dataFont, @Nullable CellStyle attrHeaderStyle, @Nullable CellStyle attrStyle,
            @Nullable CellStyle headerStyle) {
        super(workbook, sheet, rowCount, headerFont, dataFont, attrHeaderStyle, attrStyle, headerStyle);
        this.attributes = attributes;
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
