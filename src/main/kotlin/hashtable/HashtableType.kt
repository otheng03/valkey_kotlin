package valkey.kotlin.kotlin.hashtable

interface HashtableType {
    fun entryGetKey(entry: Entry): Entry
    fun hashFunction(key: Entry): Long
    fun keyCompare(key1: Entry, key2: Entry): Boolean
    fun entryDestructor(entry: Entry)
    fun entryPrefetchValue(entry: Entry)
    fun resizeAllowed(moreMem: Long, usedRatio: Double): Int
    fun rehashingStarted()
    fun rehashingCompleted()
    fun trackMemUsage(delta: Long)
    fun getMetadataSize(): Long
}