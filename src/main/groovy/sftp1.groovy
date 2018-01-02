/**
 * Created by Leocone on 29/12/17.
 */
import com.jcraft.jsch.*
java.util.Properties config = new java.util.Properties()
config.put "StrictHostKeyChecking", "no"
JSch ssh = new JSch()
Session session = ssh.getSession "testbird-ios", "10.10.10.75", 22
session.with {
    setConfig config
    setPassword "123"
    connect()

    Channel chan = openChannel "sftp"
    chan.connect()

    ChannelSftp sftp = (ChannelSftp) chan;
    def putFile = new File("/tmp/leoneed");
    def getFile = new File('/tmp/gotleoneed')
    sftp.get("/Users/testbird-ios/work/roc-xcode9/roc.version", "/tmp/leoneed")
    sftp.put("/tmp/leoneed", "/tmp/remoteleoneed0")
    /**
     * must absolute path
     */
    putFile.withInputStream { inputStream -> sftp.put(inputStream, "/tmp/remoteleoneed1")}
    getFile.withOutputStream { outputStream -> sftp.get("/Users/testbird-ios/work/roc-xcode9/roc.log.20171228180054.aaab", outputStream) }

    chan.disconnect()

    disconnect()
}
