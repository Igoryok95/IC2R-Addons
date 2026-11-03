# Molecular Transformer and ingots in 1.4.11

All seven ingot sprites used by IC2R's item models are 16x16. The two Addons
ingots had been enlarged to 32x32, introducing a different, uneven silhouette.
The original Sunnarium family also used 16x16 sprites. These seven files are
restored directly from the last pre-upscale version (`743591c^`), without
resampling or generated artwork:

- `uraniumingot.png`
- `iridiumingot.png`
- `sunnarium.png`
- `sunnariumpart.png`
- `sunnariumalloy.png`
- `enrichedsunnarium.png`
- `enrichedsunnariumalloy.png`

The Molecular Transformer used sink tier 14 although the current survival
progression ends at tier 5: Quantum solar panels and the highest transformer
output provide 8,192 EU packets. Its sink tier now follows the Quantum panel
profile. Saved energy remains intact; sink tiers are set by the constructor and
are not restored from old energy-component NBT. Buffer size (100,000 EU), recipe
energy costs and conversion logic are unchanged.

The item uses native `ItemBlockIc2` tooltip dispatch. The actual energy component
provides its tier and capacity; the energy network provides the permitted packet
size. All descriptions use native gray styling and English/Russian locale keys.
The Quantum Generator also regains the native tier line through the same item
factory. Jade retains its existing buffer, progress, energy-cost and result
display and gains tier/packet information even when no recipe is running.
The shared electrical provider also sends the actual source tier for all four
panels, and the current sink tier for Advanced Machines.

ASP XML images already contain complete backgrounds. The default dynamic GUI
rendered an additional gray frame underneath them. A client-only screen for
the four panels, Molecular Transformer and Quantum Generator omits that default
frame, preserving XML widgets, slot positions, localized text and original PNGs.

## Validation

- All 20 unit tests and the final build pass. Both UTF-8 locales contain the new
  information strings with matching format arguments. The enriched alloy's
  English label also distinguishes it from the ordinary alloy.
- All 596 required paired core/Addons GameTests pass without optional Jade,
  including real Quantum-panel energy transfer at the new tier-five limit.
- Jade's headless fake-player handshake rejects its ping payload; its integration
  is therefore verified with real client/server requests instead of test players.
- The client checks all six ASP screens in Russian and English, preserving the
  actual player containers. Resumed checks cover only the remaining language/range.
- Real Jade server data reports panel tiers 1/2/3/5 and transformer tier 5, with
  correct packet limits. Existing transformer progress and result data remain.
- Normal/advanced item tooltips contain native gray tier, capacity and packet
  information in both locales. Active restored materials are rendered alongside
  IC2R ingots; all seven restored PNGs retain their original 16x16 source data.

Reproduce the client pass with `-PsolarPresentationSmoke -PwithJade` and a local
core JAR via `-Pic2rJar=...`. It uses the isolated `RestorationSmoke` world;
`solarPresentationStart` and `solarPresentationEnglishOnly` allow bounded resumption.
