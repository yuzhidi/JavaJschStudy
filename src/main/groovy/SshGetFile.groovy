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

    File getFile() {
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

        def toLogDir = hostProperties.getProperty("toLogDir")
        if (!toLogDir) {
            throw new Exception("no toLogDir")
        }
        new File(toLogDir).mkdirs()

        def namePrefix = hostProperties.getProperty("namePrefix")
        if (!namePrefix) {
            throw new Exception("no namePrefix")
        }

        def fromLogDir = hostProperties.getProperty("fromLogDir")
        if (!fromLogDir) {
            throw new Exception("no fromLogDir")
        }
        def filePatten = hostProperties.getProperty("filePattern")
        if (!filePatten) {
            throw new Exception("no filePattern")
        }
        def chosenFileCommand
        if (keyWord) {
            chosenFileCommand = "cd $fromLogDir; for i in `ls -t ${filePatten}`;do echo \$i;grep -q $keyWord \$i; if [[ \$? == 0 ]]; then echo found:\$i;break; fi;done"
        } else {
            chosenFileCommand = "cd $fromLogDir; echo found:`ls -t ${filePatten} | head -n 1`"
        }
        println "$chosenFileCommand"

        println "$username, $password, $host, $port"

        def toLogFile
        def sessionConfig = new Properties()
        sessionConfig.put "StrictHostKeyChecking", "no"
        JSch ssh = new JSch()
        Session session = ssh.getSession username, host
        session.with {
            setConfig sessionConfig
            setPassword password
            connect()

            println "remote start >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
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
                    def remoteFileName = it.toString().split(":")[1]
                    remoteFile = fromLogDir + File.separator + remoteFileName
                    def toLogFilePath = toLogDir + File.separator + namePrefix + remoteFileName
                    toLogFilePath = keyWord ? "${toLogFilePath}-${keyWord}" : toLogFilePath
                    toLogFile = new File(toLogFilePath)

                    println "remoteFile:$remoteFile"
                }
            }
            channel.disconnect()

            println ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + toLogFile.getAbsolutePath()
            Channel chan = openChannel "sftp"
            chan.connect()

            ChannelSftp sftp = (ChannelSftp) chan
            toLogFile.withOutputStream { outputStream -> sftp.get(remoteFile, outputStream) }

            chan.disconnect()
            disconnect()
        }
         toLogFile
    }
}
