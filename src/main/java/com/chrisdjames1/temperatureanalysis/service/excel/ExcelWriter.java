package com.chrisdjames1.temperatureanalysis.service.excel;

import lombok.Getter;
import lombok.NonNull;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.lang.Nullable;

public abstract class ExcelWriter {
    @NonNull
    protected final Sheet sheet;
    protected int rowCount;
    @Getter
    protected XSSFFont headerFont;
    @Getter
    protected XSSFFont dataFont;
    @Getter
    protected CellStyle attrHeaderStyle;
    @Getter
    protected CellStyle attrStyle;

    protected ExcelWriter(@NonNull XSSFWorkbook workbook, @NonNull Sheet sheet, int rowCount,
            @Nullable XSSFFont headerFont, @Nullable XSSFFont dataFont, @Nullable CellStyle attrHeaderStyle,
            @Nullable CellStyle attrStyle) {
        this.sheet = sheet;
        this.rowCount = rowCount;
        this.headerFont = headerFont;
        this.dataFont = dataFont;
        this.attrHeaderStyle = attrHeaderStyle;
        this.attrStyle = attrStyle;
        if (this.headerFont == null) {
            // create default header font
            this.headerFont = workbook.createFont();
            this.headerFont.setFontName("Arial");
            this.headerFont.setFontHeightInPoints((short) 11);
            this.headerFont.setBold(true);
        }
        if (this.dataFont == null) {
            // create default data font
            this.dataFont = workbook.createFont();
            this.dataFont.setFontName("Arial");
            this.dataFont.setFontHeightInPoints((short) 11);
            this.dataFont.setBold(false);
        }
        if (this.attrHeaderStyle == null) {
            // create default attribute header style
            this.attrHeaderStyle = workbook.createCellStyle();
            this.attrHeaderStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            this.attrHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            this.attrHeaderStyle.setFont(this.headerFont);
        }
        if (this.attrStyle == null) {
            // create default attribute style
            this.attrStyle = workbook.createCellStyle();
            this.attrStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            this.attrStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            this.attrStyle.setFont(this.dataFont);
        }
    }

    public abstract int write();
}
