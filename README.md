# Advanced Solar Panels for IC2R

An Advanced Solar Panels adaptation for **Minecraft 1.21.1 / NeoForge**, integrated with
**IndustrialCraft 2: Refactored (IC2R) 21.1.67+**.

This project combines the classic four solar tiers and three solar helmets with the extended
solar progression from AdvancedSolarPanelsReforged. It uses IC2R's real EU network, electric-item
API, machines and dynamic GUIs. English and Russian localizations are included.

![Panels and Molecular Transformer](docs/images/panels-and-transformer.png)

## Requirements

- Minecraft **1.21.1**
- NeoForge **21.1.264 or newer**
- IC2R **21.1.67 or newer within 21.x**
- Java **21**
- Optional: JEI for crafting and Molecular Transformer recipes
- Optional: GraviSuite for additional equipment that the solar helmets can charge

Install this addon and IC2R on both the client and server. VintageCore, the original IC2 and
other Advanced Solar Panels implementations are not required. Do not install another mod using
the `advanced_solar_panels` ID alongside this one.

## Solar progression

Default values, before the configuration multiplier:

| Panel | Day EU/t | Night / rain EU/t | Storage EU | Output tier |
| --- | ---: | ---: | ---: | ---: |
| Advanced | 8 | 1 | 32,000 | 1 |
| Hybrid | 64 | 8 | 100,000 | 2 |
| Ultimate Hybrid | 512 | 64 | 1,000,000 | 3 |
| Quantum | 4,096 | 2,048 | 10,000,000 | 5 |
| Spectral | 8,192 | 4,096 | 100,000,000 | 5 |
| Singular | 32,768 | 16,384 | 1,000,000,000 | 6 |
| Light Absorbing | 131,072 | 65,536 | 1,000,000,000 | 7 |
| Photonic | 524,288 | 262,144 | 1,000,000,000 | 8 |

Panels need an unobstructed sky. Opaque roofs and dimensions without skylight stop generation;
night and local rain use the lower output. Generation runs every server tick. EU is buffered,
can charge an inserted electric item and is emitted through IC2R's normal energy network.
High tiers follow IC2R's voltage rules: use suitable cables and transformers.

The four extended tiers require their predecessor and late-game components. Their recipes are
adapted to modern IC2R rather than being copies of the old numeric-ID recipes.

## Solar helmets

| Helmet | Day / night EU/t | Capacity EU | Transfer EU/t | Tier | Inherited armor |
| --- | ---: | ---: | ---: | ---: | --- |
| Advanced | 8 / 1 | 4,000,000 | 5,000 | 3 | IC2R Nano Helmet |
| Hybrid | 64 / 8 | 20,000,000 | 12,000 | 4 | IC2R Quantum Helmet |
| Ultimate Hybrid | 512 / 64 | 40,000,000 | 50,000 | 4 | IC2R Quantum Helmet |

The nano helmet's night vision / HUD and the quantum helmet's powered abilities are inherited
from IC2R. Storage is larger than the corresponding base IC2R helmet: 4x for Advanced, 2x for
Hybrid and 4x for Ultimate. The original solar helmet armor and item textures are retained.

Helmets generate only while equipped. They charge chest / leg / foot armor first, then the
offhand and inventory, and finally their own buffer. Each EU is allocated once; generation has
no shared item-instance timer. This also works with compatible GraviSuite equipment.
Helmet upgrade recipes carry charge from their electric ingredients through IC2R's serializers.

## Molecular Transformer

The Molecular Transformer accepts EU through the IC2R network and converts items using
recipe-defined energy costs. It has its original 3D model, animated active core, dynamic GUI,
input / output slots and an optional JEI category.

- The input stays in its slot until a conversion finishes.
- Blocked output pauses the conversion without spending EU or consuming input.
- Changing the input resets incompatible partial work.
- Partial work, input and buffered EU are saved with the block entity.
- Recipes are native, synchronized, reloadable datapack recipes, not a mutable global table.

The initial recipe set includes 15 transformations involving vanilla materials, sunnarium,
iridium and IC2R uranium / diamond dust. It is an adapted set, not a claim of exact recipe
parity with every historical ASP release.

Example under `data/<namespace>/recipe/`:

```json
{
  "type": "advanced_solar_panels:molecular_transformer",
  "input": { "item": "minecraft:glowstone_dust" },
  "output": { "id": "advanced_solar_panels:sunnarium_part", "count": 1 },
  "energy": 1000000
}
```

Crafting components retain the classic sunnarium / irradiant / iridium progression. The addon
reuses IC2R's uranium ingot instead of registering an equivalent duplicate.
A creative-only Quantum Generator is retained at 512 EU/t, disabled by a redstone signal;
it has no survival recipe. Its output is fixed in this initial version.

## Configuration

`config/advanced_solar_panels-common.toml`:

- `generationMultiplier`: multiplier for panel and helmet generation, default 1.0, range 0вЂ“64.
- `helmetsChargeArmorOnly`: restrict external helmet charging to worn armor, default false.
- `drawActiveTransformerCore`: show the animated transformer core, default true.

Use matching common settings on the client and server. Crafting and conversion recipes can be
changed using datapacks. Generation does not add or alter world-generation features.

## Build

```sh
./gradlew build runGameTestServer
```

The Gradle wrapper requires Java 21. IC2R is resolved from the public release in
[Igoryok95/IC2R](https://github.com/Igoryok95/IC2R/releases/tag/21.1.67).
To use a local IC2R JAR, pass `-Pic2rJar=/absolute/path/to/ic2-neoforge-21.1.67.jar`.

Optional GraviSuite integration run in PowerShell:

```powershell
.\gradlew.bat build runGameTestServer '-PgravisuiteJar=C:/GIT/GraviSuiteReforged/build/libs/gravisuite-neoforge-1.21.1-3.0.0.jar'
```

Output: `build/libs/advanced-solar-panels-neoforge-1.21.1-1.0.0.jar`.
The separate sources JAR is for development and must not be installed as a mod.

## Validation and scope

Local validation passes **15 server game tests** and **4 JUnit resource-contract tests**.
The suite covers solar generation, roof / night behavior, real IC2R charging and network
transfer, helmet capacity / EU conservation, GraviSuite charging when present, charge-preserving
upgrades, transformer energy costs / blocked outputs / input changes / persistence, recipes,
GUI parsing, model textures and English / Russian localization parity. It also checks the six-cell UV layout
of the extended panels and the transformer's non-occluding block state.

A Russian-locale client smoke test with IC2R 21.1.67, GraviSuite 3.0.0 and JEI 19.64.0.455
loads a real world, renders the registered items and the transformer, opens its actual dynamic
GUI and registers the molecular JEI category. See the [GUI capture](docs/images/molecular-transformer-gui.png).

This is an initial compatibility release. Further manual validation of armor appearance, controls,
multiplayer behavior and shader combinations in a complete modpack remains necessary.
No Sodium / Iris incompatibility rule is imposed; the transformer uses ordinary NeoForge
rendering APIs, but compatibility is not established merely by a successful build.
Historical double-stone-slab cosmetics, old GUI styling details and helmet dye recipes have not
been reproduced. Existing IC2R gameplay and world generation are preserved.

## Credits and source history

See [NOTICE.md](NOTICE.md) for source revisions, asset provenance and original authorship.
This is an unofficial adaptation, not a release by the original ASP or IC2 authors.

## Reproduce the client smoke test

After `runGameTestServer` has created `run/world`, copy that test world into
`run/saves/SolarClientSmoke` once. Then run `./gradlew runClient -PclientSmoke` with the optional
GraviSuite argument shown above if desired. The harness is disabled unless this property is set.
It replaces a small platform around the test player, clears old test inventories and dropped
items, renders item / world / GUI captures in `run/screenshots`, and exits the client.
Use only the isolated test world for this developer command.
