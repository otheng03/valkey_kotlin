package valkey.kotlin.network

fun processInlineBuffer() {

}

fun processMultibulkBuffer() {
    // Setup argv array on client structure
    // if (c->argv) zfree(c->argv)
    // c->argv_len = min(c->multibulklen, 1024)
    // c->argv = zmalloc(sizeof(robj *) * c->argv_len);
    // c->argv_len_sum = 0
    // c->argv[c->argc++] = createStringObject(c->querybuf + c->qb_pos, c->bulklen);
}

/*
 * Parse a single command from the query buf.
 */
fun parseCommand() {

}

fun processInputBuffer() {

}