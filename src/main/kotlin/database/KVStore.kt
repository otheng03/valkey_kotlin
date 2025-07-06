package valkey.kotlin.database

import valkey.kotlin.hashtable.Hashtable
import valkey.kotlin.hashtable.KVStoreKeysHashtable

class KVStore (
    val hashtable: Array<Hashtable> = arrayOf(KVStoreKeysHashtable(), KVStoreKeysHashtable())
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

