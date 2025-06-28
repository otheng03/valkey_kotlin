package valkey.kotlin.config

import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

fun initConfigValues() {
    standardConfigs.forEach { _, option -> option.init() }
}

fun parseArg(arg: String, lenOnly: Boolean = false): Pair<Int, String?> {
    val dst = if (!lenOnly) StringBuilder() else null
    var inq = false
    var insq = false
    var done = false
    var i = 0

    fun isHexDigit(c: Char): Boolean = c in '0'..'9' || c in 'a'..'f' || c in 'A'..'F'
    fun hexDigitToInt(c: Char): Int = when (c) {
        in '0'..'9' -> c - '0'
        in 'a'..'f' -> 10 + (c - 'a')
        in 'A'..'F' -> 10 + (c - 'A')
        else -> 0
    }

    while (!done && i < arg.length) {
        val p = arg[i]
        var newChar: Char? = null

        if (inq) {
            if (p == '\\' && i + 3 < arg.length && arg[i + 1] == 'x' && isHexDigit(arg[i + 2]) && isHexDigit(arg[i + 3])) {
                val byte = (hexDigitToInt(arg[i + 2]) shl 4) + hexDigitToInt(arg[i + 3])
                newChar = byte.toChar()
                i += 3
            } else if (p == '\\' && i + 1 < arg.length) {
                i++
                newChar = when (arg[i]) {
                    'n' -> '\n'
                    'r' -> '\r'
                    't' -> '\t'
                    'b' -> '\b'
                    'a' -> '\u0007'
                    else -> arg[i]
                }
            } else if (p == '"') {
                inq = false
            } else {
                if (p == '\\' && i + 1 >= arg.length) return Pair(0, null)
                newChar = p
            }
        } else if (insq) {
            if (p == '\\' && i + 1 < arg.length && arg[i + 1] == '\\' && arg[i + 2] == '\\' && arg[i + 3] == '\'') {
                i += 3
                newChar = '\''
            } else if (p == '\\' && i + 1 < arg.length && arg[i + 1] == '\'') {
                i++
                newChar = '\''
            } else if (p == '\'') {
                insq = false
            } else {
                if (p == '\\' && i + 1 >= arg.length) return Pair(0, null)
                newChar = p
            }
        } else {
            when (p) {
                ' ', '\n', '\r', '\t', '\u0000' -> done = true
                '"' -> inq = true
                '\'' -> insq = true
                else -> newChar = p
            }
        }

        newChar?.let { dst?.append(it) }
        i++
    }

    return Pair(i, dst?.toString())
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
