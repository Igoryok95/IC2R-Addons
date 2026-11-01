# Armor and equipment hints in 1.4.10

The active `advanced_nano_layer_1.png` atlas had four transparent texels in its
otherwise opaque shoulder and sleeve UV regions, plus one opaque texel in the
open wrist-cap region. The holes now reuse adjacent original texels; the detached
texel is transparent. Only five texels change, at (88,32), (95,32), (111,40),
(111,59) and (96,39). Atlas dimensions, opaque artwork and blue details are kept.
This is a deterministic UV repair; no generated or repainted artwork is used.

The other rendered chest/backpack atlases were inspected: Gravi chest armor has
complete arm caps and sleeves. Jetpack and Lappack atlases intentionally omit
arm faces because they are backpacks. Their transparent regions remain intact.
The improved Nano legs/boots share the repaired material; leg and boot UVs are
unchanged. Unused legacy reference atlases are not substituted for active layers.

Armor, utility and tool descriptions now pass through native `Ic2Tooltip.add`.
Explicit colors and rarity-colored names retain their existing behavior.
The current engine, magnet, IC2 mode/Alt, use, sneak and jump bindings are resolved
when the tooltip is built. Rebinding or unbinding a key therefore changes the
display immediately; no literal G/H/M/Alt/right-click labels are stored in hints.
Inherited IC2R helmet, chainsaw and Quantum movement hints already use live keys.

The hotbar-magnet setting label is shortened in both locales, preserving its
translation key and actual binding. Nano/advanced jetpacks also show their live
jump + IC2 mode hover combination. The chainsaw documents its existing tree mode.

The shared vanilla floating-watchdog fix belongs to IC2R 21.1.88, not to an
Addons-only movement override. The dependency minimum remains 21.1.81; updating
core is necessary to receive that particular server-side fix.

Validation:

- All 20 Addons unit tests passed, including canonical shoulder/wrist UV masks.
- All 594 required paired core/Addons dedicated GameTests passed. The new chest
  check verifies paid native thrust for enabled Nano/advanced jetpacks and no
  jetpack thrust for disabled engines, Lappacks or the separate Gravi flight path.
- Both locales have 529 matching entries with consistent format arguments and
  no replacement characters.
- The real client checked descriptions for all 22 electric equipment items and
  nine key hints after rebinding keyboard, modifier and mouse buttons in RU/EN.
  Unbound engine keys and native Nano helmet hints were included; test bindings
  were restored without saving them to options.
- Native key-list button bounds leave a gap after both Addons labels in RU/EN.
  Screenshots show the actual settings and gray Nano chestplate tooltip.
- Original Nano armor and the repaired chestplate were rendered side by side
  from front, shoulder and wrist angles. The arm cap is continuous and the
  detached wrist pixel is absent.

Reproduce the presentation pass with `-PequipmentPresentationSmoke` and a local
IC2R 21.1.88 JAR via `-Pic2rJar=...`. It uses the isolated `RestorationSmoke`
test world and does not touch a production client or server.
