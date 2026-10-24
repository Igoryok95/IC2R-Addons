import json
from pathlib import Path
import re

ROOT=Path(__file__).resolve().parents[1]
path=ROOT/'src/main/java/com/igoryok95/ic2raddons/control/gui/GuiTimer.java'
text=path.read_text(encoding='utf-8')
names={'Ticks':'ticks','Time':'time','Stop':'stop','Start':'start','No Redstone':'no_redstone','Redstone':'redstone',
       '+1':'add1','+10':'add10','+100':'add100','+1000':'add1000','+10000':'add10000',
       '+1s':'add_second','+30s':'add_30seconds','+1m':'add_minute','+30m':'add_30minutes','+1h':'add_hour'}
for kind in ('literal','translatable'):
    regex=r'Component\.'+kind+r'\(\s*([^?;]+?)\?\s*"([^"]+)"\s*:\s*"([^"]+)"\s*\)'
    def replace(match):
        a,b=match.group(2),match.group(3)
        if a not in names or b not in names:return match.group()
        return 'Component.translatable('+match.group(1)+'? "screen.ic2r_addons.control.'+names[a]+'" : "screen.ic2r_addons.control.'+names[b]+'")'
    text=re.sub(regex,replace,text)
path.write_text(text,encoding='utf-8',newline='\n')
translations={'time':('Time','Время'),'stop':('Stop','Стоп'),'start':('Start','Старт'),
 'no_redstone':('Ignore redstone','Без сигнала'),'redstone':('Redstone','Сигнал'),
 'add_second':('+1s','+1с'),'add_30seconds':('+30s','+30с'),'add_minute':('+1m','+1м'),
 'add_30minutes':('+30m','+30м'),'add_hour':('+1h','+1ч')}
for code,index in [('en_us',0),('ru_ru',1)]:
    path=ROOT/'src/main/resources/assets/ic2r_addons/lang'/(code+'.json')
    data=json.loads(path.read_text(encoding='utf-8'))
    for suffix,values in translations.items():data['screen.ic2r_addons.control.'+suffix]=values[index]
    data['tooltip.ic2r_addons.control.assembler_energy']='%s / %s FE'
    path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8',newline='\n')
path=ROOT/'src/main/java/com/igoryok95/ic2raddons/control/gui/GuiKitAssembler.java'
text=path.read_text(encoding='utf-8')
text=re.sub(r'Component\.literal\(\s*String\.format\(\s*"%d FE/%d FE", \(int\) container.te.getEnergy\(\), TileEntityKitAssembler.CAPACITY\)\)',
            'Component.translatable("tooltip.ic2r_addons.control.assembler_energy", (int) container.te.getEnergy(), TileEntityKitAssembler.CAPACITY)',text)
path.write_text(text,encoding='utf-8',newline='\n')
print('Timer button states and assembler energy tooltip use EN/RU locales.')
