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
