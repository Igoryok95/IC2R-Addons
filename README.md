# GraviSuite Reforged for Minecraft 1.21.1 + IC2R

A NeoForge 1.21.1 port of GraviSuite Reforged, integrated with **IndustrialCraft 2: Refactored (IC2R)**.
This branch targets the modern IC2R API, EU equipment, data components and networking.

The original 1.6.4 implementation and documentation are retained in [legacy/1.6.4](legacy/1.6.4).
The upstream 1.4.7, 1.5.2 and 1.6.4 branches remain available in Git history.

## Requirements

- Minecraft **1.21.1**
- NeoForge **21.1.234 or newer** (development target: 21.1.264)
- IC2R **21.1.66**
- Java **21**

VintageCore and the original IC2 are not required. This is an addon for IC2R.

## Equipment and progression

| Equipment | EU capacity | Tier | Role |
| --- | ---: | ---: | --- |
| Advanced Lappack | 40,000,000 | 4 | Upgrade to IC2R's 20,000,000 EU Lappack; powers held tools |
| Ultimate Lappack | 100,000,000 | 4 | Higher-capacity portable EU storage |
| Advanced Electric Jetpack | 4,000,000 | 3 | Stronger thrust, hover, boost and tool charging |
| Advanced Nano Chestplate | 8,000,000 | 3 | Nano protection with the advanced jetpack, boost and tool charging |
| Gravitational Chestplate | 100,000,000 | 4 | Powered creative flight, boost, energy-backed protection and fall absorption |
| Advanced Diamond Drill | 45,000 | 2 | Normal or 3x3 mining; three efficiency settings; torch placement |
| Advanced Chainsaw | 45,000 | 2 | Faster cutting, IC2R shearing and bounded tree felling |
| Vajra | 1,000,000 | 2 | Universal tool; ore-vein or connected-block mining; Silk Touch / Fortune III |
| GraviTool | 100,000 | 2 | Electric wrench, treetap, hoe and block rotation |
| Magnet | 10,000 | 1 | Attracts dropped items while enabled in the hotbar |
| Voider | 10,000 | 1 | Removes items matching an explicitly selected block filter |
| Relocator | 10,000,000 | 4 | Ten saved destinations, personal teleportation, translocator projectiles and temporary portals |

Seven crafting components are retained: Superconductor Cover, Superconductor, Cooling Core,
Gravitational Engine, Magnetron, Vajra Core and Engine Booster.

Recipes use IC2R's shaped serializer so charge from electric ingredients is carried into the
result, up to its capacity. The addon has English and Russian localizations.

## Overlap with IC2R

| Original feature | Port decision |
| --- | --- |
| Advanced Iridium Drill | Omitted: IC2R already has an iridium drill with Silk Touch / Fortune III. Use that item; it is not registered a second time. |
| Advanced Diamond Drill | Retained for 3x3 mining and adjustable speed / energy use, beyond the normal IC2R diamond drill. |
| Advanced Nano Chestplate | Retained: IC2R's attachment recipe accepts its standard electric jetpack, not the advanced GraviSuite jetpack. This version adds stronger flight, boost, tool charging and 8 million EU. |
| Gravitational Chestplate | Retained: IC2R's quantum chestplate has a jetpack; this item provides powered creative flight and larger storage. |
| Lappacks | Capacities adapted to modern IC2R: 20 million EU in the base Lappack, then 40 and 100 million EU. |
| Quantum chestplate recipe | IC2R's original recipe is preserved. GraviSuite does not replace IC2R progression recipes. |
| Magnet | Portable item attraction; different from IC2R's stationary magnetizer. |
| GraviTool | Combines existing operations in one configurable tool instead of registering duplicate standalone tools. |

## Controls

Key bindings can be changed in Minecraft's Controls screen.

- **G**: toggle the equipped GraviSuite flight engine.
- **H**: toggle a magnet in the hotbar.
- **IC2 mode key + right-click**: cycle tool / mining / relocator mode.
- **IC2 Alt key + right-click**: cycle mining efficiency.
- **Sneak + IC2 mode key + right-click** with a Vajra: switch Silk Touch / Fortune III.
- **Sneak + IC2 mode key + right-click** with the chainsaw: toggle tree felling.
- **Sneak while mining**: single-block operation.
- **Right-click a block with the drill or Vajra**: place a torch from inventory.
- **Right-click with the magnet**: toggle it.
- **Right-click a block with the voider**: select its item as the filter.
- **Sneak + right-click with the voider**: remove matching inventory items, spending energy.
- **Sneak + right-click with the relocator**: manage destinations.
- **IC2 jump / boost keys**: jetpack ascent, hover controls and boost.

Relocator costs: 1 million EU for same-dimension personal teleportation, 1.5 million EU across
dimensions, 2 million EU for a translocator projectile and 2.5 million EU for a portal projectile.
Destinations are recorded by the server. Teleportation rejects obstructed or out-of-border destinations.

## Build

Use the checked-in Gradle wrapper and Java 21:

```sh
./gradlew build
```

The default dependency resolver downloads IC2R 21.1.66 from its public GitHub release. To build and
test against a locally patched IC2R JAR instead:

```sh
./gradlew build runGameTestServer -Pic2rJar=/absolute/path/to/ic2-neoforge-21.1.66_fixed.jar
```

PowerShell example:

```powershell
.\gradlew.bat build runGameTestServer '-Pic2rJar=C:/GIT/IC2R/build/libs/ic2-neoforge-21.1.66_fixed.jar'
```

The mod JAR is written to `build/libs/gravisuite-neoforge-1.21.1-3.0.0.jar`.
The separate `-sources.jar` is for development and must not be installed as a mod.
Install the mod and IC2R on both the client and server. Configuration is in `config/gravisuite-common.toml`.

## Port validation

Compilation against NeoForge 1.21.1 and IC2R succeeds. Runtime regression testing is in progress;
this section will be updated with the completed checks before the port is delivered.
A successful build alone does not establish compatibility with every modpack or multiplayer setup.

## Credits

Original GraviSuite concept: **SeNtiMeL** and the **IC2 Dev Team**.
GraviSuite Reforged legacy implementation and assets: **VintageModsReforged** and upstream contributors.
NeoForge 1.21.1 / IC2R integration: **Igoryok95**.

Upstream: https://github.com/VintageModsReforged/GraviSuiteReforged

IC2R: https://github.com/neo-industrial-mc/IC2R
