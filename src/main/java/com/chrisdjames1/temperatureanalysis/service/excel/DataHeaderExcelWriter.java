package com.chrisdjames1.temperatureanalysis.service.excel;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class DataHeaderExcelWriter extends ExcelWriter {

    private final String rowCategory;
    private final String columnCategory;
    @Getter
    private Row header;

    @Builder
    public DataHeaderExcelWriter(@NonNull String rowCategory, @NonNull String columnCategory, @NonNull Integer rowCount,
            @NonNull ExcelWriter excelWriter) {
        super(rowCount, excelWriter);
        this.rowCategory = rowCategory;
        this.columnCategory = columnCategory;
    }

    @Override
    public int write() {
        // Print the data headers:
        //                | [Column Category]
        // [Row Category] |
        if (StringUtils.isNotEmpty(columnCategory)) {
            Row columnCategoryHeader = sheet.createRow(rowCount++);
            Cell columnCategoryCell = columnCategoryHeader.createCell(1);
            columnCategoryCell.setCellValue(columnCategory);
            columnCategoryCell.setCellStyle(headerStyle);
        }
        header = sheet.createRow(rowCount++);
        Cell headerCell = header.createCell(0);
        headerCell.setCellValue(rowCategory);
        headerCell.setCellStyle(headerStyle);
        return rowCount;
    }
}
