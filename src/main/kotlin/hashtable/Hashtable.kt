package valkey.kotlin.hashtable

import valkey.kotlin.hashtable.HashtableConstants.BUCKET_DIVISOR
import valkey.kotlin.hashtable.HashtableConstants.BUCKET_FACTOR
import valkey.kotlin.hashtable.HashtableConstants.MAX_FILL_PERCENT_HARD
import valkey.kotlin.hashtable.HashtableConstants.MAX_FILL_PERCENT_SOFT
import valkey.kotlin.types.*
import kotlin.math.min

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
    val tables: Array<Array<HashtableBucket>> = arrayOf(emptyArray(), emptyArray()),
    val used: Array<size_t> = arrayOf(0u, 0u),
    val bucketExp: Array<int8_t> = arrayOf(-1, -1),
    val childBuckets: Array<size_t> = arrayOf(0u, 0u),
    var resizePolicy: HashtableResizePolicy = HashtableResizePolicy.ALLOW,
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

            val mask: size_t = expToMask(bucketExp[table].toInt())
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

    fun expToMask(exp: Int): size_t {
        return (if (exp == -1) 0 else (numBuckets(exp) - 1uL)) as size_t
    }

    fun numBuckets(exp: Int): size_t {
        return if (exp == -1) 0uL else 1uL shl exp
    }

    fun isRehashing(): Boolean {
        return rehashIdx != -1L
    }

    // Adds an entry and returns 1 on success. Returns 0 if there was already an entry with the same key.
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

    fun expandIfNeeded(): Int {
        val minCapacity = used[0] + used[1] + 1uL
        val tableIndex = if (isRehashing()) 1 else 0
        val numBuckets = numBuckets(bucketExp[tableIndex].toInt())
        val currentCapacity = numBuckets * ENTRIES_PER_BUCKET.toULong()
        val maxFillPercent = if (resizePolicy == HashtableResizePolicy.ALLOW) MAX_FILL_PERCENT_SOFT else MAX_FILL_PERCENT_HARD

        if (minCapacity * 100uL <= currentCapacity * maxFillPercent) {
            return 0
        }

        return resize(minCapacity, null)
    }

    // Helper function based on the C code
    private fun nextBucketExp(minCapacity: size_t): Int {
        if (minCapacity == 0uL) return -1

        // ceil(x / y) = floor((x - 1) / y) + 1
        val minBuckets = (minCapacity * BUCKET_FACTOR.toULong() - 1uL) / BUCKET_DIVISOR.toULong() + 1uL
        if (minBuckets >= ULong.MAX_VALUE / 2uL) return (ULong.SIZE_BITS - 1)
        if (minBuckets == 1uL) return 0

        // Find the position of the highest set bit (equivalent to __builtin_clzl)
        val leadingZeros = (minBuckets - 1uL).countLeadingZeroBits()
        return ULong.SIZE_BITS - leadingZeros
    }

    fun resize(minCapacity: size_t, policy: HashtableResizePolicy?): Int {
        // Adjust minimum size. We don't resize to zero currently.
        val adjustedMinCapacity = if (minCapacity == 0uL) 1uL else minCapacity

        // Size of new table.
        val exp = nextBucketExp(adjustedMinCapacity)
        val numBuckets = numBuckets(exp)
        val newCapacity = numBuckets * ENTRIES_PER_BUCKET.toULong()

        if (newCapacity < adjustedMinCapacity || numBuckets * HASHTABLE_BUCKET_SIZE.toULong() < numBuckets) {
            // Overflow
            return 0
        }

        val oldExp = bucketExp[if (isRehashing()) 1 else 0]

        if (exp == oldExp.toInt()) {
            // Can't resize to same size.
            return 0
        }

        // We can't resize if rehashing is already ongoing. Fast-forward ongoing
        // rehashing before we continue. This can happen only in exceptional
        // scenarios, such as when many insertions are made while rehashing is
        // paused.
        if (isRehashing()) {
            if (pauseRehash > 0) return 0
            while (isRehashing()) {
                rehashStep()
            }
        }

        val effectivePolicy = policy ?: resizePolicy

        if (effectivePolicy == HashtableResizePolicy.FORBID && tables[0].isNotEmpty()) {
            // Refuse to resize if resizing is forbidden and we already have a primary table.
            return 0
        }

        if (exp > oldExp) {
            // If we're growing the table, let's check if the resizeAllowed callback allows the resize.
            val fillFactor = adjustedMinCapacity.toDouble() / (numBuckets(oldExp.toInt()).toDouble() * ENTRIES_PER_BUCKET)
            if (fillFactor * 100 < MAX_FILL_PERCENT_HARD.toDouble() && resizeAllowed(numBuckets * HASHTABLE_BUCKET_SIZE.toULong(), fillFactor) == 0) {
                // Resize callback says no.
                return 0
            }
        }

        // Allocate the new hash table.
        val newTable = Array<HashtableBucket>(numBuckets.toInt()) { HashtableBucket(chained = false, presence = 0u) }

        bucketExp[1] = exp.toByte()
        tables[1] = newTable
        used[1] = 0u
        rehashIdx = 0

        rehashingStarted()

        // If the old table was empty, the rehashing is completed immediately.
        if (tables[0].isEmpty() || (used[0] == 0uL && childBuckets[0] == 0uL)) {
            rehashingCompleted()
        } else if (instantRehashing != 0u) {
            while (isRehashing()) {
                rehashStep()
            }
        }

        return 1
    }

    private fun rehashBucket(b: HashtableBucket) {
        var pos: Int
        for (pos in 0 until numBucketPositions(b)) {
            if (b.isPositionFilled(pos))
                continue

            val entry = b.entries[pos]!!
            val h2: uint8_t = b.hashes[pos]
            val hash: uint64_t
            // When shrinking, it's possible to avoid computing the hash.
            // We can just use idx has the hash.
            if (bucketExp[1] < bucketExp[0]) {
                hash = rehashIdx.toULong()
            } else {
                hash = hashEntry(entry)
            }
            val (dst, posInDstBucket) = findBucketForInsert(hash)
            val dstBucket = if (isRehashing()) tables[1][dst] else tables[0][dst]
            dstBucket.entries[posInDstBucket] = entry
            dstBucket.hashes[posInDstBucket] = h2
            dstBucket.presence = dstBucket.presence or (1 shl posInDstBucket).toUByte()
            used[0]--
            used[1]++
        }
    }

    private fun hashEntry(entry: Entry): uint64_t {
        return hashKey(entryGetKey(entry))
    }

    private fun rehashStep() {
        // Implementation for rehashing a single step would go here
        // This is a complex operation that moves entries from old table to new table
        TODO("rehashStep implementation needed")
    }

    fun rehashStepOrWriteifNeeded() {
        TODO("Not yet implemented")
    }

    // TODO : Change the return type to (HashtableBucket?, Int)
    fun findBucketForInsert(hash: ULong): Pair</*pos in bucket*/ Int, /*table index*/ Int> {
        val table = if (isRehashing()) 1 else 0
        val mask = expToMask(table)
        val bucketIdx: Int = (hash and mask).toInt()
        val b = tables[table][bucketIdx]
        while (b.isFull()) {

        }
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