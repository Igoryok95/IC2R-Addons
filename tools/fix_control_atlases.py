"""Merge the upstream internal zlib sprite namespace and register every empty-slot icon."""
import json
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'src/main/resources'
old=RES/'assets/zlib'
if old.exists():
    for source in list(old.rglob('*')):
        if not source.is_file():continue
        target=RES/'assets/ic2r_addons'/source.relative_to(old)
        target.parent.mkdir(parents=True,exist_ok=True)
        assert not target.exists() or target.read_bytes()==source.read_bytes(),target
        target.write_bytes(source.read_bytes())
        source.unlink()
    for folder in sorted(old.rglob('*'),key=lambda p:len(p.parts),reverse=True):
        if folder.is_dir():folder.rmdir()
    old.rmdir()
for root in (ROOT/'src/main/java/com/igoryok95/ic2raddons/control',RES):
    for path in root.rglob('*'):
        if path.suffix not in ('.java','.json','.mcmeta'):continue
        text=path.read_text(encoding='utf-8')
        if 'zlib:' in text:path.write_text(text.replace('zlib:','ic2r_addons:'),encoding='utf-8',newline='\n')
icons=['card','range','color','touch','power','dischargeable','transformer']
atlas=RES/'assets/minecraft/atlases/blocks.json'
atlas.write_text(json.dumps({'sources':[{'type':'minecraft:single','resource':'ic2r_addons:gui/sprites/slots/slot_'+name,'sprite':'ic2r_addons:slots/slot_'+name} for name in icons]},indent=2)+'\n',encoding='utf-8',newline='\n')
for name in icons:assert (RES/'assets/ic2r_addons/textures/gui/sprites/slots'/('slot_'+name+'.png')).is_file(),name
print('All seven control slot sprites use the combined namespace and block atlas.')
