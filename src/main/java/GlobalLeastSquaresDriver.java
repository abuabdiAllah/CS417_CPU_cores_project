import edu.odu.cs.cs417.GlobalLeastSquares;
import edu.odu.cs.cs417.TemperatureParser;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * Test driver for Global Linear Least Squares Approximation.
 * This demonstrates the functionality using the sample input data.
 */
public class GlobalLeastSquaresDriver {
    
    public static void main(String[] args) {
        try {
            // Read the sample input file
            BufferedReader reader = new BufferedReader(new FileReader("sample_input.txt"));
            List<TemperatureParser.CoreTempReading> readings = TemperatureParser.parseRawTemps(reader);
            reader.close();
            
            if (readings.isEmpty()) {
                System.out.println("No data found in sample_input.txt");
                return;
            }
            
            // Extract time steps and temperature data
            int[] times = new int[readings.size()];
            int numCores = readings.get(0).readings.length;
            double[][] coreReadings = new double[numCores][readings.size()];
            
            for (int i = 0; i < readings.size(); i++) {
                TemperatureParser.CoreTempReading reading = readings.get(i);
                times[i] = reading.step;
                
                for (int j = 0; j < numCores; j++) {
                    coreReadings[j][i] = reading.readings[j];
                }
            }
            
            // Print input data
            System.out.println("Sample Input Data:");
            System.out.println("==================");
            System.out.println("Time(s)  Core0   Core1   Core2   Core3");
            System.out.println("------   -----   -----   -----   -----");
            for (int i = 0; i < times.length; i++) {
                System.out.printf("%6d   %6.1f  %6.1f  %6.1f  %6.1f%n", 
                                 times[i], 
                                 coreReadings[0][i], 
                                 coreReadings[1][i], 
                                 coreReadings[2][i], 
                                 coreReadings[3][i]);
            }
            System.out.println();
            
            // Compute global least squares approximation
            System.out.println("Computing Global Linear Least Squares Approximation...");
            System.out.println("=====================================================");
            GlobalLeastSquares.computeGlobalLeastSquares(times, coreReadings);
            
            System.out.println("Results have been written to files:");
            for (int i = 0; i < numCores; i++) {
                System.out.println("  core" + i + ".txt");
            }
            System.out.println();
            
            // Demonstrate the matrix method as well
            System.out.println("Matrix Method Results (for verification):");
            System.out.println("=========================================");
            for (int i = 0; i < numCores; i++) {
                double[] coefficients = GlobalLeastSquares.solveLeastSquaresMatrix(times, coreReadings[i]);
                System.out.printf("Core %d: y = %.4f + %.4f * x%n", i, coefficients[0], coefficients[1]);
            }
            
        } catch (IOException e) {
            System.err.println("Error reading sample_input.txt: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 