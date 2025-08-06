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
            
            // Compute cubic spline coefficients: S(x) = a + b*x + c*x^2 + d*x^3
            // Convert from shifted form S(x) = a + b(x-x1) + c(x-x1)^2 + d(x-x1)^3
            // to standard form S(x) = A + B*x + C*x^2 + D*x^3
            
            // First compute the shifted coefficients
            double a = y1;
            double b = (y2 - y1) / h - h * (2 * M1 + M2) / 6.0;
            double c = M1 / 2.0;
            double d = (M2 - M1) / (6.0 * h);
            
            // Now convert to standard form
            double A = a - b * x1 + c * x1 * x1 - d * x1 * x1 * x1;
            double B = b - 2 * c * x1 + 3 * d * x1 * x1;
            double C = c - 3 * d * x1;
            double D = d;
            
            // Format output for cubic spline
            writer.printf("%8d <= x <= %8d ; y = %12.4f + %12.4f x + %12.4f x^2 + %12.4f x^3 ; cubic-spline%n", 
                         (int)x1, (int)x2, A, B, C, D);
        }
    }
} 
