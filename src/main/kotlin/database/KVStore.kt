package valkey.kotlin.database

import valkey.kotlin.hashtable.Hashtable
import valkey.kotlin.hashtable.HashtableBucket
import valkey.kotlin.list.VKList
import valkey.kotlin.types.size_t

const val KVSTORE_ALLOCATE_HASHTABLES_ON_DEMAND = (1 shl 0)
const val KVSTORE_FREE_EMPTY_HASHTABLES = (1 shl 1)

class KVStore<T : Hashtable> (
    val hashtable: List<T> = emptyList(),
    val numHashtables: Int = 0,
    val numHashtablesBits: Int = 0,
    val rehashing: VKList<Any?> = VKList(),
    val hashTableSizeIndex: Array<ULong> = emptyArray(),
    var bucketCount: ULong = 0u,
    var overheadHashtableRehashing: size_t = 0u      /* Overhead of hash tables rehashing in bytes. */
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

