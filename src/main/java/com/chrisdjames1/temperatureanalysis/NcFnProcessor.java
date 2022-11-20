package com.chrisdjames1.temperatureanalysis;

import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;
import ucar.nc2.write.Ncdump;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class NcFnProcessor {

    private final String path;

    @Builder
    public NcFnProcessor(@NonNull String path) {
        this.path = path;
    }

    public void process(AppFunction function, Map<String, String> fnArgs) {
        try (NetcdfFile ncFile = NetcdfFiles.open(path)) {
            if (null == function) {
                log.info("Root group: {}", ncFile.getRootGroup());
            } else {
                executeFunction(ncFile, function, fnArgs);
            }
        } catch (IOException ioe) {
            log.error(String.format("Could not open %s", path), ioe);
        }
    }

    private void executeFunction(NetcdfFile ncFile, AppFunction function, Map<String, String> fnArgs) {

        switch(function) {
            case READ_VARIABLE:
                readVariable(ncFile, fnArgs);
                break;
            case AVG_VARIABLE:
                avgVariable(ncFile, fnArgs);
                break;
            default:
                // TODO
        }

    }

    private void readVariable(NetcdfFile ncFile, Map<String, String> fnArgs) {

        String varName = Objects.requireNonNull(fnArgs.get(FnReadVariableArg.VARIABLE.getArg()),
                String.format("Missing argument '%s' for function '%s'", FnReadVariableArg.VARIABLE.getArg(),
                        AppFunction.READ_VARIABLE.getFunctionArgValue()));
        String sectionSpec = Objects.requireNonNull(fnArgs.get(FnReadVariableArg.SECTION_SPEC.getArg()),
                String.format("Missing argument '%s' for function '%s'", FnReadVariableArg.SECTION_SPEC.getArg(),
                        AppFunction.READ_VARIABLE.getFunctionArgValue()));

        log.info("Attempting to read variable '{}' with section-spec '{}'", varName, sectionSpec);

        Variable v = ncFile.findVariable(varName);
        if (v == null) {
            log.warn("Unable to find variable '{}' in file.", varName);
            return;
        }
        try {
            // sectionSpec is string specifying a potentially multidimensional array range of data, eg ":,1:2,0:3"
            Array data = v.read(sectionSpec);
            String arrayStr = Ncdump.printArray(data, varName, null);
            log.info(arrayStr);
        } catch (IOException | InvalidRangeException e) {
            log.error("Error reading variable " + varName, e);
        }
    }
    
    private void avgVariable(NetcdfFile ncFile, Map<String, String> fnArgs) {

        String varName = Objects.requireNonNull(fnArgs.get(FnAvgVariableArg.VARIABLE.getArg()),
                String.format("Missing argument '%s' for function '%s'", FnAvgVariableArg.VARIABLE.getArg(),
                        AppFunction.READ_VARIABLE.getFunctionArgValue()));
        String sectionSpec = Objects.requireNonNull(fnArgs.get(FnAvgVariableArg.SECTION_SPEC.getArg()),
                String.format("Missing argument '%s' for function '%s'", FnAvgVariableArg.SECTION_SPEC.getArg(),
                        AppFunction.READ_VARIABLE.getFunctionArgValue()));

        log.info("Attempting to average variable '{}' with section-spec '{}'", varName, sectionSpec);
        Variable v = ncFile.findVariable(varName);
        if (v == null) {
            log.warn("Unable to find variable '{}' in file.", varName);
            return;
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
            log.info("Average: " + avg);

        } catch (IOException | InvalidRangeException e) {
            log.error("Error averaging variable " + varName, e);
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


}
