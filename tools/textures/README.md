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
