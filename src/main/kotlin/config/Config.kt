package valkey.kotlin.config

import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

fun initConfigValues() {
    standardConfigs.forEach { _, option -> option.init() }
}

fun loadServerConfigFromString(config: String) {
    config.lines().filter { it.isNotBlank() && !it.startsWith('#') }.forEach { line ->
        val args = line.split(Regex("\\s+"))
        // TODO: Send List<String> to set
        args.let { standardConfigs[args[0]]?.set(args[1]) }
    }
}

fun loadServerConfigFromFile(filename: String) {
    val configString = File(Path(filename).absolutePathString()).readText(Charsets.UTF_8)
    loadServerConfigFromString(configString)
}

fun initServerConfig() {
    initConfigValues()
}
