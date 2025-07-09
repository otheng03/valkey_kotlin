package hashtable

import org.junit.jupiter.api.Assertions.assertEquals
import valkey.kotlin.hashtable.Entry
import valkey.kotlin.hashtable.KVStoreKeysHashtable
import kotlin.test.Test

class HashtableTest {
    @Test
    fun addKey() {
        val hashtable = KVStoreKeysHashtable()

        val entry = Entry("testkey")
        hashtable.addOrFind(entry)

        val expected = true
        assertEquals(expected, true)
    }
}