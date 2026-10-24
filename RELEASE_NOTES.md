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
