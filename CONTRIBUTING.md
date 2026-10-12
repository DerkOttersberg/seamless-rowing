# Contributing

Report bugs with the issue template and the exact Minecraft, loader and mod
versions. A short recording helps diagnose hand contact and compatibility.

Use the version branch matching the change: `26.3`, `26.2`, `1.21.1` or
`1.20.1`. Each contains its supported loaders. Pull requests should target that
branch; do not introduce permanent loader branches. The default branch is
`26.3`.

Run `./gradlew clean check build` with Java 25 as the Gradle launcher. The branch
catalog and Foojay resolver select the compiler toolchain. Changes to rendered
poses also need genuine packaged client/server and visual verification as
described in `docs/PORT_ACCEPTANCE.md`. CI reports alone cannot clear that gate.

Keep common code independent of loader packages and optional runtime frameworks.
Loader adapters own optional integrations, menus and lifecycle hooks. Preserve
vanilla controls, sounds, network traffic, skins and the client-only boundary.

The project uses the All Rights Reserved license in `LICENSE`. Modpacks may
include unmodified official releases under that license's stated permission.
Public source visibility does not change those terms. Discuss substantial code
contributions with the author before preparing them.
