package com.chrisdjames1.temperatureanalysis.model.cdm.dataaccesslayer;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class CdmStructure {

    @Builder.Default
    List<CdmVariable> members = new ArrayList<>();

}
