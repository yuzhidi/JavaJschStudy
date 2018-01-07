/**
 * Created by Leocone on 29/12/17.
 */
import com.jcraft.jsch.*
java.util.Properties config = new java.util.Properties()
config.put "StrictHostKeyChecking", "no"
JSch ssh = new JSch()
Session session = ssh.getSession "myname", "10.10.10.75", 22
session.with {
    setConfig config
    setPassword "123"
    connect()

    Channel chan = openChannel "sftp"
    chan.connect()

    ChannelSftp sftp = (ChannelSftp) chan;
    def putFile = new File("/tmp/testPutFile")
    def getFile = new File('/tmp/testGotFile')
    sftp.get("/tmp/sftptestsource", "/tmp/localtestsource")
    sftp.put("/tmp/leoneed", "/tmp/remoteleoneed0")
    /**
     * must absolute path
     */
    putFile.withInputStream { inputStream -> sftp.put(inputStream, "/tmp/remoteleoneed1")}
    getFile.withOutputStream { outputStream -> sftp.get("/tmp/outPutstremsource", outputStream) }

    chan.disconnect()

    disconnect()
}
