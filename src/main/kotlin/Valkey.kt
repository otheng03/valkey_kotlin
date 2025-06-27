package valkey.kotlin

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import valkey.kotlin.config.initServerConfig
import valkey.kotlin.config.loadServerConfigFromFile

class Valkey : CliktCommand() {
    val configFile by option(help = "Location of config file")

    override fun run() {
        initServerConfig()
        configFile?.let { loadServerConfigFromFile(it) }
    }
}