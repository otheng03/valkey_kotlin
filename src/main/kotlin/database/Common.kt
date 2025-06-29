package valkey.kotlin.database

import valkey.kotlin.hasFlag
import valkey.kotlin.hashtable.Hashtable

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

data class KVStore (
    val hashtable: Hashtable
    /*
    int flags;
    hashtableType *dtype;
    hashtable **hashtables;
    int num_hashtables;
    int num_hashtables_bits;
    list *rehashing;                          /* List of hash tables in this kvstore that are currently rehashing. */
    int resize_cursor;                        /* Cron job uses this cursor to gradually resize hash tables (only used if num_hashtables > 1). */
    int allocated_hashtables;                 /* The number of allocated hashtables. */
    int non_empty_hashtables;                 /* The number of non-empty hashtables. */
    unsigned long long key_count;             /* Total number of keys in this kvstore. */
    unsigned long long bucket_count;          /* Total number of buckets in this kvstore across hash tables. */
    unsigned long long *hashtable_size_index; /* Binary indexed tree (BIT) that describes cumulative key frequencies up until
                                               * given hashtable-index. */
    size_t overhead_hashtable_lut;            /* Overhead of all hashtables in bytes. */
    size_t overhead_hashtable_rehashing;      /* Overhead of hash tables rehashing in bytes. */
     */
)

/*
 * Database representation. There is only one database for simplicity of this toy project.
 */
data class ServerDb (
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
)

const val SETKEY_KEEPTTL = 1
const val SETKEY_NO_SIGNAL = 2
const val SETKEY_ALREADY_EXIST = 4
const val SETKEY_DOESNT_EXIST = 8
const val SETKEY_ADD_OR_UPDATE = 16 /* Key most likely doesn't exists */
fun setKey(key: String, flags: Int) {
    var keyfound = 0

    if (flags hasFlag SETKEY_ALREADY_EXIST) keyfound = 1
    else if (flags hasFlag SETKEY_ADD_OR_UPDATE) keyfound = -1
    else if (!(flags hasFlag SETKEY_DOESNT_EXIST)) keyfound = lookupKeyWrite(key)?.let{1} ?: 0
}

fun lookupKeyWrite(key: String, flags: Int) : ServerObject? {
    return null
}

fun lookupKeyWriteWithFlags(key: String, flags: Int) : ServerObject? {
    return lookupKeyWrite(key, flags or LOOKUP_WRITE)
}

fun lookupKeyWrite(key: String) : ServerObject? {
    return lookupKeyWriteWithFlags(key, LOOKUP_NONE)
}
