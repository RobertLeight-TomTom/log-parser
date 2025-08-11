fun main (args: Array<String>) {
    val parser = LogParser()
    val canContinue = parser.parseSettings(args)
    if (!canContinue) {
        return
    }
    parser.parse()
}