# Armor protection — 1.4.12

## Cause and scope

IC2R's charged Nano/Quantum modifier used one ID for every armor slot. Pieces
overwrote one another, and charge changes or removal made the HUD and actual
armor protection fluctuate. IC2R 21.1.90 fixes this with separate slot IDs and a
shared charged-protection hook; this Addons version requires that core version.

Advanced Nano leggings/boots, upgraded Quantum leggings/boots and all solar
helmets inherit their native charged protection. Advanced Nano and Gravi chests
derive directly from `ItemArmorElectric`, so the former class-only checks missed
them. They now explicitly supply the native chestplate value of eight points.
Existing energy thresholds remain 5,000 EU for Nano and 20,000 EU for Gravi.

A full charged suit contributes 3 + 8 + 6 + 3 = 20 armor points, or ten full
vanilla armor icons. Removing the head and chest leaves nine points from charged
leggings and boots. The same points participate in actual damage reduction.
The separate electric absorption formula, capacities, EU costs, abilities,
toughness and artwork are unchanged. Advanced/Ultimate Lappacks and the unarmored
Advanced Jetpack do not gain the protective chestplate bonus. No localized strings
or controls change.

## Validation

`ArmorProtectionGameTests` covers all four solar helmets, both protective chests,
upgraded Nano/Quantum legs and boots, native/upgraded mixed sets, per-piece EU
changes, actual damage, removal, depletion and recharge. Every Gravi armor kind
is checked charged and empty, including backpacks.

The opt-in `-ParmorProtectionSmoke` client uses only the isolated
`run/restoration-client` world. It checks sixteen real server/client states:
empty equipment; incremental upgraded Nano equip; jump/fall-energy use; upgraded
Quantum equip; partial removal; depletion/recharge; native Nano/Quantum and mixed
sets. It checks actual player attributes on both sides after network updates,
captures the native HUD and stops only its own smoke client.

Headless combined GameTests run without BC/Jade; the armor test does not need
their client handshakes. Production installations are not modified by these checks.

Completed checks: 20 Addons unit tests, all 601 required combined GameTests,
sixteen synchronized client/server states and a successful build. Standalone
IC2R also passed its 34 unit tests and all 510 required GameTests without Addons.
Screenshots are in the isolated world's `screenshots/armor-protection-stage-*.png`.

CI builds the paired core from pinned commit
`3aba7cc39b6d622f40d8b0fab8c859ac0e82994e`, so the new armor API is available
before a release asset is published. Its optional compatibility job builds and
runs unit tests with BC/Jade; native headless GameTests run only in the base job.

A separate headless BC run (`build/1.4.12-bc-validation.log`) stopped in BC's
`WorldSavedDataWireSystems` before completing the suite: BC attempted to send
`buildcraftlib:messages_to_client` to a test player without a negotiated channel.
That run is not counted as passing BC runtime validation. The completed armor
GameTests and actual client sequence do not depend on BC and passed separately.
