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
if (!new SshGetFile(hostProperties, args.keyWord).getFile()) {
    println("not got log file")
    System.exit(1)
}

/**
 * parse device log
 */

