import com.jcraft.jsch.Channel
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session

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
def CONFIG_FILE_PATH = "/tmp/gotLogConfig"
def ROC_DIR = "/Users/testbird-ios/work/roc-xcode9"
def ROC_VERSION = "roc.version"
def ROC_LOG_PREFIX = "roc.log"


def keyWord = this.args[0]
println "keyWord: $keyWord"

String chosenFileCommand="cd $ROC_DIR && cat $ROC_VERSION && for i in `ls -t roc.log*`;do echo \$i;" +
        "grep -q $keyWord \$i; if [[ \$? == 0 ]]; then echo found:\$i;break; fi;done"

def hostProperties = new Properties()
hostProperties.load(new FileReader(new File(CONFIG_FILE_PATH)))

def username = hostProperties.getProperty("username")
def password = hostProperties.getProperty("password")
def host = hostProperties.getProperty("host")
def port = hostProperties.getProperty("port")

println "$username, $password, $host, $port"

def sessionConfig = new Properties()
sessionConfig.put "StrictHostKeyChecking", "no"
JSch ssh = new JSch()
Session session = ssh.getSession username, host
session.with {
    setConfig sessionConfig
    setPassword password
    connect()

    println chosenFileCommand
    println ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
    Channel channel= openChannel"exec"
    ((ChannelExec)channel).setCommand(chosenFileCommand)

    channel.setInputStream(null)
    ((ChannelExec)channel).setErrStream(System.err)

    def inputStream =channel.getInputStream()

    def remoteFile

    channel.connect()
    inputStream.text.eachLine {
        println it
        if (it.toString().contains("found")) {
            remoteFile = ROC_DIR +File.separator + it.toString().split(":")[1]
            println "remoteFile:$remoteFile"
        }
    }
    channel.disconnect()

    println ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
    Channel chan = openChannel "sftp"
    chan.connect()

    ChannelSftp sftp = (ChannelSftp) chan
    def getFile = new File("/tmp/leoGotFile")
    getFile.withOutputStream { outputStream -> sftp.get(remoteFile, outputStream) }

    chan.disconnect()
    disconnect()
}

