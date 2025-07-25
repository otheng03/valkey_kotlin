package valkey.kotlin.database

import valkey.kotlin.hasFlag
import java.util.EnumSet

/* The actual Object */
const val OBJ_STRING = 0 /* String object. */
const val OBJ_LIST = 1   /* List object. */
const val OBJ_SET = 2    /* Set object. */
const val OBJ_ZSET = 3   /* Sorted set object. */
const val OBJ_HASH = 4   /* Hash object. */

const val OBJ_ENCODING_RAW = 0        /* Raw representation */
const val OBJ_ENCODING_INT = 1        /* Encoded as integer */
const val OBJ_ENCODING_HASHTABLE = 2  /* Encoded as a hashtable */
const val OBJ_ENCODING_ZIPMAP = 3     /* No longer used: old hash encoding. */
const val OBJ_ENCODING_LINKEDLIST = 4 /* No longer used: old list encoding. */
const val OBJ_ENCODING_ZIPLIST = 5    /* No longer used: old list/hash/zset encoding. */
const val OBJ_ENCODING_INTSET = 6     /* Encoded as intset */
const val OBJ_ENCODING_SKIPLIST = 7   /* Encoded as skiplist */
const val OBJ_ENCODING_EMBSTR = 8     /* Embedded sds string encoding */
const val OBJ_ENCODING_QUICKLIST = 9  /* Encoded as linked list of listpacks */
const val OBJ_ENCODING_STREAM = 10    /* Encoded as a radix tree of listpacks */
const val OBJ_ENCODING_LISTPACK = 11  /* Encoded as a listpack */

const val LOOKUP_NONE = 0
const val LOOKUP_NOTOUCH = 1 shl 0    // Don't update LRU
const val LOOKUP_NONOTIFY = 1 shl 1   // Don't trigger keyspace event on key misses
const val LOOKUP_NOSTATS = 1 shl 2    // Don't update keyspace hits/misses counters
const val LOOKUP_WRITE = 1 shl 3      // Delete expired keys even in replicas
const val LOOKUP_NOEXPIRE = 1 shl 4   // Avoid deleting lazy expired keys
const val LOOKUP_NOEFFECTS =          // Combination: Avoid any effects from fetching the key
    LOOKUP_NONOTIFY or LOOKUP_NOSTATS or LOOKUP_NOTOUCH or LOOKUP_NOEXPIRE

/*
 * The original version of Valkey optimizes serverObject using bitfields.
 * In this Kotlin version, I chose not to use bitfields, mainly for learning purposes.
 * Since this is just for studying Kotlin, keeping the code simple is good enough.
 * If I were using Kotlin in production, I might avoid the bitfield approach and focus on clarity rather than compactness.
 * For low-level memory manipulation, I would likely choose C or C++, as they offer advantages in that area.
 */
data class ServerObject (
    val type: UInt,
    val refcount: UInt
)

/*
 * Database representation. There is only one database for simplicity of this toy project.
 */
class ServerDb (
    val keys: KVStore
    /*
    kvstore *keys;                        /* The keyspace for this DB */
    kvstore *expires;                     /* Timeout of keys with a timeout set */
    dict *blocking_keys;                  /* Keys with clients waiting for data (BLPOP)*/
    dict *blocking_keys_unblock_on_nokey; /* Keys with clients waiting for
                                           * data, and should be unblocked if key is deleted (XREADEDGROUP).
                                           * This is a subset of blocking_keys*/
    dict *ready_keys;                     /* Blocked keys that received a PUSH */
    dict *watched_keys;                   /* WATCHED keys for MULTI/EXEC CAS */
    int id;                               /* Database ID */
    long long avg_ttl;                    /* Average TTL, just for stats */
    unsigned long expires_cursor;         /* Cursor of the active expire cycle. */
     */
) {
    fun kvstoreHashtableFind(dictIndex: Int, key: String): Pair</*hashtableIndex*/ Int, /*found*/ Boolean> {
        return Pair(0, false)
    }

    // TODO : Implement dictIndex
    fun dbFindWithDictIndex(key: String, dictIndex: Int): ServerObject? {
        return null
    }
}

enum class SetKeyProperty {
    KEEPTTL,
    NO_SIGNAL,
    ALREADY_EXIST,
    DOESNT_EXIST,
    ADD_OR_UPDATE,
}

typealias SetKeyProperties = EnumSet<SetKeyProperty>

infix fun SetKeyProperties.hasFlag(other: SetKeyProperty) = this.contains(other)

const val SETKEY_KEEPTTL = 1
const val SETKEY_NO_SIGNAL = 2
const val SETKEY_ALREADY_EXIST = 4
const val SETKEY_DOESNT_EXIST = 8
const val SETKEY_ADD_OR_UPDATE = 16 /* Key most likely doesn't exists */

fun setKey(key: String, valRef: String, flags: Int) {
    var keyFound: Int = 0

    if (flags hasFlag SETKEY_ALREADY_EXIST) keyFound = 1
    else if (flags hasFlag SETKEY_ADD_OR_UPDATE) keyFound = -1
    else if (!(flags hasFlag SETKEY_DOESNT_EXIST)) keyFound = lookupKeyWrite(key)?.let{1} ?: 0

    when (keyFound) {
        0 -> dbAdd(key, valRef)
        -1 -> dbAddInternal(key, valRef, 1)
        1 -> dbSetValue(key, valRef, 1)
    }

    if (!(flags hasFlag SETKEY_KEEPTTL)) TODO("Not implemented yet")
    if (!(flags hasFlag SETKEY_NO_SIGNAL)) TODO("Not implemented yet")
}

fun dbAdd(key: String, valRef: String) {

}

fun dbAddInternal(key: String, valRef: String, updateifExisting: Int) {

}

fun dbSetValue(key: String, valRef: String, overwrite: Int) {

}

fun dbFindWithDictIndex(key: String, didx: Int) : ServerObject? {
    return null
}

fun lookupKey(key: String, flags: Int) : ServerObject? {
    val dictIndex = getKVStoreIndexForKey(key)
    val value = dbFindWithDictIndex(key, dictIndex)
    value?.let { print("haha") } ?: { print("hoho") }

    return null
}

fun lookupKeyWriteWithFlags(key: String, flags: Int) : ServerObject? {
    return lookupKey(key, flags or LOOKUP_WRITE)
}

fun lookupKeyWrite(key: String) : ServerObject? {
    return lookupKeyWriteWithFlags(key, LOOKUP_NONE)
}
