package valkey.kotlin.list

class VKListNode<T>(
    var value: T,
    var prev: VKListNode<T>?,
    var next: VKListNode<T>?
)

/**
 * Kotlin has a powerful built-in List type,
 * but in this project we use a custom list to stay aligned with Valkey's list code base.
 */
class VKList<T>(
    var head: VKListNode<T>? = null,
    var tail: VKListNode<T>? = null,
    var dup: ((T) -> T)? = null,
    var free: ((T) -> Unit)? = null,
    var match: ((T, T) -> Boolean)? = null,
    var len: Long = 0
) {
    fun addNodeTail(hashtable: T) {
        val node = VKListNode(hashtable, null, null)
        linkNodeTail(node)
    }

    fun linkNodeTail(node: VKListNode<T>) {
        if (len == 0L) {
            head = node
            tail = node
        } else {
            node.prev = tail
            node.next = null
            tail!!.next = node
            tail = node
        }
        len++
    }

    fun unlinkNode(node: VKListNode<T>) {
        assert(len > 0)

        if (node.prev != null) {
            node.prev!!.next = node.next
        } else {
            head = node.next
        }

        if (node.next != null) {
            node.next!!.prev = node.prev
        } else {
            tail = node.prev
        }

        node.next = null
        node.prev = null

        len--
    }

}
