package valkey.kotlin.kotlin.config

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class Config(val mutable: Boolean)

internal interface StandardConfig {
    fun init() {}
    fun set() {}
    fun get() {}
    fun rewrite() {}
}

@Config(mutable = false)
internal object Port : StandardConfig {
    var port: Int = 6379

    override fun init() {
    }

    override fun set() {
    }

    override fun get() {
    }

    override fun rewrite() {
    }
}

@Config(mutable = true)
internal object Save : StandardConfig {
    var option: String = ""

    override fun init() {
    }

    override fun set() {
    }

    override fun get() {
    }

    override fun rewrite() {
    }
}

@Config(mutable = true)
internal object ServerCPUList : StandardConfig {
    override fun init() {
    }

    override fun set() {
    }

    override fun get() {
    }

    override fun rewrite() {
    }
}

internal val standardConfigs = mapOf(
    "port" to Port,
    "save" to Save,
    "server-cpulist" to ServerCPUList,
    "server_cpulist" to ServerCPUList
)