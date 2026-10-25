# Advanced Machines sound fix and energy audit

## Operating sounds

The shared machine implementation treated any nonzero heat as an active machine.
IC2R's client sound manager consequently played the operating loop during empty
manual/redstone standby and until the machine completely cooled down.

Version 1.4.3 activates the native sound lifecycle only when a valid recipe actually
advances using EU and, where required, water. Completion stops the sound; power loss,
blocked output, dry tanks and interrupted work stop it without waiting for stored
heat to disappear. Processing resumes through the same native activation path.
Manual standby, external redstone, warm-up, cooling, water use and saved partial work
retain their existing behavior. All eight Advanced Machines share this correction.
The tile field and the model's `ACTIVE` property are reconciled together, including
fresh placements and saved state mismatches; either can otherwise keep IC2R's loop alive.

## Energy comparison

The IC2R induction furnace spends 1 EU/t on heat maintenance plus 15 EU/t while
processing, for 16 EU/t total. It processes its two independent inputs in parallel.
Advanced Machines use their listed processing rate instead of their idle rate;
standby EU is not added to that rate.

| Machine | Processing EU/t | Standby EU/t | Paid ticks per operation at 100% | Processing EU per operation |
| --- | ---: | ---: | ---: | ---: |
| IC2R Induction Furnace | 16 | 1 | 13 | 208 per cycle; 104 per item with both inputs loaded |
| Rotary Macerator | 16 | 1 | 12 | 192 |
| Singularity Compressor | 16 | 1 | 12 | 192 |
| Centrifuge Extractor | 16 | 1 | 12 | 192 |
| Compacting Recycler | 16 | 1 | 1 | 16 |
| Liquescent Extruder | 24 | 1 | 12 | 288 |
| Impellerized Roller | 24 | 1 | 12 | 288 |
| Water-Jet Cutter | 24 | 1 | 12 | 288 |
| Thermal Washer | 48 | 6 | 12 | 576 |

These numbers exclude cold startup and idle maintenance between recipes. An operation
means one recipe application; outputs may contain several items, and scrap packing
consumes nine scrap per box. Recycling retains its probabilistic yield. Water demand
is separate from EU demand. The induction furnace advances by integer `heat / 30`:
at 100% this is 333 towards a threshold of 4,000, hence 13 paid processing ticks.
Its outputs are committed at the beginning of the following update.

The [attributed Advanced Machines reference](https://github.com/craftorio/ic2-advanced-machines/tree/94d01c01fc292ddcacc4121eaddbe130472e650c/src/main/java/com/chocohead/AdvMachines/te)
uses 15 EU/t for the four basic machines. At the user's request, version 1.4.3 raises
that total to 16 EU/t to match the induction furnace, with no additional 1 EU/t charge
during processing. Standby, specialized-machine ratings and all speed factors are preserved.
The [native induction implementation](https://github.com/Igoryok95/IC2R/blob/21.1.72/src/main/java/ic2/core/block/machine/tileentity/TileEntityInduction.java)
provides the comparison. Comparing 48 EU/t for washing directly with 16 EU/t for
smelting omits their different recipes and throughput. No accidental EU multiplier
was found; the basic 15-to-16 EU/t adjustment is the only consumption change.

## Regression coverage

- All eight machine types: manual/external standby, actual processing, blocked
  output, power loss/restoration, removed input and residual cooling. Both the tile
  field and blockstate used by the client sound manager must track processing.
- Exact EU deductions for recipes and standby, including the new 16 EU/t basic rate,
  unchanged 24/48 EU/t specialized rates and 1/6 EU/t standby ratings.
- Native induction furnace: silent 1 EU/t standby, 16 EU/t total processing,
  two completed smelts after 13 paid ticks, and silence after the last inputs finish.
- A single opt-in development client pass checks actual operating-loop playback
  across eight transitions in the isolated `RestorationSmoke` world. It is disabled
  unless started with `gradlew runClient -PmachineSoundSmoke`.

Results and command scope are recorded in [the validation receipt](validation/1.4.3.json).
