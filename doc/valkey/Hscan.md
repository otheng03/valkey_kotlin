# Code

```c
void hscanCommand(client *c) {
    robj *o;
    unsigned long long cursor;

    if (parseScanCursorOrReply(c, c->argv[2]->ptr, &cursor) == C_ERR) return;
    if ((o = lookupKeyReadOrReply(c, c->argv[1], shared.emptyscan)) == NULL || checkType(c, o, OBJ_HASH)) return;
    scanGenericCommand(c, o, cursor);
}
```

```c
/* Try to parse a SCAN cursor stored at buffer 'buf':
 * if the cursor is valid, store it as unsigned integer into *cursor and
 * returns C_OK. Otherwise return C_ERR and send an error to the
 * client. */
int parseScanCursorOrReply(client *c, sds buf, unsigned long long *cursor) {
    if (!string2ull(buf, sdslen(buf), cursor)) {
        addReplyError(c, "invalid cursor");
        return C_ERR;
    }
    return C_OK;
}
```

```c
/* This command implements SCAN, HSCAN and SSCAN commands.
 * If object 'o' is passed, then it must be a Hash, Set or Zset object, otherwise
 * if 'o' is NULL the command will operate on the dictionary associated with
 * the current database.
 *
 * When 'o' is not NULL the function assumes that the first argument in
 * the client arguments vector is a key so it skips it before iterating
 * in order to parse options.
 *
 * In the case of a Hash object the function returns both the field and value
 * of every element on the Hash. */
void scanGenericCommand(client *c, robj *o, unsigned long long cursor) {
    /* Step 1: Parse options. */
    getLongFromObjectOrReply(c, c->argv[i + 1], &count, NULL)
    pat = c->argv[i + 1]->ptr;
    typename = c->argv[i + 1]->ptr;
    only_keys = 1;

    /* Step 2: Iterate the collection.

    /* Set a free callback for the contents of the collected keys list if they
     * are deep copied temporary strings. We must not free them if they are just
     * a shallow copy - a pointer to the actual data in the data structure */
    } else if (o->type == OBJ_HASH && o->encoding == OBJ_ENCODING_HASHTABLE) {
        ht = o->ptr;
        free_callback = NULL;
    }
    
    /* a generic dynamic array implementation
        typedef struct vector {
            void *data;       // Pointer to the actual array data
            uint32_t alloc;   // Number of allocated items
            uint32_t len;     // Current number of used items
            size_t item_size; // Size of each element in bytes
        } vector; */
    vectorInit(&result, SCAN_VECTOR_INITIAL_ALLOC, sizeof(sds));

    /* For main hash table scan or scannable data structure. */
    if (!o || ht) {
        /* We set the max number of iterations to ten times the specified
         * COUNT, so if the hash table is in a pathological state (very
         * sparsely populated) we avoid to block too much time at the cost
         * of returning no or very few elements. */
        long maxiterations = count * 10;

        /* We pass scanData which have three pointers to the callback.
        scanData data = {
            .result = &result,
            .db = c->db,
            .o = o,
            .type = type,
            .pattern = use_pattern ? pat : NULL,
            .sampled = 0,
            .only_keys = only_keys,
        };

        do {
            /* In cluster mode there is a separate dictionary for each slot.
             * If cursor is empty, we should try exploring next non-empty slot. */
            if (o == NULL) {
                cursor = kvstoreScan(c->db->keys, cursor, onlydidx, keysScanCallback, NULL, &data);
            } else {
                cursor = hashtableScan(ht, cursor, hashtableScanCallback, &data);
            }
        } while (cursor && maxiterations-- && data.sampled < count);
    }

    /* Step 3: Reply to the client. */
    addReplyArrayLen(c, 2);
    addReplyBulkLongLong(c, cursor);
    addReplyArrayLen(c, vectorLen(&result));
    addReplyBulkCBuffer(c, *key, sdslen(*key));

    vectorCleanup(&result);
}
```

```c
/* Like hashtableScan, but additionally reallocates the memory used by the dict
 * entries using the provided allocation function. This feature was added for
 * the active defrag feature.
 *
 * The 'defragfn' callback is called with a pointer to memory that callback can
 * reallocate. The callbacks should return a new memory address or NULL, where
 * NULL means that no reallocation happened and the old memory is still valid.
 * The 'defragfn' can be NULL if you don't need defrag reallocation.
 *
 * The 'flags' argument can be used to tweak the behaviour. It's a bitwise-or
 * (zero means no flags) of the following:
 *
 * - HASHTABLE_SCAN_EMIT_REF: Emit a pointer to the entry's location in the
 *   table to the scan function instead of the actual entry. This can be used
 *   for advanced things like reallocating the memory of an entry (for the
 *   purpose of defragmentation) and updating the pointer to the entry inside
 *   the hash table.
 */
size_t hashtableScanDefrag(hashtable *ht, size_t cursor, hashtableScanFunction fn, void *privdata, void *(*defragfn)(void *), int flags) {
    if (hashtableSize(ht) == 0) return 0;

    /* Prevent entries from being moved around during the scan call, as a
     * side-effect of the scan callback. */
    hashtablePauseRehashing(ht);
    hashtablePauseAutoShrink(ht);

    /* Flags. */
    int emit_ref = (flags & HASHTABLE_SCAN_EMIT_REF);

    if (!hashtableIsRehashing(ht)) {
        /* Emit entries at the cursor index. */
        size_t mask = expToMask(ht->bucket_exp[0]);
        size_t idx = cursor & mask;
        size_t used_before = ht->used[0];
        bucket *b = &ht->tables[0][idx];
        do {
            if (b->presence != 0) {
                int pos;
                for (pos = 0; pos < ENTRIES_PER_BUCKET; pos++) {
                    if (isPositionFilled(b, pos)) {
                        void *emit = emit_ref ? &b->entries[pos] : b->entries[pos];
                        fn(privdata, emit);
                    }
                }
            }
            bucket *next = getChildBucket(b);
            if (next != NULL && defragfn != NULL) {
                next = bucketDefrag(b, next, defragfn);
            }
            b = next;
        } while (b != NULL);

        /* If any entries were deleted, fill the holes. */
        if (ht->used[0] < used_before) {
            compactBucketChain(ht, idx, 0);
        }

        /* Advance cursor. */
        cursor = nextCursor(cursor, mask);
    } else {
        int table_small, table_large;
        if (ht->bucket_exp[0] <= ht->bucket_exp[1]) {
            table_small = 0;
            table_large = 1;
        } else {
            table_small = 1;
            table_large = 0;
        }

        size_t mask_small = expToMask(ht->bucket_exp[table_small]);
        size_t mask_large = expToMask(ht->bucket_exp[table_large]);

        /* Emit entries in the smaller table, if this index hasn't already been
         * rehashed. */
        size_t idx = cursor & mask_small;
        if (table_small == 1 || ht->rehash_idx == -1 || idx >= (size_t)ht->rehash_idx) {
            size_t used_before = ht->used[table_small];
            bucket *b = &ht->tables[table_small][idx];
            do {
                if (b->presence) {
                    for (int pos = 0; pos < ENTRIES_PER_BUCKET; pos++) {
                        if (isPositionFilled(b, pos)) {
                            void *emit = emit_ref ? &b->entries[pos] : b->entries[pos];
                            fn(privdata, emit);
                        }
                    }
                }
                bucket *next = getChildBucket(b);
                if (next != NULL && defragfn != NULL) {
                    next = bucketDefrag(b, next, defragfn);
                }
                b = next;
            } while (b != NULL);
            /* If any entries were deleted, fill the holes. */
            if (ht->used[table_small] < used_before) {
                compactBucketChain(ht, idx, table_small);
            }
        }

        /* Iterate over indices in larger table that are the expansion of the
         * index pointed to by the cursor in the smaller table. */
        do {
            /* Emit entries in the larger table at this cursor, if this index
             * hash't already been rehashed. */
            idx = cursor & mask_large;
            if (table_large == 1 || ht->rehash_idx == -1 || idx >= (size_t)ht->rehash_idx) {
                size_t used_before = ht->used[table_large];
                bucket *b = &ht->tables[table_large][idx];
                do {
                    if (b->presence) {
                        for (int pos = 0; pos < ENTRIES_PER_BUCKET; pos++) {
                            if (isPositionFilled(b, pos)) {
                                void *emit = emit_ref ? &b->entries[pos] : b->entries[pos];
                                fn(privdata, emit);
                            }
                        }
                    }
                    bucket *next = getChildBucket(b);
                    if (next != NULL && defragfn != NULL) {
                        next = bucketDefrag(b, next, defragfn);
                    }
                    b = next;
                } while (b != NULL);
                /* If any entries were deleted, fill the holes. */
                if (ht->used[table_large] < used_before) {
                    compactBucketChain(ht, idx, table_large);
                }
            }

            /* Increment the reverse cursor not covered by the smaller mask. */
            cursor = nextCursor(cursor, mask_large);

            /* Continue while bits covered by mask difference is non-zero. */
        } while (cursor & (mask_small ^ mask_large));
    }
    hashtableResumeRehashing(ht);
    hashtableResumeAutoShrink(ht);
    return cursor;
}
```
