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
