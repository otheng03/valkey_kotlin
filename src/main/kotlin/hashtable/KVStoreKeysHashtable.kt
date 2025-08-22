package valkey.kotlin.hashtable

import valkey.kotlin.types.*

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
