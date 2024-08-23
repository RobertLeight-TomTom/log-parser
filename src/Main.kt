import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun main(args: Array<String>) {
    if (args.size != 2) {
        println("Usage: java -jar LogParser.jar <directory> <filter_file>")
        return
    }

    // Set the home directory using the first argument
    val homeDirectory = args[0]

    // Move any log files in the home directory to the input directory
    moveLogFilesToInputDirectory(homeDirectory)

    // Check if the input directory exists
    val hasInputDir = File("$homeDirectory/input").exists()
    if (!hasInputDir) {
        println("No input directory found")
        return
    }

    // Scan the input directory for log files
    val logFiles = scanForLogFiles("$homeDirectory/input")
    if (logFiles.isEmpty()) {
        println("No log files found in the input directory")
        return
    }

    // Read the filter file
    val filters = readFilterFile(args[1])
    if (filters.first.isEmpty() && filters.second.isEmpty()) {
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
        val outputFile = filterAndCreateFile(homeDirectory, logFile.name, filters)
        outputFiles.add(outputFile)
    }

    println("----------")
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
    val inputDirectory = "$homeDirectory/input"
    val inputDirectoryFile = File(inputDirectory)
    if (!inputDirectoryFile.exists()) {
        println("Creating input directory")
        inputDirectoryFile.mkdirs()
    }

    // Move the log files to the input directory
    println("Moving log files to the input directory")
    for (logFile in logFiles) {
        val newFile = File("$inputDirectory/${logFile.name}")
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

fun readFilterFile(filterFilePath: String): Pair<List<String>, List<String>> {
    println("Reading filter file: $filterFilePath")
    val filterFile = File(filterFilePath)
    if (!filterFile.exists()) {
        println("Filter file not found")
        return Pair(emptyList(), emptyList())
    }

    val includeFilters = mutableListOf<String>()
    val excludeFilters = mutableListOf<String>()
    filterFile.forEachLine { line ->
        if (line.startsWith("+")) {
            includeFilters.add(line.substring(1))
        } else if (line.startsWith("-")) {
            excludeFilters.add(line.substring(1))
        }
    }

    return Pair(includeFilters, excludeFilters)
}

fun createOutputDirectory(homeDirectory: String) {
    val outputDirectory = "$homeDirectory/output"
    val outputDirectoryFile = File(outputDirectory)
    if (!outputDirectoryFile.exists()) {
        println("Creating output directory")
        outputDirectoryFile.mkdirs()
    }
}

fun filterAndCreateFile(homeDirectory: String, fileName: String, filters: Pair<List<String>, List<String>>): String {
    val inputFile = File("$homeDirectory/input/$fileName")
    val dateTimeString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
    val outputFilePath = "$homeDirectory/output/${inputFile.nameWithoutExtension}_$dateTimeString.log"
    val outputFile = File(outputFilePath)

    inputFile.bufferedReader().use { reader ->
        outputFile.bufferedWriter().use { writer ->
            reader.forEachLine { line ->
                if (filters.first.any { line.contains(it) } && filters.second.none { line.contains(it) }) {
                    writer.write(line)
                    writer.newLine()
                }
            }
        }
    }

    return outputFilePath
}