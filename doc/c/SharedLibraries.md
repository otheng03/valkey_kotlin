THE INSIDE STORY ON SHARED LIBRARIES AND DYNAMIC LOADING By David M. Beazley, Brian D. Ward, and Ian R. Cooke

# Compilers and object files

- Compilers convert source files into object files containing machine code
- Object files are organized into sections (text, data, read-only) corresponding to different parts of the source program
- Each object file includes a symbol table listing all identifiers from the source code

```
#include <stdio.h>
int x = 42;

int main() {
    printf("Hello World, x = %d\n", x);
}
```

produces an object file that contains
- a text section with the machine code instructions of the program
- a data section with the global variable x, and a “read-only” section with the string literal Hello World, x = %d\n.
- a symbol table for all the identifiers that appear in the source code.

An easy way to view the symbol table is with the Unix command nm—for example,
```
$ nm hello.o
00000000 T main
U printf
00000000 D x
``````

# Linkers and linking

- The linker (like `ld`) combines object files and libraries to create executable files
- Its primary job is binding symbolic names to actual memory addresses

**Two-Pass Process:**
1. **First Pass**: Concatenates sections from all object files
    - All text sections are joined together
    - All data sections are joined together
    - Same for other section types

2. **Second Pass**: Resolves symbols to real memory addresses
    - Uses relocation lists from each object file
    - Relocation lists contain symbol names and their offsets that need patching

For example, the relocation list for the earlier example looks something like this:
```
$ objdump -r hello.o
hello.o: file format elf32-i386

RELOCATION RECORDS FOR [.text]:
OFFSET TYPE VALUE
0000000a R_386_32 x
00000010 R_386_32 .rodata
00000015 R_386_PC32 printf
```

# Static libraries

To improve modularity and reusability, programming libraries usually include commonly used functions.
The traditional library is an archive (.a file)
When a static library is included during program linking, the linker makes a pass through the library and adds all the code
and data corresponding to symbols used in the source program. The linker ignores unreferenced library symbols and
aborts with an error when it encounters a redefined symbol

# Dynamic libraries
**Shared Libraries vs Static Libraries:**
- Shared libraries delay linking until runtime, performed by a dynamic linker-loader
- Programs and libraries remain separate until execution
- Enables easier maintenance - library updates don't require application recompilation

```
Executable
┌────────────────────┐
│ foo()              │                                Shared library (libc.so)
│ ...                │      ┌────────────────────┐    ┌────────────────────┐
│ call malloc ---------------> malloc: jmp x -----------> malloc:          │
│ ...                │      |                    │    └────────────────────┘
│ call printf ---------------> printf: jmp ??? ----+  Dynamic linker (ld.so.1)
│ ...                │      └────────────────────┘ │  ┌────────────────────┐
└────────────────────┘                             +---->bindsymbol:       │
                                                      └────────────────────┘
```

**Memory Optimization Benefits:**
- Library code is read-only and shared among processes using virtual memory techniques
- Single physical copy of library instructions serves hundreds of programs
- Significantly reduces memory usage and improves system performance

**Runtime Symbol Resolution:**
- Dynamic linker searches libraries in the order they were specified during linking
- Uses first definition of any symbol encountered
- Duplicate symbols can exist in special cases (weak definitions, library conflicts, `LD_LIBRARY_PATH` issues)

**Library Loading Process:**
1. **Startup**: OS loads dynamic linker (`ld.so`) instead of program directly
2. **Library Discovery**: Linker scans embedded library names (simple names like `libm.so.6`, not absolute paths)
3. **Search Path**: Uses configurable search path from `/etc/ld.so.conf`, executable, or `LD_LIBRARY_PATH`
4. **Loading Order**: Libraries load in link order, with dependencies added via breadth-first traversal
5. **Optimization**: Uses cache file (`/etc/ld.so.cache`) to avoid slow directory searches
6. **Fallback**: Manual search of system directories (`/lib`, `/usr/lib`) if cache lookup fails

# Dynamic loading

**Dynamic Loading API:**
- Three core functions provided by the dynamic linker:
  - loads a new shared library at runtime `dlopen()`
  - looks up specific symbols in the loaded library `dlsym()`
  - unloads the library from memory `dlclose()`

**Key Difference from Static Linking:**
- Dynamically loaded modules are completely decoupled from the main application
- Not used to resolve unbound symbols in the normal linking process
- Loaded independently with their own dependency chains

**Module Isolation and Symbol Namespaces:**
- Each dynamically loaded module gets its own **private linkchain**
- Creates a tree structure of library dependencies rather than a flat list
- Results in **private symbol namespaces** for each module

**Symbol Resolution Order:** When binding symbols in a dynamically loaded module:
1. Search the module itself first
2. Search libraries in the module's linkchain
3. Search the main executable and its libraries

**Benefits of Tree Structure:**
- **Symbol Isolation**: Modules with identical symbol names don't clash
- **Namespace Protection**: Each module operates independently
- **Predictable Behavior**: Explains why scripting language extensions work even with namespace conflicts

**Limitation:**
- Extension modules cannot dynamically bind to symbols defined in other dynamically loaded modules (unless special loader options are used)

This system provides the foundation for plugin architectures and extensible applications,
ensuring modules remain isolated while still being able to interact with the main program.


