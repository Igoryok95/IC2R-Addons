# IC2R Addons: implementation notes

## Identity and migration

The 1.2.0 loader ID, registry / resource namespace and network identity are `ic2r_addons`.
Java packages live under `com.igoryok95.ic2raddons`, split into solar, equipment, machines, control and bridge modules.
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

## Advanced Machines module (1.3.0)

`machines` registers eight `Ic2TileEntityBlock` processing machines in the existing addon
namespace and creative tab. The heating base derives from IC2R's electric machine, retaining
its energy network, sounds, discharge slot, inventory synchronization and wrench handling.
It uses the live IC2R recipe-manager getters on each lookup; recipe collections are never mutated.
The recycler adds a nine-scrap packing case before delegating to IC2R's recyclable-item manager,
preserving its blacklist and random yield without caching a manager across levels / reloads.

Heat and paid partial work are separate: output blockage / energy loss pauses work, and input
identity changes clear it. Persistent state includes the pending input components, progress,
heat, EU, inventory and fluid components. Water consumption uses internal tank draining; external
draining is disabled. Container slots use IC2R's fluid-container processing and return empties.
Thermal shock is checked before processing, never on a simulated fill. On world loading, the
previous water level is initialized from the restored tank so a filled hot machine stays safe.

IC2R only registers NeoForge capabilities for its own block-entity registry. The addon explicitly
registers sided item wrappers for all eight types and a simulation-safe water-only fluid handler
for the two water machines. Native IC2R fluid import upgrades still use the `Fluids` component.
The optional integrations reuse IC2R JEI categories, add scrap packing and supply server-owned
Jade telemetry. Original modern textures are already 32x32 and are copied unchanged.

`-PmachinesSmoke` launches the isolated `SolarClientSmoke` development world, checks all 96
active / inactive model faces and opens all eight synchronized GUIs. It has no effect during
ordinary play. The server regression suite also covers real EU transfer, recipe output,
water / item capabilities, redstone warm-up, output blockage, persistence, empty input inspection,
wrench rotation, bucket returns and the cancellable native thermal explosion.

## Optional BuildCraft implementation

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

## Nuclear Control / Energy Control (1.4.0)

`tools/control-import.json` pins Zuxelus/Energy-Control's `1.21.1-neoforge` revision.
Its monitoring core and internal utility classes are namespaced under `control` /
`control.lib`; there is no second common mod entrypoint. The existing addon constructor
registers its content, networking, capabilities and common config. Client registrations
remain physically client-only. Optional upstream integrations that require unrelated mods
are not copied as dependencies; the mandatory integration is `CrossIC2R`.

CrossIC2R uses IReactor / IReactorChamber, native Energy components, IEnergyStorage,
EnergyNet NodeStats and IC2R fuel-use data. EU is labelled as EU. Unregistered delegates
are tolerated during initialization; remote reads check loaded chunks before accessing tiles.
The restored counters implement IEnergyConductor directly and sample native throughput once
per server tick. Menu properties split double bits into four 16-bit values to avoid network
truncation. Persistent totals / sample counts use doubles / longs and survive reloading.

`-PcontrolSmoke -PwithBC -PwithJade` opens eight real synchronized menus, thermal / alarm
screens and a live reactor panel in the isolated developer world. It then captures the
actual Jade engine overlay. Run in en_us and ru_ru; screenshots in documentation use en_us.
No developer smoke executes during normal play.

Control rendering explicitly maps seven empty-slot sprites into the block atlas. The restored
counter body overrides the base entity block's invisible render shape. Kit Assembler gauges
and panel / timer edit boxes render after their background; assembler and timer menus attach
the opening server player to their update listeners and remove that reference on close.
The assembler pauses without discarding partial work during a power interruption. Smoke
coverage uses a real partially processed recipe and an active server countdown, then captures
each registered control block separately. The original Kit Assembler PNGs are unchanged.

The control module registers only CrossIC2R. It adds no BuildCraft monitoring adapter;
the pre-existing optional EU engine / wrench bridge remains a separate feature.
