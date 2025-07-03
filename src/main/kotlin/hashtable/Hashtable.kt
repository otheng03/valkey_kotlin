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
    val tables: Array<Array<HashtableBucket?>> = arrayOf(arrayOfNulls(1), arrayOfNulls(1)),
    val used: Array<UInt> = arrayOf(0u, 0u),
    val bucketExponential: Array<Int> = arrayOf(0, 0),
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

            val mask: Long = expToMask(bucketExponential[table])
            val bucketIdx = hash and mask.toULong() // TODO: Check type safety of toULong
            if (table == 0 && rehashIdx >= 0 && bucketIdx < rehashIdx.toUInt()) {   // TODO: Check type safety of toUInt
                continue
            }
            val bucket = tables[table].get(bucketIdx.toInt())   // TODO: Check type safety of toInt
            do {
                // Find candidate entries with presence flag set and matching h2 hash.
                for (pos in 0..numBucketPositions(bucket)) {
                    if (bucket.isPositionFilled(pos) && bucket.hashes[pos] == h2) {

                    }
                }
            } while (bucket != null)
        }
    }

    fun numBucketPositions(bucket: HashtableBucket?): Int {
        return ENTRIES_PER_BUCKET - (if (bucket?.chained == true) 1 else 0)
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
