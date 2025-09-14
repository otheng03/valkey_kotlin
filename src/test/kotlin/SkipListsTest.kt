package valkey.kotlin.skiplists

import java.util.Random
import kotlin.test.Test


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

internal class SkipList() {
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
}

class SkipListsTest {
    @Test
    fun testSkipList() {
    }
}