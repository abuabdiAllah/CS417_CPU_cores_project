# CS417 Project Part 2 - Temparature Parser with Piecewise Linear Interpolation

This a Java application for parsing CPU core temperature data from input files and computing piecewise linear interpolation between adjacent temperature readings.

## Required Libraries

This project requires:
- **Java 11 or higher** (JDK)
- **Gradle** (included via wrapper - no separate installation needed)

## Compilation Instructions

### Use Gradle 

**On Linux/macOS or Git Bash (Windows):**

./gradlew build



## How to Run Your Program

### Use Gradle

**On Linux servers or Git Bash (Windows):**
```bash
# Run with default input file (sample_input.txt) specified in build.gradle
./gradlew run

# Run with custom input file
./gradlew run --args="your_input_file.txt"
```


### Platform Compatibility
This project is compatible with **Linux servers** and **Git Bash on Windows**. 

**Note:** Windows PowerShell caused me a lot of issues with the Gradle wrapper commands and I don't recommend it

## Sample Output

### Input File Format
```
61.0 63.0 50.0 58.0
80.0 81.0 68.0 77.0
62.0 63.0 52.0 60.0
```

### Example Program Output
```
Piecewise Linear Interpolation Results (see):
=====================================
Core #  0
       0 <= x <=       30 ; y =      61.0000 +       0.6333 x ; interpolation
      30 <= x <=       60 ; y =      98.0000 +      -0.6000 x ; interpolation
      60 <= x <=       90 ; y =      20.0000 +       0.7000 x ; interpolation
      90 <= x <=      120 ; y =     128.0000 +      -0.5000 x ; interpolation


```

The program outputs:
1. **Piecewise linear interpolation** showing line of best fit equations for each core
2. **Time segments** with slope and intercept for each adjacent pair of temperature readings
3. **Mathematical format** y = b + mx where b is intercept and m is slope



## Mathematical Background

The piecewise linear interpolation uses least squares approximation to compute lines of best fit between adjacent temperature readings. read the part 2 intructions of the project and also https://www.cs.odu.edu/~tkennedy/cs417/latest/Public/approximationWhirlwindIntroduction/index.html
