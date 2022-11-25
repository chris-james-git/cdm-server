package com.chrisdjames1.temperatureanalysis.model.cdm.dataaccesslayer;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class CdmGroup {

    private String name;

    private CdmDataset dataset;

    @Builder.Default
    private List<CdmDimension> dimensions = new ArrayList<>();

    @Builder.Default
    private List<CdmVariable> variables = new ArrayList<>();

    @Builder.Default
    private List<CdmAttribute> attributes = new ArrayList<>();

    @Builder.Default
    private List<CdmEnumTypedef> enumTypedefs = new ArrayList<>();

    @Builder.Default
    private List<CdmGroup> groups = new ArrayList<>();

}
