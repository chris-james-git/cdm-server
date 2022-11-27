package com.chrisdjames1.temperatureanalysis.model.cdm.dataaccesslayer;

import lombok.Builder;
import lombok.Data;
import ucar.ma2.DataType;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class CdmArray {

    @Builder.Default
    private List<Integer> shape = new ArrayList<>();

    private DataType dataType;

    @Builder.Default
    private List<Object> data = new ArrayList<>();

}
