package com.chrisdjames1.temperatureanalysis.service;

import com.chrisdjames1.temperatureanalysis.NcFnProcessor;
import com.chrisdjames1.temperatureanalysis.model.cdm.dataaccesslayer.CdmAttribute;
import com.chrisdjames1.temperatureanalysis.model.cdm.dataaccesslayer.CdmDataset;
import com.chrisdjames1.temperatureanalysis.model.cdm.dataaccesslayer.CdmDimension;
import com.chrisdjames1.temperatureanalysis.model.cdm.dataaccesslayer.CdmEnumTypedef;
import com.chrisdjames1.temperatureanalysis.model.cdm.dataaccesslayer.CdmGroup;
import com.chrisdjames1.temperatureanalysis.model.cdm.dataaccesslayer.CdmVariable;
import com.chrisdjames1.temperatureanalysis.model.cdm.tx.CdmDataAccessLayerTranslator;
import com.chrisdjames1.temperatureanalysis.model.value.AppFunction;
import com.chrisdjames1.temperatureanalysis.model.value.FnReadVariableArg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AnalysisService {

    private final NetcdFileService netcdFileService;

    public AnalysisService(NetcdFileService netcdFileService) {
        this.netcdFileService = netcdFileService;
    }

    public String readVariableToString(String variableName, String sectionSpec) {

        var processor = new NcFnProcessor(false);
        return processor.processFunctionToString(netcdFileService.getNcFile(), AppFunction.READ_VARIABLE,
                createReadVariableArgs(variableName, sectionSpec));
    }

    public String readVariable2dToExcel(String variableName, String sectionSpec) {

        var processor = new NcFnProcessor(true);
        return processor.readVariable2DToExcel(netcdFileService.getNcFile(), variableName, sectionSpec);
    }

    public CdmGroup readRootGroupSchema() {

        NetcdfFile ncFile = netcdFileService.getNcFile();
        Group rootGroup = ncFile.getRootGroup();

        CdmDataset cdmDataset = CdmDataAccessLayerTranslator.translateNetcdfFile(ncFile);
        List<CdmDimension> cdmDimensions = CdmDataAccessLayerTranslator.translateDimensions(rootGroup.getDimensions());
        List<CdmEnumTypedef> cdmEnumTypedefs =
                CdmDataAccessLayerTranslator.translateEnumTypedefs(rootGroup.getEnumTypedefs());
        List<CdmVariable> cdmVariables = CdmDataAccessLayerTranslator.translateVariables(rootGroup.getVariables());
        List<CdmAttribute> cdmAttributes = CdmDataAccessLayerTranslator.translateAttributes(rootGroup.attributes());

        return CdmGroup.builder().name(rootGroup.getFullName()).dataset(cdmDataset).dimensions(cdmDimensions)
                .enumTypedefs(cdmEnumTypedefs).variables(cdmVariables).attributes(cdmAttributes).build();
    }

    public String readRootGroupRaw() {

        var processor = new NcFnProcessor(false);
        return processor.processFunctionToString(netcdFileService.getNcFile(), AppFunction.READ_ROOT_GROUP, Map.of());
    }

    private static Map<String, String> createReadVariableArgs(String variableName, String sectionSpec) {
        return Map.of(FnReadVariableArg.VARIABLE.getArg(), variableName,
                FnReadVariableArg.SECTION_SPEC.getArg(), sectionSpec);
    }
}
