Fixtures for manual regression:

- unprototyped/simple_recursive.nss
  - Minimal recursive script used to exercise call-graph/SCC prototyping.
  - Compile to .ncs with your preferred nwscript compiler, then run:
    java -jar NCSDecomp.jar --strict-signatures <compiled.ncs>

Purpose: ensure PrototypeEngine resolves mutually-recursive calls without
falling back to on-demand prototyping or throwing the legacy
"Hit JSR on unprototyped subroutine" error.

