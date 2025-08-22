package valkey.kotlin.list

class ListNode<T>(
    var value: T,
    var prev: ListNode<T>?,
    var next: ListNode<T>?
)