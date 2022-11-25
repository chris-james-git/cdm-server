package com.chrisdjames1.temperatureanalysis.model.cdm.dataaccesslayer;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CdmDimension {

    private String name;

    private Integer length;

    private boolean unlimited;

    private boolean shared;

    private boolean variableLength;

}
