# Solar helmet self-charging audit

Checked IC2R Addons 1.4.12 with IC2R 21.1.92 on Minecraft 1.21.1 / NeoForge
21.1.264. Generation multiplier was 1.0; `helmetsChargeArmorOnly` was false.

## Reported symptom and cause

A Hybrid Solar Helmet remained empty even after the player removed their other
armor. The removed armor was still in the player inventory.

All four addon helmets use the same allocation order:

1. Worn chestplate, leggings and boots.
2. Offhand, then the ordinary inventory, unless armor-only charging is enabled.
3. The helmet's own battery receives the remaining generation.

The inventory pass includes unequipped electric armor. Taking armor off therefore
does not stop it receiving energy. If compatible inventory armor accepts all the
generation, the helmet remains empty until that armor is full or moved out of the
inventory. A fully charged tool accepts no energy and does not cause this symptom.

## Checked profiles

| Helmet | Day, EU/t | Night/rain, EU/t | Own capacity, EU |
| --- | ---: | ---: | ---: |
| Advanced Solar Helmet | 8 | 1 | 8,000,000 |
| Hybrid Solar Helmet | 64 | 8 | 40,000,000 |
| Ultimate Hybrid Solar Helmet | 512 | 64 | 100,000,000 |
| Quantum Solar Helmet | 4,096 | 2,048 | 100,000,000 |

All require the helmet to be worn and an unobstructed sky in a skylit dimension.
The ordinary Solar Helmet in IC2R is a separate utility item without an internal
electric battery; it supplies its worn chestplate and has no self-charge buffer.

## Validation

- An isolated real client/server run passed all 16 states: each of the four
  helmets in daylight and at night, with an otherwise empty inventory or fully
  charged advanced tools. The inventory screen stayed open. Server and client
  both reported positive, increasing helmet charge.
- A native regression reproduced the reported inventory-armor behavior for all
  four profiles. All generation went into an unequipped compatible chestplate in
  the inventory; the helmet stayed at zero. Removing that chestplate from the
  inventory made the next generated energy enter the helmet.
- All 20 addon unit tests and all 98 required addon GameTests passed.

Run the client check with:

```shell
./gradlew runClient -Pic2rJar=/absolute/path/ic2-neoforge-21.1.92.jar -PsolarHelmetChargingSmoke
```

Runtime charging policy, generation rates and capacities were not changed by this
audit. The added client check is opt-in and uses only the isolated test world.
