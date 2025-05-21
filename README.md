# Log Parser

A simple Kotlin-based command-line tool to filter `.log` files using custom filter files and optional time-based filtering.

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

To run the parser, use `java -jar`, as shown:
```bash
java -jar build/libs/log-parser.jar <logs_directory> <filter_file_with_extension> [--from HH:mm:ss] [--to HH:mm:ss]
```

Example:
```bash
java -jar build/libs/log-parser.jar JIRA-1234 filters/MapUpdateFilter.txt --from 10:45:00 --to 10:55:00
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

## :stopwatch: Time-Based Filtering

You can optionally filter logs based on the first timestamp found in each line. Use:
- `--from HH:mm:ss` – Only include lines at or after this time
- `--to HH:mm:ss` – Only include lines at or before this time

You can use either or both. Time is matched against the first timestamp in the format `dd.MM.yyyy HH:mm:ss.SSS` (e.g., `23.04.2025 10:48:49.950`).
Lines without a valid timestamp in this format will be skipped.

---

## :hammer_and_wrench: Requirements

- Kotlin 1.7+
- Gradle (wrapper included)
- JDK 8 or higher

---

Happy parsing!
