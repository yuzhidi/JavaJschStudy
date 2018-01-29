/**
 * Created by Leocone on 2/1/18.
 */
/**
  * file content:
 username=
 password=
 host=
 port= (optional)
 */
def isTestParse = false


def args = new Args()

if (!args.parse(this.args)) {
    System.exit(1)
}

def config_file_path
if (args.host == null) {
    config_file_path = Thread.currentThread().getContextClassLoader().getResource("gotLogConfig").getPath()
} else {
    config_file_path = args.host
}

/**
 * got device log
 */
def hostProperties = new Properties()
hostProperties.load(new FileReader(new File(config_file_path)))


def targetFile
if (isTestParse) {
    targetFile = new File ("/Users/wangliang/bugLog/logFile-04044cb7d69048c8ba50ce9345324673")
} else {
    targetFile = new SshGetFile(hostProperties, args.keyWord).getFile()
    if (!targetFile.exists() || targetFile.size() == 0) {
        println("not got log file")
        System.exit(1)
    }
}

if (!hostProperties.getProperty("enableParse") || !(args.keyWord)) {
    System.exit(0)
}
/**
 * parse device log
 */
println("###### prepare parse ######")
// part of log
def process = "grep -n ${args.keyWord} ${targetFile.absolutePath}".execute() | "head -n 1".execute() | "cut -d : -f1".execute()
def lineNumberFirst = process.text.trim()
process.closeStreams()

def keyWordCoveredSuffix = "keyWordCovered"

println "lineNumberFirst:$lineNumberFirst"

process = "grep -n ${args.keyWord} ${targetFile.absolutePath}".execute() | "tail -n 1".execute() | "cut -d : -f1".execute()
def lineNumberLast = process.text.trim()
process.closeStreams()
println "lineNumberLast:$lineNumberLast"

def keyWordCoveredLogFile = new File("${targetFile.absolutePath}-${keyWordCoveredSuffix}")
if (lineNumberFirst && lineNumberLast && lineNumberFirst != lineNumberLast) {
    process = ['sed', '-n', "${lineNumberFirst}, ${lineNumberLast}p", "${targetFile.absolutePath}"].execute()
    def text = process.text
    process.closeStreams()
    keyWordCoveredLogFile.write(text)
} else {
    println("exit as head tail number error")
    System.exit(1)
}

def udidLogFile
if (keyWordCoveredLogFile.length() && args.udid && args.keyWord != args.udid) {
//    println args.udid
    udidLogFile = new File("${keyWordCoveredLogFile.absolutePath}-${args.udid}")
    def devLogTag="DEV.${args.udid.substring(0,8)}"
    println devLogTag
    println udidLogFile.absolutePath
    println  "grep [$devLogTag] ${keyWordCoveredLogFile.absolutePath}"
    println "sed s/^.*\\[${devLogTag}\\]//g"
    process = ["grep", "\\[$devLogTag\\]", "${keyWordCoveredLogFile.absolutePath}"].execute() | ['sed', "s/^.*\\[${devLogTag}\\]//g"].execute()
    def text = process.text
    process.closeStreams()

    udidLogFile.write(text)
    println "wc ${udidLogFile.absolutePath}".execute().text
}

new LogParser(udidLogFile ? udidLogFile : targetFile).doParser()

