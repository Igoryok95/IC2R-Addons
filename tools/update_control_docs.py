"""Update current English product documentation without rewriting historical releases."""
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
description = ('IC2R Addons for Minecraft 1.21.1 NeoForge: Nuclear Control screens, reactor sensors, alarms and native EU counters; '
               'all eight Advanced Machines, four classic solar tiers and solar helmets, Molecular Transformer, powered armor and electric tools. '
               'Optional BuildCraft EU engine and wrench integration; Jade diagnostics and JEI recipes. English and Russian localization.')
readme = ROOT/'README.md'
text = readme.read_text(encoding='utf-8')
text = text.replace('All eight Advanced Machines, four classic solar panel tiers up to Quantum, four solar helmets, powered equipment and optional BuildCraft integration',
                    'Nuclear Control monitoring, all eight Advanced Machines, four classic solar panel tiers up to Quantum, four solar helmets, powered equipment and optional BuildCraft integration')
text = text.replace('Advanced Machines, Advanced Solar Panels and GraviSuite features', 'Nuclear Control / Energy Control, Advanced Machines, Advanced Solar Panels and GraviSuite features')
text = text.replace('ic2r-addons-neoforge-1.21.1-1.3.0.jar', 'ic2r-addons-neoforge-1.21.1-1.4.0.jar')
text = text.replace('Do not keep duplicate standalone ASP / GraviSuite installations.', 'Do not keep duplicate standalone ASP / GraviSuite / Energy Control / Nuclear Control installations.')
text = text.replace('docs/images/engine-jade-en-1.2.1.png', 'docs/images/engine-jade-en-1.4.0.png')
section = '''## Nuclear Control monitoring (1.4.0)

The monitoring core of Energy Control's NeoForge 1.21.1 branch is integrated into the same
`ic2r_addons` mod container, resource namespace and JAR. A separate Energy Control installation
is not required. Original authors and GPL-3.0 source / asset terms are preserved in
[NOTICE.md](NOTICE.md) and the bundled `LICENSES/Energy-Control-GPL-3.0.txt`.

- Information Panels, Advanced Information Panels, Holographic Panels and their extensions;
  a Portable Panel, Card Holder, Kit Assembler and programmable Timer.
- Local / Remote Thermal Monitors, Howler / Industrial Alarms and signal lamps.
  Thermal thresholds produce redstone signals; wire an inverted safe-temperature signal
  into the IC2R reactor to stop it when the threshold is exceeded.
- Reactor cards read native heat, maximum heat, EU/t, running state and remaining fuel life.
  Fuel duration uses IC2R's component wear, including MOX, rather than vanilla item damage.
- Energy / fluid / inventory / redstone / text / time cards, combined storage arrays and
  sensor range / display upgrades. Native EU cards work with IC2R storage, solar panels,
  the Molecular Transformer and all eight Advanced Machines.
- Restored Energy Counter and Average Energy Counter measure actual IC2R network flow.
  Five sides accept EU; the marked front face is the output. The base maximum packet is
  32 EU; up to four IC2R Transformer Upgrades raise it to 128 / 512 / 2048 / 8192 EU.
  Counters retain fractional accumulated EU, sampling time and upgrades on disk. Their
  synchronized menus show cumulative EU, current / average EU/t and include an explicit reset.
- Restored Counter Sensor Kit / Card and classic thermometers. The Digital Thermometer
  has 12,000 EU capacity, tier 1 and consumes 50 EU per reading.
- Jade reports server-owned counter readings and monitor thresholds. JEI includes crafting
  and Kit Assembler recipes. Control uses existing IC2R circuits / machine casings rather
  than registering duplicate components.

`ic2r_addons-control.toml` controls refresh periods, range, allowed / custom alarm sounds and
the dedicated-server translation language (`en_us` / `ru_ru`). External web monitoring is
disabled by default. Sensor reads do not force-load an unloaded target chunk.
All interface labels, tooltips, messages and configuration labels have English / Russian keys.

The engine's previous mojibake separator is replaced with an ASCII `|` in both languages;
the screenshot below is captured from the actual corrected client.

![Native IC2R reactor monitoring](docs/images/control-world-en-1.4.0.png)
![Synchronized energy counter](docs/images/control-counter-en-1.4.0.png)
![Information Panel settings](docs/images/control-panel-en-1.4.0.png)

'''
if '## Nuclear Control monitoring (1.4.0)' not in text:
    text = text.replace('## Advanced Machines\n', section+'## Advanced Machines\n')
readme.write_text(text, encoding='utf-8', newline='\n')

notes = ROOT/'RELEASE_NOTES.md'
text = notes.read_text(encoding='utf-8')
release = '''# IC2R Addons 1.4.0

Minecraft 1.21.1 / NeoForge 21.1.264+ / IC2R 21.1.68+ / Java 21.

Integrates Energy Control / Nuclear Control monitoring into the single addon JAR and
`ic2r_addons` namespace: information / advanced / holographic panels and extensions,
portable displays, card holder, sensor kits / cards, thermal monitors, alarms, lamps,
timer and Kit Assembler. A native IC2R integration reads EU storage / flow, reactor heat,
output and real fuel wear, including the existing addon processing machines and solar equipment.

Restores native EU Energy / Average Counters, counter cards and classic thermometers.
Counter menus synchronize full double-precision readings, support Transformer Upgrades,
explicit reset and persistent totals / sample duration. Jade shows server-provided readings.
Reuses IC2R circuits / casings, completes English / Russian localization and guards remote
sensor reads against forced chunk loading. Network edits require a nearby non-spectator player.

Fixes the corrupted engine rate separator (`в†’`) by using `|` in both locales. Adds a
regression check that rejects misdecoded Cyrillic in English translations. Documentation
screenshots are captured again from the corrected client in English.

The integrated Energy Control module retains GPL-3.0 and source / artwork attribution.
See NOTICE.md, the bundled LICENSES directory and docs/validation/1.4.0.json for evidence.

---

'''
if not text.startswith('# IC2R Addons 1.4.0'):
    notes.write_text(release+text, encoding='utf-8', newline='\n')

porting = ROOT/'PORTING.md'
text = porting.read_text(encoding='utf-8').replace('split into solar, equipment, machines and bridge modules.', 'split into solar, equipment, machines, control and bridge modules.')
text += '''
## Nuclear Control / Energy Control (1.4.0)

`tools/control-import.json` pins Zuxelus/Energy-Control's `1.21.1-neoforge` revision.
Its monitoring core and internal utility classes are namespaced under `control` /
`control.lib`; there is no second common mod entrypoint. The existing addon constructor
registers its content, networking, capabilities and common config. Client registrations
remain physically client-only. Optional upstream integrations that require unrelated mods
are not copied as dependencies; the mandatory integration is `CrossIC2R`.

CrossIC2R uses IReactor / IReactorChamber, native Energy components, IEnergyStorage,
EnergyNet NodeStats and IC2R fuel-use data. EU is labelled as EU. Unregistered delegates
are tolerated during initialization; remote reads check loaded chunks before accessing tiles.
The restored counters implement IEnergyConductor directly and sample native throughput once
per server tick. Menu properties split double bits into four 16-bit values to avoid network
truncation. Persistent totals / sample counts use doubles / longs and survive reloading.

`-PcontrolSmoke -PwithBC -PwithJade` opens eight real synchronized menus, thermal / alarm
screens and a live reactor panel in the isolated developer world. It then captures the
actual Jade engine overlay. Run in en_us and ru_ru; screenshots in documentation use en_us.
No developer smoke executes during normal play.
'''
porting.write_text(text, encoding='utf-8', newline='\n')

notice = ROOT/'NOTICE.md'
text = notice.read_text(encoding='utf-8')
text += '''
## Nuclear Control / Energy Control integration (1.4.0)

- Original Nuclear Control: **Shedar (Oleg Romanovskiy)**; original idea: **Nargon and
  Noosius**. Nuclear Control 2 contributors include **Xbony2, dmf444 and Pyrolusite**.
- Modern monitoring implementation / artwork: **Zuxelus / Energy Control**, branch
  `1.21.1-neoforge`, revision `f3ec46404a6cd3f27e7a0276e589377a491f157c`:
  https://github.com/Zuxelus/Energy-Control/tree/f3ec46404a6cd3f27e7a0276e589377a491f157c .
  The imported `control` / `control.lib` module retains **GPL-3.0-only**. The complete
  license is bundled as `LICENSES/Energy-Control-GPL-3.0.txt`; adapted source files are
  published in this repository and in the matching source JAR. The import manifest records
  source paths and excluded optional integrations. No original author identity is replaced.
- Classic counter-card and thermometer icons: Nuclear Control 2, revision
  `43b6371957d2448f0128b52b8a486ff9e7ac3c7f`:
  https://github.com/xbony2/Nuclear-Control/tree/43b6371957d2448f0128b52b8a486ff9e7ac3c7f .
  These original PNGs are copied unchanged; its BSD-3-Clause license is bundled as
  `LICENSES/Nuclear-Control-BSD-3-Clause.txt`.
- IC2R adapters, restored native EU counters / menus, current registry / data-component
  integration, completed English / Russian text and regression checks: **Igoryok95**.

The earlier rights section describes the ASP / GraviSuite / Advanced Machines materials;
it does not revoke the GPL or BSD permissions of this monitoring module. This integration
does not claim that the entire collection of third-party artwork has been relicensed.
'''
notice.write_text(text, encoding='utf-8', newline='\n')

metadata = ROOT/'src/main/resources/META-INF/neoforge.mods.toml'
text = metadata.read_text(encoding='utf-8')
text = text.replace('license="All Rights Reserved"', 'license="Mixed: Nuclear Control GPL-3.0-only; see NOTICE.md and LICENSES"')
start=text.index('description="');end=text.index('"\n',start)
text=text[:start]+'description="'+description+text[end:]
text=text.replace('VintageModsReforged; NeoForge', 'VintageModsReforged; Shedar, Xbony2, dmf444, Pyrolusite, Zuxelus; NeoForge')
for mod in ('energycontrol','nuclearcontrol'):
    text+='\n[[dependencies.ic2r_addons]]\nmodId="'+mod+'"\ntype="incompatible"\nversionRange="[0,)"\nside="BOTH"\n'
metadata.write_text(text,encoding='utf-8',newline='\n')
for code in ('en_us','ru_ru'):
    path=ROOT/'src/main/resources/assets/ic2r_addons/lang'/(code+'.json')
    values=json.loads(path.read_text(encoding='utf-8'))
    values['fml.menu.mods.info.description.ic2r_addons']=description if code=='en_us' else 'IC2R Addons для Minecraft 1.21.1 NeoForge: экраны Nuclear Control, датчики реактора, сигнализация и счётчики EU; восемь улучшенных машин, четыре уровня солнечных панелей и шлемов, молекулярный преобразователь, энергетическая броня и инструменты. Необязательный мост BuildCraft, диагностика Jade и рецепты JEI. Полный английский и русский перевод.'
    path.write_text(json.dumps(values,ensure_ascii=False,indent=2)+'\n',encoding='utf-8',newline='\n')
print('Current documentation, attribution, metadata and localized description updated for 1.4.0.')
