"""Import the pinned GPL Energy Control core into the combined addon namespace."""
import json
from pathlib import Path
import shutil
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
SOURCE = Path(sys.argv[1]).resolve(strict=True)
REVISION = 'f3ec46404a6cd3f27e7a0276e589377a491f157c'
assert subprocess.check_output(['git', '-C', str(SOURCE), 'rev-parse', 'HEAD'], text=True).strip() == REVISION

def rewrite(text):
    return (text.replace('com.zuxelus.energycontrol', 'com.igoryok95.ic2raddons.control')
            .replace('com.zuxelus.zlib', 'com.igoryok95.ic2raddons.control.lib')
            .replace('energycontrol:', 'ic2r_addons:')
            .replace('"energycontrol"', '"ic2r_addons"')
            .replace('.energycontrol', '.ic2r_addons')
            .replace('"ec.', '"control.ic2r_addons.')
            .replace('"msg.ec.', '"message.ic2r_addons.control.')
            .replace('energycontrol-common.toml', 'ic2r_addons-control.toml'))

excluded = {'CrossAppEng.java', 'CrossBigReactors.java', 'CrossMekanism.java', 'CrossMekanismGenerators.java',
            'ItemCardAppEngInv.java', 'ItemKitAppEng.java', 'ItemCardBigReactors.java', 'ItemKitBigReactors.java'}
imported = []
for source in (SOURCE/'src/main/java').rglob('*.java'):
    if source.name in excluded:
        continue
    rel = source.relative_to(SOURCE/'src/main/java').as_posix()
    rel = rel.replace('com/zuxelus/energycontrol', 'com/igoryok95/ic2raddons/control')
    rel = rel.replace('com/zuxelus/zlib', 'com/igoryok95/ic2raddons/control/lib')
    target = ROOT/'src/main/java'/rel
    assert not target.exists(), target
    target.parent.mkdir(parents=True, exist_ok=True)
    text = rewrite(source.read_text(encoding='utf-8-sig'))
    if source.name == 'EnergyControl.java':
        text = text.replace('@Mod(EnergyControl.MODID)\n', '')
        text = text.replace('public static final String NAME = "Energy Control";', 'public static final String NAME = "IC2R Addons";')
        text = text.replace('public static final String VERSION = "1";', 'public static final String VERSION = "1.4.0";')
    if source.name == 'CrossModLoader.java':
        begin = text.index('\tpublic static void init() {')
        end = text.index('\n\t@SuppressWarnings', begin)
        text = text[:begin] + '\tpublic static void init() {\n\t\tloadCrossModSafely(ModIDs.IC2, () -> CrossIC2R::new);\n\t}\n' + text[end:]
    if source.name == 'ECCreativeTab.java':
        text = text.replace('CREATIVE_TABS.register("main",', 'CREATIVE_TABS.register("control",')
        text = text.replace('"itemGroup.ic2r_addons"', '"itemGroup.ic2r_addons.control"')
    # The unused commented legacy render method is not part of the integration.
    if source.name == 'InfoPanel.java':
        import re
        text = re.sub(r'/\*@Override // TODO.*?\*/', '', text, flags=re.S)
    target.write_text('// SPDX-License-Identifier: GPL-3.0-only\n// Energy Control by Zuxelus; IC2R integration by Igoryok95. See NOTICE.md.\n' + text, encoding='utf-8')
    imported.append(rel)

for source in (SOURCE/'src/main/resources').rglob('*'):
    if not source.is_file():
        continue
    rel = source.relative_to(SOURCE/'src/main/resources').as_posix()
    if rel in ('pack.mcmeta', 'logo.png'):
        continue
    rel = rel.replace('energycontrol', 'ic2r_addons')
    target = ROOT/'src/main/resources'/rel
    target.parent.mkdir(parents=True, exist_ok=True)
    if source.suffix == '.json':
        incoming = json.loads(rewrite(source.read_text(encoding='utf-8-sig')))
        if target.exists():
            current = json.loads(target.read_text(encoding='utf-8'))
            if '/lang/' in rel:
                assert not current.keys() & incoming.keys(), rel
                current.update(incoming)
                incoming = current
            elif rel == 'data/minecraft/tags/block/mineable/pickaxe.json':
                current['values'] = list(dict.fromkeys(current['values'] + incoming['values']))
                incoming = current
            else:
                raise ValueError('Resource collision: ' + rel)
        target.write_text(json.dumps(incoming, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    else:
        assert not target.exists(), target
        if source.suffix in ('.mcmeta', '.txt'):
            target.write_text(rewrite(source.read_text(encoding='utf-8-sig')), encoding='utf-8')
        else:
            shutil.copy2(source, target)

licenses = ROOT/'src/main/resources/LICENSES'
licenses.mkdir(exist_ok=True)
shutil.copy2(SOURCE/'LICENSE', licenses/'Energy-Control-GPL-3.0.txt')
(ROOT/'tools/control-import.json').write_text(json.dumps({'repository': 'https://github.com/Zuxelus/Energy-Control',
    'revision': REVISION, 'branch': '1.21.1-neoforge', 'license': 'GPL-3.0-only',
    'java_sources': imported, 'excluded_optional_integrations': sorted(excluded)}, indent=2)+'\n', encoding='utf-8')
print(json.dumps({'java_files': len(imported), 'revision': REVISION}))
