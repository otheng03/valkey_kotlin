package valkey.kotlin.hashtable

import valkey.kotlin.types.*

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
    var rehashIdx: ssize_t,
    val tables: Array<Array<HashtableBucket?>> = arrayOf(emptyArray(), emptyArray()),
    val used: Array<size_t> = arrayOf(0u, 0u),
    val bucketExp: Array<int8_t> = arrayOf(0, 0),
    val pauseRehash: int16_t,
    val pauseAutoShrink: int16_t,
    val childBuckets: Array<size_t> = arrayOf(0u, 0u),
    val metadata: Metadata,
) {
    init {
        rehashIdx = -1
        for (tableIdx in 0..1) {
            resetTable(tableIdx)
        }
    }

    fun resetTable(tableIdx: Int) {
        tables[tableIdx] = emptyArray()
        bucketExp[tableIdx] = -1
        used[tableIdx] = 0u
    }

    fun size(): UInt {
        return used[0] + used[1]
    }

    open fun hashKey(key: String): ULong {
        // TODO : Use siphash
        return key.hashCode().toULong()
    }

    fun find(key: String): Pair<Hashtable?, /*found*/ Boolean> {
        if (size() == 0u)
            return Pair(null, false)
        val hash = hashKey(key)
        val posInBucket = 0
        val ret = findBucket(hash, key)
    }

    fun findBucket(hash: ULong, key: String): Pair<HashtableBucket?, Int>? {
        if (size() == 0u)
            return Pair(null, 0)
        val h2 = hash.highBits()
        var table: Int

        // TODO: this.rehashStepOnReadIfNeeded()

        for (table in 0..1) {
            if (used[table] == 0u)
                continue

            val mask: Long = expToMask(bucketExp[table])
            val bucketIdx = hash and mask.toULong() // TODO: Check type safety of toULong
            if (table == 0 && rehashIdx >= 0 && bucketIdx < rehashIdx.toUInt()) {   // TODO: Check type safety of toUInt
                continue
            }
            val bucket = tables[table].get(bucketIdx.toInt())   // TODO: Check type safety of toInt
            do {
                // Find candidate entries with presence flag set and matching h2 hash.
                for (pos in 0..numBucketPositions(bucket)) {
                    if (bucket?.isPositionFilled(pos) == true && bucket.hashes[pos] == h2) {
                    }
                }
            } while (bucket != null)
        }
    }

    fun numBucketPositions(hashtableBucket: HashtableBucket?): Int {
        return ENTRIES_PER_BUCKET - (if (hashtableBucket?.chained == true) 1 else 0)
    }

    fun expToMask(exp: Int): Long {
        return if (exp == -1) 0 else numBuckets(exp) - 1
    }

    fun numBuckets(exp: Int): Long {
        return if (exp == -1) 0L else 1L shl exp
    }
}

fun ULong.highBits(): UByte {
    val CHAR_BIT = 8
    return (this shr (CHAR_BIT * 7)).toUByte()
}
