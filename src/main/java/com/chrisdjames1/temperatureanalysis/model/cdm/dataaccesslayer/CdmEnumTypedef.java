package com.chrisdjames1.temperatureanalysis.model.cdm.dataaccesslayer;

import lombok.Builder;
import lombok.Data;
import ucar.ma2.DataType;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class CdmEnumTypedef {

    private String name;

    private DataType baseType;

    @Builder.Default
    private Map<Integer, String> map = new HashMap<>();

}
