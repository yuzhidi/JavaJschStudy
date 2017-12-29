import com.jcraft.jsch.Channel
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import com.jcraft.jsch.UserInfo

def ROC_DIR = "~/work/roc-xcode9"
def ROC_VERSION = "roc.version"
def ROC_LOG_PREFIX = "roc.log"
/**
 * Created by Leocone on 27/12/17.
 */
println "helloJschGroovy"
def mUserInfo = new UserInfo() {
    @Override
    String getPassphrase() {
        return null
    }

    @Override
    String getPassword() {
        return "123"
    }

    @Override
    boolean promptPassword(String message) {
        return true
    }

    @Override
    boolean promptPassphrase(String message) {
        return true
    }

    @Override
    boolean promptYesNo(String message) {
        return true
    }

    @Override
    void showMessage(String message) {

    }
}

JSch jsch=new JSch()
Session session=jsch.getSession"testbird-ios", "10.10.10.75", 22

session.setUserInfo(mUserInfo)
session.connect()

def keyWord = "dev"
String chosenFileCommand="cd $ROC_DIR && cat $ROC_VERSION && for i in `ls -t roc.log*`;do echo \$i;" +
        "grep -q $keyWord \$i; if [[ \$? == 0 ]]; then echo found:\$i;break; fi;done"
println chosenFileCommand

Channel channel=session.openChannel("exec");
((ChannelExec)channel).setCommand(chosenFileCommand);

channel.setInputStream(null)
((ChannelExec)channel).setErrStream(System.err);

def inputStream =channel.getInputStream();

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

//def scpFileCommand = "scp -f $remoteFile"
//channel=session.openChannel("exec")
//((ChannelExec)channel).setCommand(scpFileCommand)
//// get I/O streams for remote scp
//OutputStream out=channel.getOutputStream();
//inputStream=channel.getInputStream();
//
//channel.connect();



session.disconnect()
println "helloGroovy end"
