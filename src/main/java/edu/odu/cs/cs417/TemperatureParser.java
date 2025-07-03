package edu.odu.cs.cs417;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class TemperatureParser
{
    /**
     * A pair of values where:
     * <ul>
     *   <li> _step_ attribute represents the time at which the readind was
     *        taken.
     *   </li>
     *   <li> _readings_ is a vector with _n_ temperature readings,
     *        where _n_ is the number of CPU Cores.
     *   </li>
     * </ul>
     */
    public static class CoreTempReading
    {
        /**
         * Time-step at which the readings were measured.
         */
        public int step;

        /**
         * Temperature readings (one per CPU core).
         */
        public double[] readings;

        /**
         * Create a reading for the specified time-step.
         *
         * @param time time-step at which the readings were measured
         * @param theReadings temperature readings (one per core)
         */
        public CoreTempReading(int time, double[] theReadings)
        {
            this.step = time;
            this.readings = theReadings;
        }

        /**
         * Generate a printable form of the temperature reading in the
         * form (time, [core_0, core_1, ... core_{n-1}]).
         *
         * @return the string representation
         */
        @Override
        public String toString()
        {
            StringBuilder bld = new StringBuilder();
            bld.append("(" + this.step + ", [");

            for (int i = 0; i < this.readings.length - 1; i++) {
                bld.append(this.readings[i]).append(", ");
            }
            bld.append(this.readings[this.readings.length - 1]).append("])");

            return bld.toString();
        }
    }

    /**
     * Take an input file and parse all core temps. Assume a step size of
     * 30 seconds.
     *
     * @param inputTemps an input file
     *
     * @return a vector of 2-tuples (pairs) containing time step and core
     *         temperature readings
     */
    public static List<CoreTempReading> parseRawTemps(BufferedReader inputTemps)
    {
        return parseRawTemps(inputTemps, 30);
    }

    /**
     * Take an input file and time-step size and parse all core temps.
     *
     * @param inputTemps an input file
     * @param stepSize time-step in seconds
     *
     * @return a vector of 2-tuples (pairs) containing time step and core
     *         temperature readings
     */
    public static List<CoreTempReading> parseRawTemps(
        BufferedReader inputTemps, int stepSize
    )
    {
        String[][] rawLines = inputTemps.lines()
            .map(s -> s.split("([^0-9]*\\s)|([^0-9]*$)"))
            .toArray(String[][]::new);

        List<CoreTempReading> allReadings = new Vector<>(rawLines.length);

        int step = 0;
        for (String[] line : rawLines) {
            final double[] tempReadings = Arrays.stream(line)
                .mapToDouble(token -> Double.parseDouble(token))
                .toArray();

            allReadings.add(new CoreTempReading(step, tempReadings));

            step += stepSize;
        }

        return allReadings;
    }
}
