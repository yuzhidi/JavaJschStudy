class ArgsTest extends GroovyTestCase {
    void testUsage() {
        def argsParser = new Args()
        def args = [] as String[]
        argsParser.parse(args)
    }
}
