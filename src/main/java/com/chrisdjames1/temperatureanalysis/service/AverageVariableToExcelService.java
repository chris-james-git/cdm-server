package com.chrisdjames1.temperatureanalysis.service;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import ucar.nc2.NetcdfFile;

@Service
public class AverageVariableToExcelService {

    // TODO: Add support for calculating the means across n-1 dimensions. A designated dimension becomes the rows
    //  with the other dimensions being averaged. For example - changing 2D to 1D average - for a sectionSpec
    //  "0:2073,140:143,0:0", support a new parameter averageOnIndex. With it having a value of 0, time (0:2073)
    //  would be the rows and the averages are taken from the rest of the data for each time unit.
    //  Another example, for a sectionSpec "0:2073,0:180,0:360", setting averageOnIndex=1 would tabulate rows as
    //  latitude vs. average temperature at that entire latitude (because longitude is full range 0:360) across the
    //  time units 0:2073.

    public String averageVariableToExcel(NetcdfFile ncFile, String variableName, String sectionSpec,
            @Nullable Integer averageOnIndex, @Nullable String fileName) {

        throw new UnsupportedOperationException("todo");
    }
}
