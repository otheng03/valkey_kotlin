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
    var kvs: KVStore<KVStoreKeysHashtable>
)

class KVStoreKeysHashtable(
    val kvs: KVStore<KVStoreKeysHashtable>,
    val metadata: KVStoreHashtableMetadata
) : Hashtable() {
    companion object Factory {
        fun create(kvs: KVStore<KVStoreKeysHashtable>): KVStoreKeysHashtable {
            return KVStoreKeysHashtable(kvs, KVStoreHashtableMetadata(null, kvs))
        }
    }

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
        kvs.rehashing.addNodeTail(this)
        metadata.rehashingNode = kvs.rehashing.tail!!

        val (from, to) = rehashingInfo()
        kvs.bucketCount += to
        kvs.overheadHashtableRehashing += from * HASHTABLE_BUCKET_SIZE
    }

    override fun rehashingCompleted() {
        metadata.rehashingNode.let {
            kvs.rehashing.unlinkNode(metadata.rehashingNode!!)
            metadata.rehashingNode = null
        }

        val (from, to) = rehashingInfo()
        kvs.bucketCount -= from
        kvs.overheadHashtableRehashing -= from * HASHTABLE_BUCKET_SIZE

        _rehashingCompleted()
    }

    override fun getMetadataSize(): size_t {
        //kvstoreHashtableMetadataSize
        TODO("Not yet implemented")
    }
}
