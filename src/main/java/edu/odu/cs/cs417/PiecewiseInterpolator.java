package edu.odu.cs.cs417;

import java.util.List;

/**
 * A utility class for computing piecewise linear interpolation
 * between adjacent temperature readings for each CPU core.
 */
public class PiecewiseInterpolator {
    
    /**
     * Compute piecewise linear interpolation for all cores.
     * 
     * @param times Array of time steps
     * @param coreReadings 2D array of temperature readings [core][time]
     */
    public static void computeInterpolation(int[] times, double[][] coreReadings) {
        if (times.length == 0 || coreReadings.length == 0) {
            System.out.println("No temperature readings to interpolate.");
            return;
        }
        
        int numberOfCores = coreReadings.length;
        int numberOfReadings = times.length;
        
        // Process each core separately
        for (int coreIdx = 0; coreIdx < numberOfCores; ++coreIdx) {
            System.out.printf("Core # %2d%n", coreIdx);
            computeInterpolationForCore(times, coreReadings[coreIdx]);
            System.out.println();
        }
    }
    
    /**
     * Compute piecewise linear interpolation for a specific core.
     * 
     * @param times Array of time steps
     * @param coreTemps Array of temperature readings for this core
     */
    private static void computeInterpolationForCore(int[] times, double[] coreTemps) {
        for (int i = 0; i < times.length - 1; ++i) {
            double x1 = times[i];
            double y1 = coreTemps[i];
            double x2 = times[i + 1];
            double y2 = coreTemps[i + 1];
            
            // Compute slope and intercept using least squares
            double slope = (y2 - y1) / (x2 - x1);
            double intercept = y1 - slope * x1;
            
            // Format output exactly as specified
            System.out.printf("%8.0f <= x <= %8.0f ; y = %12.4f + %12.4f x ; interpolation%n", 
                             x1, x2, intercept, slope);
        }
    }
} 