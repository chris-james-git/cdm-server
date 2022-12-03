package com.chrisdjames1.temperatureanalysis.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TotalCounter {
    private double total;
    private long count;
    public void add(Double value) {
        if (!Double.isNaN(value)) {
            total += value;
            count++;
        }
    }
    public void add(Float value) {
        if (!Float.isNaN(value)) {
            total += value;
            count++;
        }
    }

    public double average() {
        // 0.0 / 0 == NaN - this will convert to Excel #NUM! error.
        return total / count;
    }
}
