# Overview

ZSETs are ordered sets using two data structures to hold the same elements 
in order to get O(log(N)) INSERT and REMOVE operations into a sorted data structure.
- hash table : object -> score
- skip list : score -> object

## Skip lists

Skip Lists: A Probabilistic Alternative to Balanced Trees - William Pugh

- A data structure that can be used in place of balanced trees.
- Use probabilistic balancing rather than strictly enforced balancing for much simpler and faster insertion and deletion than balanced trees.

A node’s ith forward pointer, instead of pointing 2^i–1 nodes ahead, points to the next node of level i or higher.
Insertions or deletions would require only local modifications; the level of a node, chosen randomly when the node is inserted, need never change. 
Some arrangements of levels would give poor execution times, but we will see that such arrangements are rare.
Because these data structures are linked lists with extra pointers that skip over intermediate nodes, named them skip lists.
