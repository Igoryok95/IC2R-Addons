# Blue armor texture reproduction

The inventory icons and worn nano / quantum leggings and boots keep their IC2R pixel layout.
Existing chestplate and solar helmet artwork is retained. The nano layer-1 sheet retains the
legacy chest / arm region and replaces the inherited boot regions with the blue variant.

A built-in imagegen edit produced `blue-reference.png` from the original nano leggings icon,
with the existing advanced nano chestplate as palette reference. The final hue is anchored to
the existing chestplate colors (nano RGB 119/187/255; quantum RGB 105/211/255), with a small
contribution from the generated reference. Original alpha, silhouettes and UV positions are
retained deterministically rather than trusting a generated raster to preserve game UVs.

Prompt: replace only the green energy indicators with chestplate-matching icy blue; preserve
the original dark metal, silhouette, transparency and exact pixel grid; no new details or text.

From the repository root, with Java 21:

```sh
java tools/textures/BlueArmor.java
```

Final resources are under `src/main/resources/assets/solar_gravisuite/textures/item/` and
`src/main/resources/assets/solar_gravisuite/textures/models/armor/`. Source artwork is retained under
`source/`; authorship and rights are documented in the root NOTICE.md.
