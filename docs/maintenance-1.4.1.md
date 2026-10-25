# IC2R Addons 1.4.1 maintenance changes

Version 1.4.1 starts from Addons 1.4.0 and targets the IC2R 21.1.70
maintenance API. Installation is separate from publishing this release.

## Preserved fixes

- All eight Advanced Machines display heat, speed and pressure warm-up as 0-100%.
  Internal 0-10,000 processing state, recipes, costs and warm-up behavior are unchanged.
- Energy-array cards keep full per-target readings instead of narrowing to 32-bit
  integers. Totals, percentages, portable progress bars, missing-target states and
  older integer-valued saved cards remain readable.
- Advanced tank cards retain long-valued amounts/capacities in text and bars. Their
  percentage calculation no longer overflows integer multiplication.
- Shared card name formatting uses the item's display name without loading the
  client-only Minecraft class on a dedicated server.
- The existing four solar panels keep their original charge gauge. Exact grouped
  charge/capacity appears in a live tooltip, with bounded tier/output/generation rows.

## Scope after cleanup

The four original solar panel tiers through Quantum remain. The retired-panel
migration is preserved. Existing Advanced Machines, Nuclear Control, equipment,
optional BuildCraft bridge, Jade and JEI integrations remain available.
Ultimate Lappack stays at 100 million EU and its original recipe; Gravitational
Chestplate recipes and all other equipment balance are unchanged.

No restored upper solar tiers, new panel recipes, Classic material dependencies,
capacity rebalance or Colossal integration are included. Earlier development work
remains recoverable from the previous branch.

## Optional villager trades

The six professions belong to IC2R itself and remain usable without Addons.
When Addons is installed it extends three matching professions:

- Electrical Engineer: monitoring circuits, Advanced Machines sharp plates,
  superconducting components and the first Advanced Solar Panel.
- Nuclear Engineer: thermometers, thermal monitors, information panels and a
  portable panel.
- Greg: advanced chainsaw/drill and the Advanced Lappack, gated behind addon
  cooling cores or engine boosters and master-level prices.

The other three professions retain their native trades. All panel and equipment
capacities remain at their 1.4.0 values; these trades introduce no Classic items.

## Standby controls and BuildCraft pipe wrenching

All eight Advanced Machines have a saved, server-synchronized local torch switch.
It requests standby without producing a neighboring redstone signal. External
redstone and normal processing remain independent; original standby EU and water
consumption remain unchanged. Torch sprites are vanilla lit/unlit textures. Header
text reserves space for the button, and the Thermal Washer upgrades move below it.

The BuildCraft bridge previously forwarded wrench clicks only to the wrench item,
which rotates engines but misses iron pipe activation. It now calls the BC block
handler first, preserving BC pipe-part selection, then falls back to item rotation.
BC invokes the registered IC2 wrench adapter for pipe actions, so the electric
wrench pays once. The integration remains optional and respects cancellation.

## Validation

19 JUnit tests, 82 server GameTests without optional mods and 94 with IC2R 21.1.70, BuildCraft, Jade,
Create, Mekanism and Almost Unified. The Russian client checks all four solar and
eight Advanced Machines menus, live exact tooltips, percentage text and slot/title
bounds. Native/new IC2R menus and the sound slider are checked in the same isolated
client. Equipment capacities and recipes retain released 1.4.0 balance. Tool artwork and item rarities now follow the originals.
See the [validation receipt](validation/1.4.1.json) for artifact hashes and scope.

## Engine and item presentation

The optional engine fixes BCCE pipe topology/passive extraction, automatic facing
and held transforms. Forestry thermal/piston behavior and original GUI are restored,
with native IC2R upgrades and a 5 EU/t to 2 MJ/t base, capped at 8 MJ/t. Jade adds
separate EU/MJ bars and thermal/redstone diagnostics. See the engine restoration note.

Original ASP/GraviSuite rarity colors are restored after inherited armor defaults.
All registered items have English/Russian display names. Native IC2R tool silhouettes
and alpha are preserved, with blue grips; Vajra restores its original transparent gaps.

Vajra now uses a detailed native 32x32 remaster of the original design, rather than
doubling its 16px pixels. The open frame uses true binary alpha; a regression test
checks independent 32px detail and enclosed transparent openings. See the saved
[prompt and asset workflow](../tools/textures/1.4.1-vajra-prompt.md).

CI validates the addon against the released IC2R 21.1.70 binary. It does not rebuild
the dependency inside a different NeoForge toolchain; standalone core source
validation is recorded by the core repository.
