package hashtable

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import valkey.kotlin.database.KVStore
import valkey.kotlin.hashtable.Entry
import valkey.kotlin.hashtable.KVStoreKeysHashtable
import kotlin.test.Test

class HashtableTest {
    @Test
    fun addKey() {
        val kvs = KVStore<KVStoreKeysHashtable>()
        val hashtable = KVStoreKeysHashtable.Factory.create(kvs)

        val entry = Entry("testkey")
        val (success, exisingEntry) = hashtable.addOrFind(entry)

        assertEquals(true, success)
        assertNull(exisingEntry)
    }
}