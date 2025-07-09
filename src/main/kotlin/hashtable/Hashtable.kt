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

fun ULong.highBits(): UByte {
    val CHAR_BIT = 8
    return (this shr (CHAR_BIT * 7)).toUByte()
}

data class Entry(
    val key: String
)

abstract class Hashtable(
    var rehashIdx: ssize_t = -1,
    val tables: Array<Array<HashtableBucket?>> = arrayOf(emptyArray(), emptyArray()),
    val used: Array<size_t> = arrayOf(0u, 0u),
    val bucketExp: Array<int8_t> = arrayOf(-1, -1),
    val childBuckets: Array<size_t> = arrayOf(0u, 0u),
    var pauseRehash: int16_t = 0,
    var pauseAutoShrink: int16_t = 0,
    //val childBuckets: Array<size_t> = arrayOf(0u, 0u),
    //val metadata: Metadata,
    // From hashtableType
    var instantRehashing: UInt = 0u
) {
    init {
        for (tableIdx in 0..1) {
            resetTable(tableIdx)
        }
    }

    fun resetTable(tableIdx: Int) {
        tables[tableIdx] = emptyArray()
        used[tableIdx] = 0u
        bucketExp[tableIdx] = -1
        childBuckets[tableIdx] = 0u
    }

    fun size(): size_t {
        return used[0] + used[1]
    }

    open fun hashKey(key: String): ULong {
        // TODO : Use siphash
        return key.hashCode().toULong()
    }

    fun find(key: String): Pair<Hashtable?, /*found*/ Boolean> {
        if (size() == 0uL)
            return Pair(null, false)
        val hash = hashKey(key)
        val posInBucket = 0
        val ret = findBucket(hash, key)
        // TODO
        return Pair(null, false)
    }

    fun findBucket(hash: ULong, key: String): Pair</*pos in bucket*/ Int, HashtableBucket?>? {
        if (size() == 0uL)
            return Pair(0, null)
        val h2 = hash.highBits()
        var table: Int

        // TODO: this.rehashStepOnReadIfNeeded()

        for (table in 0..1) {
            if (used[table] == 0uL)
                continue

            val mask: Long = expToMask(bucketExp[table].toInt())
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
        // TODO
        return null
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

    fun addOrFind(entry: Entry): Pair</*success*/ Boolean, /*existing entry*/ Entry?> {
        val key = entryGetKey(entry)
        val hash = hashKey(key)
        val ret = findBucket(hash, key)
        val posInBucket = ret?.let { ret.first } ?: 0
        val hashTable = ret?.second
        return hashTable?.let {
            Pair(false, hashTable.entries[posInBucket])
        } ?: run {
            insert(hash, entry)
            Pair(true, entry)
        }
    }

    fun insert(hash: ULong, entry: Entry?) {
        expandIfNeeded()
        rehashStepOrWriteifNeeded()
        val bucket = findBucketForInsert(hash)
        TODO("Not yet implemented")
    }

    fun expandIfNeeded() {
        TODO("Not yet implemented")
    }

    fun rehashStepOrWriteifNeeded() {
        TODO("Not yet implemented")
    }

    fun findBucketForInsert(hash: ULong): Pair</*pos in bucket*/ Int, /*table index*/ Int> {
        return Pair(0, 0)
    }

    abstract fun entryGetKey(entry: Entry): String
    abstract fun hashFunction(key: String): uint64_t
    abstract fun keyCompare(key1: String, key2: String): Int
    abstract fun resizeAllowed(moreMem: size_t, usedRatio: Double): Int
    abstract fun getMetadataSize(): size_t

    open fun entryDestructor(entry: Entry) {
        // Do nothing
    }

    open fun entryPrefetchValue(entry: Entry) {
        // Do nothing
    }

    open fun rehashingStarted() {
        // Do nothing
    }

    open fun rehashingCompleted() {
        // Do nothing
    }
}

class KVStoreKeysHashtable : Hashtable() {
    override fun entryGetKey(entry: Entry): String {
        return entry.key
    }

    override fun hashFunction(key: String): uint64_t {
        //hashtableSdsHash
        TODO("Not yet implemented")
    }

    override fun keyCompare(key1: String, key2: String): Int {
        //hashtableSdsKeyCompare
        TODO("Not yet implemented")
    }

    override fun resizeAllowed(moreMem: size_t, usedRatio: Double): Int {
        //hashtableResizeAllowed
        TODO("Not yet implemented")
    }

    override fun entryDestructor(entry: Entry) {
        //hashtableObjectDestructor
        TODO("Not yet implemented")
    }

    override fun entryPrefetchValue(entry: Entry) {
        //hashtableObjectPrefetchValue
        TODO("Not yet implemented")
    }

    override fun rehashingStarted() {
        //kvstoreHashtableRehashingStarted
        TODO("Not yet implemented")
    }

    override fun rehashingCompleted() {
        //kvstoreHashtableRehashingCompleted
        TODO("Not yet implemented")
    }

    override fun getMetadataSize(): size_t {
        //kvstoreHashtableMetadataSize
        TODO("Not yet implemented")
    }
}