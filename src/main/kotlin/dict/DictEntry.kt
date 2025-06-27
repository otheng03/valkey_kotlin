package valkey.kotlin.dict

data class DictEntry(
    val key: String,
    var value: Value,
    var next: DictEntry? = null
) {
    sealed class Value {
        data class any(val data: Any?) : Value()
        data class u64(val data: ULong) : Value()
        data class s64(val data: Long) : Value()
        data class d(val data: Double) : Value()
    }
}