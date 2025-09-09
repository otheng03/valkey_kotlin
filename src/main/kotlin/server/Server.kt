package valkey.kotlin.server

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.readUTF8Line
import io.ktor.utils.io.writeStringUtf8
import io.netty.channel.nio.NioIoHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hwllo, World!")
        }
    }
}

object Server {
    private val logger = KotlinLogging.logger {}

    var configFile: String? = null
    var port: Int = 0
    var save: String = ""
    val bossGroup = NioIoHandler.newFactory()

    fun start() = runBlocking {
        val selectorManager = SelectorManager(Dispatchers.IO)
        val serverSocket = aSocket(selectorManager).tcp().bind("127.0.0.1", port)
        logger.info { "Server started on port $port" }
        while (true) {
            val socket = serverSocket.accept()
            logger.info { "Client connected: ${socket.remoteAddress}" }
            launch {
                val receiveChannel = socket.openReadChannel()
                val sendChannel = socket.openWriteChannel(autoFlush = true)
                sendChannel.writeStringUtf8("I'm simple echo server!\n")
                while (true) {
                    val line = receiveChannel.readUTF8Line() ?: break
                    logger.info { "[${socket.remoteAddress}] Received: $line" }
                    sendChannel.writeStringUtf8("$line\n")
                }
            }
        }
    }
}
