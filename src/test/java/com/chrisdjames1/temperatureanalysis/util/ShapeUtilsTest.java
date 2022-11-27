package com.chrisdjames1.temperatureanalysis.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ShapeUtilsTest {

    @Test
    public void testCountShapeDimensionsWithZeroDimensions() {
        int dimensions = ShapeUtils.countShapeDimensions(new int[]{1, 1, 1});
        assertEquals(0, dimensions);
        dimensions = ShapeUtils.countShapeDimensions(new int[]{1, 1});
        assertEquals(0, dimensions);
        dimensions = ShapeUtils.countShapeDimensions(new int[]{1});
        assertEquals(0, dimensions);
    }

    @Test
    public void testCountShapeDimensionsWithOneDimension() {
        int dimensions = ShapeUtils.countShapeDimensions(new int[]{1, 1, 5});
        assertEquals(1, dimensions);
        dimensions = ShapeUtils.countShapeDimensions(new int[]{1, 4, 1});
        assertEquals(1, dimensions);
        dimensions = ShapeUtils.countShapeDimensions(new int[]{6, 1, 1});
        assertEquals(1, dimensions);
    }

    @Test
    public void testCountShapeDimensionsWithTwoDimensions() {
        int dimensions = ShapeUtils.countShapeDimensions(new int[]{1, 4, 6});
        assertEquals(2, dimensions);
        dimensions = ShapeUtils.countShapeDimensions(new int[]{2, 1, 9});
        assertEquals(2, dimensions);
        dimensions = ShapeUtils.countShapeDimensions(new int[]{7, 3, 1});
        assertEquals(2, dimensions);
    }

    @Test
    public void testCountShapeDimensionsWithThreeDimensions() {
        int dimensions = ShapeUtils.countShapeDimensions(new int[]{5, 2, 8});
        assertEquals(3, dimensions);
    }

    @Test
    public void testFind1stShapeDimensionIndex() {
        int index = ShapeUtils.findNthShapeDimensionIndex(new int[]{7, 2, 1}, 1);
        assertEquals(0, index);
        index = ShapeUtils.findNthShapeDimensionIndex(new int[]{1, 4, 6}, 1);
        assertEquals(1, index);
        index = ShapeUtils.findNthShapeDimensionIndex(new int[]{1, 1, 8}, 1);
        assertEquals(2, index);
    }

    @Test
    public void testFind2ndShapeDimensionIndex() {
        int index = ShapeUtils.findNthShapeDimensionIndex(new int[]{7, 2, 1}, 2);
        assertEquals(1, index);
        index = ShapeUtils.findNthShapeDimensionIndex(new int[]{1, 4, 6}, 2);
        assertEquals(2, index);
        index = ShapeUtils.findNthShapeDimensionIndex(new int[]{1, 1, 8}, 2);
        assertEquals(-1, index);
    }

}