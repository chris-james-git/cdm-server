package com.chrisdjames1.temperatureanalysis.service;

import com.chrisdjames1.temperatureanalysis.model.value.AppFunction;
import com.chrisdjames1.temperatureanalysis.model.value.FnReadVariableArg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.write.Ncdump;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class NetcdReaderService {

    public String readVariableToJsonArrayString(NetcdfFile ncFile, Map<String, String> fnArgs) {

        Array data = readVariableToArray(ncFile, fnArgs);
        String arrayStr = Ncdump.printArray(data, null, null).replaceAll("\\{", "[").replaceAll("}", "]");
        log.debug(arrayStr);

        return arrayStr;
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
        log.debug("Attempting to read variable '{}' with section-spec '{}'", varName, sectionSpec);

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
}
