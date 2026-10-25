# Electric engine restoration and maintenance

The optional BuildCraft bridge remains separate from Nuclear Control. Without
BuildCraft no engine block, capabilities or recipe is registered. Forestry is not
a runtime dependency.

## Balance and energy accounting

The agreed default is **5 EU/t to 2 MJ/t** (40 MJ/s at 20 TPS). Four native IC2R
upgrade slots accept overclockers, storage upgrades and transformers. Overclockers
use IC2R's increasing speed and EU demand, with generation capped at **8 MJ/t**.
Extra upgrades cannot bypass that ceiling or turn surplus speed into free energy.
Storage upgrades affect the EU buffer; transformers affect accepted EU packets.

The default input buffer is 10,000 EU, tier 2 / 128 EU packets. The independent
output buffer holds 10,000 MJ, corresponding to Forestry's 100,000 RF buffer.
EU is charged only when MJ is generated. Delivery drains existing MJ rather than
charging EU twice. API 2 and legacy passive extraction share a delivery budget,
respect simulation and minimum requests, and cannot duplicate stored energy.
Delivery follows the installed upgrades too: 2 MJ/t at the base setting, up to
8 MJ/t when overclocked. A full buffer cannot bypass the base output limit.
Saved inventory, upgrades, EU, MJ, heat and piston phase survive reloading.

Since 1.4.2, GUI and Jade distinguish three live rates. **From grid** uses the
IC2R energy network's measured incoming EU for the latest calculation tick, excluding
battery discharge. **Consumption** is EU actually removed for MJ conversion in a
tick, and **Output** is MJ actually accepted by consumers across both output APIs.
Neither conversion nor delivery is a number embedded in a translation. A stopped
engine can still charge from the grid; a disconnected engine can consume its buffer.

`baseEuPerTick` replaces the previous `euPerTick` configuration key so the old
32 EU/t default does not silently retain the superseded balance. The default
conversion ratio remains 0.4 MJ/EU. Generation, storage and input tier remain
configurable; the agreed 8 MJ/t generation ceiling is enforced independently.

## Thermal lifecycle and GUI

Original Forestry's 0-3000 heat state controls blue/green/yellow/orange/red stages,
piston speed, gradual cooling and forced cooldown. Temperature is shown in Celsius
and percent. Heat does not multiply conversion efficiency. Switching off lets the
current piston stroke finish before stopping; the trunk cools gradually.

The original `electricalengine.png` background and engine ledger are retained.
Four IC2R upgrade slots replace the circuit board; the battery slot, energy gauge
and live status, heat, rates, energy buffers and input voltage remain visible.
The old circuit-board slot outline is covered using a plain region of the original
background, without modifying the source PNG. All labels have English/Russian
translations. Jade uses separate blue EU and red MJ bars plus stage, temperature,
redstone, rates, packet size and stop reason. Version 1.4.2 fixes inventory coordinates
to the original background: main grid at (7, 83), hotbar at (7, 141). Clickable item
areas start one pixel inside those borders; all 36 player slots are checked.

## Connections and rendering

The engine selects a valid neighboring MJ receiver automatically until the player
successfully turns it with a wrench. As in BCCE, manual selection suppresses automatic
realignment, including when that face has no receiver. The `manualDirection` save flag
survives reloads; older saves default to automatic mode, and placing the block again
resets the flag. BCCE 8.0.25
uses legacy connector/passive-provider capabilities to construct pipe connections
even when API 2 can transfer energy. Both are supplied on the same piston face,
with cache invalidation and neighbor notifications after a facing change.

The placed ForestryCE geometry is retained. Held-item transforms match vanilla
block/BC engine scale (0.4 first person, 0.375 third person), correcting the former
world-size item rendering. IC2 wrench pipe clicks invoke native block activation
before rotation, preserving iron-pipe output switching and charge/cancellation.

## Original references

- [ForestryMC revision 72e15b0](https://github.com/ForestryMC/ForestryMC/tree/72e15b0f08daa25d8293cf28b5d5e1da200f9161):
  `TileEngine`, `TileEngineElectric`, `TemperatureState`, `GuiEngineElectric` and
  original GUI textures. Copyright SirSengir / Forestry contributors,
  LGPL-3.0-or-later; see the bundled Forestry license and NOTICE.md.
- [BCCE 8.0.25](https://github.com/BCCE-team/BuildCraft/tree/8.0.25): native engine
  behavior, wrench block activation and MJ pipe capabilities.
- [ForestryCE renderer reference](https://github.com/thedarkcolour/ForestryCE/tree/ae51f88a6f1f3180a3d3bdddd842cf699cee03e7):
  engine geometry and texture family, already attributed in NOTICE.md.

Regression coverage includes real wood Kinesis pipe connection/extraction, mixed
API calls, upgrade limits, saving, gradual cooling, completed strokes, manual rotation
with IC2R/BC wrenches, live grid input after disconnection and actual client GUI/Jade
rendering. Installation into an existing client, ZIP or server is a separate operation.
