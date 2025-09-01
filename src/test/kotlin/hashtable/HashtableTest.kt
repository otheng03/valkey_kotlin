package hashtable

import valkey.kotlin.database.KVStore
import valkey.kotlin.hashtable.Entry
import valkey.kotlin.hashtable.KVStoreKeysHashtable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HashtableTest {
    @Test
    fun addKey() {
        val key = "testkey"
        val kvs = KVStore<KVStoreKeysHashtable>()
        val hashtable = KVStoreKeysHashtable.Factory.create(kvs)
        val entry = Entry(key)

        val (success, exisingEntry) = hashtable.addOrFind(entry)
        assertEquals(true, success)
        assertEquals(entry, exisingEntry)

        assertEquals(entry, hashtable.find(key))

        assertTrue(hashtable.delete(key))
        assertNull(hashtable.find(key))
    }
}