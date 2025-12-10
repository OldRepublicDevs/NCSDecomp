---
name: SubroutinePrototypingRefactor
overview: Eliminate "unprototyped subroutine" failures by converting prototyping to a robust, graph-based fixed-point analysis with safe fallbacks.
todos:
  - id: build-callgraph
    content: Implement CallGraphBuilder to scan all ASubroutine nodes and return call graph
    status: completed
  - id: scc-collapse
    content: Add SCCUtil using Tarjan to group recursive subroutines
    status: completed
    dependencies:
      - build-callgraph
  - id: proto-engine
    content: Create PrototypeEngine to run fixed-point signature inference over SCCs
    status: completed
    dependencies:
      - scc-collapse
  - id: adapt-FileDecompiler
    content: Replace legacy multi-pass prototyping loop with PrototypeEngine invocation
    status: completed
    dependencies:
      - proto-engine
  - id: slim-DoTypes
    content: Remove prototype-generation paths from DoTypes; make it rely on pre-computed signatures
    status: completed
    dependencies:
      - adapt-FileDecompiler
  - id: migrate-on-demand
    content: Audit callers (e.g., MainPass, SubScriptState) to ensure they no longer attempt lazy prototyping
    status: completed
    dependencies:
      - slim-DoTypes
  - id: strict-flag
    content: Add --strict-signatures CLI option and plumbing
    status: completed
    dependencies:
      - proto-engine
  - id: tests-realworld
    content: Add regression tests covering previous failure cases
    status: completed
    dependencies:
      - proto-engine
---

# Subroutine Prototyping Refactor

Goal: Replace the current ad-hoc multi-pass prototyping (spread across `FileDecompiler`, `SubroutinePathFinder`, and `DoTypes`) with a clear, deterministic pipeline that guarantees every reachable subroutine obtains a sound prototype (signature) without runtime exceptions.

## Key Ideas

1. **Call-graph first** – Build a complete call graph (JSR targets) in one sweep before prototyping.
2. **SCC handling** – Use Tarjan/Kosaraju to collapse strongly-connected components so mutually-recursive subs are analysed together.
3. **Fixed-point data-flow** – Iterate over SCCs propagating (paramCount, paramTypes*, returnType*) until convergence; unknowns start as “Any”.
4. **Reachability pruning** – Start from `main` (& optional `globals`) to ignore dead subs early.
5. **Safe fallback** – After `N` iterations, freeze unresolved types to `Any`, never throw.  Emit diagnostics instead of crashing.
6. **Single responsibility** – Move prototyping logic into a new `analysis/PrototypeEngine` class; trim duties of `DoTypes` to pure type-checking.

## Implementation Todos

- build-callgraph: Parse JSR destinations in every `ASubroutine` to produce `Map<SubId, Set<SubId>>` (`src/.../analysis/CallGraphBuilder.java`).
- scc-collapse: Implement Tarjan to output `List<SCC>` (new `SCCUtil.java`).
- proto-engine: Create `PrototypeEngine` that:
- accepts call graph + `SubroutineAnalysisData`
- processes SCCs topologically
- iterates param/return inference until no changes
- commits results back into each `SubroutineState`.
- adapt-FileDecompiler: Replace current `for(pass…)` block (≈ lines 740-770, 789-805) with single call to `PrototypeEngine.run()`.
- slim-DoTypes: Remove prototype synthesis paths (`prototypeSubroutine`, fallback sections 246-279, 285-309); retain stack validation only.
- migrate-on-demand: Where on-demand prototype was called, now just `assert substate.isPrototyped()`.
- strict-flag: Add CLI flag `--strict-signatures` in `NCSDecompCLI` to abort if any `Any` types remain.
- tests-realworld: Add regression scripts under `tests/fixtures` with scripts previously failing; verify decompile completes and signatures make sense.

## File Touch Points

- `[src/main/java/com/kotor/resource/formats/ncs/analysis/CallGraphBuilder.java]` (new)
- `[src/main/java/com/kotor/resource/formats/ncs/analysis/SCCUtil.java]` (new)
- `[src/main/java/com/kotor/resource/formats/ncs/analysis/PrototypeEngine.java]` (new)
- Update existing:
- `[src/main/java/com/kotor/resource/formats/ncs/FileDecompiler.java]`
- `[src/main/java/com/kotor/resource/formats/ncs/DoTypes.java]`
- `[src/main/java/com/kotor/resource/formats/ncs/NCSDecompCLI.java]`

## Optional Enhancements

- Memoize inferred struct types to reduce allocations.
- Parallelise SCC analysis (independent components).

## Expected Outcome

No `RuntimeException("Hit JSR on unprototyped subroutine …")`; analysis is faster (single pass over bytecode to build graph, predictable iteration), codebase becomes cleaner and easier to reason about.