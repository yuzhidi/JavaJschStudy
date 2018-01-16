import com.jcraft.jsch.Channel
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session

/**
 * Created by Leocone on 2/1/18.
 */
class SshGetFile {
    def hostProperties
    def keyWord

    SshGetFile(Properties properties, String keyword) {
        hostProperties = properties
        keyWord = keyword
    }

    boolean getFile() {
        def username = hostProperties.getProperty("username")
        if (!username) {
            throw new Exception("no username")
        }
        def password = hostProperties.getProperty("password")
        if (!password) {
            throw new Exception("no password")
        }
        def host = hostProperties.getProperty("host")
        if (!host) {
            throw new Exception("no host")
        }
        def port = hostProperties.getProperty("port")
        def targetFilePath = hostProperties.getProperty("targetFilePath")
        if (!targetFilePath) {
            throw new Exception("no targetFilePath")
        }
        def targetFile = new File("${targetFilePath}-${keyWord}")
        def logDir = hostProperties.getProperty("logDir")
        if (!logDir) {
            throw new Exception("no logDir")
        }
        def filePatten = hostProperties.getProperty("fileNamePattern")
        if (!filePatten) {
            throw new Exception("no fileNamePattern")
        }
        def chosenFileCommand = "cd $logDir; for i in `ls -t ${filePatten}`;do echo \$i;grep -q $keyWord \$i; if [[ \$? == 0 ]]; then echo found:\$i;break; fi;done"
        println "$chosenFileCommand"

        println "$username, $password, $host, $port"

        def sessionConfig = new Properties()
        sessionConfig.put "StrictHostKeyChecking", "no"
        JSch ssh = new JSch()
        Session session = ssh.getSession username, host
        session.with {
            setConfig sessionConfig
            setPassword password
            connect()

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
                    remoteFile = hostProperties.getProperty("logDir") +File.separator + it.toString().split(":")[1]
                    println "remoteFile:$remoteFile"
                }
            }
            channel.disconnect()

            println ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + targetFile.getAbsolutePath()
            Channel chan = openChannel "sftp"
            chan.connect()

            ChannelSftp sftp = (ChannelSftp) chan
             targetFile.withOutputStream { outputStream -> sftp.get(remoteFile, outputStream) }

            chan.disconnect()
            disconnect()
        }
        targetFile.size() > 0 ? true : false
    }
}
