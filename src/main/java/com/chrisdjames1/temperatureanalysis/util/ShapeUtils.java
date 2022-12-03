package com.chrisdjames1.temperatureanalysis.util;

public class ShapeUtils {

    /**
     * Counts the number of values in shape that are greater than 1.
     */
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

    /**
     * Finds the index of the nth item that has a value greater than 1. For example, if shape is {3, 1, 2, 1, 1, 3} and
     * if n is 3 then it returns 5 because that is the index of the 3rd item that is greater than 1.
     */
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
