package valkey.kotlin.hashtable

/* --- Global Constants --- */

// bytes, the most common cache line size
const val HASHTABLE_BUCKET_SIZE = 64

// Scan flags
const val HASHTABLE_SCAN_EMIT_REF = 1 shl 0

// Iterator flags
const val HASHTABLE_ITER_SAFE = 1 shl 0
const val HASHTABLE_ITER_PREFETCH_VALUES = 1 shl 1

/* --- Global variables --- */

val hashFunctionSeed = ByteArray(16)

/* --- Global functions --- */

fun hashtableSetHashFunctionSeed(seed: ByteArray) {
    require(seed.size == hashFunctionSeed.size) {
        "Seed size must be ${hashFunctionSeed.size} bytes"
    }
    seed.copyInto(hashFunctionSeed)
}

class Hashtable(
    val instantRehashing: UInt,
    val rehashIdx: Long,
    val tables: Bucket,
    val used: ULong,
    val bucketExponential: Array<Byte> = arrayOf(0, 0),
    val pauseRehash: Short,
    val pauseAutoShrink: Short,
    val childBuckets: Array<ULong> = arrayOf(0u, 0u),
    val metadata: Metadata,
) {
}
