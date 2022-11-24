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

    private final NetcdFileService netcdFileService;

    public AnalysisService(NetcdFileService netcdFileService) {
        this.netcdFileService = netcdFileService;
    }

    public String readVariable(String variableName, String sectionSpec) {

        var firstFileReader = new NcFnProcessor(false);
        return firstFileReader.processFunction(netcdFileService.getNcFile(), AppFunction.READ_VARIABLE,
                Map.of(FnReadVariableArg.VARIABLE.getArg(), variableName,
                        FnReadVariableArg.SECTION_SPEC.getArg(), sectionSpec));
    }

}
