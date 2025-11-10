# Changelog

## [2.0.0] - 2025-10-11

### Added
- In Tessellator.getVertexState, the use of PriorityQueue has been removed, and instead, the indices are sorted using Arrays.sort and reusable buffers are added, which reduces the number of allocations and improves performance.
- In CommandSolderArmor and CommandSetFusionEnergy, the getAliases and getTabCompletions methods now return Collections.emptyList(), which eliminates the need to create new empty lists every time they are called.