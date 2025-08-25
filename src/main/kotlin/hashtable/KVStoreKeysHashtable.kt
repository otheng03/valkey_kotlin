package valkey.kotlin.hashtable

import valkey.kotlin.database.KVStore
import valkey.kotlin.list.VKListNode
import valkey.kotlin.types.*


/**
 * kvstoreHashtableMetadata is a small per-hashtable auxiliary record the kvstore layer attaches
 * to each underlying hashtable instance. It carries two key pieces of information:
 */
class KVStoreHashtableMetadata(
    var rehashingNode: VKListNode<Any?>?,
    var kvs: KVStore
)

class KVStoreKeysHashtable(
    val kvs: KVStore,
    val metadata: KVStoreHashtableMetadata
) : Hashtable() {
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

    // kvstoreHashtableRehashingStarted is the C implementation of this function
    override fun rehashingStarted() {
        kvs.rehashing.addNodeTail(this)
        metadata.rehashingNode = kvs.rehashing.tail!!

        val (from, to) = rehashingInfo()
        kvs.bucketCount += to
        kvs.overheadHashtableRehashing += from * HASHTABLE_BUCKET_SIZE
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
