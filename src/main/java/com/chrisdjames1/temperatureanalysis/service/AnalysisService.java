package com.chrisdjames1.temperatureanalysis.service;

import com.chrisdjames1.temperatureanalysis.AppFunction;
import com.chrisdjames1.temperatureanalysis.FnReadVariableArg;
import com.chrisdjames1.temperatureanalysis.NcFnProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class AnalysisService {

    // TODO: Move to properties
    private static final String PATH = "C:/Users/Chris James/Downloads/Land_and_Ocean_LatLong1.nc";

    public String readVariable(String variableName, String sectionSpec) {

        var firstFileReader = NcFnProcessor.builder().path(PATH).build();
        return firstFileReader.process(AppFunction.READ_VARIABLE,
                Map.of(FnReadVariableArg.VARIABLE.getArg(), variableName,
                        FnReadVariableArg.SECTION_SPEC.getArg(), sectionSpec));
    }

}
