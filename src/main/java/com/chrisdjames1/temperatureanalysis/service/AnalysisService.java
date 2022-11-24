package com.chrisdjames1.temperatureanalysis.service;

import com.chrisdjames1.temperatureanalysis.NcFnProcessor;
import com.chrisdjames1.temperatureanalysis.model.value.AppFunction;
import com.chrisdjames1.temperatureanalysis.model.value.FnReadVariableArg;
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

        var processor = new NcFnProcessor(false);
        return processor.processFunction(netcdFileService.getNcFile(), AppFunction.READ_VARIABLE,
                Map.of(FnReadVariableArg.VARIABLE.getArg(), variableName,
                        FnReadVariableArg.SECTION_SPEC.getArg(), sectionSpec));
    }

    public String readRootGroup() {

        var processor = new NcFnProcessor(false);
        return processor.processFunction(netcdFileService.getNcFile(), AppFunction.READ_ROOT_GROUP, Map.of());
    }
}
