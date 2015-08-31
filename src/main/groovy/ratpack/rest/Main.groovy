package ratpack.rest

import ratpack.func.Action
import ratpack.groovy.Groovy
import ratpack.server.BaseDir
import ratpack.server.RatpackServer
import ratpack.server.RatpackServerSpec
import ratpack.server.ServerConfig

class Main {

    static void main(String[] args) {
        Action<RatpackServerSpec> serverConfigLoader = { RatpackServerSpec spec ->
            def serverConfig = ServerConfig.builder().baseDir(BaseDir.find('ratpack.groovy'))

            args.each {
                serverConfig.json(getClass().getResource("/${it}.json"))
            }
            serverConfig.sysProps 'ratpack.rest.'

            spec.serverConfig serverConfig
        }

        RatpackServer.start(Action.join(Groovy.Script.app(), serverConfigLoader))
    }

}
