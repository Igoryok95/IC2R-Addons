# IC2R Addons 1.4.7

Minecraft 1.21.1 / NeoForge 21.1.264+ / Java 21 / IC2R 21.1.81+.

- Apply the native IC2R water/air speed compensation to Advanced Diamond Drill,
  Advanced Iridium Drill and Vajra, across all three efficiency modes.
- Preserve each tool's speed ratio to the corresponding native drill while
  underwater, airborne or affected by both penalties. Respect Aqua Affinity and
  other submerged-mining attribute changes.
- Preserve base speed profiles, EU per block, capacities, mining modes and drops.
- Require IC2R 21.1.81 for the shared helper. That paired version also fixes actual
  Nano Saber damage after toggling and native/advanced chainsaw shutdown sounds.

See [speed comparison and validation](docs/tool-environment-1.4.7.md).

---

# IC2R Addons 1.4.6

Minecraft 1.21.1 / NeoForge 21.1.264+ / Java 21.

- Send localized tool setting notifications to normal chat, following IC2R's
  `Label: Value` style. Cover drills, Vajra, GraviTool, tree felling, relocator,
  utility toggles, the magnet hotkey and voider filter selection.
- Replace obsolete Forge mining callbacks with native server harvesting for
  3 x 3 mining, connected blocks, ore veins and tree felling.
- Enforce charged-tool requirements through the native server break checks;
  preserve protection checks, ordinary drops and one EU charge per mined block.
- Expose the hoe tool ability in GraviTool's hoe mode, restoring real tilling.
- Send Energy Control player packets only over negotiated channels, including
  mixed test environments and server actors without an Addons client connection.
- Preserve released tool capacity, EU and speed values. Document differences from
  historical GraviSuite sources instead of silently changing the balance.
- Pair with IC2R **21.1.80** for interrupted-machine sound cancellation and
  correct output-blocked/power-restored operation states.

See [details and balance audit](docs/tool-modes-and-mining-1.4.6.md).

---

# IC2R Addons 1.4.5

Minecraft 1.21.1 / NeoForge 21.1.264+ / Java 21.

- Give full `100.00%` warm-up values enough separation from machine slots and
  energy indicators in all eight Advanced Machines.
- Align the caption/value column and adjust the three crowded processing columns
  while preserving gauge backgrounds, recipe buttons and inventory order.
- Keep the normal font size, two decimals, localization and all machine behavior.
- Validate actual rendered bounds at maximum warm-up with filled slots, in Russian
  and English. IC2R **21.1.75** fixes the native Induction Furnace layout.

See [GUI validation](docs/warmup-spacing-1.4.5.md).

---

# IC2R Addons 1.4.4

Minecraft 1.21.1 / NeoForge 21.1.264+ / Java 21.
Use **IC2R 21.1.74** for the complete JEI/Jade fixes; the base API requirement remains 21.1.70.

- Resolve real input and player-inventory slots for the Molecular Transformer and
  Compacting Recycler. Use explicit JEI category identifiers for all eight Advanced
  Machines and add recipe arrows to both custom recipe machines.
- Combined with core 21.1.74, fix the per-frame JEI transfer exceptions and recipe
  screen stutter caused by native slot offsets being applied to addon layouts.
- Add server-owned EU gauges for Advanced Machines, panels and the Molecular
  Transformer, actual accepted solar generation and conditions, and active
  transformation progress, consumed EU and result.
- Use two-decimal GUI and Jade warm-up percentages, matching the Induction Furnace;
  show colored heat/progress gauges and readable labels. Preserve engine EU/MJ gauges.
- Show Nuclear Control counter packet limits and thermal-monitor redstone state.
- Keep English/Russian locale parity and operation without JEI, Jade or BuildCraft.
  Preserve recipes, capacities, speed, energy balance and the 1.4.3 sound fixes.

See [details](docs/jei-jade-1.4.4.md) and [validation](docs/validation/1.4.4.json).
This build does not update the player's client, exchange ZIP or production server.

---

# IC2R Addons 1.4.3

Minecraft 1.21.1 / NeoForge 21.1.264+ / IC2R 21.1.70 / Java 21.

- Advanced Machines previously kept their operating sound active whenever stored heat
  was above zero, including empty redstone standby and cooling. All eight machines
  now follow actual recipe processing, like the native induction furnace. Normal
  completion and interrupted work use IC2R's existing sound lifecycle.
- Keep manual and external-redstone warm-up, water consumption, processing speed,
  saved heat and partial work unchanged. No neighboring redstone signal is emitted.
- Align the Rotary Macerator, Singularity Compressor, Centrifuge Extractor and
  Compacting Recycler with the induction furnace: 16 EU/t total processing, 1 EU/t
  standby. Previously they consumed 15 EU/t while processing. Specialized machines
  retain 24 or 48 EU/t; the Thermal Washer retains its 6 EU/t standby demand.
  Tooltips use the same default processing constant as the machine implementation.

See the [energy comparison](docs/advanced-machines-1.4.3.md) and
[validation](docs/validation/1.4.3.json). Publishing and installing this maintenance
build remain separate operations.

---

# IC2R Addons 1.4.2

Minecraft 1.21.1 / NeoForge 21.1.264+ / IC2R 21.1.70 / Java 21.

- Stop automatic receiver alignment from undoing wrench rotation. Manual direction
  is saved and survives ticking, neighbor updates and world reloads. New placements
  still find an adjacent MJ consumer, including after its pipe ports initialize.
- Align the clickable inventory and hotbar with their original Forestry background;
  remove the overlapping rows caused by a six-pixel vertical offset.
- Show measured EU received from the IC2R grid separately from conversion consumption
  and actual MJ delivery in both the GUI and Jade. Replace Jade's misleading
  conversion "Input" label with "Consumption" in English and Russian.
- Keep engine balance, thermal behavior, upgrades, recipes and dependencies unchanged.

See [engine mechanics](docs/forestry-engine-restoration.md) and
[validation](docs/validation/1.4.2.json).

---

# IC2R Addons 1.4.1

Minecraft 1.21.1 / NeoForge 21.1.264+ / IC2R 21.1.70 / Java 21.

- Show heat, rotor speed and pressure as 0-100% across all eight Advanced Machines;
  retain original internal warm-up and processing balance.
- Add saved, synchronized torch buttons to all eight Advanced Machines for local
  standby warm-up without emitting redstone or locking neighboring hoppers. Preserve
  original EU/water requirements, normal processing and external redstone behavior.
- Route IC2 wrench clicks through BuildCraft block activation before item rotation,
  so iron transport pipes switch output as with the BC wrench. Electric pipe actions
  charge 100 EU once; empty tools and canceled interactions remain blocked.
- Restore Forestry engine thermal stages, gradual cooling, complete piston strokes
  and original GUI, with a battery and four IC2R upgrades. Default 5 EU/t to 2 MJ/t;
  overclocking stops at 8 MJ/t. Fix BC pipe topology, passive extraction, automatic
  alignment and oversized held models. Jade includes EU/MJ bars and thermal state.
- Restore original ASP/GraviSuite rarity colors, including quantum armor. Complete
  IC2R sensor kit/card names in both languages. Native 32x32 IC2R tool silhouettes
  gain blue grips; Vajra gains native 32px detail with its transparent frame preserved.
- Keep solar charge gauges with exact live tooltips and bounded tier/output text.
- Preserve large energy-array and tank-card readings, totals and portable bars,
  including legacy integer-valued saves. Avoid client-only name lookup on servers.
- Extend IC2R's Electrical Engineer, Nuclear Engineer and Greg trades with appropriate
  Addons components, monitoring devices and tools.
- Retain four solar tiers through Quantum, original equipment capacities, transfer
  limits and recipes. No Classic progression or capacity rebalance is included.

See [scope and validation](docs/maintenance-1.4.1.md). Requires IC2R 21.1.70 or newer within 21.x.
Installation into an existing client/server remains a separate operation.

---

# IC2R Addons 1.4.0

Minecraft 1.21.1 / NeoForge 21.1.264+ / IC2R 21.1.68+ / Java 21.

Integrates Energy Control / Nuclear Control monitoring into the single addon JAR and
`ic2r_addons` namespace: information / advanced / holographic panels and extensions,
portable displays, card holder, sensor kits / cards, thermal monitors, alarms, lamps,
timer and Kit Assembler. A native IC2R integration reads EU storage / flow, reactor heat,
output and real fuel wear, including the existing addon processing machines and solar equipment.

Restores native EU Energy / Average Counters, counter cards and classic thermometers.
Counter menus synchronize full double-precision readings, support Transformer Upgrades,
explicit reset and persistent totals / sample duration. Jade shows server-provided readings.
Reuses IC2R circuits / casings, completes English / Russian localization and guards remote
sensor reads against forced chunk loading. Network edits require a nearby non-spectator player.

Fixes the corrupted engine rate separator (`в†’`) by using `|` in both locales. Adds a
regression check that rejects misdecoded Cyrillic in English translations. Documentation
screenshots are captured again from the corrected client in English.

Fixes all seven missing empty-slot icons, invisible counter bodies and hidden GUI fields /
gauges. Kit Assembler and Timer menus now synchronize live values with their opening player.
The assembler retains partial work through a power interruption and saving; it consumes
inputs only after processing completes. Original Kit Assembler world textures are preserved.

The integrated Energy Control module retains GPL-3.0 and source / artwork attribution.
See NOTICE.md, the bundled LICENSES directory and docs/validation/1.4.0.json for evidence.

---

# IC2R Addons 1.3.0

Minecraft 1.21.1 / NeoForge 21.1.264+ / IC2R 21.1.68+ / Java 21.

Integrates all eight Advanced Machines: Rotary Macerator, Singularity Compressor,
Centrifuge Extractor, Compacting Recycler, Liquescent Extruder, Impellerized Roller,
Water-Jet Cutter and Thermal Washer. The Induction Furnace remains the existing IC2R machine.
The Sharpened Iron Plate and all nine original crafting patterns are included.

The machines retain heat / spin-up, redstone standby, tier-2 EU input, 10,000 EU buffers,
specialized output inventories and original active / idle consumption. Seven machines use
IC2R's processing recipe managers directly, including datapack modifications. The recycler
retains IC2R recycling rules and packs nine scrap into a scrap box without random losses.
Water machines accept pipes and containers, return empty containers and consume water internally.
The Thermal Washer retains its hot-empty-refill explosion; loading a filled hot tank is safe.
If server protection cancels that explosion, the same refill does not trigger it again every tick.

Adds explicit sided item / fluid pipe support, persistent partial work / heat / water,
IC2R wrench rotation, full English / Russian text, original modern 32x32 textures,
JEI catalysts and a scrap-packing category. Optional Jade shows warm-up, progress, EU/t,
water and the thermal hazard. Optional BuildCraft, solar and equipment features remain available.

GUI layouts use standard IC2R slot widgets. The extruder and roller use compact arrows:
the current IC2R Metal Former indicator is 64 pixels wide and does not fit the legacy layout.
Regression validation checks actual gauge bounds against inventory slots; client smoke runs
exercise partially completed recipes with populated inputs / outputs in all eight menus.

Validation passed: 15 JUnit checks, 65 base server GameTests and 73 with BuildCraft / Jade.
English and Russian client runs opened all eight menus with real partial processing,
checked all 96 model faces and confirmed 57 addon items under four equivalent JEI searches.
The packaged JAR's metadata, recipes, 188 matching EN/RU locale keys, ZIP integrity and SHA-256
were verified. See `docs/validation/1.3.0.json`.

---

# IC2R Addons 1.2.1

For Minecraft 1.21.1 / NeoForge 21.1.264+ / IC2R 21.1.67+.

The solar progression now stops at Quantum: Advanced, Hybrid, Ultimate Hybrid and Quantum.
Spectral, Singular, Light Absorbing and Photonic panels are removed from crafting, creative
tabs and JEI. Existing blocks / item stacks migrate to Quantum, preserving stack counts
and charging-slot items; buffered energy above 10M EU is discarded to match Quantum capacity.
Aliases cover both `ic2r_addons` and the older `solar_gravisuite` namespace.

The new Quantum Solar Helmet upgrades the Ultimate Hybrid Solar Helmet with a Quantum Solar
Panel, two advanced circuits and a lapotron crystal. It stores 100M EU, generates 4,096 EU/t
in daylight or 2,048 EU/t at night / in rain, and accepts tier-5 charging at 200,000 EU/t.
IC2R quantum-helmet abilities and ingredient charge are retained. Armor / tools receive
generated EU before the helmet's own buffer. The generation multiplier still applies.

Hybrid and Ultimate item icons restore the supplied classic designs, replacing the 1.2.0
redesign. The new helmet's 32x32 icon keeps that Ultimate silhouette, with a brighter blue solar
crown and lighter shell. Worn armor retains its UV layout. English and Russian names,
mod-list descriptions and documentation reflect the four-tier progression.

The optional BuildCraft engine, wrench bridge and Jade diagnostics remain available.

Validation: 12 JUnit resource checks, 46 base server GameTests and 54 with BuildCraft.
Compatibility is checked with Jade present and absent. Real English and Russian client
smoke runs verify the new equipment rendering, live Jade data and identical JEI query
results (48 addon items with BuildCraft). See `docs/validation/1.2.1.json`.

Install the runtime JAR on both client and server, replacing the previous addon JAR.
The sources JAR is for development. SHA256SUMS-1.2.1.txt contains artifact hashes.


# IC2R Addons 1.2.0

- Renamed the repository, mod container, Java packages, registry / resource namespace, configuration files, HUD labels and key category to IC2R Addons (`ic2r_addons`).
- Added aliases for 1.1.1 saved content, non-destructive configuration migration and legacy equipment custom-data reads.
- Added an optional BCCE 8.0.25 bridge: IC2R / BuildCraft wrench interoperability and a native EU-to-MJ electric engine.
- Adapted ForestryCE tin-engine geometry / artwork and piston animation, with native IC2 EU input and BC MJ output. Forestry is not required.
- Added configurable voltage, buffer, conversion, tick rate and redstone requirement; simulated or rejected output never consumes energy.
- Added independently optional Jade diagnostics with localized energy / rate / voltage / status lines.
- Refreshed tool icons and existing pixel textures for 32-pixel detail while preserving original styling, blue armor accents, UV maps and alpha.
- Kept complete English / Russian coverage for tooltips, HUD, messages, settings and new diagnostics.

Requires Java 21, Minecraft 1.21.1, NeoForge 21.1.264+ and IC2R 21.1.67. Install the runtime JAR on client and server and remove the previous addon JAR. BuildCraft, Jade and JEI remain optional.

Validation: 11 JUnit checks; 43 GameTests without BC and 51 with BC; Jade present / absent; real English / Russian client rendering and Jade network telemetry; 51 matching JEI items across four queries.
