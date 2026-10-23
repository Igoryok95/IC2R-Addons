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
| Advanced Nano Leggings | 4,000,000 | 3 | Nano protection, 5,000 EU transfer and +15% sprint speed for 8 EU/t |
| Advanced Nano Boots | 4,000,000 | 3 | Nano protection, 5,000 EU transfer and heavy-fall absorption while energy lasts |
| Advanced Quantum Leggings | 40,000,000 | 4 | IC2R quantum sprint / swimming abilities with four times the storage and 50,000 EU transfer |
| Advanced Quantum Boots | 40,000,000 | 4 | IC2R quantum jump, four times the storage, 50,000 EU transfer and half the fall-absorption energy cost |
| Advanced Diamond Drill | 45,000 | 2 | Normal or 3x3 mining; three efficiency settings; torch placement |
| Advanced Chainsaw | 45,000 | 2 | Faster cutting, IC2R shearing and bounded tree felling |
| Advanced Iridium Drill | 1,000,000 | 3 | Upgrade of the IC2R iridium drill: 3x3 mining, faster modes, Silk Touch / Fortune III |
| Vajra | 10,000,000 | 3 | Universal tool; ore-vein or connected-block mining; Silk Touch / Fortune III |
| GraviTool | 100,000 | 2 | Electric wrench, treetap, hoe and block rotation |
| Magnet | 10,000 | 1 | Attracts dropped items while enabled in the hotbar |
| Voider | 10,000 | 1 | Removes items matching an explicitly selected block filter |
| Relocator | 10,000,000 | 4 | Ten saved destinations, personal teleportation, translocator projectiles and temporary portals |

Seven crafting components are retained: Superconductor Cover, Superconductor, Cooling Core,
Gravitational Engine, Magnetron, Vajra Core and Engine Booster.

Recipes use IC2R's shaped serializer so charge from electric ingredients is carried into the
result, up to its capacity. The addon has English and Russian localizations.

The four leg and boot upgrades require their corresponding IC2R armor pieces. Their armor
materials and item textures reuse the original IC2R artwork. Nano boots keep the original
5,000 EU cost per point of fall damage but remove the base boots' heavy-fall cutoff; quantum
boots use 10,000 instead of 20,000 EU per point beyond the first ten blocks.
Quantum sprint, swimming, jump and hazmat behavior are inherited from IC2R.
There are no helmet upgrades in this addon; helmets are reserved for the planned solar addon.

## Overlap with IC2R

| Original feature | Port decision |
| --- | --- |
| Advanced Iridium Drill | Retained as an upgrade requiring the IC2R iridium drill: 1 million instead of 300,000 EU, 5,000 instead of 1,000 EU transfer, 3x3 mining and higher speed. |
| Advanced Diamond Drill | Retained for 3x3 mining and adjustable speed / energy use, beyond the normal IC2R diamond drill. |
| Advanced Nano Chestplate | Retained: IC2R's attachment recipe accepts its standard electric jetpack, not the advanced GraviSuite jetpack. This version adds stronger flight, boost, tool charging and 8 million EU. |
| Gravitational Chestplate | Retained: IC2R's quantum chestplate has a jetpack; this item provides powered creative flight and larger storage. |
| Lappacks | Capacities adapted to modern IC2R: 20 million EU in the base Lappack, then 40 and 100 million EU. |
| Nano / quantum legs and boots | Separate upgrades of the corresponding IC2R pieces, with four times their storage and faster charging. Base quantum movement abilities are retained. |
| Quantum chestplate recipe | IC2R's original recipe is preserved. GraviSuite does not replace IC2R progression recipes. |
| Magnet | Portable item attraction; different from IC2R's stationary magnetizer. |
| GraviTool | Combines existing operations in one configurable tool instead of registering duplicate standalone tools. |

## Controls

Key bindings can be changed in Minecraft's Controls screen.

- **G**: toggle the equipped GraviSuite flight engine.
- **H**: toggle a magnet in the hotbar.
- **IC2 mode key + right-click**: cycle tool / mining / relocator mode.
- **IC2 Alt key + right-click**: cycle mining efficiency.
- **Sneak + IC2 mode key + right-click** with the Advanced Iridium Drill or Vajra: switch Silk Touch / Fortune III.
- **Sneak + IC2 mode key + right-click** with the chainsaw: toggle tree felling.
- **Sneak while mining**: single-block operation.
- **Right-click a block with the drill or Vajra**: place a torch from inventory.
- **Right-click with the magnet**: toggle it.
- **Right-click a block with the voider**: select its item as the filter.
- **Sneak + right-click with the voider**: remove matching inventory items, spending energy.
- **Sneak + right-click with the relocator**: manage destinations.
- **IC2 jump / boost keys**: jetpack ascent, hover controls and boost.
- **Sprint while wearing Advanced Nano Leggings**: automatic +15% movement speed while paying 8 EU/t.
- **IC2 quantum movement controls**: retained by Advanced Quantum Leggings and Boots, including their speed / jump toggles.

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

The local Java 21 build passes four JUnit resource-contract tests and 24 NeoForge server game
tests against IC2R 21.1.66. These cover EU capacity / tier / transfer behavior, MFSU charging,
charge-preserving upgrade recipes, mining energy costs, tool upgrades, armor upgrades, fall
absorption, sprint cleanup, flight cleanup, packet validation, item filtering and teleportation.
English / Russian key parity, format arguments, recipe outputs, item models and item textures
are checked automatically. CI runs the same build and game tests using the public IC2R release.

An isolated development client also loads the mod and completes resource / model atlas loading
without GraviSuite resource errors. Armor appearance on the player and controls still need
manual verification in a full client;
the server tests do not establish compatibility with every modpack or multiplayer setup.
The relocator projectile currently uses an item renderer rather than the legacy plasma mesh.
Legacy engine audio and exact legacy visual effects are not reproduced.

## Credits

Original GraviSuite concept: **SeNtiMeL** and the **IC2 Dev Team**.
GraviSuite Reforged legacy implementation and assets: **VintageModsReforged** and upstream contributors.
NeoForge 1.21.1 / IC2R integration: **Igoryok95**.

Upstream: https://github.com/VintageModsReforged/GraviSuiteReforged

IC2R: https://github.com/neo-industrial-mc/IC2R
