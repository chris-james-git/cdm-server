package com.chrisdjames1.temperatureanalysis.util;

public class ShapeUtils {

    public static int countShapeDimensions(int[] shape) {
        if (shape.length == 0) {
            throw new IllegalArgumentException("Empty shape!");
        }
        int dimensions = 0;
        for (int val : shape) {
            if (val > 1) {
                dimensions++;
            }
        }
        return dimensions;
    }
}
