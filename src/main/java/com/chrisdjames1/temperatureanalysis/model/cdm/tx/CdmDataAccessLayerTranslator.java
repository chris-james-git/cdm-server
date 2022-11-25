package com.chrisdjames1.temperatureanalysis.model.cdm.tx;

import com.chrisdjames1.temperatureanalysis.model.cdm.dataaccesslayer.CdmAttribute;
import com.chrisdjames1.temperatureanalysis.model.cdm.dataaccesslayer.CdmDataset;
import com.chrisdjames1.temperatureanalysis.model.cdm.dataaccesslayer.CdmDimension;
import com.chrisdjames1.temperatureanalysis.model.cdm.dataaccesslayer.CdmEnumTypedef;
import com.chrisdjames1.temperatureanalysis.model.cdm.dataaccesslayer.CdmVariable;
import ucar.nc2.AttributeContainer;
import ucar.nc2.Dimension;
import ucar.nc2.EnumTypedef;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CdmDataAccessLayerTranslator {

    public static CdmDataset translateNetcdfFile(NetcdfFile ncFile) {
        String fileLocation = ncFile.getLocation();
        Path filePath = Paths.get(fileLocation);
        return CdmDataset.builder().location(filePath.getFileName().toString()).build();
    }

    public static List<CdmDimension> translateDimensions(Collection<Dimension> dimensions) {
        return dimensions.stream().map(dim -> CdmDimension.builder().name(dim.getName()).length(dim.getLength())
                .unlimited(dim.isUnlimited()).shared(dim.isShared()).variableLength(dim.isVariableLength()).build())
                .collect(Collectors.toList());
    }

    public static List<CdmVariable> translateVariables(Collection<Variable> variables) {
        return variables.stream().map(variable -> {
            List<CdmAttribute> attributes = translateAttributes(variable.attributes());
            return CdmVariable.builder().name(variable.getFullName())
                    .shape(Arrays.stream(variable.getShape()).boxed().collect(Collectors.toList()))
                    .dataType(variable.getDataType()).attributes(attributes).build();
        }).collect(Collectors.toList());
    }

    public static List<CdmAttribute> translateAttributes(AttributeContainer attributeContainer) {
        List<CdmAttribute> attributes = new ArrayList<>();
        attributeContainer.iterator().forEachRemaining(attr -> {
            List<Object> values = new ArrayList<>();
            for (int i = 0; i < attr.getLength(); i++) {
                values.add(attr.getValue(i));
            }
            attributes.add(CdmAttribute.builder().name(attr.getName()).dataType(attr.getDataType()).values(values)
                    .build());
        });
        return attributes;
    }

    public static List<CdmEnumTypedef> translateEnumTypedefs(Collection<EnumTypedef> enumTypedefs) {
        return enumTypedefs.stream().map(def -> CdmEnumTypedef.builder().name(def.getShortName())
                .baseType(def.getBaseType()).map(def.getMap()).build()).collect(Collectors.toList());
    }
}
