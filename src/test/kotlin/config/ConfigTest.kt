package config

import org.junit.jupiter.api.Assertions.assertEquals
import valkey.kotlin.config.loadServerConfigFromString
import valkey.kotlin.server.Server
import kotlin.test.Test

class ConfigTest {
    @Test
    fun loadServerConfigFromString() {
        loadServerConfigFromString("# port 6380")
        assertEquals(0, Server.port)

        loadServerConfigFromString("port 6380")
        assertEquals(6380, Server.port)
    }
}
