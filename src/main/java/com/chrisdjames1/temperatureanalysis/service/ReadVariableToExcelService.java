package com.chrisdjames1.temperatureanalysis.service;

import com.chrisdjames1.temperatureanalysis.model.cdm.dataaccesslayer.CdmAttribute;
import com.chrisdjames1.temperatureanalysis.model.cdm.tx.CdmDataAccessLayerTranslator;
import com.chrisdjames1.temperatureanalysis.service.excel.AttributesExcelWriter;
import com.chrisdjames1.temperatureanalysis.util.ShapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.IndexIterator;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ReadVariableToExcelService {

    private final NetcdReaderService netcdReaderService;

    public ReadVariableToExcelService(NetcdReaderService netcdReaderService) {
        this.netcdReaderService = netcdReaderService;
    }

    /**
     * Reads a 1D or 2D query on an NC file into to a table in an XLSX file.
     *
     * @param ncFile           A {@link NetcdfFile} instance.
     * @param variableName     Name of the target variable in the {@link NetcdfFile}.
     * @param sectionSpec      1D or 2D configured CSV ranges for the target variable e.g "0:2073,141:142,0:0".
     * @param columnIndexFor1d Index of the dimension to use as the label for the value column when only one dimension
     *                         has a range in the {@code sectionSpec}. Cannot be the same as the index of the dimension
     *                         that has the range. Has no effect on the data output - just affects the column label. For
     *                         example, if {@code sectionSpec} is "0:2073,141:141,32:32" then the rows will represent
     *                         the first dimension (index 0) because that dimension has range 0:2073. If
     *                         {@code columnIndexFor1d} is 1 then the columns will be titled with the name of the second
     *                         dimension (e.g. latitude) and the column header will be the value of that dimension, i.e
     *                         "141".
     * @param fileName         (optional) Name to give the generated XLSX file.
     * @return The path to the generated XLSX file.
     */
    public String readVariable2dToExcel(NetcdfFile ncFile, String variableName, String sectionSpec,
            @Nullable Integer columnIndexFor1d, @Nullable String fileName) {

        Array data = netcdReaderService.readVariableToArray(ncFile, variableName, sectionSpec);
        int[] shape = data.getShape();
        int shapeDimensionCount = ShapeUtils.countShapeDimensions(shape);

        if (shapeDimensionCount < 1 || shapeDimensionCount > 2) {
            throw new IllegalArgumentException("Shape is not 1D or 2D. Excel output only supports 1D or 2D data.");
        }
        if (shapeDimensionCount == 1 && shape.length > 2 && columnIndexFor1d == null) {
            throw new IllegalArgumentException("Shape is 1D but there are more than 2 dimensions and no " +
                    "column-index-for-1D was provided. Please specify the index from the section-spec to use as the " +
                    "column in the 1D dataset.");
        }

        String[] sectionSpecArr = sectionSpec.split(",");
        List<Integer> sectionStarts = new ArrayList<>();
        for (String section : sectionSpecArr) {
            String[] sectionRange = section.split(":");
            int start = Integer.parseInt(sectionRange[0]);
            sectionStarts.add(start);
        }

        Variable v = Objects.requireNonNull(ncFile.findVariable(variableName));
        List<CdmAttribute> attributes = CdmDataAccessLayerTranslator.translateAttributes(v.attributes());

        int rowCategoryIndex = ShapeUtils.findNthShapeDimensionIndex(shape, 1);
        if (rowCategoryIndex == -1) {
            throw new IllegalStateException("Unable to locate row category index.");
        } else if (shapeDimensionCount == 1 && columnIndexFor1d != null && rowCategoryIndex == columnIndexFor1d) {
            throw new IllegalArgumentException("1D row data identified in index " + rowCategoryIndex + " which is " +
                    "the same as the column-index-for-1D. Please provide a different value for column-index-for-1D " +
                    "or change the configuration of the section-spec.");
        }

        if (columnIndexFor1d == null && shape.length == 2 && shapeDimensionCount == 1) {
            // Support null columnIndexFor1D when there are only 2 dimensions
            columnIndexFor1d = rowCategoryIndex == 0 ? 1 : 0;
        } else if (columnIndexFor1d == null && shape.length == 1) {
            // Support single dimension data
            columnIndexFor1d = 1; // We will add a "Value" dimension below for the column
        }

        assert columnIndexFor1d != null;

        int columnCategoryIndex = shapeDimensionCount == 2 ?
                ShapeUtils.findNthShapeDimensionIndex(shape, 2) : columnIndexFor1d;
        if (columnCategoryIndex == -1) {
            throw new IllegalStateException("Unable to locate header category index.");
        }
        List<Dimension> dimensions = new ArrayList<>(v.getDimensions());
        if (shape.length == 1) {
            if (dimensions.size() != 1) {
                throw new IllegalStateException("Unexpected state: shape length 1 and dimensions size != 1");
            }
            // Need to fake a dimension for the "Value" column
            dimensions.add(new Dimension("", 1));
        }

        String rowCategory = dimensions.get(rowCategoryIndex).getName();
        String columnCategory = dimensions.get(columnCategoryIndex).getName();

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(variableName);

            // Print the attributes at the top of the sheet
            AttributesExcelWriter attributesExcelWriter = AttributesExcelWriter.builder().workbook(workbook)
                    .sheet(sheet).attributes(attributes).rowCount(0).build();
            int rowCount = attributesExcelWriter.write();
            XSSFFont headerFont = attributesExcelWriter.getHeaderFont();
            CellStyle attrHeaderStyle = attributesExcelWriter.getAttrHeaderStyle();
            CellStyle attrStyle = attributesExcelWriter.getAttrStyle();

            if (shape.length > 2) {

                // blank row
                rowCount++;

                // 2D data with more than 2 variable dimensions to pick from.
                // List the fixed dimensions
                Row fixedDimensionsHeader = sheet.createRow(rowCount++);
                Cell fixedDimensionHeaderCell = fixedDimensionsHeader.createCell(0);
                fixedDimensionHeaderCell.setCellValue("Fixed Dimension");
                fixedDimensionHeaderCell.setCellStyle(attrHeaderStyle);
                Cell fixedDimensionValueHeaderCell = fixedDimensionsHeader.createCell(1);
                fixedDimensionValueHeaderCell.setCellValue("Value");
                fixedDimensionValueHeaderCell.setCellStyle(attrHeaderStyle);
                for (int i = 0; i < dimensions.size(); i++) {
                    if (i != rowCategoryIndex && i != columnCategoryIndex) {
                        Row fixedDimension = sheet.createRow(rowCount++);
                        Cell fixedDimensionNameCell = fixedDimension.createCell(0);
                        fixedDimensionNameCell.setCellValue(dimensions.get(i).getName());
                        fixedDimensionNameCell.setCellStyle(attrStyle);
                        Cell fixedDimensionValueCell = fixedDimension.createCell(1);
                        fixedDimensionValueCell.setCellValue(sectionStarts.get(i));
                        fixedDimensionValueCell.setCellStyle(attrStyle);
                    }
                }
            }

            // blank row
            rowCount++;

            // Print the column headers
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setFont(headerFont);

            if (columnCategory != null && !columnCategory.equals("")) {
                Row columnCategoryHeader = sheet.createRow(rowCount++);
                Cell columnCategoryCell = columnCategoryHeader.createCell(1);
                columnCategoryCell.setCellValue(columnCategory);
                columnCategoryCell.setCellStyle(headerStyle);
            }

            Row header = sheet.createRow(rowCount++);
            Cell headerCell = header.createCell(0);
            headerCell.setCellValue(rowCategory);
            headerCell.setCellStyle(headerStyle);

            // Print the column category labels
            int max = shapeDimensionCount > 1 ? shape[columnCategoryIndex] : 1;
            for (int col = 0; col < max; col++) {
                headerCell = header.createCell(col + 1);
                if (sectionStarts.size() >= columnCategoryIndex + 1) {
                    headerCell.setCellValue(sectionStarts.get(columnCategoryIndex) + col);
                } else {
                    headerCell.setCellValue("Value");
                }
                headerCell.setCellStyle(headerStyle);
            }

            Map<Integer, Row> dataRows = new HashMap<>();

            // Print the row category labels
            for (int row = 0; row < shape[rowCategoryIndex]; row++) {
                Row dataRow = dataRows.computeIfAbsent(row + rowCount, sheet::createRow);
                Cell rowCategoryCell = dataRow.createCell(0);
                rowCategoryCell.setCellValue(sectionStarts.get(rowCategoryIndex) + row);
                rowCategoryCell.setCellStyle(headerStyle);
            }

            // Print the data
            IndexIterator ixIter = data.getIndexIterator();

            DataType dataType = data.getDataType();
            while (ixIter.hasNext()) {
                Object next = ixIter.next();
                int[] counter = ixIter.getCurrentCounter();
                int rowOffset = counter[rowCategoryIndex];
                int colOffset = counter.length >= columnCategoryIndex + 1 ? counter[columnCategoryIndex] : 0;
                Row dataRow = dataRows.get(rowOffset + rowCount);
                Cell dataCell = dataRow.createCell(colOffset + 1);

                // NaN converts to the Excel error value #NUM!
                switch (dataType) {
                    case DOUBLE:
                        dataCell.setCellValue((Double) next);
                        break;
                    case FLOAT:
                        dataCell.setCellValue(((Float) next).doubleValue());
                        break;
                    case STRING:
                        dataCell.setCellValue((String) next);
                        break;
                    default:
                        throw new UnsupportedOperationException("Data type not yet implemented: " + dataType);
                }
            }

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
