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
