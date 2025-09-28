# Valkey Module API (VM_*) Functions Documentation

## Overview

The Valkey Module API provides a comprehensive set of functions that allow external modules to extend Valkey's functionality. These functions, prefixed with `VM_` (ValkeyModule), enable modules to create commands, manage data, handle events, and interact with the server's internals. This document covers the core API functions and their usage patterns.

## API Architecture

### Context-Based Design

All module operations operate within a **ValkeyModuleCtx** (context) that provides:
- Module identity and metadata
- Client connection information
- Memory management tracking
- Error handling state
- Thread safety guarantees

### Function Categories

The VM_* API functions fall into several categories:
1. **Core Management**: Context, initialization, command creation
2. **Data Operations**: Key manipulation, data type handling
3. **Memory Management**: Allocation, automatic memory tracking
4. **Communication**: Replies, notifications, events
5. **Configuration**: Module settings, runtime parameters

## Core Management Functions

### moduleCreateContext()

**Purpose**: Creates and initializes a module execution context.

```c
void moduleCreateContext(ValkeyModuleCtx *out_ctx, ValkeyModule *module, int ctx_flags)
```

**Parameters**:
- `out_ctx`: Output context structure to initialize
- `module`: Module that owns this context (NULL during loading)
- `ctx_flags`: Context behavior flags

**Context Flags**:
```c
#define VALKEYMODULE_CTX_NONE (0)
#define VALKEYMODULE_CTX_AUTO_MEMORY (1 << 0)          // Enable automatic memory collection
#define VALKEYMODULE_CTX_KEYS_POS_REQUEST (1 << 1)     // Key position analysis mode
#define VALKEYMODULE_CTX_BLOCKED_REPLY (1 << 2)        // Handling blocked client reply
#define VALKEYMODULE_CTX_BLOCKED_TIMEOUT (1 << 3)      // Blocked client timeout
#define VALKEYMODULE_CTX_THREAD_SAFE (1 << 4)          // Thread-safe context
#define VALKEYMODULE_CTX_BLOCKED_DISCONNECTED (1 << 5) // Client disconnected
#define VALKEYMODULE_CTX_TEMP_CLIENT (1 << 6)          // Temporary client
#define VALKEYMODULE_CTX_NEW_CLIENT (1 << 7)           // New client allocation
#define VALKEYMODULE_CTX_COMMAND (1 << 8)              // Command execution context
```

**Implementation Details**:

1. **Context Initialization**:
```c
memset(out_ctx, 0, sizeof(ValkeyModuleCtx));
out_ctx->getapifuncptr = (void *)(unsigned long)&VM_GetApi;  // API discovery
out_ctx->module = module;
out_ctx->flags = ctx_flags;
```

2. **Client Management**:
- `TEMP_CLIENT`: Allocates from server's client pool for efficiency
- `NEW_CLIENT`: Creates a dedicated fake client for the context
- Default: Uses existing client from command execution

3. **Execution Tracking**:
- Increments `execution_nesting` counter except for command and thread-safe contexts
- Calculates yield time for long-running operations
- Manages Global Interpreter Lock (GIL) for thread safety

**Usage Patterns**:

```c
// Command execution context
ValkeyModuleCtx ctx;
moduleCreateContext(&ctx, module, VALKEYMODULE_CTX_COMMAND);
ctx.client = c;  // Assign command client

// Background processing context
ValkeyModuleCtx ctx;
moduleCreateContext(&ctx, module, VALKEYMODULE_CTX_TEMP_CLIENT);

// Thread-safe context
ValkeyModuleCtx ctx;
moduleCreateContext(&ctx, module, VALKEYMODULE_CTX_THREAD_SAFE | VALKEYMODULE_CTX_NEW_CLIENT);
```

### VM_GetApi()

**Purpose**: Discovers and binds API functions by name for dynamic linking.

```c
int VM_GetApi(const char *funcname, void **targetPtrPtr)
```

**Parameters**:
- `funcname`: Name of the API function to lookup
- `targetPtrPtr`: Pointer to store the discovered function pointer

**Return Values**:
- `VALKEYMODULE_OK`: Function found and bound successfully
- `VALKEYMODULE_ERR`: Function not found in API registry

**Implementation**:
```c
int VM_GetApi(const char *funcname, void **targetPtrPtr) {
    dictEntry *he = dictFind(server.moduleapi, funcname);  // Lookup in API dictionary
    if (!he) return VALKEYMODULE_ERR;
    *targetPtrPtr = dictGetVal(he);  // Return function pointer
    return VALKEYMODULE_OK;
}
```

**Usage**: This function is primarily used internally by the `valkeymodule.h` header macros and should not be called directly by module developers.

**API Registration Process**:
1. Server populates `server.moduleapi` dictionary during initialization
2. Each VM_* function is registered with its string name
3. Module header uses VM_GetApi to resolve function pointers at runtime
4. Enables API versioning and backwards compatibility

### VM_CreateCommand()

**Purpose**: Registers a new command with the Valkey server.

```c
int VM_CreateCommand(ValkeyModuleCtx *ctx,
                     const char *name,
                     ValkeyModuleCmdFunc cmdfunc,
                     const char *strflags,
                     int firstkey,
                     int lastkey,
                     int keystep)
```

**Parameters**:
- `ctx`: Module context (must be during OnLoad)
- `name`: Command name (case-insensitive)
- `cmdfunc`: Function pointer to command implementation
- `strflags`: Space-separated command flags string
- `firstkey`: Position of first key argument (1-based, 0 = no keys)
- `lastkey`: Position of last key argument (-1 = variable, 0 = no keys)
- `keystep`: Step between key arguments (typically 1)

**Command Flags**:
```
"admin"           - Administrative command
"asking"          - Can be used in cluster ASKING state
"blocking"        - Command may block the client
"deny-oom"        - Deny when out of memory
"fast"            - Fast command (O(1) or O(log N))
"getkeys-api"     - Uses custom key discovery
"loading"         - Allowed during database loading
"may-replicate"   - May replicate to replicas
"no-auth"         - No authentication required
"no-cluster"      - Not available in cluster mode
"no-mandatory-keys" - May not access declared keys
"no-monitor"      - Not logged by MONITOR
"no-multi"        - Not allowed in transactions
"no-script"       - Not allowed in scripts
"pubsub"          - Pub/Sub related command
"random"          - May return different results for same args
"readonly"        - Does not modify data
"skip-monitor"    - Skip MONITOR logging
"stale"           - May run on stale replica
"write"           - Modifies data
```

**Validation Process**:

1. **Timing Check**: Must be called during `OnLoad` phase
```c
if (!ctx->module->onload) return VALKEYMODULE_ERR;
```

2. **Flag Parsing**: Converts string flags to internal bitmap
```c
int64_t flags = commandFlagsFromString((char *)strflags);
if (flags == -1) return VALKEYMODULE_ERR;  // Invalid flag
```

3. **Cluster Compatibility**: Checks CMD_MODULE_NO_CLUSTER
```c
if ((flags & CMD_MODULE_NO_CLUSTER) && server.cluster_enabled)
    return VALKEYMODULE_ERR;
```

4. **Name Validation**: Ensures command name is valid and unique
```c
if (!isCommandNameValid(name)) return VALKEYMODULE_ERR;
if (lookupCommandByCString(name) != NULL) return VALKEYMODULE_ERR;
```

**Command Proxy Creation**:

The function creates a `ValkeyModuleCommand` proxy structure:

```c
typedef struct ValkeyModuleCommand {
    struct ValkeyModule *module;        // Owning module
    ValkeyModuleCmdFunc func;          // Implementation function
    struct serverCommand *serverCmd;   // Server command structure
    struct ValkeyModuleCommand *parent; // Parent for subcommands
    struct ValkeyModuleCommand *subcommands; // Subcommand list
} ValkeyModuleCommand;
```

**Key Specification Setup**:

For commands with keys (firstkey != 0):
```c
cp->serverCmd->key_specs_num = 1;
cp->serverCmd->key_specs = zcalloc(sizeof(keySpec));
cp->serverCmd->key_specs[0].flags = CMD_KEY_FULL_ACCESS;
cp->serverCmd->key_specs[0].begin_search_type = KSPEC_BS_INDEX;
cp->serverCmd->key_specs[0].bs.index.pos = firstkey;
cp->serverCmd->key_specs[0].find_keys_type = KSPEC_FK_RANGE;
cp->serverCmd->key_specs[0].fk.range.lastkey = (lastkey < 0) ? lastkey : (lastkey - firstkey);
cp->serverCmd->key_specs[0].fk.range.keystep = keystep;
```

**Registration Process**:
1. Create command proxy structure
2. Set up server command metadata
3. Configure key specifications
4. Drain I/O threads to prevent concurrent access
5. Add to server command hashtables
6. Assign ACL command ID

**Example Usage**:
```c
// Simple command with no keys
VM_CreateCommand(ctx, "mymodule.hello", HelloCommand, "readonly fast", 0, 0, 0);

// Command with single key
VM_CreateCommand(ctx, "mymodule.get", GetCommand, "readonly", 1, 1, 1);

// Command with variable keys
VM_CreateCommand(ctx, "mymodule.mget", MGetCommand, "readonly", 1, -1, 1);

// Write command with multiple keys
VM_CreateCommand(ctx, "mymodule.mset", MSetCommand, "write deny-oom", 1, -1, 2);
```

## Data Type and Key Management Functions

### VM_OpenKey()

**Purpose**: Opens a key for reading/writing with various access modes.

```c
ValkeyModuleKey *VM_OpenKey(ValkeyModuleCtx *ctx, ValkeyModuleString *keyname, int mode)
```

**Access Modes**:
- `VALKEYMODULE_READ`: Read-only access
- `VALKEYMODULE_WRITE`: Write access (may create key)
- `VALKEYMODULE_OPEN_KEY_NOTOUCH`: Don't update LRU/LFU
- `VALKEYMODULE_OPEN_KEY_NONOTIFY`: No keyspace notifications
- `VALKEYMODULE_OPEN_KEY_NOSTATS`: No hit/miss statistics
- `VALKEYMODULE_OPEN_KEY_NOEXPIRE`: Don't expire lazy-expired keys
- `VALKEYMODULE_OPEN_KEY_NOEFFECTS`: Combination of above NO* flags

### VM_CloseKey()

**Purpose**: Closes a key handle and releases associated resources.

```c
void VM_CloseKey(ValkeyModuleKey *key)
```

**Automatic Cleanup**: Keys are automatically closed when context is freed if not explicitly closed.

### VM_KeyType()

**Purpose**: Returns the type of value stored at a key.

```c
int VM_KeyType(ValkeyModuleKey *key)
```

**Return Values**:
- `VALKEYMODULE_KEYTYPE_EMPTY`: Key doesn't exist
- `VALKEYMODULE_KEYTYPE_STRING`: String value
- `VALKEYMODULE_KEYTYPE_LIST`: List value
- `VALKEYMODULE_KEYTYPE_HASH`: Hash value
- `VALKEYMODULE_KEYTYPE_SET`: Set value
- `VALKEYMODULE_KEYTYPE_ZSET`: Sorted set value
- `VALKEYMODULE_KEYTYPE_MODULE`: Module-specific type
- `VALKEYMODULE_KEYTYPE_STREAM`: Stream value

## Memory Management Functions

### VM_Alloc() / VM_Free()

**Purpose**: Module-specific memory allocation with tracking.

```c
void *VM_Alloc(size_t bytes)
void VM_Free(void *ptr)
```

**Features**:
- Memory usage tracking per module
- Integration with server memory policies
- Out-of-memory handling
- Memory leak detection in debug mode

### Automatic Memory Management

The module system provides automatic memory collection for temporary allocations:

```c
void autoMemoryCollect(ValkeyModuleCtx *ctx) {
    if (!(ctx->flags & VALKEYMODULE_CTX_AUTO_MEMORY)) return;

    for (int j = 0; j < ctx->amqueue_used; j++) {
        struct AutoMemEntry *amentry = &ctx->amqueue[j];
        if (amentry->type != VALKEYMODULE_AM_FREED) {
            switch (amentry->type) {
                case VALKEYMODULE_AM_STRING:
                    decrRefCount(amentry->ptr);
                    break;
                case VALKEYMODULE_AM_KEY:
                    VM_CloseKey(amentry->ptr);
                    break;
                // ... other types
            }
        }
    }
}
```

## Advanced Features

### Command Information and Metadata

#### VM_SetCommandInfo()

**Purpose**: Sets detailed metadata for a command after creation.

```c
int VM_SetCommandInfo(ValkeyModuleCommand *command, const ValkeyModuleCommandInfo *info)
```

**Command Info Structure**:
```c
typedef struct ValkeyModuleCommandInfo {
    const char *version;
    const char *summary;
    const char *complexity;
    const char *since;
    ValkeyModuleCommandHistoryEntry *history;
    const char *tips;
    int arity;
    ValkeyModuleCommandKeySpec *key_specs;
    ValkeyModuleCommandArg *args;
} ValkeyModuleCommandInfo;
```

### Subcommand Support

#### VM_CreateSubcommand()

**Purpose**: Creates a subcommand under an existing parent command.

```c
int VM_CreateSubcommand(ValkeyModuleCommand *parent,
                        const char *name,
                        ValkeyModuleCmdFunc cmdfunc,
                        const char *strflags,
                        int firstkey,
                        int lastkey,
                        int keystep)
```

**Hierarchical Commands**: Enables creating command families like `MEMORY USAGE`, `CONFIG GET`, etc.

### ACL Integration

#### VM_AddACLCategory()

**Purpose**: Defines a new ACL category for module commands.

```c
int VM_AddACLCategory(ValkeyModuleCtx *ctx, const char *name)
```

#### VM_SetCommandACLCategories()

**Purpose**: Assigns ACL categories to a command.

```c
int VM_SetCommandACLCategories(ValkeyModuleCommand *command, const char *aclflags)
```

**ACL Categories**: Allow fine-grained permission control over module commands.

## Thread Safety and Execution Model

### Execution Nesting Counter

The module system tracks execution depth to prevent recursion issues:

```c
void enterExecutionUnit(int update_cached_time, int clear_flags) {
    server.execution_nesting++;
    if (update_cached_time) updateCachedTimeWithUs(0, 0);
    if (clear_flags) clearActiveFlags();
}

void exitExecutionUnit(void) {
    server.execution_nesting--;
}
```

### Context Cleanup and Resource Management

```c
void moduleFreeContext(ValkeyModuleCtx *ctx) {
    // Handle execution nesting
    if (!(ctx->flags & (VALKEYMODULE_CTX_THREAD_SAFE | VALKEYMODULE_CTX_COMMAND))) {
        exitExecutionUnit();
        postExecutionUnitOperations();
    }

    // Automatic memory collection
    autoMemoryCollect(ctx);

    // Pool allocation cleanup
    poolAllocRelease(ctx);

    // Client management
    if (ctx->flags & VALKEYMODULE_CTX_TEMP_CLIENT)
        moduleReleaseTempClient(ctx->client);
    else if (ctx->flags & VALKEYMODULE_CTX_NEW_CLIENT)
        freeClient(ctx->client);
}
```

## Error Handling and Best Practices

### Function Return Patterns

Most VM_* functions follow these return conventions:
- `VALKEYMODULE_OK` (0): Success
- `VALKEYMODULE_ERR` (1): Error/failure
- `NULL`: Invalid state or not found
- Type-specific values: For query functions

### Context Requirements

Many functions require specific context states:
- **OnLoad Only**: `VM_CreateCommand`, `VM_SetModuleAttribs`
- **With Client**: Reply functions, key operations
- **Thread-Safe**: Functions that can be called from background threads

### Memory Management Rules

1. **Always match allocations**: Every `VM_Alloc` needs `VM_Free`
2. **Use automatic memory**: Enable `VALKEYMODULE_CTX_AUTO_MEMORY` for temporary allocations
3. **Close resources**: Explicitly close keys, call replies, etc.
4. **Reference counting**: Understand string object lifecycle

## Integration with Valkey Core

### Command Dispatcher

All module commands are executed through the central dispatcher:

```c
void ValkeyModuleCommandDispatcher(client *c) {
    ValkeyModuleCommand *cp = c->cmd->module_cmd;
    ValkeyModuleCtx ctx;

    moduleCreateContext(&ctx, cp->module, VALKEYMODULE_CTX_COMMAND);
    ctx.client = c;

    cp->func(&ctx, (void **)c->argv, c->argc);  // Call module function

    moduleFreeContext(&ctx);
}
```

### Event System Integration

Modules can register for server events and fire custom events:

```c
// Event registration during OnLoad
VM_SubscribeToServerEvent(ctx, ValkeyModuleEvent_ClientConnected, callback);

// Custom event firing
moduleFireServerEvent(VALKEYMODULE_EVENT_MODULE_CHANGE,
                     VALKEYMODULE_SUBEVENT_MODULE_LOADED,
                     module);
```

### Configuration Integration

Modules can define configuration parameters that integrate with Valkey's configuration system:

```c
// Define configuration during OnLoad
VM_RegisterModuleConfig(ctx, "myconfig", VM_CONFIG_INT, callback);

// Load configurations
VM_LoadConfigs(ctx);
```

## Performance Considerations

### Context Creation Overhead

- **Temp Client**: Efficient for short operations, reuses pooled clients
- **New Client**: Higher overhead, use for long-running operations
- **Existing Client**: Zero overhead for command contexts

### Memory Allocation Patterns

- **Small allocations**: Use pool allocator for temporary data
- **Large allocations**: Use `VM_Alloc`/`VM_Free` directly
- **String objects**: Understand reference counting to avoid copies

### Command Flag Optimization

Choose appropriate flags for optimal performance:
- `"fast"`: For O(1) or O(log N) operations
- `"readonly"`: Enables read-only optimizations
- `"random"`: Prevents result caching

This comprehensive API documentation provides the foundation for developing robust Valkey modules using the VM_* function interface.