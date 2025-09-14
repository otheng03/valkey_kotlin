package valkey.kotlin.skiplists

import org.junit.jupiter.api.Assertions.assertEquals
import java.util.*
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull


/**
 * Search Algorithm:
 *   We search for an element by traversing forward pointers that do not overshoot the node containing the element being
 *   searched for. When no more progress can be made at the current level of forward pointers, the search moves down to
 *   the next level of forward pointers, the search moves down to the next level. When we can make no more progress at
 *   level 1, we must be immediately in front of the node that contains the desired element (if it is in the list).
 *
 * Insertion and Deletion Algorithms
 *   To insert or delete a node, we simply search and splice. A vector update is maintained so that when the search is
 *   complete (and we are ready to perform the splice), update[] contains a pointer to the rightmost node of level i or
 *   higher that is to the left of the location of the insertion/deletion.
 *   If an insertion generates a node with a level greater than the previous maximum level of the list, we update the
 *   maximum level of the list and initialize the appropriate portions of the update vector. After each deletion, we
 *   check if we have deleted the maximum element of the list and if so, decrease the maximum level of the list.
 */

internal class Node(value: Int, level: Int) {
    var value: Int
    var forward: Array<Node?>? // array to hold references to different levels

    init {
        this.value = value
        this.forward = arrayOfNulls<Node>(level + 1) // level + 1 because level is 0-based
    }
}

internal class SkipList {
    var maxLevel: Int
    var level: Int
    var head: Node
    val random: Random

    init {
        maxLevel = 16
        level = 0
        head = Node(Integer.MIN_VALUE, maxLevel)
        random = Random()
    }

    fun randomLevel(): Int {
        var lvl = 0
        while (random.nextBoolean() && lvl < maxLevel) {
            lvl++
        }
        return lvl
    }

    fun insert(value: Int) {
        val update = arrayOfNulls<Node>(maxLevel + 1)
        var current: Node? = head

        for (i in level downTo 0) {
            while (current!!.forward!![i] != null && current.forward!![i]!!.value < value) {
                current = current.forward!![i]!!
            }
            update[i] = current
        }

        current = current!!.forward!![0]

        if (current == null || current.value != value) {
            val lvl = randomLevel()

            if (lvl > level) {
                for (i in level + 1..lvl) {
                    update[i] = head
                }
                level = lvl
            }

            val newNode = Node(value, lvl)
            for (i in 0..lvl) {
                newNode.forward!![i] = update[i]!!.forward!![i]
                update[i]!!.forward!![i] = newNode
            }
        }
    }

    fun search(value: Int): Node? {
        var current: Node? = head
        for (i in level downTo 0) {
            while (current!!.forward!![i] != null && current.forward!![i]!!.value < value) {
                current = current.forward!![i]!!
            }
        }
        current = current!!.forward!![0]
        if (current != null && current.value == value) {
            return current
        } else {
            return null
        }
    }

    fun delete(value: Int) {
        val update = arrayOfNulls<Node>(maxLevel + 1)
        var current: Node? = head

        for (i in level downTo 0) {
            while (current!!.forward!![i] != null && current.forward!![i]!!.value < value) {
                current = current.forward!![i]!!
            }
            update[i] = current
        }
        current = current!!.forward!![0]

        if (current != null && current.value == value) {
            for (i in 0..level) {
                if (update[i]!!.forward!![i] != current) {
                    break
                }
                update[i]!!.forward!![i] = current.forward!![i]
            }

            while (level > 0 && head.forward!![level] == null) {
                level--
            }
        }
    }
}

class SkipListsTest {
    @Test
    fun testInsertSingleElement() {
        val skipList = SkipList()
        skipList.insert(5)

        val result = skipList.search(5)
        assertNotNull(result)
        assertEquals(5, result.value)
    }

    @Test
    fun testInsertMultipleElements() {
        val skipList = SkipList()
        val values = listOf(3, 7, 1, 9, 2, 8, 4)

        values.forEach { skipList.insert(it) }

        // Verify all elements can be found
        values.forEach { value ->
            val result = skipList.search(value)
            assertNotNull(result, "Element $value should be found")
            assertEquals(value, result.value)
        }
    }

    @Test
    fun testInsertDuplicateElements() {
        val skipList = SkipList()
        skipList.insert(5)
        skipList.insert(5) // Insert duplicate

        val result = skipList.search(5)
        assertNotNull(result)
        assertEquals(5, result.value)
    }

    @Test
    fun testSearchInEmptyList() {
        val skipList = SkipList()
        val result = skipList.search(5)
        assertNull(result)
    }

    @Test
    fun testSearchNonExistentElement() {
        val skipList = SkipList()
        skipList.insert(3)
        skipList.insert(7)
        skipList.insert(9)

        val result = skipList.search(5) // Element not in list
        assertNull(result)
    }

    @Test
    fun testSearchExistingElements() {
        val skipList = SkipList()
        val values = listOf(1, 3, 5, 7, 9, 11, 13, 15)

        values.forEach { skipList.insert(it) }

        // Test searching for each element
        values.forEach { value ->
            val result = skipList.search(value)
            assertNotNull(result, "Element $value should be found")
            assertEquals(value, result.value)
        }
    }

    @Test
    fun testSearchBoundaryValues() {
        val skipList = SkipList()
        skipList.insert(Int.MAX_VALUE)
        skipList.insert(0)
        skipList.insert(-1)

        assertNotNull(skipList.search(Int.MAX_VALUE))
        assertEquals(Int.MAX_VALUE, skipList.search(Int.MAX_VALUE)!!.value)

        assertNotNull(skipList.search(0))
        assertEquals(0, skipList.search(0)!!.value)

        assertNotNull(skipList.search(-1))
        assertEquals(-1, skipList.search(-1)!!.value)
    }

    @Test
    fun testDeleteFromEmptyList() {
        val skipList = SkipList()
        skipList.delete(5) // Should not crash

        val result = skipList.search(5)
        assertNull(result)
    }

    @Test
    fun testDeleteExistingElement() {
        val skipList = SkipList()
        skipList.insert(5)

        // Verify element exists
        assertNotNull(skipList.search(5))

        // Delete the element
        skipList.delete(5)

        // Verify element is gone
        assertNull(skipList.search(5))
    }

    @Test
    fun testDeleteNonExistentElement() {
        val skipList = SkipList()
        skipList.insert(3)
        skipList.insert(7)

        // Delete element that doesn't exist
        skipList.delete(5)

        // Verify original elements are still there
        assertNotNull(skipList.search(3))
        assertNotNull(skipList.search(7))
    }

    @Test
    fun testDeleteMultipleElements() {
        val skipList = SkipList()
        val values = listOf(1, 3, 5, 7, 9)

        values.forEach { skipList.insert(it) }

        // Delete some elements
        skipList.delete(3)
        skipList.delete(7)

        // Verify deleted elements are gone
        assertNull(skipList.search(3))
        assertNull(skipList.search(7))

        // Verify remaining elements are still there
        assertNotNull(skipList.search(1))
        assertNotNull(skipList.search(5))
        assertNotNull(skipList.search(9))
    }

    @Test
    fun testDeleteAllElements() {
        val skipList = SkipList()
        val values = listOf(2, 4, 6, 8)

        values.forEach { skipList.insert(it) }
        values.forEach { skipList.delete(it) }

        // Verify all elements are gone
        values.forEach { value ->
            assertNull(skipList.search(value), "Element $value should be deleted")
        }
    }

    @Test
    fun testInsertSearchDeleteCombined() {
        val skipList = SkipList()

        // Insert elements
        skipList.insert(10)
        skipList.insert(5)
        skipList.insert(15)

        // Search and verify
        assertNotNull(skipList.search(10))
        assertNotNull(skipList.search(5))
        assertNotNull(skipList.search(15))

        // Delete middle element
        skipList.delete(10)
        assertNull(skipList.search(10))

        // Verify others still exist
        assertNotNull(skipList.search(5))
        assertNotNull(skipList.search(15))

        // Insert new element
        skipList.insert(12)
        assertNotNull(skipList.search(12))

        // Final verification
        assertNull(skipList.search(10))
        assertNotNull(skipList.search(5))
        assertNotNull(skipList.search(15))
        assertNotNull(skipList.search(12))
    }

    @Test
    fun testLargeDataSet() {
        val skipList = SkipList()
        val values = (1..100).toList()

        // Insert all values
        values.forEach { skipList.insert(it) }

        // Verify all values can be found
        values.forEach { value ->
            assertNotNull(skipList.search(value), "Value $value should be found")
        }

        // Delete every other element
        values.filter { it % 2 == 0 }.forEach { skipList.delete(it) }

        // Verify deleted elements are gone and remaining elements exist
        values.forEach { value ->
            if (value % 2 == 0) {
                assertNull(skipList.search(value), "Even value $value should be deleted")
            } else {
                assertNotNull(skipList.search(value), "Odd value $value should still exist")
            }
        }
    }
}