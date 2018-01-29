/**
 * Created by Leocone on 24/1/18.
 */
class LogParser {
    File logFile

    LogParser(logFile) {
        this.logFile = logFile
    }

    void doParser() {
        println("###### do parse ######")
        logFile.text.eachLine {
            if (it.contains("init device success")) {
                println it
            } else if (it.contains("LANG:")) {
                println(it)
            }
        }
    }
}
