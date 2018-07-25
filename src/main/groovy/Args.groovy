import org.kohsuke.args4j.CmdLineException
import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.Option
import org.kohsuke.args4j.spi.StringArrayOptionHandler

/**
 * Created by Leocone on 2/1/18.
 */
class Args {
    @Option(name="-k", usage="found by this keyword")
    String keyWord

    @Option(name="-host",usage="host config")
    String host

    @Option(name="-u",usage="udid")
    String udid

    @Option(name="-s",usage="parse source file")
    String sourceFile

    @Option(name="-ignores", handler = StringArrayOptionHandler.class)
    String[] multiValuedArray;

    boolean parse(String[] args) {
        CmdLineParser parser = new CmdLineParser(this)
//        if (args.size() == 0) {
//            System.err.println("please input arguments:")
//            usage parser, null
//            return false
//        }
        try {
            // parse the arguments.
            parser.parseArgument(args)
        } catch( CmdLineException e ) {
            usage parser, e
            return false
        }
        true
    }

    def usage(parser, exception) {
        if (exception != null) {
            System.err.println(exception.getMessage())
        }
        parser.printUsage(System.err)
        System.err.println()
    }
}
