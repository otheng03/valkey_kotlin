package valkey.kotlin.server

import io.netty.channel.nio.NioIoHandler
import kotlinx.coroutines.runBlocking

object Server {
    var configFile: String? = null
    var port: Int = 0
    var save: String = ""
    val bossGroup = NioIoHandler.newFactory()

    fun start() = runBlocking {

    }
}
