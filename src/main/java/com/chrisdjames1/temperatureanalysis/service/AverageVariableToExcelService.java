package com.chrisdjames1.temperatureanalysis.service;

import com.chrisdjames1.temperatureanalysis.model.cdm.dataaccesslayer.CdmAttribute;
import com.chrisdjames1.temperatureanalysis.model.cdm.tx.CdmDataAccessLayerTranslator;
import com.chrisdjames1.temperatureanalysis.service.excel.AttributesExcelWriter;
import com.chrisdjames1.temperatureanalysis.service.excel.DataHeaderExcelWriter;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import ucar.ma2.Array;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class AverageVariableToExcelService {
    private final NetcdReaderService netcdReaderService;

    public AverageVariableToExcelService(NetcdReaderService netcdReaderService) {
        this.netcdReaderService = netcdReaderService;
    }

    // TODO: Add support for calculating the means across n-1 dimensions. A designated dimension becomes the rows
    //  with the other dimensions being averaged. For example - changing 2D to 1D average - for a sectionSpec
    //  "0:2073,140:143,0:0", support a new parameter averageOnIndex. With it having a value of 0, time (0:2073)
    //  would be the rows and the averages are taken from the rest of the data for each time unit.
    //  Another example, for a sectionSpec "0:2073,0:180,0:360", setting averageOnIndex=1 would tabulate rows as
    //  latitude vs. average temperature at that entire latitude (because longitude is full range 0:360) across the
    //  time units 0:2073.

    public String averageVariableToExcel(NetcdfFile ncFile, String variableName, String sectionSpec,
            @Nullable Integer averageOnIndex, @Nullable String fileName) {

        Array data = netcdReaderService.readVariableToArray(ncFile, variableName, sectionSpec);
        int[] shape = data.getShape();
        if (shape.length <= 1) {
            throw new UnsupportedOperationException("Currently only shapes of length 2 or greater are supported.");
        }
        String[] sectionSpecArr = sectionSpec.split(",");

        if (averageOnIndex != null) {
            if (shape.length < averageOnIndex + 1) {
                throw new IndexOutOfBoundsException(
                        String.format("averageOnIndex (%d) exceeds the number of elements in shape (%d)",
                                averageOnIndex, shape.length));
            }
        } else {
            averageOnIndex = 0;
        }

        List<Integer> sectionStarts = new ArrayList<>();
        for (String section : sectionSpecArr) {
            String[] sectionRange = section.split(":");
            int start = Integer.parseInt(sectionRange[0]);
            sectionStarts.add(start);
        }

        Variable v = Objects.requireNonNull(ncFile.findVariable(variableName));
        List<CdmAttribute> attributes = CdmDataAccessLayerTranslator.translateAttributes(v.attributes());
        List<Dimension> dimensions = new ArrayList<>(v.getDimensions());
        int rowCategoryIndex = averageOnIndex;
        String rowCategory = dimensions.get(rowCategoryIndex).getName();
        String columnCategory = String.format("Avg %s", sectionSpec); // TODO? Improve the column category name

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(variableName);

            // Print the attributes at the top of the sheet
            AttributesExcelWriter attributesExcelWriter = AttributesExcelWriter.builder().workbook(workbook)
                    .sheet(sheet).attributes(attributes).rowCount(0).build();
            int rowCount = attributesExcelWriter.write();
            CellStyle headerStyle = attributesExcelWriter.getHeaderStyle();

            // blank row
            rowCount++;

            DataHeaderExcelWriter dataHeaderExcelWriter = DataHeaderExcelWriter.builder().rowCategory(rowCategory)
                    .columnCategory(columnCategory).rowCount(rowCount).excelWriter(attributesExcelWriter).build();
            rowCount = dataHeaderExcelWriter.write();

            // Print the column header for the values column
            Cell columnHeaderCell = dataHeaderExcelWriter.getHeader().createCell(1);
            columnHeaderCell.setCellValue("Value");
            columnHeaderCell.setCellStyle(headerStyle);

            // TODO: Print the row category labels

            // TODO: Average and print data

            // Write the content to a temporary file
            File currDir = new File(".");
            String path = currDir.getAbsolutePath();
            String fileLocation = path.substring(0, path.length() - 1) +
                    (StringUtils.isNotEmpty(fileName) ? fileName : "temp.xlsx");

            FileOutputStream outputStream = new FileOutputStream(fileLocation);
            workbook.write(outputStream);

            return fileLocation;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
