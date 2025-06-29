package valkey.kotlin

infix fun Int.hasFlag(flag: Int): Boolean = this and flag != 0