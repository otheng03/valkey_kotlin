# Valkey Module Loading System: `moduleLoad` Function Documentation

## Overview

The `moduleLoad` function is the core component of Valkey's dynamic module loading system. It handles the complete lifecycle of loading a shared library module, from file validation to registration in the server's module registry. This function is located in `src/module.c:12523`.

## The Inside Story on Shared Libraries and Dynamic Loading

### What Are Shared Libraries?

Shared libraries (also called dynamic libraries) are executable code modules that can be loaded and linked at runtime rather than compile time. They enable:

- **Code Reuse**: Multiple programs can share the same library code in memory
- **Modularity**: Applications can be extended without recompilation
- **Memory Efficiency**: Only one copy of library code exists in system memory
- **Update Flexibility**: Libraries can be updated independently of the main application

### Platform-Specific Shared Library Formats

| Platform | Extension | Format | Loader |
|----------|-----------|--------|---------|
| Linux | `.so` (Shared Object) | ELF | `ld.so` |
| macOS | `.dylib` (Dynamic Library) | Mach-O | `dyld` |
| Windows | `.dll` (Dynamic Link Library) | PE | Windows Loader |
| FreeBSD | `.so` | ELF | `rtld` |

### The Dynamic Loading Process: Under the Hood

#### 1. ELF/Mach-O File Structure

When `dlopen()` is called, the system loader performs these steps:

**ELF Header Parsing** (Linux/FreeBSD):
```
ELF Header → Program Headers → Section Headers
     ↓              ↓              ↓
Magic Number    LOAD segments   .symtab, .dynsym
Architecture    Memory layout   Symbol tables
Entry point     Permissions     Relocations
```

**Mach-O Header Parsing** (macOS):
```
Mach-O Header → Load Commands → Segments
      ↓              ↓           ↓
CPU Type        LC_SEGMENT     __TEXT (code)
File Type       LC_DYLIB       __DATA (data)
Load Commands   LC_SYMTAB      __LINKEDIT (symbols)
```

#### 2. Memory Mapping and Layout

The loader maps library segments into virtual memory:

```
Virtual Memory Layout:
┌─────────────────┐ ← High Address
│     Stack       │
├─────────────────┤
│       ↓         │ (grows down)
│                 │
│       ↑         │ (grows up)
├─────────────────┤
│     Heap        │
├─────────────────┤
│   Module.so     │ ← dlopen() maps here
│   (Code+Data)   │
├─────────────────┤
│   libc.so       │
├─────────────────┤
│   Main Program  │
├─────────────────┤
│   Reserved      │
└─────────────────┘ ← Low Address
```

#### 3. Symbol Resolution and Binding

**Symbol Table Structure**:
```c
typedef struct {
    Elf64_Word    st_name;     // Symbol name offset
    unsigned char st_info;     // Symbol type and binding
    unsigned char st_other;    // Symbol visibility
    Elf64_Section st_shndx;    // Section index
    Elf64_Addr    st_value;    // Symbol value/address
    Elf64_Xword   st_size;     // Symbol size
} Elf64_Sym;
```

**dlsym() Resolution Process**:
1. Search module's symbol table (`.dynsym` section)
2. Hash table lookup for O(1) access
3. String table lookup for symbol name matching
4. Return symbol's virtual address

### RTLD Flags Deep Dive

#### RTLD_NOW vs RTLD_LAZY
```c
// RTLD_NOW (Valkey's choice)
handle = dlopen("module.so", RTLD_NOW);
// ✓ All symbols resolved immediately
// ✓ Fails fast if symbols missing
// ✗ Slower startup time

// RTLD_LAZY (alternative)
handle = dlopen("module.so", RTLD_LAZY);
// ✓ Faster startup time
// ✗ Runtime failures possible
// ✗ Deferred symbol resolution
```

#### RTLD_LOCAL vs RTLD_GLOBAL
```c
// RTLD_LOCAL (Valkey's choice)
handle = dlopen("module.so", RTLD_LOCAL);
// ✓ Module symbols isolated
// ✓ No symbol pollution
// ✗ Symbols unavailable to other modules

// RTLD_GLOBAL (alternative)
handle = dlopen("module.so", RTLD_GLOBAL);
// ✓ Symbols available globally
// ✗ Potential symbol conflicts
// ✗ Security implications
```

#### RTLD_DEEPBIND (Linux/FreeBSD Specific)

**Without RTLD_DEEPBIND**:
```
Symbol Resolution Order:
1. Global symbol table
2. Main program symbols
3. Previously loaded libraries
4. Current module symbols ← Looked up last
```

**With RTLD_DEEPBIND**:
```
Symbol Resolution Order:
1. Current module symbols ← Looked up first
2. Module's dependencies
3. Global symbol table
4. Main program symbols
```

**Why This Matters**:
```c
// Both main program and module define 'malloc'
// Without RTLD_DEEPBIND: module uses main program's malloc
// With RTLD_DEEPBIND: module uses its own malloc
```

### Address Space Layout Randomization (ASLR) Impact

Modern systems use ASLR for security, which affects module loading:

```c
// Different addresses each run due to ASLR
void *handle1 = dlopen("module.so", RTLD_NOW);  // 0x7f1234567000
void *handle2 = dlopen("module.so", RTLD_NOW);  // 0x7f9876543000 (different run)
```

**Valkey Implications**:
- Function pointers change between runs
- No hardcoded address assumptions
- Position-independent code (PIC) required

### Memory Management and Reference Counting

#### dlopen/dlclose Reference Counting
```c
void *h1 = dlopen("module.so", RTLD_NOW);  // ref_count = 1
void *h2 = dlopen("module.so", RTLD_NOW);  // ref_count = 2, same handle returned
dlclose(h1);                               // ref_count = 1, module stays loaded
dlclose(h2);                               // ref_count = 0, module unloaded
```

#### Global Constructor/Destructor Calls
```c
// In module code
__attribute__((constructor))
void module_init(void) {
    // Called automatically on dlopen()
}

__attribute__((destructor))
void module_cleanup(void) {
    // Called automatically on dlclose()
}
```

### Debugging Dynamic Loading

#### Common Tools
```bash
# List loaded shared libraries
ldd valkey-server
cat /proc/PID/maps  # Linux
vmmap PID           # macOS

# Debug symbol resolution
LD_DEBUG=symbols valkey-server  # Linux
DYLD_PRINT_BINDINGS=1 valkey-server  # macOS

# Trace dlopen calls
strace -e openat valkey-server  # Linux
dtruss -f valkey-server        # macOS
```

#### GDB Debugging
```bash
# Set breakpoint on dlopen
(gdb) break dlopen
(gdb) info sharedlibrary  # List loaded libraries
(gdb) info symbol 0x7fff12345678  # Resolve address to symbol
```

### Security Considerations in Dynamic Loading

#### 1. Library Path Hijacking
```bash
# Dangerous: Attacker controls LD_LIBRARY_PATH
export LD_LIBRARY_PATH="/tmp:/usr/lib"
./valkey-server  # May load malicious libraries from /tmp
```

#### 2. Symbol Interposition
```c
// Malicious library overriding system functions
int open(const char *path, int flags) {
    // Log all file opens, then call real open()
    return real_open(path, flags);
}
```

#### 3. Code Injection via Modules
- Modules run with full server privileges
- No sandboxing by default
- Trust boundary at module load time

### Performance Implications

#### Loading Time Factors
```c
// Factors affecting dlopen() performance:
// 1. File I/O for reading shared library
// 2. Symbol table size (O(n) for linear search, O(1) for hash)
// 3. Number of relocations to perform
// 4. Dependency chain depth
// 5. RTLD_NOW vs RTLD_LAZY flag choice
```

#### Memory Overhead
```c
// Memory cost per loaded module:
// - Code segments (read-only, shared)
// - Data segments (read-write, per-process)
// - Symbol tables and relocation info
// - PLT/GOT entries for dynamic linking
```

This deep dive explains the fundamental mechanisms that Valkey's `moduleLoad` function leverages to provide its powerful dynamic extension capabilities.

## Function Signature

```c
int moduleLoad(const char *path, void **module_argv, int module_argc, int is_loadex)
```

### Parameters

- **`path`**: Absolute or relative path to the shared library file (.so on Linux, .dylib on macOS)
- **`module_argv`**: Array of arguments to pass to the module's initialization function
- **`module_argc`**: Number of arguments in `module_argv`
- **`is_loadex`**: Boolean flag indicating if this is a LOADEX operation (extended load with configuration)

### Return Value

- **`C_OK`**: Module loaded successfully
- **`C_ERR`**: Module loading failed

## Detailed Loading Process

### 1. File Permission Validation

```c
struct stat st;
if (stat(path, &st) == 0) {
    if (!(st.st_mode & (S_IXUSR | S_IXGRP | S_IXOTH))) {
        serverLog(LL_WARNING, "Module %s failed to load: It does not have execute permissions.", path);
        return C_ERR;
    }
}
```

**Purpose**: Ensures the module file has execute permissions for at least one of: user, group, or others.

**Security Note**: This is a "best effort" check - the actual loading may still fail due to other permission issues.

### 2. Dynamic Library Loading

```c
int dlopen_flags = RTLD_NOW | RTLD_LOCAL;
#if (defined(__GLIBC__) || defined(__FreeBSD__)) && !defined(VALKEY_ADDRESS_SANITIZER)
    dlopen_flags |= RTLD_DEEPBIND;
#endif

handle = dlopen(path, dlopen_flags);
```

**Loading Flags**:
- **`RTLD_NOW`**: Resolve all symbols immediately (not lazily)
- **`RTLD_LOCAL`**: Symbols are not available for subsequently loaded libraries
- **`RTLD_DEEPBIND`**: (Linux/FreeBSD only) Prefer module's symbols over global symbols

**RTLD_DEEPBIND Exclusions**:
- Disabled when using AddressSanitizer (ASAN) due to compatibility issues
- Only available on glibc and FreeBSD systems

### 3. Entry Point Resolution

```c
const char *onLoadNames[] = {"ValkeyModule_OnLoad", "RedisModule_OnLoad"};
for (size_t i = 0; i < sizeof(onLoadNames) / sizeof(onLoadNames[0]); i++) {
    onload = (int (*)(void *, void **, int))(unsigned long)dlsym(handle, onLoadNames[i]);
    if (onload != NULL) {
        if (i != 0) {
            serverLog(LL_NOTICE, "Legacy Redis Module %s found", path);
        }
        break;
    }
}
```

**Entry Point Search Order**:
1. `ValkeyModule_OnLoad` (preferred)
2. `RedisModule_OnLoad` (legacy compatibility)

**Function Signature Expected**: `int OnLoad(void *ctx, void **argv, int argc)`

### 4. Module Context Creation and Initialization

```c
ValkeyModuleCtx ctx;
moduleCreateContext(&ctx, NULL, VALKEYMODULE_CTX_TEMP_CLIENT);
if (onload((void *)&ctx, module_argv, module_argc) == VALKEYMODULE_ERR) {
    // Handle initialization failure
}
```

**Context Setup**:
- Creates a temporary module context with a temporary client
- Context is passed to the module's `OnLoad` function
- Module registers itself and its commands during this call

**Initialization Failure Handling**:
- If `ctx.module` exists: Full cleanup including command unregistration
- If `ctx.module` is NULL: Indicates `ValkeyModule_Init` failed (usually due to name collision)

### 5. Module Registration

```c
dictAdd(modules, ctx.module->name, ctx.module);
ctx.module->blocked_clients = 0;
ctx.module->handle = handle;
```

**Registration Steps**:
1. Add module to global `modules` dictionary
2. Initialize blocked clients counter
3. Store the `dlopen` handle for later cleanup

### 6. Load Metadata Storage

```c
ctx.module->loadmod = zmalloc(sizeof(struct moduleLoadQueueEntry));
ctx.module->loadmod->path = sdsnew(path);
ctx.module->loadmod->argv = module_argc ? zmalloc(sizeof(robj *) * module_argc) : NULL;
ctx.module->loadmod->argc = module_argc;
for (int i = 0; i < module_argc; i++) {
    ctx.module->loadmod->argv[i] = module_argv[i];
    incrRefCount(ctx.module->loadmod->argv[i]);
}
```

**Purpose**: Stores loading parameters for:
- Module inspection commands
- Configuration regeneration
- Potential reloading scenarios

### 7. ACL Integration

```c
if (ctx.module->num_commands_with_acl_categories) {
    ACLRecomputeCommandBitsFromCommandRulesAllUsers();
}
```

**Purpose**: Updates user permission bitmasks if the module registered commands with ACL categories.

### 8. Configuration Validation

```c
int post_load_err = 0;
if (listLength(ctx.module->module_configs) && !ctx.module->configs_initialized) {
    serverLogRaw(LL_WARNING, "Module Configurations were not set, likely a missing LoadConfigs call. Unloading the module.");
    post_load_err = 1;
}

if (is_loadex && dictSize(server.module_configs_queue)) {
    serverLogRaw(LL_WARNING, "Loadex configurations were not applied, likely due to invalid arguments. Unloading the module.");
    post_load_err = 1;
}
```

**Configuration Checks**:
1. Verifies that modules with configurations called `LoadConfigs`
2. For LOADEX operations, ensures all configuration queue entries were processed

### 9. Event Notification

```c
moduleFireServerEvent(VALKEYMODULE_EVENT_MODULE_CHANGE, VALKEYMODULE_SUBEVENT_MODULE_LOADED, ctx.module);
```

**Purpose**: Notifies other modules about the newly loaded module via the event system.

### 10. Context Cleanup

```c
moduleFreeContext(&ctx);
```

**Purpose**: Releases the temporary context resources while preserving the registered module.

## Error Handling

### Common Failure Scenarios

1. **File Not Found/Accessible**
   ```
   Module %s failed to load: %s (dlerror message)
   ```

2. **Missing Entry Point**
   ```
   Module %s does not export ValkeyModule_OnLoad() or RedisModule_OnLoad() symbol. Module not loaded.
   ```

3. **Initialization Failure**
   ```
   Module %s initialization failed. Module not loaded.
   ```
   ```
   Module %s initialization failed. Module name is busy.
   ```

4. **Configuration Issues**
   ```
   Module Configurations were not set, likely a missing LoadConfigs call. Unloading the module.
   ```

### Cleanup on Failure

- Calls `dlclose()` to unload the shared library
- Frees any allocated module structures
- Unregisters any partially registered commands
- Releases the module context

## Integration Points

### Called From

1. **`moduleLoadFromQueue()`** - During server startup
2. **`moduleLoadCommand()`** - Via `MODULE LOAD` command
3. **`moduleLoadexCommand()`** - Via `MODULE LOADEX` command

### Dependencies

- **`moduleCreateContext()`** - Context management
- **`moduleFreeContext()`** - Context cleanup
- **`moduleUnregisterCleanup()`** - Failure cleanup
- **`ACLRecomputeCommandBitsFromCommandRulesAllUsers()`** - Permission updates
- **`moduleFireServerEvent()`** - Event notifications

## Security Considerations

1. **File Permissions**: Basic execute permission check
2. **Symbol Isolation**: `RTLD_LOCAL` prevents symbol pollution
3. **Deep Binding**: `RTLD_DEEPBIND` prevents symbol hijacking (where available)
4. **Configuration Validation**: Ensures proper module configuration setup

## Platform-Specific Behavior

### Linux/FreeBSD
- Uses `RTLD_DEEPBIND` for symbol isolation
- Disabled under AddressSanitizer

### macOS/Other Platforms
- Standard `RTLD_NOW | RTLD_LOCAL` flags only
- No deep binding support

## Related Functions

- **`moduleUnload()`** - Counterpart unloading function
- **`moduleLoadFromQueue()`** - Batch loading during startup
- **`moduleEnqueueLoadModule()`** - Queue modules for later loading
- **`moduleLoadQueueEntryFree()`** - Cleanup load metadata

## Usage Examples

### Basic Module Loading
```c
// Load module without arguments
if (moduleLoad("/path/to/module.so", NULL, 0, 0) == C_OK) {
    // Module loaded successfully
}
```

### Module Loading with Arguments
```c
// Prepare arguments
void **argv = zmalloc(sizeof(void*) * 2);
argv[0] = createStringObject("arg1", 4);
argv[1] = createStringObject("arg2", 4);

// Load with arguments
if (moduleLoad("/path/to/module.so", argv, 2, 0) == C_OK) {
    // Module loaded successfully
}
```

This documentation provides a comprehensive understanding of the `moduleLoad` function's implementation, error handling, and integration within the Valkey module system.