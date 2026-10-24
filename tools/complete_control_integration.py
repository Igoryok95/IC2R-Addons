import json
from pathlib import Path
import re
import shutil
import sys

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT/'src/main/resources'
JAVA = ROOT/'src/main/java/com/igoryok95/ic2raddons/control'
classic = Path(sys.argv[1]).resolve(strict=True)
en, ru = [json.loads((RES/'assets/ic2r_addons/lang'/(c+'.json')).read_text(encoding='utf-8')) for c in ('en_us', 'ru_ru')]
missing = {
 'InfoPanelHeatChange': ('Heat change: %s', 'Изменение нагрева: %s'),
 'InfoPanelLiquidAmount': ('Fluid: %s', 'Жидкость: %s'), 'InfoPanelLiquidCapacity': ('Capacity: %s', 'Вместимость: %s'),
 'InfoPanelLiquidFree': ('Free: %s', 'Свободно: %s'), 'InfoPanelLiquidPercentage': ('Filled: %s', 'Заполнение: %s'),
 'InfoPanelPressure': ('Pressure: %s', 'Давление: %s'), 'InfoPanelProgress': ('Progress: %s', 'Прогресс: %s'),
 'thermometer.no_power': ('The digital thermometer needs 50 EU', 'Цифровому термометру нужно 50 EU'),
}
for key, (english, russian) in missing.items():
    en['message.ic2r_addons.control.'+key], ru['message.ic2r_addons.control.'+key] = english, russian
names = {
 'energy_counter': ('Energy Counter', 'Счётчик энергии'), 'average_counter': ('Average Energy Counter', 'Счётчик средней мощности'),
 'card_counter': ('Counter Sensor Card', 'Карта датчика счётчика'), 'kit_counter': ('Counter Sensor Kit', 'Набор датчика счётчика'),
 'thermometer': ('Thermometer', 'Термометр'), 'thermometer_digital': ('Digital Thermometer', 'Цифровой термометр'),
}
for name, (english, russian) in names.items():
    prefix = 'block' if name.endswith('counter') and name in ('energy_counter','average_counter') else 'item'
    en[prefix+'.ic2r_addons.'+name], ru[prefix+'.ic2r_addons.'+name] = english, russian
for code, values in [('en_us',en),('ru_ru',ru)]:
    (RES/'assets/ic2r_addons/lang'/(code+'.json')).write_text(json.dumps(values,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')

items = JAVA/'init/ModItems.java'
text = items.read_text(encoding='utf-8')
anchor = 'public static final DeferredItem<Item> kit_energy'
if 'thermometer_digital = ITEMS' not in text:
    insert = ('public static final DeferredItem<Item> thermometer = ITEMS.registerItem("thermometer", ItemThermometer::new);\n'
              'public static final DeferredItem<Item> thermometer_digital = ITEMS.registerItem("thermometer_digital", ItemThermometer.Digital::new);\n'
              'public static final DeferredItem<Item> card_counter = ITEMS.registerItem("card_counter", ItemCardCounter::new);\n'
              'public static final DeferredItem<Item> kit_counter = ITEMS.registerItem("kit_counter", ItemKitCounter::new);\n  ')
    assert anchor in text
    text = text.replace(anchor,insert+anchor)
for name, target in [('basic_circuit','CIRCUIT'),('advanced_circuit','ADVANCED_CIRCUIT'),('machine_casing','MACHINE')]:
    text = re.sub(r'public static final DeferredItem<Item> '+name+r'\s*=\s*ITEMS\.registerSimpleItem\("'+name+r'"\);',
                  'public static final java.util.function.Supplier<Item> '+name+' = () -> ic2.core.ref.Ic2Items.'+target+';',text)
items.write_text(text,encoding='utf-8')
creative = JAVA/'ECCreativeTab.java'
text = creative.read_text(encoding='utf-8')
anchor = 'addItem(output, ModItems.kit_energy);'
if 'addItem(output, ModItems.thermometer);' not in text:
    text = text.replace(anchor, '\n'.join('addItem(output, ModItems.'+n+');' for n in ('thermometer','thermometer_digital','kit_counter','card_counter'))+'\n'+anchor)
creative.write_text(text,encoding='utf-8')

for name,target in [('thermometer','thermometer'),('thermometerDigital','thermometer_digital'),('cardCounter','card_counter'),('kitCounter','kit_counter')]:
    shutil.copy2(classic/'src/main/resources/assets/nuclearcontrol/textures/items'/(name+'.png'),RES/'assets/ic2r_addons/textures/item'/(target+'.png'))
shutil.copy2(classic/'LICENSE',RES/'LICENSES/Nuclear-Control-BSD-3-Clause.txt')

# Drop unregistered legacy model stubs; the actual imported assets are retained.
for name in ('card_energy_draconic','card_reactor_draconic','kit_draconic','kit_vanilla'):
    path=RES/'assets/ic2r_addons/models/item'/(name+'.json')
    if path.is_file(): path.unlink()
for file in (RES/'assets/ic2r_addons/models').rglob('*.json'):
    data=json.loads(file.read_text(encoding='utf-8'))
    for key,value in data.get('textures',{}).items():
        value=value.replace('ic2r_addons:items/','ic2r_addons:item/').replace('ic2r_addons:blocks/','ic2r_addons:block/')
        data['textures'][key]=value
    file.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')

recipes=RES/'data/ic2r_addons/recipe'
for name in ('basic_circuit','advanced_circuit','machine_casing'):
    path=recipes/(name+'.json')
    if path.is_file():path.unlink()
for file in recipes.rglob('*.json'):
    text=file.read_text(encoding='utf-8')
    for name,target in [('basic_circuit','circuit'),('advanced_circuit','advanced_circuit'),('machine_casing','machine')]:
        text=text.replace('ic2r_addons:'+name+'"','ic2:'+target+'"')
    file.write_text(text,encoding='utf-8')

def shaped(name, pattern, keys):
    data={'type':'minecraft:crafting_shaped','category':'misc','pattern':pattern,'key':{k:{'item':v} for k,v in keys.items()},'result':{'id':'ic2r_addons:'+name}}
    (recipes/(name+'.json')).write_text(json.dumps(data,indent=2)+'\n',encoding='utf-8')
shaped('energy_counter',[' R ','CMC',' W '],{'R':'minecraft:redstone','C':'ic2:circuit','M':'ic2:machine','W':'ic2:cable_copper_insulated'})
shaped('average_counter',[' C ','CEC',' R '],{'C':'ic2:circuit','E':'ic2r_addons:energy_counter','R':'minecraft:redstone'})
shaped('kit_ic2',['RP','CF'],{'R':'minecraft:redstone','P':'minecraft:paper','C':'ic2:circuit','F':'ic2r_addons:radio_transmitter'})
shaped('kit_counter',['RP','CF'],{'R':'minecraft:redstone','P':'minecraft:paper','C':'ic2r_addons:energy_counter','F':'ic2r_addons:radio_transmitter'})
shaped('thermometer',[' G ','GRG',' G '],{'G':'minecraft:glass_pane','R':'minecraft:redstone'})
shaped('thermometer_digital',[' C ',' BT',' R '],{'C':'ic2:circuit','B':'ic2:re_battery','T':'ic2r_addons:thermometer','R':'minecraft:redstone'})
for name in ('energy_counter','average_counter'):
    path=RES/'data/ic2r_addons/loot_table/blocks'/(name+'.json')
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(json.dumps({'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'ic2r_addons:'+name}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]},indent=2)+'\n',encoding='utf-8')
    path=RES/'data/minecraft/tags/block/mineable/pickaxe.json'
    data=json.loads(path.read_text())
    data['values']=list(dict.fromkeys(data['values']+['ic2r_addons:'+name]))
    path.write_text(json.dumps(data,indent=2)+'\n',encoding='utf-8')
print('Control content, classic assets, locales and IC2R recipes completed.')
