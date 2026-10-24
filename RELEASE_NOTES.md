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
