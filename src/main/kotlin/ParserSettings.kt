import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ParserSettings {
    private var homeDirectory: String = ""
    private var filterFile: String = ""
    private var fromTime: LocalTime? = null
    private var toTime: LocalTime? = null

    fun parse(args: Array<String>): Boolean {
        // At least one argument is required - the path to the log file(s)
        if (args.isEmpty()) {
            println("No arguments given - cannot continue")
            return false
        }

        // Set the home directory where we will create the input/output folders
        homeDirectory = args[0]
        if (args.size == 1) {
            println("One argument detected - continue")
            printSettings()
            return true
        }

        val maxArgs = args.size
        var currentArg = 1
        var nextArg = 2

        // For every optional argument, expect a parameter to be given
        while (currentArg < maxArgs) {
            if (nextArg >= maxArgs) {
                println("Argument count does not match")
                return false
            }
            if (args[currentArg].contains("--filter")) {
                filterFile = args[nextArg]
            } else if (args[currentArg].contains("--from")) {
                fromTime = parseTimeArg(args[nextArg])
                if (fromTime == null) {
                    println("Parsing from time failed")
                    return false
                }
            } else if (args[currentArg].contains("--to")) {
                toTime = parseTimeArg(args[nextArg])
                if (toTime == null) {
                    println("Parsing to time failed")
                    return false
                }
            }
            currentArg += 2
            nextArg += 2
        }

        println("All settings parsed - continue")
        printSettings()
        return true
    }

    fun getHomeDirectory(): String {
        return homeDirectory
    }

    fun getFilterFile(): String {
        return filterFile
    }

    fun getFromTime(): LocalTime? {
        return fromTime
    }

    fun getToTime(): LocalTime? {
        return toTime
    }

    private fun parseTimeArg(arg: String): LocalTime? {
        return try {
            LocalTime.parse(arg, DateTimeFormatter.ofPattern("HH:mm:ss"))
        } catch (e: Exception) {
            null
        }
    }

    private fun printSettings() {
        println("Finished parsing arguments:")
        println("Log directory: $homeDirectory")
        println("Filter file: $filterFile")
        println("From time: $fromTime")
        println("To time: $toTime")
    }
}