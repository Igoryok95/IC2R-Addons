"""Complete UTF-8 locales and the combined Nuclear Control resource namespace."""
import json
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
LOCALES = ROOT/'src/main/resources/assets/ic2r_addons/lang'
en, ru = [json.loads((LOCALES/(code+'.json')).read_text(encoding='utf-8')) for code in ('en_us', 'ru_ru')]
en['jade.ic2r_addons.engine.rate'] = 'Input: %s EU/t | Output: %s MJ/t'
ru['jade.ic2r_addons.engine.rate'] = 'Вход: %s EU/т | Выход: %s MJ/т'
translations = {
    'InfoPanelOutputObsidian': 'Выработка обсидиана: %s', 'InfoPanelDifference': 'Баланс: %s',
    'InfoPanelConsumptionSteam': 'Расход пара: %s', 'InfoPanelMixingChambers': 'Камеры смешивания: %s',
    'InfoPanelConsumption': 'Потребление: %s', 'InfoPanelConsumptionWater': 'Расход воды: %s',
    'InfoPanelTurbines': 'Турбины: %s', 'InfoPanelTemp': 'Температура: %s',
    'InfoPanelConsumptionCarbon': 'Расход углерода: %s', 'InfoPanelOutputSteam': 'Выработка пара: %s',
    'InfoPanelCarbon': 'Углерод: %s', 'InfoPanelHeatingChambers': 'Нагревательные камеры: %s',
}
for suffix, text in translations.items():
    ru['message.ic2r_addons.control.'+suffix] = text
en['resourcePack.ic2r_addons'] = 'IC2R Addons custom alarm sounds'
ru['resourcePack.ic2r_addons'] = 'Пользовательские звуки сигнализации IC2R Addons'
en['itemGroup.ic2r_addons.control'] = 'IC2R Addons: Nuclear Control'
ru['itemGroup.ic2r_addons.control'] = 'IC2R Addons: Ядерный контроль'
en.pop('itemGroup.ic2r_addons', None)
ru.pop('itemGroup.ic2r_addons', None)

labels = {'txt': ('text', 'Text', 'Текст'), 'Style': ('style', 'Style', 'Стиль'),
          '<': ('previous', '<', '<'), '>': ('next', '>', '>'), 'B': ('bars', 'B', 'П'),
          '-': ('decrease', '-', '-'), '+': ('increase', '+', '+'),
          'Reset': ('reset', 'Reset', 'Сброс'), 'Ticks': ('ticks', 'Ticks', 'Такты')}
for literal, (name, english, russian) in labels.items():
    en['screen.ic2r_addons.control.'+name] = english
    ru['screen.ic2r_addons.control.'+name] = russian
for amount in (1, 10, 100, 1000, 10000):
    for sign, prefix in (('+', 'add'), ('-', 'subtract')):
        key = 'screen.ic2r_addons.control.'+prefix+str(amount)
        en[key] = ru[key] = sign+str(amount)
        labels[sign+str(amount)] = (prefix+str(amount), sign+str(amount), sign+str(amount))

common = {
 'coordinates': ('X: %s, Y: %s, Z: %s', 'X: %s, Y: %s, Z: %s'),
 'pages': ('%s / %s', '%s / %s'),
 'format_error': ('Invalid text format: %s', 'Неверный формат текста: %s'),
 'counter.total': ('Total: %s EU', 'Всего: %s EU'),
 'counter.average': ('Average: %s EU/t', 'В среднем: %s EU/т'),
 'counter.current': ('Current: %s EU/t', 'Сейчас: %s EU/т'),
 'counter.tier': ('Maximum packet: %s EU', 'Максимальный пакет: %s EU'),
 'counter.reset': ('Shift + right-click to reset', 'Shift + ПКМ для сброса'),
 'counter.reset_done': ('Energy counter reset', 'Счётчик энергии сброшен'),
 'thermometer.heat': ('Reactor heat: %s / %s', 'Нагрев реактора: %s / %s'),
 'thermometer.no_target': ('No reactor found', 'Реактор не найден'),
}
for suffix, (english, russian) in common.items():
    en['message.ic2r_addons.control.'+suffix] = english
    ru['message.ic2r_addons.control.'+suffix] = russian

for file in (ROOT/'src/main/java/com/igoryok95/ic2raddons/control').rglob('*.java'):
    text = file.read_text(encoding='utf-8')
    for literal, (name, english, russian) in labels.items():
        text = text.replace('Component.literal('+json.dumps(literal)+')', 'Component.translatable("screen.ic2r_addons.control.'+name+'")')
    text = text.replace('/assets/energycontrol/', '/assets/ic2r_addons/')
    text = text.replace('tooltip.add(Component.literal(String.format("x: %d, y: %d, z: %d", target.getX(), target.getY(), target.getZ())));',
                        'tooltip.add(Component.translatable("message.ic2r_addons.control.coordinates", target.getX(), target.getY(), target.getZ()));')
    text = text.replace('(page + 1) + " / " + pageCount', 'Component.translatable("message.ic2r_addons.control.pages", page + 1, pageCount)')
    text = text.replace('isServer ? "On" : I18n.get("message.ic2r_addons.control.InfoPanelOn")', 'com.igoryok95.ic2raddons.control.utils.Translator.get("message.ic2r_addons.control.InfoPanelOn")')
    text = text.replace('isServer ? "Off" : I18n.get("message.ic2r_addons.control.InfoPanelOff")', 'com.igoryok95.ic2raddons.control.utils.Translator.get("message.ic2r_addons.control.InfoPanelOff")')
    text = text.replace('return "Format error: " + pattern;', 'return pattern("message.ic2r_addons.control.format_error").replace("%s", pattern);')
    text = text.replace('.define("wsToken", "78c2b80a-1203-43fd-a9af-75cec29f5acf")', '.define("wsToken", "")')
    file.write_text(text, encoding='utf-8')

keys = re.compile(r'\b([A-Z_]+) =.*?\.define(?:InRange)?\("([A-Za-z]+)"', re.S)
config = ROOT/'src/main/java/com/igoryok95/ic2raddons/control/config/ConfigHandler.java'
text = config.read_text(encoding='utf-8')
config_names = {
 'howlerAlarmRange': ('Alarm range', 'Дальность сирены'), 'maxAlarmRange': ('Maximum alarm range', 'Максимальная дальность сирены'),
 'allowedAlarms': ('Allowed alarm sounds', 'Разрешённые звуки сирены'), 'screenRefreshPeriod': ('Panel refresh interval', 'Интервал обновления панелей'),
 'rangeTriggerRefreshPeriod': ('Range trigger refresh interval', 'Интервал обновления порогового датчика'),
 'thermalMonitorRefreshPeriod': ('Thermal monitor refresh interval', 'Интервал обновления термомонитора'),
 'useCustomSounds': ('Custom alarm sounds', 'Пользовательские звуки сирены'), 'alarmPause': ('Pause between alarms', 'Пауза между сигналами'),
 'disableRangeCheck': ('Disable sensor range limit', 'Отключить ограничение дальности датчиков'),
 'wsEnabled': ('Enable external monitoring', 'Включить внешний мониторинг'), 'wsHost': ('Monitoring host', 'Адрес мониторинга'),
 'wsPort': ('Monitoring port', 'Порт мониторинга'), 'wsToken': ('Monitoring access token', 'Токен доступа к мониторингу'),
 'wsRefreshRate': ('External monitoring interval', 'Интервал внешнего мониторинга'), 'wsServerID': ('Monitoring server ID', 'Идентификатор сервера мониторинга'),
 'serverLanguage': ('Dedicated server language', 'Язык выделенного сервера'),
}
for name, (english, russian) in config_names.items():
    key = 'config.ic2r_addons.control.'+name
    text = re.sub(r'\.define(InRange)?\("'+name+'"', lambda m: '.translation("'+key+'").define'+(m.group(1) or '')+'("'+name+'"', text)
    en[key], ru[key] = english, russian
config.write_text(text, encoding='utf-8')

for locale, values in [('en_us', en), ('ru_ru', ru)]:
    (LOCALES/(locale+'.json')).write_text(json.dumps(values, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')
print(json.dumps({'en_keys':len(en), 'ru_keys':len(ru), 'missing_ru':list(en.keys()-ru.keys())}))
