package valkey.kotlin.config

import valkey.kotlin.server.Server

const val DEFAULT_PORT = 6379
const val DEFAULT_SAVE = ""

internal interface StandardConfig<T> {
    fun init()
    fun set(optionValue: String)
    fun get(): T
}

internal class IntegerConfig(
    val default: Int,
    val setter: (Int) -> Unit,
    val getter: () -> Int,
) : StandardConfig<Int> {
    override fun init() {
        setter(default)
    }

    override fun set(optionValue: String) {
        optionValue.toIntOrNull()?.let { setter(it) }
    }

    override fun get(): Int {
        return getter()
    }
}

internal class StringConfig(
    val default: String,
    val setter: (String) -> Unit,
    val getter: () -> String,
) : StandardConfig<String> {
    override fun init() {
        setter(default)
    }

    override fun set(optionValue: String) {
        setter(optionValue)
    }

    override fun get(): String {
        return getter()
    }
}

internal val standardConfigs = mapOf(
    "port" to IntegerConfig(
        default = DEFAULT_PORT,
        setter = { value -> Server.port = value },
        getter = { Server.port }
    ),
    "save" to StringConfig(
        default = DEFAULT_SAVE,
        setter = { value -> Server.save = value },
        getter = { Server.save }
    )
)
