# Current original tool sprites (1.4.1)

Run `python tools/textures/restore_original_tools.py --ic2r ../IC2R` with Pillow.
Use `--check` to verify without writing. Six icons retain native IC2R 32x32 pixels
and alpha, changing only orange grips to blue. Treetap wood and drill tips remain.
Vajra uses `source/vajra-original-16.png`, enlarged 2x without smoothing, preserving
its transparent gaps. Provenance: `docs/tool-texture-provenance.json`.

`RefreshTextures.java` leaves these seven current icons untouched. The atlas
instructions below describe historical 1.2.0 tooling and other equipment.

# Texture sources and reproducible processing

## 1.2.0 hybrid refresh

`1.2.0-reference.png` contains the original addon silhouettes and IC2R electric-tool style references.
The built-in imagegen prompt requested one 2x2 transparent pixel-art atlas: advanced diamond drill,
advanced iridium drill, advanced chainsaw and powered wrench, matching IC2R metallic shading while
retaining orange / graphite grips and blue upgrade accents. No text, background or antialiasing.
The generated source is `1.2.0-tools-generated.png`.

Run from the repository root with Java 21:

```sh
java tools/textures/RefreshTextures.java
```

The processor reloads original artwork from Git commit `64b4c20`, applies edge-aware 2x scaling,
keeps armor UV / transparency and normalizes green armor indicators to blue. Four tool-atlas cells and sixteen equipment-atlas cells become strict 32x32 icons.
The equipment atlas preserves original nano / quantum / solar armor and device silhouettes,
with restrained 32-pixel metallic detail and blue energy indicators. Imported ForestryCE sources are in `engine-original`; the renderer
and textures remain LGPL-3.0-or-later (see NOTICE.md and the bundled license).

The older `BlueArmor.java` and its references document the original 1.1.0 blue-armor adaptation.

## 1.2.1 classic solar helmets

Hybrid and Ultimate item icons restore the user's supplied classic reference in
`source/classic-solar-helmets-reference.png`. The 32x32 icons copy that design, remove the
green background and use one texture layer so the legacy overlay cannot cover the restored
detail. The existing Nano icon remains unchanged. Quantum preserves the restored Ultimate
silhouette, uses a brighter blue crown, and retains the existing worn armor UV coordinates.
The built-in imagegen output in `1.2.1-helmet-palette.png` supplies color only.

Run the full sequence after refreshing the original assets:

```sh
java tools/textures/RefreshTextures.java
java tools/textures/ClassicHelmets.java
java tools/textures/QuantumHelmet.java
```

The exact palette prompt is in `1.2.1-prompts.md`. The enlarged four-helmet comparison is
`1.2.1-helmets-preview.png`; runtime icons remain 32x32.
