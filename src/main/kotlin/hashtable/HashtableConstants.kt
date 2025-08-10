package valkey.kotlin.hashtable

object HashtableConstants {
    const val BUCKET_SIZE = 64 // bytes, the most common cache line size
    const val ENTRIES_PER_BUCKET = 7 // for 64-bit systems
    const val BUCKET_FACTOR = 5
    const val BUCKET_DIVISOR = 32
    const val MAX_FILL_PERCENT_SOFT = 100uL
    const val MAX_FILL_PERCENT_HARD = 500uL
    const val MIN_FILL_PERCENT_SOFT = 13
    const val MIN_FILL_PERCENT_HARD = 3
}