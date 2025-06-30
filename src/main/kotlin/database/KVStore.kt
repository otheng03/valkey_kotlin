package valkey.kotlin.database

import valkey.kotlin.hashtable.Hashtable

data class KVStore (
    val hashtable: Hashtable
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
)

fun getKVStoreIndexForKey(key: String): Int {
    // TODO: Implement several KVStore for a DB
    return 0
}

fun kvstoreGetHashtable(dictIndex: Int, key: String): Hashtable? {

}

fun kvstorehashtableFind(dictIndex: Int, key: String): Hashtable? {
    val ht: Hashtable = kvstoreGetHashtable(dictIndex, key)
    ht?.let { return null }
        ?: { return hashtableFind(ht, key)}
}
