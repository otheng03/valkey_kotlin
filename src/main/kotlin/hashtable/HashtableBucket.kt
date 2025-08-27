package valkey.kotlin.hashtable

/*
 * Original Bucket of valkey is optimized to fit within a CPU cache line.
 * In this kotlin toy project, the Bucket implementation is simplified for better understanding and easier understanding.
 * In a 64-bit system, there are up to 7 entries per bucket.
 * These are unordered and an entry can b inserted in any of the free slots.
 *
 *  * Bucket layout
 *         +------------------------------------------------------------------+
 *         | Metadata | Entry | Entry | Entry | Entry | Entry | Entry | Entry |
 *         +------------------------------------------------------------------+
 *        /            ` - - . _ _
 *       /                         `- - . _ _
 *      /                                     ` - . _
 *     +----------------------------------------------+
 *     | c ppppppp hash hash hash hash hash hash hash |
 *     +----------------------------------------------+
 *      |    |       |
 *      |    |      One byte of hash for each entry position in the bucket.
 *      |    |
 *      |   Presence bits. One bit for each entry position, indicating if an
 *      |   entry present or not.
 *      |
 *     Chained? One bit. If set, the last entry is a child bucket pointer.
 *
 * 64-bit version, 7 entries per bucket:
 *     1 bit     7 bits    [1 byte] x 7  [8 bytes] x 7 = 64 bytes
 *     chained   presence  hashes        entries
 */

const val ENTRIES_PER_BUCKET = 7
typealias BUCKET_BITS_TYPE  = UByte
const val BITS_NEEDED_TO_STORE_POS_WITHIN_BUCKET = 3

data class HashtableBucket (
    var chained: Boolean,
    var presence: UByte,
    val hashes: Array<UByte> = arrayOf(0u, 0u, 0u, 0u, 0u, 0u, 0u),
    val entries: Array<Any?> = arrayOfNulls(ENTRIES_PER_BUCKET)
) {
    companion object Factory {
        fun create(): HashtableBucket {
            return HashtableBucket(false, 0u)
        }
    }

    fun isPositionFilled(position: Int): Boolean {
        return presence and ((1 shl position).toUByte()) > 0u
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HashtableBucket

        if (chained != other.chained) return false
        if (presence != other.presence) return false
        if (!hashes.contentEquals(other.hashes)) return false
        if (!entries.contentEquals(other.entries)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = chained.hashCode()
        result = 31 * result + presence.hashCode()
        result = 31 * result + hashes.contentHashCode()
        result = 31 * result + entries.contentHashCode()
        return result
    }

    fun numBucketPositions(): Int {
        return ENTRIES_PER_BUCKET - (if (chained) 1 else 0)
    }

    fun isFull(): Boolean {
        return presence.toInt() == ((1 shl numBucketPositions()) - 1)
    }

    fun getChildBucket(): HashtableBucket? {
        return (if (chained) entries[ENTRIES_PER_BUCKET - 1] else null) as HashtableBucket?;
    }

    fun convertToChained() {
        assert(!chained)
        val pos = ENTRIES_PER_BUCKET - 1
        assert(isPositionFilled(pos))
        val child = HashtableBucket.create()
        moveEntryTo(pos, child, 0)
        chained = true
        entries[pos] = child
    }

    fun convertToUnchained() {
        assert(chained)
        chained = false
        assert(!isPositionFilled(ENTRIES_PER_BUCKET - 1))
    }

    fun moveEntryTo(posFrom: Int, bucketTo: HashtableBucket, posTo: Int) {
        bucketTo.entries[posTo] = entries[posFrom]
        bucketTo.hashes[posTo] = hashes[posFrom]
        bucketTo.presence = presence or (1 shl posTo).toUByte()
        presence = presence and (1 shl posFrom).inv().toUByte()
    }
}
