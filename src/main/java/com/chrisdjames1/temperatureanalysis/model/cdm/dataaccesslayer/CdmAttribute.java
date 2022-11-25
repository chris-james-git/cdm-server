package com.chrisdjames1.temperatureanalysis.model.cdm.dataaccesslayer;

import lombok.Builder;
import lombok.Data;
import ucar.ma2.DataType;

import java.util.List;

@Data
@Builder
public class CdmAttribute {

    private String name;

    private DataType dataType;

    private List<Object> values;

}
