package edu.odu.cs.cs417;

import java.util.List;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

/**
 * This class implements Global Linear Least Squares Approximation
 * using the normal equations method (X^T * X | X^T * Y) from class.
 * 
 */
public class GlobalLeastSquares {
    
    /**
     * Compute global linear least squares approximation for all CPU cores.
     * 
     * @param times Array of time steps
     * @param coreReadings Array of temperature readings [core][time]
     */
    public static void computeGlobalLeastSquares(int[] times, double[][] coreReadings) {
        if (times.length == 0 || coreReadings.length == 0) {
            System.out.println("Nothing to process, check parser!");
            return;
        }
        
        int numberOfCores = coreReadings.length;
        
        // Process each core separately
        for (int coreIdx = 0; coreIdx < numberOfCores; ++coreIdx) {
            computeLeastSquaresForCore(times, coreReadings[coreIdx], coreIdx);
        }
    }
    
    /**
     * Compute least squares approximation for a specific core using normal equations.
     * 
     * @param times Array of time steps
     * @param coreTemps Array of temperature readings for this core
     * @param coreIdx Index of the core being processed
     */
    private static void computeLeastSquaresForCore(int[] times, double[] coreTemps, int coreIdx) {
        String filename = "core" + coreIdx + ".txt";
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // First, write piecewise interpolation (same as PiecewiseInterpolator)
            for (int i = 0; i < times.length - 1; ++i) {
                double x1 = times[i];
                double y1 = coreTemps[i]; 
                double x2 = times[i + 1];
                double y2 = coreTemps[i + 1];
                
                // Compute slope and intercept using least squares
                double slope = (y2 - y1) / (x2 - x1);
                double intercept = y1 - slope * x1;
                
                // Format output for piecewise interpolation
                writer.printf("%8d <= x <= %8d ; y = %12.4f + %12.4f x ; interpolation%n", 
                             (int)x1, (int)x2, intercept, slope);
            }
            
            // Now compute global least squares for range
            int n = times.length;
            
            // Compute sums needed for normal equations
            double sumX = 0.0, sumY = 0.0, sumXY = 0.0, sumX2 = 0.0;
            
            for (int i = 0; i < n; i++) {
                double x = times[i];
                double y = coreTemps[i];
                
                sumX += x;
                sumY += y;
                sumXY += x * y;
                sumX2 += x * x;
            }
            
            // Solve normal equations: [n   sumX] [b] = [sumY]
            //                        [sumX sumX2] [m]   [sumXY]
            
            // solve the 2x2 system
            double det = n * sumX2 - sumX * sumX;
            
            if (Math.abs(det) < 1e-10) {
                writer.println("Warning: System is singular or nearly singular");
                return;
            }
            
            // Solve for intercept (b) and slope (m)
            double intercept = (sumY * sumX2 - sumX * sumXY) / det;
            double slope = (n * sumXY - sumX * sumY) / det;
            
            // Write global least squares result
            writer.printf("%8d <= x <= %8d ; y = %12.4f + %12.4f x ; least-squares%n", 
                         times[0], times[n-1], intercept, slope);
            
        } catch (IOException e) {
            System.err.println("Error writing to file " + filename + ": " + e.getMessage());
        }
    }
    
    /**
     * solve matrix operations
     * 
     * @param times Array of time steps
     * @param coreTemps Array of temperature readings for this core
     * @return Array containing [intercept, slope]
     */
    public static double[] solveLeastSquaresMatrix(int[] times, double[] coreTemps) {
        int n = times.length;
        
        // Build X^T * X matrix (2x2)
        double[][] xtx = new double[2][2];
        xtx[0][0] = n;           // sum of 1's
        xtx[0][1] = 0;           // sum of x's
        xtx[1][0] = 0;           // sum of x's
        xtx[1][1] = 0;           // sum of x^2's
        
        // Build X^T * Y vector (2x1)
        double[] xty = new double[2];
        xty[0] = 0;              // sum of y's
        xty[1] = 0;              // sum of x*y's
        
        // Fill the matrices
        for (int i = 0; i < n; i++) {
            double x = times[i];
            double y = coreTemps[i];
            
            xtx[0][1] += x;
            xtx[1][0] += x;
            xtx[1][1] += x * x;
            
            xty[0] += y;
            xty[1] += x * y;
        }
        
        // Solve (X^T * X) * coefficients = X^T * Y using Gaussian elimination
        return solveLinearSystem(xtx, xty);
    }
    
    /**
     * Solve a 2x2 linear system using Gaussian elimination.
     * 
     * @param A Coefficient matrix (2x2)
     * @param b Right-hand side vector (2x1)
     * @return Solution vector [x, y]
     */
    private static double[] solveLinearSystem(double[][] A, double[] b) {
        double det = A[0][0] * A[1][1] - A[0][1] * A[1][0];
        
        if (Math.abs(det) < 1e-10) {
            throw new ArithmeticException("Matrix is singular");
        }
        
        // Using Cramer's rule i think
        double x = (b[0] * A[1][1] - A[0][1] * b[1]) / det;
        double y = (A[0][0] * b[1] - b[0] * A[1][0]) / det;
        
        return new double[]{x, y};
    }
} 
