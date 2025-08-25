package valkey.kotlin.database

import valkey.kotlin.hashtable.Hashtable
import valkey.kotlin.list.VKList
import valkey.kotlin.types.size_t

const val KVSTORE_ALLOCATE_HASHTABLES_ON_DEMAND = (1 shl 0)
const val KVSTORE_FREE_EMPTY_HASHTABLES = (1 shl 1)

class KVStore (
    val hashtable: Array<Hashtable> = emptyArray(),
    val numHashtables: Int = 0,
    val numHashtablesBits: Int = 0,
    val rehashing: VKList<Any?> = VKList(),
    val hashTableSizeIndex: Array<ULong> = emptyArray(),
    var bucketCount: ULong = 0u,
    var overheadHashtableRehashing: size_t = 0u      /* Overhead of hash tables rehashing in bytes. */

    /*
    int flags;
    hashtableType *dtype;
    hashtable **hashtables;
    int num_hashtables;
    int num_hashtables_bits;
    list *rehashing;                          /* List of hash tables in this kvstore that are currently rehashing. */
    int resize_cursor;                        /* Cron job uses this cursor to gradually resize hash tables (only used if num_hashtables > 1). */
    int allocated_hashtables;                 /* The number of allocated hashtables. */
    int non_empty_hashtables;                 /* The number of non-empty hashtables. */
    unsigned long long key_count;             /* Total number of keys in this kvstore. */
    unsigned long long bucket_count;          /* Total number of buckets in this kvstore across hash tables. */
    unsigned long long *hashtable_size_index; /* Binary indexed tree (BIT) that describes cumulative key frequencies up until
                                               * given hashtable-index. */
    size_t overhead_hashtable_lut;            /* Overhead of all hashtables in bytes. */
    size_t overhead_hashtable_rehashing;      /* Overhead of hash tables rehashing in bytes. */
     */
) {
    companion object Factory {
        fun create(): KVStore{
            // Do not support cluster mode
            return KVStore()
        }
    }

    fun kvstoreGetHashtable(dictIndex: Int): Hashtable? {
        return hashtable[dictIndex]
    }

    fun kvstorehashtableFind(dictIndex: Int, key: String): Pair<Hashtable?, /*found*/ Boolean> {
        val ht: Hashtable? = kvstoreGetHashtable(dictIndex)
        return ht?.find(key) ?: Pair(null, false)
    }
}

// Returns which dict index should be used with kvstore for a given key.
fun getKVStoreIndexForKey(key: String): Int {
    // TODO: Implement several KVStore for a DB
    return 0
}

