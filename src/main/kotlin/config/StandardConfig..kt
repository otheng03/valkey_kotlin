package valkey.kotlin.kotlin.config

typealias TypeData = Any

enum class ConfigType {
    BOOLEAN_CONFIG, STRING_CONFIG, INTEGER_CONFIG
}

interface TypeInterface {
    fun init()
    fun set()
    fun get()
    fun rewrite()
}

data class StandardConfig(
    val name: String,         // The user visible name of this config
    val alias: String?,       // An alias that can also be used for this config
    val flags: UInt,
    val interfaceFunc: TypeInterface
    // TODO: configType
    // TODO: privdata
) {
};

class PortTypeInterface(var port: Int) : TypeInterface {
    override fun init() {
        TODO("Not yet implemented")
    }

    override fun set() {
        TODO("Not yet implemented")
    }

    override fun get() {
        TODO("Not yet implemented")
    }

    override fun rewrite() {
        TODO("Not yet implemented")
    }
}

class SaveTypeInterface(var option: String) : TypeInterface {
    override fun init() {
        TODO("Not yet implemented")
    }

    override fun set() {
        TODO("Not yet implemented")
    }

    override fun get() {
        TODO("Not yet implemented")
    }

    override fun rewrite() {
        TODO("Not yet implemented")
    }
}

const val IMMUTABLE_CONFIG: UInt = 1u
const val MODIFIABLE_CONFIG: UInt = 2u

val standardConfigs = listOf(
    StandardConfig("port", null, IMMUTABLE_CONFIG, PortTypeInterface(6379)),
    StandardConfig("save", null, MODIFIABLE_CONFIG, SaveTypeInterface("")),
)