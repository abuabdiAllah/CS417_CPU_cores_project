package edu.odu.cs.cs417;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

/**
 * This class implements cubic spline interpolation from scratch.
 * 
 * A cubic spline is a piecewise cubic polynomial that:
 * 1. Passes through all data points
 * 2. Has continuous first and second derivatives at interior points
 * 3. Uses natural boundary conditions (second derivative = 0 at endpoints)
 * 
 * The implementation solves a tridiagonal system to find second derivatives,
 * then constructs cubic polynomials between each pair of points.
 */
public class CubicSplineInterpolator {
    
    /**
     * Compute cubic spline interpolation for all CPU cores.
     * 
     * @param times Array of time steps
     * @param coreReadings Array of temperature readings [core][time]
     */
    public static void computeCubicSpline(int[] times, double[][] coreReadings) {
        if (times.length == 0 || coreReadings.length == 0) {
            System.out.println("Nothing to interpolate, check parser!");
            return;
        }
        
        int numberOfCores = coreReadings.length;
        
        // Process each core separately
        for (int coreIdx = 0; coreIdx < numberOfCores; ++coreIdx) {
            computeCubicSplineForCore(times, coreReadings[coreIdx], coreIdx);
        }
    }
    
    /**
     * Compute cubic spline interpolation for a specific core.
     * 
     * @param times Array of time steps
     * @param coreTemps Array of temperature readings for this core
     * @param coreIdx Index of the core being processed
     */
    private static void computeCubicSplineForCore(int[] times, double[] coreTemps, int coreIdx) {
        String filename = "core" + coreIdx + ".txt";
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // First, write piecewise interpolation (same as existing code)
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
            
            // Now compute global least squares for entire range
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
            
            // Solve normal equations using Cramer's rule
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
            
            // Now compute and write cubic spline interpolation
            if (n >= 2) {
                double[] secondDerivatives = computeCubicSplineCoefficients(times, coreTemps);
                writeCubicSplineSegments(writer, times, coreTemps, secondDerivatives);
            }
            
        } catch (IOException e) {
            System.err.println("Error writing to file " + filename + ": " + e.getMessage());
        }
    }
    
    /**
     * Compute the second derivatives needed for cubic spline interpolation.
     * Uses natural boundary conditions (second derivative = 0 at endpoints).
     * 
     * @param times Array of time steps
     * @param temps Array of temperature readings
     * @return Array of second derivatives at each point
     */
    private static double[] computeCubicSplineCoefficients(int[] times, double[] temps) {
        int n = times.length;
        double[] secondDerivatives = new double[n];
        
        if (n < 3) {
            // For 2 points, use linear interpolation (second derivatives = 0)
            secondDerivatives[0] = 0.0;
            secondDerivatives[1] = 0.0;
            return secondDerivatives;
        }
        
        // Set up tridiagonal system for natural spline
        // Natural boundary conditions: M[0] = M[n-1] = 0
        double[] a = new double[n]; // subdiagonal
        double[] b = new double[n]; // diagonal
        double[] c = new double[n]; // superdiagonal
        double[] d = new double[n]; // right-hand side
        
        // Natural boundary conditions
        b[0] = 1.0;
        c[0] = 0.0;
        d[0] = 0.0;
        
        b[n-1] = 1.0;
        a[n-1] = 0.0;
        d[n-1] = 0.0;
        
        // Interior points
        for (int i = 1; i < n - 1; i++) {
            double h_i = times[i] - times[i-1];
            double h_i1 = times[i+1] - times[i];
            
            a[i] = h_i;
            b[i] = 2.0 * (h_i + h_i1);
            c[i] = h_i1;
            
            double f_i1 = temps[i+1];
            double f_i = temps[i];
            double f_i_1 = temps[i-1];
            
            d[i] = 6.0 * ((f_i1 - f_i) / h_i1 - (f_i - f_i_1) / h_i);
        }
        
        // Solve tridiagonal system using Thomas algorithm
        return solveTridiagonalSystem(a, b, c, d);
    }
    
    /**
     * Solve a tridiagonal system using the Thomas algorithm.
     * 
     * @param a Subdiagonal elements
     * @param b Diagonal elements
     * @param c Superdiagonal elements
     * @param d Right-hand side vector
     * @return Solution vector
     */
    private static double[] solveTridiagonalSystem(double[] a, double[] b, double[] c, double[] d) {
        int n = d.length;
        double[] x = new double[n];
        
        // Forward elimination
        double[] cPrime = new double[n];
        double[] dPrime = new double[n];
        
        cPrime[0] = c[0] / b[0];
        dPrime[0] = d[0] / b[0];
        
        for (int i = 1; i < n; i++) {
            double denominator = b[i] - a[i] * cPrime[i-1];
            cPrime[i] = c[i] / denominator;
            dPrime[i] = (d[i] - a[i] * dPrime[i-1]) / denominator;
        }
        
        // Back substitution
        x[n-1] = dPrime[n-1];
        for (int i = n-2; i >= 0; i--) {
            x[i] = dPrime[i] - cPrime[i] * x[i+1];
        }
        
        return x;
    }
    
    /**
     * Write the cubic spline segments to the output file.
     * 
     * @param writer PrintWriter for output
     * @param times Array of time steps
     * @param temps Array of temperature readings
     * @param secondDerivatives Array of second derivatives
     */
    private static void writeCubicSplineSegments(PrintWriter writer, int[] times, 
                                                double[] temps, double[] secondDerivatives) {
        int n = times.length;
        
        for (int i = 0; i < n - 1; i++) {
            double x1 = times[i];
            double y1 = temps[i];
            double x2 = times[i + 1];
            double y2 = temps[i + 1];
            
            double h = x2 - x1;
            double M1 = secondDerivatives[i];
            double M2 = secondDerivatives[i + 1];
            
            // Compute cubic spline coefficients: S(x) = a + b(x-x1) + c(x-x1)^2 + d(x-x1)^3
            double a = y1;
            double b = (y2 - y1) / h - h * (2 * M1 + M2) / 6.0;
            double c = M1 / 2.0;
            double d = (M2 - M1) / (6.0 * h);
            
            // Format output for cubic spline
            writer.printf("%8d <= x <= %8d ; y = %12.4f + %12.4f(x-%d) + %12.4f(x-%d)^2 + %12.4f(x-%d)^3 ; cubic-spline%n", 
                         (int)x1, (int)x2, a, b, (int)x1, c, (int)x1, d, (int)x1);
        }
    }
} 