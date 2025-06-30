package valkey.kotlin.command

/*
struct serverCommand {
    /* Declarative data */
    const char *declared_name;    /* A string representing the command declared_name.
                                   * It is a const char * for native commands and SDS for module commands. */
    const char *summary;          /* Summary of the command (optional). */
    const char *complexity;       /* Complexity description (optional). */
    const char *since;            /* Debut version of the command (optional). */
    int doc_flags;                /* Flags for documentation (see CMD_DOC_*). */
    const char *replaced_by;      /* In case the command is deprecated, this is the successor command. */
    const char *deprecated_since; /* In case the command is deprecated, when did it happen? */
    serverCommandGroup group;     /* Command group */
    commandHistory *history;      /* History of the command */
    int num_history;
    const char **tips; /* An array of strings that are meant to be tips for clients/proxies regarding this command */
    int num_tips;
    serverCommandProc *proc; /* Command implementation */
    int arity;               /* Number of arguments, it is possible to use -N to say >= N */
    uint64_t flags;          /* Command flags, see CMD_*. */
    uint64_t acl_categories; /* ACl categories, see ACL_CATEGORY_*. */
    keySpec *key_specs;
    int key_specs_num;
    /* Use a function to determine keys arguments in a command line.
     * Used for Cluster redirect (may be NULL) */
    serverGetKeysProc *getkeys_proc;
    int num_args; /* Length of args array. */
    /* Array of subcommands (may be NULL) */
    struct serverCommand *subcommands;
    /* Array of arguments (may be NULL) */
    struct serverCommandArg *args;
#ifdef LOG_REQ_RES
    /* Reply schema */
    struct jsonObject *reply_schema;
#endif

    /* Runtime populated data */
    long long microseconds, calls, rejected_calls, failed_calls;
    int id;           /* Command ID. This is a progressive ID starting from 0 that
                         is assigned at runtime, and is used in order to check
                         ACLs. A connection is able to execute a given command if
                         the user associated to the connection has this command
                         bit set in the bitmap of allowed commands. */
    sds fullname;     /* Includes parent name if any: "parentcmd|childcmd". Unchanged if command is renamed. */
    sds current_name; /* Same as fullname, becomes a separate string if command is renamed. */
    struct hdr_histogram
        *latency_histogram;        /* Points to the command latency command histogram (unit of time nanosecond). */
    keySpec legacy_range_key_spec; /* The legacy (first,last,step) key spec is
                                    * still maintained (if applicable) so that
                                    * we can still support the reply format of
                                    * COMMAND INFO and COMMAND GETKEYS */
    hashtable *subcommands_ht;     /* Subcommands hash table. The key is the subcommand sds name
                                    * (not the fullname), and the value is the serverCommand structure pointer. */
    struct serverCommand *parent;
    struct ValkeyModuleCommand *module_cmd; /* A pointer to the module command data (NULL if native command) */
};
 */
class Commoand {
}