/**
 * Created by Leocone on 2/1/18.
 */
def configPath = Thread.currentThread().getContextClassLoader().getResource("gotLogConfig").getPath()
println "config path:" + configPath
println "cat $configPath".execute().text
