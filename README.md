# AE2 Portals Bridge

A Fabric mod that makes [Applied Energistics 2](https://github.com/AppliedEnergistics/Applied-Energistics-2) cables connect through [Immersive Portals](https://github.com/iPortalTeam/ImmersivePortalsMod) — **functionally, not just visually**. Place a cable on one side of a portal, another cable on the other side, and they form a single ME network. Power, items, channels, and ME controller connectivity all cross the portal as if the cables were directly adjacent.

## Status

**Phase 0 — scaffold only.** The project builds, loads into the game, and registers a no-op entrypoint. Nothing actually works yet.

| Phase | Scope | Status |
|-------|-------|--------|
| 0 | Fabric Loom scaffold, mod entrypoint, Mixin stubs | **done** |
| 1 | Visual layer — render AE2 cable geometry through the portal | not started |
| 2 | Functional layer (same-dimension) — Mixin into `CablePart.updateConnections()` to detect portal-adjacent cables and add remote cable's grid node as a connection | not started |
| 3 | Functional layer (cross-dimension) — use AE2's P2P tunnel mechanism internally when the portal leads to a different Level | not started |
| 4 | Testing, edge cases, polish | not started |

## Requirements

- Minecraft **1.21.1**
- Fabric Loader **0.16.9+**
- Fabric API **0.110.5+** (for 1.21.1)
- Java **21+** (build environment)
- Runtime: Applied Energistics 2 (Fabric) **19.2.4+** and Immersive Portals **0.21.3+**

## Build

The repo has the Gradle wrapper properties file (`gradle/wrapper/gradle-wrapper.properties`) but not the wrapper JAR. On first checkout, generate the wrapper from any system Gradle 8.10+ install:

```bash
# One-time: generate gradle-wrapper.jar / gradlew / gradlew.bat
gradle wrapper --gradle-version 8.10.2

# Build the mod
./gradlew build
```

The built JAR will be in `build/libs/ae2-portals-bridge-0.1.0.jar`.

If you don't have a system Gradle, download it from https://gradle.org/releases/ and use that to run `gradle wrapper` once.

## Project layout

```
ae2-portals-bridge/
├── build.gradle                      Fabric Loom 1.21.1, depends on AE2 + IP
├── settings.gradle
├── gradle.properties                 version + dependency pins
├── gradle/wrapper/gradle-wrapper.properties   (no JAR — see Build above)
├── src/main/java/com/example/ae2portals/
│   ├── Ae2PortalsBridge.java         entrypoint
│   └── bridge/
│       └── PortalCableBridge.java    visual + functional coordination
├── src/main/java/com/example/ae2portals/mixin/
│   ├── client/
│   │   └── PortalRenderCallbackMixin.java   Phase 1 hook (no-op stub)
│   └── server/
│       └── CablePartGridMixin.java          Phase 2 hook (no-op stub)
├── src/main/resources/
│   ├── fabric.mod.json               mod metadata
│   └── ae2portals.mixins.json        Mixin config
├── LICENSE                           MIT
└── README.md
```

## Development status — honest assessment

The plan is grounded in real source I pulled from the AE2 repo
(`appeng/client/render/cablebus/CableBusBakedModel.java` and
`appeng/parts/networking/CablePart.java`). The visual layer is
straightforward — Immersive Portals exposes a render callback and AE2
already has `CableBuilder` for assembling cable quads. The functional
layer is the hard part: AE2's `CablePart.updateConnections()` only looks
at the local chunk for grid neighbors, and bridging that to the
"other side" of a portal requires a Mixin that detects portal-adjacent
cables and adds the remote cable's grid node as a connection. For
cross-dimension portals, AE2's existing P2P tunnel mechanism is the
likely answer (the cable effectively becomes a P2P when one side is
in a different Level).

The IP API path in this scaffold is a placeholder — the repo
restructured between versions and the actual `PortalRenderer` /
`PortalAPI` class names need to be confirmed against the current
`1.21` branch source before Phase 1 work begins. The README's roadmap
table will be updated as each phase is actually built and tested.

## License

MIT — see `LICENSE`.

## Workflow — branches, releases, testing

| Branch | Purpose |
|--------|---------|
| `main` | Stable. The latest released version. Eric (the owner of this mod's test instance) tests releases before they land here. |
| `develop` | Active development. Every commit here is a work-in-progress; the source of truth for the next release. |

**Testing a build:**
1. Go to https://github.com/kevin655655/ae2-portals-bridge/releases
2. Download the jar from the latest **prerelease** (tagged `vX.Y.Z-dev.N`)
3. Drop it in `mods/` on a dev Minecraft 1.21.1 instance with AE2 Fabric 19.2.18+ and Immersive Portals 6.0.6+ already installed
4. Report back: log output, screenshot, crash report — whatever you have

The build that produced each release is reproducible: `git checkout <tag>` then `./gradlew build` gives the same jar.

**Cutting a new test build** (from anywhere with `gh` CLI authenticated and the repo cloned):

```bash
# Auto-increments the next dev tag (v0.2.0-dev.1, v0.2.0-dev.2, ...):
./scripts/release.sh

# Or pin a specific tag:
./scripts/release.sh v0.2.0-dev.3
```

`scripts/release.sh` runs `./gradlew clean build`, creates a prerelease GitHub Release tagged at the current `develop` HEAD, uploads the jar as the only asset, and generates notes from `git log <prev-tag>..HEAD`. Requires `gh` CLI authenticated as `kevin655655`.

**Stable releases** (drop the `-dev.N` suffix, e.g. `./scripts/release.sh v0.2.0`) publish as a full release, not prerelease. The default auto-increment is dev-only; stable is always a deliberate choice.

## License

MIT — see `LICENSE`.
