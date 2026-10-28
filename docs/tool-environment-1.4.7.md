# Mining environment compensation — 1.4.7

## Cause and correction

Native IC2R drills partially compensate Minecraft's water and airborne mining
penalties. Addons mining tools inherited `ItemElectricTool` directly and returned
only their profile speed. Their normal advantage disappeared under one penalty;
with both penalties, an advanced tool could be slower than its native predecessor.

All three Addons mining tools now call
`ItemDrill.getEnvironmentSpeedMultiplier` from IC2R 21.1.81. This is the same rule
used by the native drills, with a factor of three underwater when the submerged
mining attribute is below one, and another factor of three while off the ground.
With Aqua Affinity's full submerged-mining attribute, the water bonus is omitted
because the vanilla water penalty is already absent.

## Effective speed comparison

These values are player mining speed before block hardness and harvest divisors,
without Efficiency, Haste or other modifiers. Both drills are charged and use their
normal profile. The underwater rows assume no Aqua Affinity.

| Environment | Native diamond drill | Advanced before | Advanced after |
| --- | ---: | ---: | ---: |
| Dry, on the ground | 16 | 48 | 48 |
| Underwater, on the ground | 9.6 | 9.6 | 28.8 |
| Dry, off the ground | 9.6 | 9.6 | 28.8 |
| Underwater, off the ground | 5.76 | 1.92 | 17.28 |

The advanced diamond drill's low-power and fine profiles retain their respective
1.5x and 1x ratios to the native diamond drill. The advanced iridium drill follows
the same 3x / 1.5x / 1x progression relative to the native iridium drill. Vajra also
uses the shared compensation across its existing 128 / 64 / 32 profiles.

Base speeds and manual EU costs remain unchanged:

| Tool | Normal speed / EU | Low-power speed / EU | Fine speed / EU |
| --- | --- | --- | --- |
| Advanced diamond drill | 48 / 160 | 24 / 80 | 16 / 50 |
| Advanced iridium drill | 72 / 1000 | 36 / 600 | 24 / 400 |
| Vajra | 128 / 3200 | 64 / 1600 | 32 / 800 |

Advanced chainsaw speed still follows its native chainsaw base; this fix changes
the three mining tools. Empty tools retain their existing slow/blocked behavior.
The machine miner API, 3 x 3 and vein limits, drops and per-block charging are unchanged.

## Compatibility and validation

The required IC2R range is now **21.1.81 through 21.x**, matching the shared helper's
first release. NeoForge remains **21.1.264+**; JEI, Jade and BuildCraft stay optional.

One native server test covers all 54 combinations of the three tools, their three
efficiency profiles, ground contact, water and the submerged-mining attribute.
It checks actual player mining speed, native drill baselines, unchanged charge
during speed inspection and empty-tool behavior. Existing harvest tests continue
to cover actual per-block energy consumption, drops and mining modes.

The final build and all 19 unit tests passed. The paired run with IC2R 21.1.81
passed all 584 required native server tests. See
[the validation record](validation/1.4.7.json).
