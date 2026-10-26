# JEI and Jade integration — 1.4.4

## JEI

The severe recipe-screen stutter came from core's fixed dynamic-container slot
indices. Use **IC2R 21.1.74** with this addon: its handlers inspect the actual menu,
exclude service slots and no longer address nonexistent player slots. All eight
Advanced Machines reference explicit category identifiers. Metal-forming variants
have dedicated dynamic transfer registrations in the core.

The Compacting Recycler and Molecular Transformer register input-only transfer
handlers and recipe-arrow buttons for their own categories. Their outputs, battery
slots and upgrades are excluded. Kit Assembler keeps its existing exact ingredient
counts and ordinary server-validated inventory-click transfer.

## Jade

| Addon blocks | Information |
| --- | --- |
| All eight Advanced Machines | EU gauge, fractional warm-up and progress gauges, consumption; water and thermal-shock warning where applicable |
| Four solar tiers | EU gauge, actually accepted generation, maximum output packet, sunlight conditions |
| Molecular Transformer | EU gauge; active recipe progress, spent/required EU, consumption and result |
| Electric engine | Existing measured network intake, conversion consumption, actual delivery, EU/MJ gauges and thermal state; white gauge labels |
| Nuclear Control counters | Accumulated/current/average EU and maximum packet |
| Thermal monitors | Alarm threshold and powered state |

Readings are collected on the server. Full solar buffers show zero accepted
generation, rather than reporting nominal generation as actual intake. An idle
transformer does not display an artificial `0 / 0` recipe cost. Core providers do
not duplicate addon overlays. General inventory/fluid contents remain Jade's job.

## GUI and compatibility

All eight Advanced Machines display warm-up to two decimal places, including
`100.00%`, matching the native Induction Furnace in 21.1.74. Jade uses the same
precision. All new labels have English and Russian entries. Optional integration
classes are isolated from normal event-subscriber signatures. Neither JEI, Jade
nor BuildCraft is required or bundled. Base IC2R API compatibility remains 21.1.70;
update the core to 21.1.74 to obtain its new transfer handlers and Jade support.

Machine recipes, capacity, processing speed, water and EU balance are unchanged.
The earlier quiet standby/cooling fix and 16 EU/t basic-machine processing remain.

## Validation

- 19 JUnit tests; 85 GameTests without JEI, Jade or BC.
- The optional BuildCraft/Jade server suite is recorded in the validation receipt.
- Real client: all eight Advanced Machines, Molecular Transformer and Kit Assembler,
  plus native IC2R machines; 25 menu/category combinations and 962 recipe layouts.
  Simulation checks, actual ingredient transfer and recipe-screen opening passed.
- Separate real-client launch opened native and addon GUIs with no optional mods.
- Jade gauges and fractional text were visually inspected in an isolated world.
- 525 English and 525 Russian keys; no missing Russian keys or replacement characters.

See [machine-readable results](validation/1.4.4.json). No production installation
is included in this source update.
