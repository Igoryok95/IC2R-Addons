# Unsupported-block mining speed in 1.4.8

The Advanced Diamond and Advanced Iridium Drills returned `0.1` both when empty
and when a block was outside their pickaxe/shovel tags. Levers have no matching
tool tag, so even a fully charged drill took ten times the normal time to break
one. The same fallback affected other unsupported blocks.

Charged drills now return the native hand-speed fallback of `1` on unsupported
blocks. This matches a diamond pickaxe and IC2R's native diamond drill on a lever.
It does not grant pickaxe speed to wood or make additional blocks eligible for
area mining. Supported-block profiles, environment compensation, energy costs,
capacities and mining areas are unchanged. Empty tools keep their existing
restriction; Vajra still uses its universal-tool profile. The Advanced Chainsaw
inherits IC2R's fallback, and GraviTool inherits the native wrench behavior;
neither contained the incorrect `0.1` unsupported-block branch.

Native regressions compare actual player mining speed on a lever while holding
nothing, a diamond pickaxe, an IC2R diamond drill and both Advanced drills in all
three efficiency modes. Every registered breakable default block outside the
drills' effective tags is checked for the corrected fallback. Existing tests
continue to cover mining profiles, water/air penalties, actual native harvests,
energy use, drops and area modes.

| Tool on a lever | Previous speed multiplier | Corrected multiplier |
| --- | --- | --- |
| Empty hand | 1 | 1 |
| Vanilla diamond pickaxe | 1 | 1 |
| IC2R diamond drill | 1 | 1 |
| Advanced Diamond / Iridium Drill, charged | 0.1 | 1 |

Validation passed: 19 unit tests, a release build and 587 combined core/Addons
GameTests on NeoForge 21.1.264 with IC2R 21.1.83. The fallback regression checked
4,734 block/profile combinations in the loaded registries. The existing 54
environment/profile checks and native harvesting/energy regressions also passed.
