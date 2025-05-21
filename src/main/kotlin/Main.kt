import java.io.File
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class Filters(val mandatoryFilters: List<String>, val includeFilters: List<String>, val excludeFilters: List<String>)

fun main(args: Array<String>) {
    var fromTime: LocalTime? = null
    var toTime: LocalTime? = null

    val cleanedArgs = args.filterNot {
        it.startsWith("--from") || it.startsWith("--to")
    }.toTypedArray()

    args.forEachIndexed { index, arg ->
        if (arg == "--from" && args.size > index + 1) {
            fromTime = parseTimeArg(args[index + 1])
        } else if (arg == "--to" && args.size > index + 1) {
            toTime = parseTimeArg(args[index + 1])
        }
    }

    val argsValid = quickValidateArgs(args, cleanedArgs, fromTime, toTime)
    if (!argsValid) {
        println("[ERROR] Usage: `java -jar LogParser.jar <logs_directory> <filter_file_with_extension> [--from HH:mm:ss] [--to HH:mm:ss]`")
        return
    }

    // Set the home directory using the first argument
    val homeDirectory = cleanedArgs[0]

    // Move any log files in the home directory to the input directory
    moveLogFilesToInputDirectory(homeDirectory)

    // Check if the input directory exists
    val hasInputDir = File("$homeDirectory${File.separator}input").exists()
    if (!hasInputDir) {
        println("No input directory found")
        return
    }

    // Scan the input directory for log files
    val logFiles = scanForLogFiles("$homeDirectory${File.separator}input")
    if (logFiles.isEmpty()) {
        println("No log files found in the input directory")
        return
    }

    // Read the filter file
    val filters = readFilterFile(args[1])
    if (filters.mandatoryFilters.isEmpty() && filters.includeFilters.isEmpty() && filters.excludeFilters.isEmpty()) {
        println("No filters found in the filter file")
        return
    }

    // Create the output directory
    createOutputDirectory(homeDirectory)

    println("Ready to parse input files")

    val outputFiles = mutableListOf<String>()

    // Parse the log files
    for (logFile in logFiles) {
        println("Parsing file: ${logFile.name}")
        val outputFile = filterAndCreateFile(homeDirectory, logFile.name, filters, fromTime, toTime)
        outputFiles.add(outputFile)
    }

    println("----------")
    println("Successfully parsed all log files in directory '$homeDirectory'" +
            (fromTime?.let { " from $it" } ?: " from the start") +
            (toTime?.let { " to $it" } ?: " to the end"))
    println("Output files:${System.lineSeparator()}${outputFiles.joinToString(System.lineSeparator())}")
    println("----------")
}

fun moveLogFilesToInputDirectory(homeDirectory: String) {
    // Scan the home directory for log files
    val logFiles = scanForLogFiles(homeDirectory)
    if (logFiles.isEmpty()) {
        return
    }

    // Create the input directory if it doesn't exist
    val inputDirectory = "$homeDirectory${File.separator}input"
    val inputDirectoryFile = File(inputDirectory)
    if (!inputDirectoryFile.exists()) {
        println("Creating input directory")
        inputDirectoryFile.mkdirs()
    }

    // Move the log files to the input directory
    println("Moving log files to the input directory")
    for (logFile in logFiles) {
        val newFile = File("$inputDirectory${File.separator}${logFile.name}")
        logFile.renameTo(newFile)
    }
}

fun scanForLogFiles(directoryPath: String): List<File> {
    println("Scanning for log files in the directory: $directoryPath")
    val directory = File(directoryPath)
    return directory.listFiles { file ->
        file.isFile && file.extension == "log"
    }?.toList() ?: emptyList()
}

fun readFilterFile(filterFilePath: String): Filters {
    println("Reading filter file: $filterFilePath")
    val filterFile = File(filterFilePath)
    if (!filterFile.exists()) {
        println("Filter file not found")
        return Filters(emptyList(), emptyList(), emptyList())
    }

    val mandatoryFilters = mutableListOf<String>()
    val includeFilters = mutableListOf<String>()
    val excludeFilters = mutableListOf<String>()
    filterFile.forEachLine { line ->
        if (line.startsWith("!")) {
            mandatoryFilters.add(line.substring(1))
        } else if (line.startsWith("+")) {
            includeFilters.add(line.substring(1))
        } else if (line.startsWith("-")) {
            excludeFilters.add(line.substring(1))
        }
    }

    return Filters(mandatoryFilters, includeFilters, excludeFilters)
}

fun createOutputDirectory(homeDirectory: String) {
    val outputDirectory = "$homeDirectory${File.separator}output"
    val outputDirectoryFile = File(outputDirectory)
    if (!outputDirectoryFile.exists()) {
        println("Creating output directory")
        outputDirectoryFile.mkdirs()
    }
}

fun filterAndCreateFile(
    homeDirectory: String,
    fileName: String,
    filters: Filters,
    fromTime: LocalTime?,
    toTime: LocalTime?
): String {
    val inputFile = File("$homeDirectory${File.separator}input${File.separator}$fileName")
    val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss.SSS")
    val outputFilePath =
        "$homeDirectory${File.separator}" +
                "output${File.separator}" +
                inputFile.nameWithoutExtension +
                "_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) +
                ".log"
    val outputFile = File(outputFilePath)

    val timestampRegex = Regex("""\d{2}\.\d{2}\.\d{4} \d{2}:\d{2}:\d{2}\.\d{3}""")

    inputFile.bufferedReader().use { reader ->
        outputFile.bufferedWriter().use { writer ->
            reader.forEachLine { line ->
                val match = timestampRegex.find(line)
                if (match != null) {
                    val timestampStr = match.value
                    val lineTime = try {
                        LocalDateTime.parse(timestampStr, dateTimeFormatter).toLocalTime()
                    } catch (e: Exception) {
                        return@forEachLine
                    }

                    val isInTimeRange = (fromTime == null || lineTime >= fromTime) && (toTime == null || lineTime <= toTime)

                    if (isInTimeRange &&
                        (filters.mandatoryFilters.isEmpty() || filters.mandatoryFilters.any { line.contains(it) }) &&
                        (filters.includeFilters.isEmpty() || filters.includeFilters.any { line.contains(it) }) &&
                        filters.excludeFilters.none { line.contains(it) }) {
                        writer.write(line)
                        writer.newLine()
                    }
                }
            }
        }
    }

    return outputFilePath
}

fun parseTimeArg(arg: String): LocalTime? {
    return try {
        LocalTime.parse(arg, DateTimeFormatter.ofPattern("HH:mm:ss"))
    } catch (e: Exception) {
        null
    }
}

fun quickValidateArgs(
    args: Array<String>,
    cleanedArgs: Array<String>,
    fromTime: LocalTime?,
    toTime: LocalTime?
): Boolean {
    fromTime?.let { from ->
        toTime?.let { to ->
            if (from > to) {
                println("[ERROR] Start time '$from' is later than end time '$to'")
                return false
            }
        }
    }

    if (cleanedArgs.size < 2) {
        println("[ERROR] Missing argument <logs_directory> or <filter_file_with_extension>")
        return false
    }

    if (args.contains("--from") && args.size < 4) {
        println("[ERROR] Argument '--from' given, but no start time provided")
        return false
    }

    if (args.contains("--to") && args.size < 4) {
        println("[ERROR] Argument '--to' given, but no end time provided")
        return false
    }

    if (args.contains("--from") && args.contains("--to") && args.size < 6) {
        println("[ERROR] Arguments '--from' and '--to' both given, but no time provided")
        return false
    }

    return true
}