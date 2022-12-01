package com.chrisdjames1.temperatureanalysis.service;

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
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AnalysisService {

    private final NetcdFileService netcdFileService;
    private final ReadVariableToExcelService readVariableToExcelService;
    private final AverageVariableToExcelService averageVariableToExcelService;
    private final FunctionProcessorService functionProcessorService;

    public AnalysisService(NetcdFileService netcdFileService, ReadVariableToExcelService readVariableToExcelService,
            AverageVariableToExcelService averageVariableToExcelService,
            FunctionProcessorService functionProcessorService) {
        this.netcdFileService = netcdFileService;
        this.readVariableToExcelService = readVariableToExcelService;
        this.averageVariableToExcelService = averageVariableToExcelService;
        this.functionProcessorService = functionProcessorService;
    }

    public String readVariableToString(String variableName, String sectionSpec) {
        return functionProcessorService.processFunctionToString(netcdFileService.getNcFile(), AppFunction.READ_VARIABLE,
                createReadVariableArgs(variableName, sectionSpec));
    }

    public String readVariable2dToExcel(String variableName, String sectionSpec, @Nullable Integer columnIndexFor1D,
            @Nullable String fileName) {

        validateXlsxFileName(fileName);
        return readVariableToExcelService.readVariable2dToExcel(netcdFileService.getNcFile(), variableName, sectionSpec,
                columnIndexFor1D, fileName);
    }

    public String averageVariableToExcel(String variableName, String sectionSpec, @Nullable Integer averageOnIndex,
            @Nullable String fileName) {

        validateXlsxFileName(fileName);
        return averageVariableToExcelService.averageVariableToExcel(netcdFileService.getNcFile(), variableName,
                sectionSpec, averageOnIndex, fileName);
    }

    public CdmGroup readRootGroupSchema() {

        NetcdfFile ncFile = netcdFileService.getNcFile();
        Group rootGroup = ncFile.getRootGroup();

        CdmDataset cdmDataset = CdmDataAccessLayerTranslator.translateNetcdfFile(ncFile);
        List<CdmDimension> cdmDimensions = CdmDataAccessLayerTranslator.translateDimensions(rootGroup.getDimensions());
        List<CdmEnumTypedef> cdmEnumTypedefs = CdmDataAccessLayerTranslator.translateEnumTypedefs(
                rootGroup.getEnumTypedefs());
        List<CdmVariable> cdmVariables = CdmDataAccessLayerTranslator.translateVariables(rootGroup.getVariables());
        List<CdmAttribute> cdmAttributes = CdmDataAccessLayerTranslator.translateAttributes(rootGroup.attributes());

        return CdmGroup.builder().name(rootGroup.getFullName()).dataset(cdmDataset).dimensions(
                cdmDimensions).enumTypedefs(cdmEnumTypedefs).variables(cdmVariables).attributes(cdmAttributes).build();
    }

    public String readRootGroupRaw() {
        return functionProcessorService.processFunctionToString(netcdFileService.getNcFile(),
                AppFunction.READ_ROOT_GROUP, Map.of());
    }

    private static Map<String, String> createReadVariableArgs(String variableName, String sectionSpec) {
        return Map.of(FnReadVariableArg.VARIABLE.getArg(), variableName, FnReadVariableArg.SECTION_SPEC.getArg(),
                sectionSpec);
    }

    private static void validateXlsxFileName(String fileName) {
        if (StringUtils.isEmpty(fileName) || fileName.matches("^\\w+\\.xlsx$")) return;
        throw new IllegalArgumentException("Illegal filename. Must be a word followed by .xlsx");
    }
}
