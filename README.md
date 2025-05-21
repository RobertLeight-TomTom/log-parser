# Log Parser

A simple Kotlin-based command-line tool to filter `.log` files using custom filter files.

## :package: Building the JAR

To build the project:
```bash
./gradlew clean build
```

This will create a runnable JAR file in:
```
build/libs/log-parser.jar
```

## :rocket: Running the Log Parser

You can run the parser in two ways:

### 1. Run via `java -jar`:
```bash
java -jar build/libs/log-parser.jar <logs_directory> <filter_file_with_extension>
```

Example:
```bash
java -jar build/libs/log-parser.jar logs filters/ErrorFilter.txt
```

### 2. Use the helper script:

You can also use the provided script:
```bash
./LogParser.sh <logs_directory> <filter_file_with_extension>
```

Make sure the script has execute permission:
```bash
chmod +x LogParser.sh
```

## :receipt: Filter File Format

Each line in your filter file should begin with:
- `!` – Mandatory filters (lines must include these)
- `+` – Include filters (at least one should match)
- `-` – Exclude filters (lines must not contain these)


Example filter file (`filters/ExampleFilter.txt`):
```
!ERROR
+User
-DEBUG
```

## :hammer_and_wrench: Requirements

- Kotlin 1.7+
- Gradle (wrapper included)
- JDK 8 or higher

---

Happy parsing!
