# NeoForge 1.21.1 implementation notes

The baseline was craftorio's Forge 1.20.1 ASP adaptation. Registration now uses NeoForge deferred
registries, Java 21, modern resource paths / data components / NBT registry lookup, IC2R 21.1.67
and JEI 19. The build resolves IC2R from the maintained public fork release.

Solar panels remain IC2R `TileEntityBaseGenerator` subclasses, and the transformer remains an
IC2R electric machine. Common GUI parsing calls the public IC2R parser with an addon class so
module resources resolve without reflection. Gauge styles must exist on both sides because the
server parses GUI definitions too.

Solar helmet implementations inherit IC2R Nano / Quantum helmet classes. They retain their
powered functions and allocate each tick's generated EU through the electric-item API. New
capacities are upgrades of the current IC2R values. The shared singleton timer from the old
migration has been removed.

Transformer recipes use a native recipe type, codec and stream codec. Inputs remain unconsumed
until completion; output capacity is checked before energy is spent. Pending input identity,
output and energy progress are serialized. A changed input or changed recipe resets incompatible
partial work. The normal IC2R component serialization retains the energy buffer.

The original 3D transformer geometry uses NeoForge's block-entity and item renderer extension
points. Active-core billboards use the supplied ASP 4.3.0 textures. No external updater or custom
IC2 energy implementation is included.

## Solar GraviSuite 1.1.0 integration

The existing ASP mod ID is retained as the only loader entry. GraviSuite initialization now
runs from that entry point; its client subscribers attach to the same mod container. Its
registrations, resource locations, payload IDs and test-template namespaces remain `gravisuite`.
Both existing config filenames are explicitly registered on the combined container.
The standalone GraviSuite mod is marked incompatible to prevent duplicate registrations.

Leg / boot subclasses retain IC2R movement, armor and fall behavior, but select addon-owned
armor material layers. Nano layer 1 combines the original chestplate art with the upgraded
boot regions; layer 2 contains the leggings. Quantum layers use IC2R UVs with blue accents.
Existing chestplate and helmet artwork is retained. Storage is synchronized across advanced
nano pieces (8M EU) and top quantum / gravitational pieces (100M EU); the hybrid helmet has 40M.

## Solar GraviSuite 1.1.1 unified identity

The loader entry, all registries, assets, datapacks, localization keys, network payloads and
armor / attribute resources now use `solar_gravisuite`. The 1.1.0 namespaces described above
are historical only. No registry aliases or missing-mapping migration are supplied.
Both former resource trees are merged with collision checking, including localization maps.
The two config specs use `solar_gravisuite-solar.toml` and `solar_gravisuite-equipment.toml`.

JEI obtains the item creator ID through NeoForge's Item API. The shared namespace therefore
provides the same display mod name for both content families. The developer-only JEI smoke
check compares full result sets for `@solar`, `@gravi`, `@Solar` and `@solar_gravisuite`,
including all registered items rather than checking only counts or representative samples.
