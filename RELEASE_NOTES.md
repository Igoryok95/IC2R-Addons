# Solar GraviSuite for IC2R 1.1.0

Advanced Solar Panels and GraviSuite now ship as **one mod and one JAR** for Minecraft 1.21.1,
NeoForge 21.1.264+, IC2R 21.1.67+ and Java 21.

- Includes all eight solar tiers, three solar helmets, Molecular Transformer, powered armor,
  advanced tools, lappacks, jetpack, gravitational flight, magnet, voider and relocator.
- Fixes the new nano / quantum leggings and boots: blue energy details match the existing
  chestplates, both in inventory icons and on worn armor. Chestplate and helmet art is retained.
- Uniform advanced nano capacity: helmet / chestplate / leggings / boots each hold **8M EU**.
  Top quantum / gravitational set: **100M EU per piece**. Hybrid solar helmet: **40M EU**.
- Preserves legacy item / block / payload IDs, component data and both existing config files.
- Integrates both test suites, adds full-set capacity / serialization and blue-texture checks.
- Renames the project and repository to Solar GraviSuite; retains GraviSuite Git history and
  historical sources in the combined project.

Validation: 40 server game tests and 9 JUnit resource tests. Client smoke testing covers the
combined mod, two worn armor sets, inventory icons and the transformer GUI with JEI.
Complete multiplayer modpack / controls / shader compatibility still needs manual validation.

Install the main `solar-gravisuite-neoforge-1.21.1-1.1.0.jar` and IC2R on both client and server;
remove the separate ASP / GraviSuite JARs. IC2R remains a separate required dependency.

---

# Advanced Solar Panels for IC2R 1.0.0

Initial Minecraft 1.21.1 / NeoForge 21.1.264+ adaptation targeting IC2R 21.1.67+ and Java 21.

- Eight solar tiers: Advanced, Hybrid, Ultimate Hybrid, Quantum, Spectral, Singular,
  Light Absorbing and Photonic.
- Three solar helmets with upgraded 4M / 20M / 40M EU capacities and inherited IC2R armor
  functions; every-tick generation and charge-conserving inventory / GraviSuite charging.
- Molecular Transformer with native datapack recipes, real IC2R energy transfer, saved partial
  work, output-space protection, original 3D geometry / textures, dynamic GUI and JEI category.
- English / Russian localizations and generation / helmet-charging / active-core configuration.
- Correct six-face UV mapping for the extended panels' 96x16 atlases, and a non-occluding
  transformer block state that preserves floor faces and model lighting.
- Developer-only client smoke test with inventory / dropped-item cleanup between runs.

Validation: 15 NeoForge server game tests and 4 JUnit resource tests pass. A real Russian-locale
client with IC2R 21.1.67, GraviSuite 3.0.0 and JEI 19.64.0.455 loads the test world, renders the
items / transformer and opens the transformer GUI. Captures are included in the repository.
The complete multiplayer modpack, worn armor controls and shader combinations still need
further manual validation.

Install the main JAR and IC2R on both client and server. Keep one ASP implementation and one
IC2R JAR. This release is an unofficial adaptation; source / asset credits are in NOTICE.md.
