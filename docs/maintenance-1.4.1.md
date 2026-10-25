# IC2R Addons 1.4.1 maintenance branch

This branch starts from released Addons 1.4.0 and targets the IC2R 21.1.70
maintenance API. It is development source, not a published or installed update.

## Preserved fixes

- All eight Advanced Machines display heat, speed and pressure warm-up as 0-100%.
  Internal 0-10,000 processing state, recipes, costs and warm-up behavior are unchanged.
- Energy-array cards keep full per-target readings instead of narrowing to 32-bit
  integers. Totals, percentages, portable progress bars, missing-target states and
  older integer-valued saved cards remain readable.
- Advanced tank cards retain long-valued amounts/capacities in text and bars. Their
  percentage calculation no longer overflows integer multiplication.
- Shared card name formatting uses the item's display name without loading the
  client-only Minecraft class on a dedicated server.
- The existing four solar panels keep their original charge gauge. Exact grouped
  charge/capacity appears in a live tooltip, with bounded tier/output/generation rows.

## Scope after cleanup

The four original solar panel tiers through Quantum remain. The retired-panel
migration is preserved. Existing Advanced Machines, Nuclear Control, equipment,
optional BuildCraft bridge, Jade and JEI integrations remain at the 1.4.0 baseline.
Ultimate Lappack stays at 100 million EU and its original recipe; Gravitational
Chestplate recipes and all other equipment balance are unchanged.

No restored upper solar tiers, new panel recipes, Classic material dependencies,
capacity rebalance or Colossal integration are included. Earlier development work
remains recoverable from the previous branch.

## Optional villager trades

The six professions belong to IC2R itself and remain usable without Addons.
When Addons is installed it extends three matching professions:

- Electrical Engineer: monitoring circuits, Advanced Machines sharp plates,
  superconducting components and the first Advanced Solar Panel.
- Nuclear Engineer: thermometers, thermal monitors, information panels and a
  portable panel.
- Greg: advanced chainsaw/drill and the Advanced Lappack, gated behind addon
  cooling cores or engine boosters and master-level prices.

The other three professions retain their native trades. All panel and equipment
capacities remain at their 1.4.0 values; these trades introduce no Classic items.

## Validation

17 JUnit tests and 87 server GameTests pass with IC2R 21.1.70, BuildCraft, Jade,
Create, Mekanism and Almost Unified. The Russian client checks all four solar and
eight Advanced Machines menus, live exact tooltips, percentage text and slot/title
bounds. Native/new IC2R menus and the sound slider are checked in the same isolated
client. Equipment and crafting files have no diff from released 1.4.0.
See the [validation receipt](validation/1.4.1.json) for artifact hashes and scope.
