# IC2R Addons: implementation notes

## Identity and migration

The 1.2.0 loader ID, registry / resource namespace and network identity are `ic2r_addons`.
Java packages live under `com.igoryok95.ic2raddons`, split into solar, equipment and bridge modules.
Registry aliases resolve 1.1.1 `solar_gravisuite` entries, including block entities. Equipment
custom data falls back to the old compound and migrates on modification. Config migration copies
existing solar / equipment settings only when their new files are absent.

## Existing solar and equipment modules

The four classic solar panels stop at Quantum. Removed higher tiers alias to Quantum
for blocks, item stacks and block entities; restored EU is capped at 10M while charging-slot
contents survive. The fourth solar helmet uses the Quantum profile (4096 / 2048 EU/t),
100M EU storage, tier 5 and 200k EU/t transfer.

Solar panels use IC2R TileEntityBaseGenerator; the transformer uses its electric machine / energy
components, native recipe codec and stream codec. Pending work and EU survive saving. Solar helmets
inherit IC2R nano / quantum functions and allocate generated EU once per tick. Powered equipment,
mining, relocation and armor progression remain integrated into the single mod container.

The original baselines are documented in NOTICE.md. Earlier 1.1.0 split namespaces and 1.1.1 unified
Solar GraviSuite identity are historical; current registrations all use the new identity.

## Optional BuildCraft boundary

Only the BuildCraft-present branch loads `bridge.bc` content. The electric engine owns an IC2R
Energy sink and exposes BC API 2 MjPortProvider endpoints on the piston side. Other sides accept
native EU. Both direct pushes and passive extraction share one tick budget; simulation and refused
output consume no EU. IC2R owns buffer persistence / input tier behavior.

The wrench adapter recognizes IC2R tools and wrench-mode GraviTool. Right-click dispatches through
the actual BC wrench item behavior, retaining engine rotation rules and native pipe actions.
BC-wrench clicks on IC2 machines use IC2R facing / wrench-drop functions. Harvest / break hooks
retain left-click dismantling and avoid duplicate vanilla drops; cancelled events are respected.

Engine geometry and piston animation are adapted from ForestryCE, with source / asset provenance
and LGPL text included. It does not load Forestry classes or bundle either upstream mod.

## Jade and localization

Jade registers against common IC2 tile classes and checks a dependency-free EngineTelemetry interface.
It can load without BuildCraft. Actual server telemetry is rendered using translation keys for all
labels and states. English / Russian key coverage and formatting are checked by JUnit. User-entered
relocator destination names are the only literal text components; all built-in player-facing text
uses locales.

## Textures and validation

Eighteen generated 32x32 icons combine original addon silhouettes with IC2R metallic detail. Remaining
pixel artwork uses reproducible edge-aware refinement; worn armor keeps normalized UV layouts and
blue energy accents. In 1.2.1, the supplied classic Hybrid / Ultimate helmet icons
replace the generated variants; Quantum uses the same silhouette with a blue crown. Source atlases and Java processors are retained in tools/textures.

Server GameTests use non-networked FakePlayer actors so Jade / BC do not try to negotiate packets
with vanilla mock connections. The real client smoke independently verifies networked Jade data,
JEI searches, machines / armor / GUI rendering and English / Russian text. The smoke property is
`ic2r_addons.clientSmoke`; it is disabled in normal play.
