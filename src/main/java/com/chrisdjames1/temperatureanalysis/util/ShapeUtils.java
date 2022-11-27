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

    public static int findNthShapeDimensionIndex(int[] shape, int n) {
        if (n < 1) {
            throw new IllegalArgumentException("n must be greater than 0");
        }
        if (shape.length == 0) {
            throw new IllegalArgumentException("Empty shape!");
        }
        int dimensionCount = 0;
        for (int i = 0; i < shape.length; i++) {
            if (shape[i] > 1) {
                dimensionCount++;
                if (dimensionCount == n) {
                    return i;
                }
            }
        }
        return -1;
    }
}
