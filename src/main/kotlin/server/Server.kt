package valkey.kotlin.server

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.netty.channel.nio.NioIoHandler

fun Application.module() {
    configureRouting()
}

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hwllo, World!")
        }
    }
}

object Server {
    var configFile: String? = null
    var port: Int = 0
    var save: String = ""
    val bossGroup = NioIoHandler.newFactory()

    fun start() {
        embeddedServer(
            Netty,
            port = port,
            host = "0.0.0.0",
            module = Application::module
        ).start(wait = true)
    }
}
