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
    val chained: Boolean,
    val presence: UByte,
    val hashes: Array<Byte?> = arrayOfNulls(ENTRIES_PER_BUCKET),
    val entries: Array<Any?> = arrayOfNulls(ENTRIES_PER_BUCKET)
) {
    fun isPositionFilled(position: Int): Boolean {
        return presence and ((1 shl position).toUByte()) > 0u
    }
}
