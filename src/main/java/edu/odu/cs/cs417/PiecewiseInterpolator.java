package edu.odu.cs.cs417;

import java.util.List;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

/**
 * This is a class for computing piecewise linear interpolation
 * between adjacent points for each CPU core.
 */
public class PiecewiseInterpolator {
    
    /**
     * Bring in the parsed data we designed in part1 of the project.
     * 
     * @param times Array of time 
     * @param coreReadings array of temperature readings [core][time]
     */
    public static void computeInterpolation(int[] times, double[][] coreReadings) {
        if (times.length == 0 || coreReadings.length == 0) {
            System.out.println("Nothing to interpolate, check parser!");
            return;
        }
        //get bothh array dimensions
        int numberOfCores = coreReadings.length;
        //int numberOfReadings = times.length; Line not needed because we can calculate
        //the times under computerInterpolationForCore for each core instead.
        
        // Process each core separately
        for (int coreIdx = 0; coreIdx < numberOfCores; ++coreIdx) {
            computeInterpolationForCore(times, coreReadings[coreIdx], coreIdx);
        }
    }
    
    /**
     * Compute piecewise linear interpolation for a specific core.
     * 
     * @param times Array of time steps
     * @param coreTemps Array of temperature readings for this core
     * @param coreIdx Index of the core being processed
     */
    private static void computeInterpolationForCore(int[] times, double[] coreTemps, int coreIdx) {
        String filename = "core" + coreIdx + ".txt";
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (int i = 0; i < times.length - 1; ++i) {
                double x1 = times[i]; // subscript1 should be k, but for clear code syntax I used 1 and 2
                double y1 = coreTemps[i]; 
                double x2 = times[i + 1]; //subscript 2 should be k+1
                double y2 = coreTemps[i + 1];
                
                // Compute slope and intercept using least squares
                double slope = (y2 - y1) / (x2 - x1); //m
                double intercept = y1 - slope * x1; //b
                
                // Format output just like sample output
                writer.printf("%8.0f <= x <= %8.0f ; y = %12.4f + %12.4f x ; interpolation%n", 
                             x1, x2, intercept, slope);
            }
        } catch (IOException e) {
            System.err.println("Error writing to file " + filename + ": " + e.getMessage());
        }
    }
} 