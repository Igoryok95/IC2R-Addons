# Tool actions in 1.4.13

Advanced drills supported shovel mining but their right click only attempted to
place a torch. They now try IC2R's native shovel path action first, then keep the
existing torch fallback when pathing does not apply.

Requires IC2R 21.1.95 or later within 21.x. The shared method is supplied by that
core version, so the declared minimum prevents a missing-method failure with an
older core.

| Tool | Normal | Low power | Fine |
|---|---:|---:|---:|
| Advanced Diamond Drill | 160 EU | 80 EU | 50 EU |
| Advanced Iridium Drill | 1,000 EU | 600 EU | 400 EU |

These are the existing mining-profile costs. A path action changes one ground
block, including in 3x3 mining mode. Native clearance and tool-event rules apply;
an insufficient charge cannot make a path. IC2 mode and extra-key shortcuts keep
their existing settings behavior. Ground pathing does not consume a torch, while
right-clicking other suitable surfaces still places one. Vajra's torch behavior
is preserved.

The Advanced Chainsaw inherits the core stripping fix without duplicating it:
100 EU per successful action, native wood mappings and axis preservation, and
priority for mode shortcuts and shield use.

Regression tests exercise both advanced drills through native server interactions
in all three efficiency profiles and both mining modes. They check single-block
scope, exact costs, low power, shortcut handling and torch/Vajra compatibility.
The paired core tests also run their stripping cases against the registered
Advanced Chainsaw when this addon is installed.

The final local build passed 20 Addons unit tests and all 627 combined required
server GameTests against IC2R 21.1.95. The standalone core passed 34 unit tests
and 532 server GameTests. Two saw variants passed 126 wood/axis comparisons in the
combined run. Gameplay client, server and modpack ZIP installation is separate
from these isolated tests.

Paired GitHub validation uses a Gradle-compatible checkout directory and the same
NeoForge 21.1.264 runtime for both builds. An existing machine-test fixture now
selects a recipe whose sample input resolves back to that same recipe and is
fully consumed. Previously it used an arbitrary first entry from a hash map;
overlapping native recipes could make its expected output differ from the recipe
actually selected by the manager. Machine processing and recipe priorities are
unchanged.
