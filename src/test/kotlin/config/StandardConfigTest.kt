package config

import org.junit.jupiter.api.Assertions.*
import valkey.kotlin.config.DEFAULT_PORT
import valkey.kotlin.config.standardConfigs
import valkey.kotlin.server.Server
import kotlin.test.Test

class StandardConfigTest {
    @Test
    fun ensureDefaultValueOfServerPort() {
        standardConfigs["port"]!!.init()
        assertEquals(DEFAULT_PORT, Server.port)
    }

    @Test
    fun ensureServerPortIsSet() {
        standardConfigs["port"]!!.set("12345")
        assertEquals(12345, Server.port)
    }
}
