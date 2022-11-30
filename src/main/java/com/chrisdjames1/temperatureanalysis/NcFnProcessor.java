package com.chrisdjames1.temperatureanalysis;

import com.chrisdjames1.temperatureanalysis.model.cdm.dataaccesslayer.CdmAttribute;
import com.chrisdjames1.temperatureanalysis.model.cdm.tx.CdmDataAccessLayerTranslator;
import com.chrisdjames1.temperatureanalysis.model.value.AppFunction;
import com.chrisdjames1.temperatureanalysis.model.value.FnAvgVariableArg;
import com.chrisdjames1.temperatureanalysis.model.value.FnReadVariableArg;
import com.chrisdjames1.temperatureanalysis.util.ShapeUtils;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.lang.Nullable;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;
import ucar.nc2.write.Ncdump;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class NcFnProcessor {

    private boolean doLog = true;

    public void openFileAndProcessFunction(String path, AppFunction function, Map<String, String> fnArgs) {
        try (NetcdfFile ncFile = NetcdfFiles.open(path)) {
            processFunctionToString(ncFile, function, fnArgs);
        } catch (IOException e) {
            throw new RuntimeException("Could not open " + path, e);
        }
    }

    public String processFunctionToString(NetcdfFile ncFile, AppFunction function, Map<String, String> fnArgs) {

        switch(function) {
            case READ_ROOT_GROUP:
                String rootGroup = ncFile.getRootGroup().toString();
                info("Root group: {}", rootGroup);
                return rootGroup;
            case READ_VARIABLE:
                return readVariableToJsonArrayString(ncFile, fnArgs);
            case AVG_VARIABLE:
                return avgVariable(ncFile, fnArgs);
            default:
                throw new RuntimeException("Unsupported function: " + function.name());
        }
    }

    private String readVariableToJsonArrayString(NetcdfFile ncFile, Map<String, String> fnArgs) {

        Array data = readVariableToArray(ncFile, fnArgs);
        String arrayStr = Ncdump.printArray(data, null, null).replaceAll("\\{", "[")
                .replaceAll("}", "]");
        info(arrayStr);

        return arrayStr;
    }

    public String readVariable2dToExcel(NetcdfFile ncFile, String variableName, String sectionSpec,
            @Nullable Integer columnIndexFor1d, @Nullable String fileName) {

        Array data = readVariableToArray(ncFile, variableName, sectionSpec);

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

            int rowCount = 0;

            // Print the attributes at the top fo the sheet

            CellStyle attrHeaderStyle = workbook.createCellStyle();
            attrHeaderStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            attrHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            XSSFFont headerFont = workbook.createFont();
            headerFont.setFontName("Arial");
            headerFont.setFontHeightInPoints((short) 11);
            headerFont.setBold(true);
            attrHeaderStyle.setFont(headerFont);

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

            CellStyle attrStyle = workbook.createCellStyle();
            attrStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            attrStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            XSSFFont dataFont = workbook.createFont();
            dataFont.setFontName("Arial");
            dataFont.setFontHeightInPoints((short) 11);
            dataFont.setBold(false);
            attrStyle.setFont(dataFont);

            for (CdmAttribute attr : attributes) {

                Row attrRow = sheet.createRow(rowCount++);
                Cell attrCell = attrRow.createCell(0);
                attrCell.setCellValue(attr.getName());
                attrCell.setCellStyle(attrStyle);
                attrCell = attrRow.createCell(1);
                attrCell.setCellValue(attr.getDataType().toString());
                attrCell.setCellStyle(attrStyle);
                attrCell = attrRow.createCell(2);
                attrCell.setCellValue(attr.getValues().stream().map(Object::toString).collect(Collectors.joining(", ")));
                attrCell.setCellStyle(attrStyle);
            }

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
                    (fileName != null && !fileName.equals("") ? fileName : "temp.xlsx");

            FileOutputStream outputStream = new FileOutputStream(fileLocation);
            workbook.write(outputStream);

            return fileLocation;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Array readVariableToArray(NetcdfFile ncFile, Map<String, String> fnArgs) {
        String varName = Objects.requireNonNull(fnArgs.get(FnReadVariableArg.VARIABLE.getArg()),
                String.format("Missing argument '%s' for function '%s'", FnReadVariableArg.VARIABLE.getArg(),
                        AppFunction.READ_VARIABLE.getFunctionArgValue()));
        String sectionSpec = Objects.requireNonNull(fnArgs.get(FnReadVariableArg.SECTION_SPEC.getArg()),
                String.format("Missing argument '%s' for function '%s'", FnReadVariableArg.SECTION_SPEC.getArg(),
                        AppFunction.READ_VARIABLE.getFunctionArgValue()));

        return readVariableToArray(ncFile, varName, sectionSpec);
    }

    public Array readVariableToArray(NetcdfFile ncFile, String varName, String sectionSpec) {
        info("Attempting to read variable '{}' with section-spec '{}'", varName, sectionSpec);

        Variable v = ncFile.findVariable(varName);
        if (v == null) {
            throw new RuntimeException("Unable fo find variable " + varName);
        }
        try {
            // sectionSpec is string specifying a potentially multidimensional array range of data, eg ":,1:2,0:3"
            return v.read(sectionSpec);
        } catch (IOException | InvalidRangeException e) {
            throw new RuntimeException("Error reading variable " + varName, e);
        }
    }
    
    private String avgVariable(NetcdfFile ncFile, Map<String, String> fnArgs) {

        String varName = Objects.requireNonNull(fnArgs.get(FnAvgVariableArg.VARIABLE.getArg()),
                String.format("Missing argument '%s' for function '%s'", FnAvgVariableArg.VARIABLE.getArg(),
                        AppFunction.READ_VARIABLE.getFunctionArgValue()));
        String sectionSpec = Objects.requireNonNull(fnArgs.get(FnAvgVariableArg.SECTION_SPEC.getArg()),
                String.format("Missing argument '%s' for function '%s'", FnAvgVariableArg.SECTION_SPEC.getArg(),
                        AppFunction.READ_VARIABLE.getFunctionArgValue()));

        info("Attempting to average variable '{}' with section-spec '{}'", varName, sectionSpec);
        Variable v = ncFile.findVariable(varName);
        if (v == null) {
            throw new RuntimeException("Unable fo find variable " + varName);
        }
        try {
            Array data = v.read(sectionSpec);
            DataType dataType = data.getDataType();

            IndexIterator ixIter = data.getIndexIterator();
            double avg;

            if (dataType.isIntegral()) {
                avg = averageIntegrals(ixIter);
            } else if (dataType.isFloatingPoint()) {
                avg = averageFloatingPoints(ixIter, dataType);
            } else {
                throw new IllegalStateException("Cannot average data type: " + dataType);
            }
            info("Average: " + avg);
            return String.valueOf(avg);
        } catch (IOException | InvalidRangeException e) {
            throw new RuntimeException("Error averaging variable " + varName, e);
        }
    }

    private double averageIntegrals(IndexIterator ixIter) {
        long total = 0L;
        long count = 0L;
        while (ixIter.hasNext()) {
            total += (long) ixIter.next();
            count++;
        }
        return total / (double) count;
    }

    private double averageFloatingPoints(IndexIterator ixIter, DataType dataType) {
        double total = 0;
        long count = 0L;
        switch (dataType) {
            case DOUBLE:
                while (ixIter.hasNext()) {
                    double dbl = (double) ixIter.next();
                    if (!Double.isNaN(dbl)) {
                        total += dbl;
                        count++;
                    }
                }
                break;
            case FLOAT:
                while (ixIter.hasNext()) {
                    float flt = (float) ixIter.next();
                    if (!Float.isNaN(flt)) {
                        total += flt;
                        count ++;
                    }
                }
                break;
            default:
                throw new RuntimeException("Illegal data type: " + dataType);
        }
        return total / count;
    }

    private void info(String message, Object... args) {
        if (doLog) log.info(message, args);
    }

}
