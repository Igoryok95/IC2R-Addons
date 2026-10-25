# Source and asset provenance

Original Advanced Solar Panels authors: **Icedfire, SeNtiMeL and Chocohead**.
Original texture credits include **Krezzard**. IndustrialCraft 2 belongs to the IC2 Dev Team.

## Advanced Machines integration in 1.3.0

Advanced Machines was created by **immibis** and maintained by **Chocohead**. The Forge / IC2R
migration reference is [craftorio/ic2-advanced-machines](https://github.com/craftorio/ic2-advanced-machines/tree/94d01c01fc292ddcacc4121eaddbe130472e650c),
branch `forge/1.20.1`, revision `94d01c01fc292ddcacc4121eaddbe130472e650c`. Its `forge/1.12.2`
branch was also consulted for original warm-up, water use and thermal-shock behavior.
Original distribution: https://www.curseforge.com/minecraft/mc-mods/advanced-machines .

The eight leaf machine types, crafting patterns, GUI layouts and modern texture set were adapted
from that reference. The modern 32x32 PNGs are copied without modification from `modern_overlay`;
the Sharpened Iron Plate retains its original size. The Classic texture pack
and redundant legacy GUI backgrounds are not bundled. No separate Advanced Machines binary is
bundled or required. `tools/machines/import_advanced_machines.py` records the initial resource /
leaf-class import; the heating, water and recycler implementations are maintained in this addon.

The NeoForge registration, safe partial-work persistence, current recipe-result API, explicit
item / fluid capabilities, water-container handling, IC2R sound lifecycle, JEI scrap-packing
category, Jade telemetry, English / Russian adaptation and regression checks are maintained
by **Igoryok95**. Original code and artwork remain attributed to their respective authors;
the existing rights statement below applies to these adapted materials as well.

- Original ASP 4.3.0 binary reference supplied by the user: SHA-256 `6530286f2cd6545f4d7b510d64199a1ec5a4f77915fea2f7274c9a0fbdbc011a`.
  Original distribution: https://www.curseforge.com/minecraft/mc-mods/advanced-solar-panels/files/2652182
- Forge 1.20.1 migration baseline: https://github.com/craftorio/ic2-advanced-solar-panels
  (branch `forge/1.20.1`, revision `4ae8961`). Its Java implementation, recipe / GUI definitions
  and modernized resource paths were adapted to NeoForge 1.21.1 and IC2R.
- Extended solar tier values and top textures: https://github.com/VintageModsReforged/AdvancedSolarPanelsReforged
  (revision `29067c116d51b0b8853d5b8b2da90b7b78423e05`). The four extended tiers and their
  recipes were adapted in earlier versions and are removed in 1.2.1.
- Minecraft 1.21.1 Molecular Transformer renderer adaptation:
  https://github.com/EugeneHeisst/advanced-solar-panels-port
  (revision `146d35feae50859e812f2c0409731cc89ede2093`). Only the renderer was used;
  that project's separate IC2 implementation, updater and installers are not dependencies.
- IC2R integration target: https://github.com/Igoryok95/IC2R , release 21.1.68.
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

## IC2R Addons 1.2.0

The ForestryCE engine renderer and tin / solar engine texture family are adapted from
https://github.com/thedarkcolour/ForestryCE/tree/ae51f88a6f1f3180a3d3bdddd842cf699cee03e7 . These adapted materials remain
under LGPL-3.0-or-later; a copy is bundled in `src/main/resources/LICENSES/ForestryCE-LGPL-3.0.txt`.
Original authors include the Forestry contributors and thedarkcolour. Forestry is not bundled.
The integration uses the public BCCE API 2 from https://github.com/BCCE-team/BuildCraft/tree/8.0.25;
BuildCraft is resolved separately and is not bundled.

The new 1.2.0 tool atlas was produced with the built-in imagegen tool using IC2R electric tools
and the original addon textures as references: orange / graphite grips, silver metallic shading,
blue upgrade details and original diagonal tool silhouettes. `tools/textures/1.2.0-tools-generated.png`
is the generated source; `RefreshTextures.java` converts its four cells to strict 32x32 RGBA icons
and applies edge-aware 2x pixel refinement to the remaining original item / block / armor assets.
Existing artwork remains attributed to its original authors; the refresh does not claim ownership
of those underlying assets. The public Git author for this adaptation is Igoryok95.

A second 4x4 generated atlas, `tools/textures/1.2.0-equipment-generated.png`, refreshes sixteen
armor / device icons from `1.2.0-equipment-reference.png`. The prompt preserves exact cell order,
original angular silhouettes and equipment colors, adds restrained IC2R steel / graphite shading
at 32 pixels, and keeps blue / cyan armor indicators. Both atlases are sampled onto strict
32x32 transparent pixel grids by `RefreshTextures.java`; worn armor retains its original UV layout.

## IC2R Addons 1.2.1

The Quantum Solar Helmet reuses the existing Ultimate Solar Helmet's item silhouette,
classic item design and worn-armor UV sheets. The user-supplied comparison image
`tools/textures/source/classic-solar-helmets-reference.png` is the reference for restoring
the Hybrid / Ultimate item icons. `ClassicHelmets.java` samples the two supplied icons onto
a 32x32 grid and removes only the green background; no replacement silhouette is generated. A built-in imagegen edit of the existing helmet
strip supplied only the brighter blue palette in `tools/textures/1.2.1-helmet-palette.png`.
`QuantumHelmet.java` transfers that palette to the original pixels, slightly brightens the
shell and preserves all alpha / UV coordinates. The generated reference's geometry is not
used in the runtime assets. Run `java tools/textures/ClassicHelmets.java` and then
`java tools/textures/QuantumHelmet.java` after any full `RefreshTextures.java` refresh. See `tools/textures/1.2.1-prompts.md` for the exact prompt.
Original artwork attribution and licensing above remain applicable.

## Nuclear Control / Energy Control integration (1.4.0)

- Original Nuclear Control: **Shedar (Oleg Romanovskiy)**; original idea: **Nargon and
  Noosius**. Nuclear Control 2 contributors include **Xbony2, dmf444 and Pyrolusite**.
- Modern monitoring implementation / artwork: **Zuxelus / Energy Control**, branch
  `1.21.1-neoforge`, revision `f3ec46404a6cd3f27e7a0276e589377a491f157c`:
  https://github.com/Zuxelus/Energy-Control/tree/f3ec46404a6cd3f27e7a0276e589377a491f157c .
  The imported `control` / `control.lib` module retains **GPL-3.0-only**. The complete
  license is bundled as `LICENSES/Energy-Control-GPL-3.0.txt`; adapted source files are
  published in this repository and in the matching source JAR. The import manifest records
  source paths and excluded optional integrations. No original author identity is replaced.
- Classic counter-card and thermometer icons: Nuclear Control 2, revision
  `43b6371957d2448f0128b52b8a486ff9e7ac3c7f`:
  https://github.com/xbony2/Nuclear-Control/tree/43b6371957d2448f0128b52b8a486ff9e7ac3c7f .
  These original PNGs are copied unchanged; its BSD-3-Clause license is bundled as
  `LICENSES/Nuclear-Control-BSD-3-Clause.txt`.
- IC2R adapters, restored native EU counters / menus, current registry / data-component
  integration, completed English / Russian text and regression checks: **Igoryok95**.

The earlier rights section describes the ASP / GraviSuite / Advanced Machines materials;
it does not revoke the GPL or BSD permissions of this monitoring module. This integration
does not claim that the entire collection of third-party artwork has been relicensed.

## Development 1.4.1: original tools, rarity and Forestry engine

- Six advanced tool/mode icons use the 32x32 IC2R originals with orange grips
  recolored blue. Alpha, geometry and other pixels are preserved. Reproduction:
  `tools/textures/restore_original_tools.py`; hashes: `docs/tool-texture-provenance.json`.
- Vajra restores the original 16x16 GraviSuite sprite from revision `64b4c20`, retained
  in `tools/textures/source/vajra-original-16.png`, enlarged by nearest-neighbor 2x.
  The generated tool/Vajra atlas cells described above are historical and retired.
- ASP rarities are verified against the original 4.3.0 binary identified above:
  `TEs` and `ItemArmourSolarHelmet.SolarHelmetTypes`. GraviSuite references are the
  retained 1.6.4 item classes. Quantum Helmet/advanced iridium drill use the epic
  tier; new nano/quantum armor parts match their respective chestplates.
- Thermal logic and unchanged `electricalengine.png` / `engine_ledger.png` are
  adapted from [ForestryMC](https://github.com/ForestryMC/ForestryMC/tree/72e15b0f08daa25d8293cf28b5d5e1da200f9161),
  especially `TileEngine`, `TileEngineElectric` and `GuiEngineElectric`.
  Copyright SirSengir and Forestry contributors, LGPL-3.0-or-later; the existing
  bundled Forestry license applies. IC2R upgrades replace the Forestry circuit board.
