package org.example.dict

interface DictType {
    fun hashFunction(key: String): ULong
    // It may be not used in Kotlin
    fun keyDup(key: String): String
    // It may be not used in Kotlin
    fun keyCompare(key1: String, key2: String): Int
    fun valDestructor(value: Object)
    fun resizeAllowed(moreMem: Long, usedRatior: Double): Int
    // Invoked at the start of dict initialization/rehashing (old and new ht are already created)
    fun rehashingStarted(d: Dict)
    // Invoked at the end of dict initialization/rehashing of all he entries from old to new ht.
    // Both ht still exists and are cleaned up after this callback.
    fun rehashingCompleted(d: Dict)
    // Allow a dict to carry extra caller-defined metadata.
    // The extra memory is initialized to 0 when a dict is allocated.
    fun dictMetadataBytes(d: Dict): Long
}

class Dict : DictType {
    override fun hashFunction(key: String): ULong {
        TODO("Not yet implemented")
    }

    override fun keyDup(key: String): String {
        TODO("Not yet implemented")
    }

    override fun keyCompare(key1: String, key2: String): Int {
        TODO("Not yet implemented")
    }

    override fun valDestructor(value: Object) {
        TODO("Not yet implemented")
    }

    override fun resizeAllowed(moreMem: Long, usedRatior: Double): Int {
        TODO("Not yet implemented")
    }

    override fun rehashingStarted(d: Dict) {
        TODO("Not yet implemented")
    }

    override fun rehashingCompleted(d: Dict) {
        TODO("Not yet implemented")
    }

    override fun dictMetadataBytes(d: Dict): Long {
        TODO("Not yet implemented")
    }
}