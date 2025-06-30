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

open class Hashtable(
    val instantRehashing: UInt,
    val rehashIdx: Long,
    val tables: Bucket,
    val used: Array<UInt> = arrayOf(0u, 0u),
    val bucketExponential: Array<Byte> = arrayOf(0, 0),
    val pauseRehash: Short,
    val pauseAutoShrink: Short,
    val childBuckets: Array<ULong> = arrayOf(0u, 0u),
    val metadata: Metadata,
) {
    fun size(): UInt {
        return used[0] + used[1]
    }

    open fun hashKey(key: String): ULong {
        // TODO : Use siphash
        return key.hashCode().toULong()
    }

    fun find(dictIndex: Int, key: String): Hashtable? {
        if (size() == 0u)
            return null
        val hash = hashKey(key)
        val posInBucket = 0
        val ret = findBucket(hash, key)
    }

    fun findBucket(hash: ULong, key: String): Pair<Bucket, Int>? {
        if (size() == 0u)
            return null
        val h2 = hash.highBits()
    }
}

fun ULong.highBits(): UByte {
    val CHAR_BIT = 8
    return (this shr (CHAR_BIT * 7)).toUByte()
}
