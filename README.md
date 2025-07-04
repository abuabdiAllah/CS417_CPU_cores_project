# CS417 Project Part 1 - Temperature Parser

A Java application for parsing CPU core temperature data from input files.

## Required Libraries

This project requires:
- **Java 11 or higher** (JDK)
- **Gradle** (included via wrapper - no separate installation needed)

## Compilation Instructions

### Using Gradle (Recommended)

**On Linux/macOS or Git Bash (Windows):**
```bash
# Build the project
./gradlew build
```

### Using Java Compiler directly
```bash
# Compile all Java files
javac -cp "src/main/java" src/main/java/*.java src/main/java/edu/odu/cs/cs417/*.java
```

## How to Run Your Program

### Using Gradle (Recommended)

**On Linux servers or Git Bash (Windows):**
```bash
# Run with default input file (sample_input.txt) specified in build.gradle
./gradlew run

# Run with custom input file
./gradlew run --args="your_input_file.txt"
```

### Using Java directly
```bash
# Run the compiled classes
java -cp "src/main/java" ParseTempsDriver sample_input.txt

# Or run the JAR file (after building)
java -jar build/libs/ParseTemps.jar sample_input.txt
```

### Platform Compatibility
This project is compatible with **Linux servers** and **Git Bash on Windows**. 

**Note:** Windows PowerShell has been known to cause issues with the Gradle wrapper commands.

## Sample Output

### Input File Format
```
61.0 63.0 50.0 58.0
80.0 81.0 68.0 77.0
62.0 63.0 52.0 60.0
```

### Program Output
```
(0, [61.0, 63.0, 50.0, 58.0])
(30, [80.0, 81.0, 68.0, 77.0])
(60, [62.0, 63.0, 52.0, 60.0])
...

Core #  0
       0 -> 61.00
      30 -> 80.00
      60 -> 62.00
      90 -> 83.00
     120 -> 68.00
     ...

Core #  1
       0 -> 63.00
      30 -> 81.00
      60 -> 63.00
      90 -> 82.00
     120 -> 69.00
     ...

Core #  2
       0 -> 50.00
      30 -> 68.00
      60 -> 52.00
      90 -> 70.00
     120 -> 58.00
     ...

Core #  3
       0 -> 58.00
      30 -> 77.00
      60 -> 60.00
      90 -> 79.00
     120 -> 65.00
     ...
```

The program outputs:
1. **Parsed readings** showing time step and temperature values for all cores
2. **Core-by-core breakdown** displaying temperature readings for each CPU core over time