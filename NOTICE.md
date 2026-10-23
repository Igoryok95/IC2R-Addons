# Source and asset provenance

Original Advanced Solar Panels authors: **Icedfire, SeNtiMeL and Chocohead**.
Original texture credits include **Krezzard**. IndustrialCraft 2 belongs to the IC2 Dev Team.

- Original ASP 4.3.0 binary reference supplied by the user: SHA-256 `6530286f2cd6545f4d7b510d64199a1ec5a4f77915fea2f7274c9a0fbdbc011a`.
  Original distribution: https://www.curseforge.com/minecraft/mc-mods/advanced-solar-panels/files/2652182
- Forge 1.20.1 migration baseline: https://github.com/craftorio/ic2-advanced-solar-panels
  (branch `forge/1.20.1`, revision `4ae8961`). Its Java implementation, recipe / GUI definitions
  and modernized resource paths were adapted to NeoForge 1.21.1 and IC2R.
- Extended solar tier values and top textures: https://github.com/VintageModsReforged/AdvancedSolarPanelsReforged
  (revision `29067c116d51b0b8853d5b8b2da90b7b78423e05`). The four extended tiers and their
  recipes are adapted for current IC2R.
- Minecraft 1.21.1 Molecular Transformer renderer adaptation:
  https://github.com/EugeneHeisst/advanced-solar-panels-port
  (revision `146d35feae50859e812f2c0409731cc89ede2093`). Only the renderer was used;
  that project's separate IC2 implementation, updater and installers are not dependencies.
- IC2R integration target: https://github.com/Igoryok95/IC2R , release 21.1.67.
- NeoForge adaptation, modern helmet progression, recipe synchronization, EU / persistence
  fixes, integration tests and English / Russian documentation: **Igoryok95**.

The 1.12.2 reference JAR is not bundled in this repository or in the new mod. Original assets
are retained as assets, not claimed as newly created artwork. Publicly available port sources
are distinct from a verified official source release by Chocohead; an official 4.3.0 source
repository was not located during this investigation.

## Rights

The referenced projects do not provide a general permissive license covering all original ASP
code and artwork. No additional license grant for those materials is inferred from their public
availability. Original copyrights and applicable terms remain with their respective authors.
The mod metadata retains the baseline's `All Rights Reserved` designation; this repository does
not relicense original IC2 / ASP assets or claim ownership of the original projects.

## GraviSuite integration in Solar GraviSuite 1.1.0

- GraviSuite Reforged upstream: https://github.com/VintageModsReforged/GraviSuiteReforged .
  Original concept: SeNtiMeL and the IC2 Dev Team; Reforged implementation / artwork:
  VintageModsReforged and upstream contributors.
- Integrated NeoForge / IC2R port: former Igoryok95/GraviSuiteReforged,
  branch `neoforge-1.21.1`, revision `8344f20989fd2df928421b7d9c9729da2eb171e4`.
  Its Git ancestry is preserved by the 1.1.0 merge, and the 1.6.4 baseline is retained in
  `legacy/gravisuite/1.6.4`. The standalone personal fork is retired after integration.
- New leggings / boots reuse IC2R artwork with blue energy accents. A built-in imagegen edit
  supplied the cyan-blue reference; `tools/textures/BlueArmor.java` transfers its hue while
  retaining the original pixel grid, alpha and worn-armor UV layout. Reference and input
  images are retained under `tools/textures`; existing chestplate / helmet artwork is unchanged.
  Run `java tools/textures/BlueArmor.java` from the project root to reproduce these textures.

The source- and asset-rights statement above also applies to integrated GraviSuite / IC2R
materials; no ownership of original artwork or additional permissive license is claimed.
