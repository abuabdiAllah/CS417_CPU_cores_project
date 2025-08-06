# CS417 Project (plus Extra Credit)- Temperature Parser with Piecewise Linear Interpolation, Global Linear Least Squares Approximation, and Cubic Spline Interpolation

This is a Java application for parsing CPU core temperature data from input files and computing three types of interpolation:
1. Piecewise linear interpolation between adjacent temperature readings
2. Global linear least squares approximation across all data points
3. Cubic spline interpolation for smooth curve fitting

## Required Libraries

This project requires:
- **Java 11 or higher** (JDK)
- **Gradle** (included via wrapper - no separate installation needed)

## Compilation Instructions

### Use Gradle 

**On Linux/macOS or Git Bash (Windows):**

```bash
./gradlew build
```

## How to Run Your Program

### Use Gradle

**On Linux servers or Git Bash (Windows):**
```bash
# Run with default input file (sample_input.txt) specified in build.gradle
./gradlew run

# Run with custom input file
./gradlew run --args="your_input_file.txt"
```

### Manual Compilation and Execution

**On Linux/macOS or Git Bash (Windows):**
```bash
# Compile the Global Least Squares Driver
javac -cp src/main/java src/main/java/GlobalLeastSquaresDriver.java

# Run the Global Least Squares Driver
java -cp src/main/java GlobalLeastSquaresDriver
```

### Platform Compatibility
This project is compatible with **Linux servers** and **Git Bash on Windows**. 

**Note:** Windows PowerShell caused a lot of issues with the Gradle wrapper commands, and I don't recommend it (just like most Microsoft software, harsh but true).

## Sample Output

### Input File Format
```
61.0 63.0 50.0 58.0
80.0 81.0 68.0 77.0
62.0 63.0 52.0 60.0
83.0 82.0 70.0 79.0
68.0 69.0 58.0 65.0
```

### Example Program Output

**Console Output:**
```
Sample Input Data:
==================
Time(s)  Core0   Core1   Core2   Core3
------   -----   -----   -----   -----
     0     61.0    63.0    50.0    58.0
    30     80.0    81.0    68.0    77.0
    60     62.0    63.0    52.0    60.0
    90     83.0    82.0    70.0    79.0
   120     68.0    69.0    58.0    65.0

Computing Global Linear Least Squares Approximation...
=====================================================
Results have been written to files:
  core0.txt
  core1.txt
  core2.txt
  core3.txt

Matrix Method Results (for verification):
=========================================
Core 0: y = 67.4000 + 0.0567 * x
Core 1: y = 69.0000 + 0.0433 * x
Core 2: y = 56.0000 + 0.0600 * x
Core 3: y = 64.6000 + 0.0533 * x
```

**File Output (core0.txt):**
```
       0 <= x <=       30 ; y =      61.0000 +       0.6333 x ; interpolation
      30 <= x <=       60 ; y =      98.0000 +      -0.6000 x ; interpolation
      60 <= x <=       90 ; y =      20.0000 +       0.7000 x ; interpolation
      90 <= x <=      120 ; y =     128.0000 +      -0.5000 x ; interpolation
       0 <= x <=      120 ; y =      67.4000 +       0.0567 x ; least-squares
       0 <= x <=       30 ; y =      61.0000 +       0.6333 x +       0.0000 x^2 +       0.0000 x^3 ; cubic-spline
      30 <= x <=       60 ; y =      80.0000 +      -0.6000 x +       0.0000 x^2 +       0.0000 x^3 ; cubic-spline
      60 <= x <=       90 ; y =      62.0000 +       0.7000 x +       0.0000 x^2 +       0.0000 x^3 ; cubic-spline
      90 <= x <=      120 ; y =      83.0000 +      -0.5000 x +       0.0000 x^2 +       0.0000 x^3 ; cubic-spline
```

The program outputs in txt files:
1. **Piecewise linear interpolation** showing line of best fit equations for each core between adjacent points
2. **Global linear least squares approximation** showing the best-fit line across all data points for each core
3. **Cubic spline interpolation** showing smooth cubic polynomial segments with continuous first and second derivatives
4. **Time segments** with slope and intercept for each adjacent pair of temperature readings
5. **Mathematical format** y = b + mx for linear methods and y = a + bx + cx² + dx³ for cubic splines



For more details, read the instructions of the project in canvas and also https://www.cs.odu.edu/~tkennedy/cs417/latest/Public/approximationWhirlwindIntroduction/index.html
