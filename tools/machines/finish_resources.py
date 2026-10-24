"""Build the Advanced Machines localized resource definitions (UTF-8)."""
from pathlib import Path
import json

root = Path(__file__).resolve().parents[2] / "src/main/resources"
assets = root / "assets/ic2r_addons"
names = {
    "rotary_macerator": ("Rotary Macerator", "Роторный дробитель"),
    "singularity_compressor": ("Singularity Compressor", "Сингулярный компрессор"),
    "centrifuge_extractor": ("Centrifuge Extractor", "Центрифужный экстрактор"),
    "compacting_recycler": ("Compacting Recycler", "Уплотняющий утилизатор"),
    "liquescent_extruder": ("Liquescent Extruder", "Разогреваемый экструдер"),
    "impellerized_roller": ("Impellerized Roller", "Усиленный прокатный стан"),
    "water_jet_cutter": ("Water-Jet Cutter", "Гидроабразивный резак"),
    "thermal_washer": ("Thermal Washer", "Термопромыватель"),
}
extra = {
    "item.ic2r_addons.sharp_plate": ("Sharpened Iron Plate", "Заточенная железная пластина"),
    "ic2r_addons.machines.gui.temp": ("Heat:", "Нагрев:"),
    "ic2r_addons.machines.gui.speed": ("Speed:", "Скор.:"),
    "ic2r_addons.machines.gui.speed.rot_unit": ("RPM", "об/мин"),
    "ic2r_addons.machines.gui.speed.vel_unit": ("cm/s", "см/с"),
    "ic2r_addons.machines.gui.pressure": ("Pressure:", "Давл.:"),
    "ic2r_addons.machines.gui.pressure.unit": ("Gibbl", "Гиббл"),
    "ic2r_addons.machines.gui.water_containers": ("Water containers", "Ёмкости с водой"),
    "tooltip.ic2r_addons.machines.power": ("Tier 2 · 10,000 EU · %s EU/t while processing", "Энергоуровень 2 · 10 000 EU · %s EU/т при работе"),
    "tooltip.ic2r_addons.machines.warmup": ("Speeds up while working. Redstone keeps it warm for %s EU/t.", "Ускоряется при работе. Редстоун поддерживает разгон за %s EU/т."),
    "tooltip.ic2r_addons.machines.water": ("Accepts water from pipes or containers; 8,000 mB tank.", "Принимает воду из труб и ёмкостей; бак на 8000 мБ."),
    "tooltip.ic2r_addons.machines.thermal_shock": ("Do not refill an empty Thermal Washer at 50%% heat or above: thermal explosion!", "Не заполняйте пустой термопромыватель при нагреве от 50%%: произойдёт тепловой взрыв!"),
    "tooltip.ic2r_addons.machines.recycler": ("Recycles items with IC2R's 1-in-8 scrap chance and blacklist. Compacts 9 scrap into 1 scrap box with no random loss.", "Перерабатывает предметы с шансом IC2R 1 к 8 и его чёрным списком. Упаковывает 9 утилей в 1 коробку без случайных потерь."),
    "jei.ic2r_addons.machines.compacting": ("9 scrap = 1 scrap box", "9 утилей = 1 коробка"),
    "jade.ic2r_addons.advanced_machines": ("Advanced Machines", "Улучшенные машины"),
    "jade.ic2r_addons.machines.heat": ("Warm-up: %s%%", "Разгон: %s%%"),
    "jade.ic2r_addons.machines.progress": ("Progress: %s%%", "Прогресс: %s%%"),
    "jade.ic2r_addons.machines.eu": ("Consumption: %s EU/t", "Потребление: %s EU/т"),
    "jade.ic2r_addons.machines.water": ("Water: %s / %s mB", "Вода: %s / %s мБ"),
    "fml.menu.mods.info.description.ic2r_addons": (
        "IC2R Addons for Minecraft 1.21.1 NeoForge: all eight Advanced Machines, four classic solar tiers and solar helmets, Molecular Transformer, powered armor, flight and electric tools. Optional BuildCraft integration adds compatible wrenches and a native EU-to-MJ engine; Jade shows live energy and machine diagnostics.",
        "IC2R Addons для Minecraft 1.21.1 NeoForge: все восемь машин Advanced Machines, четыре классические ступени солнечных панелей и шлемов, молекулярный преобразователь, улучшенная броня, полёт и электроинструменты. Необязательная интеграция с BuildCraft добавляет совместимые ключи и электродвигатель EU–MJ; Jade показывает состояние и энергопотребление машин."),
}
for language, index in [("en_us", 0), ("ru_ru", 1)]:
    file = assets / "lang" / (language + ".json")
    data = json.loads(file.read_text(encoding="utf-8"))
    for key, pair in names.items():
        data["block.ic2r_addons." + key] = pair[index]
        data["container.ic2r_addons." + key] = pair[index]
    data.update({key: pair[index] for key, pair in extra.items()})
    file.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8", newline="\n")

for name in ["water_jet_cutter", "thermal_washer"]:
    file = assets / "guidef" / (name + ".xml")
    text = file.read_text(encoding="utf-8")
    if 'name="fluid"' not in text:
        text = text.replace('height="166"', 'height="190"').replace('<playerInventory x="7" y="83"/>',
            '<slot name="fluid" x="7" y="79"/>\n    <slot name="cell" x="25" y="79"/>\n'
            '    <text x="48" y="84">{ic2r_addons.machines.gui.water_containers}</text>\n'
            '    <playerInventory x="7" y="107"/>')
    file.write_text(text, encoding="utf-8", newline="\n")

# Native slot widgets avoid the legacy GlTexture/Image upload path and match IC2R GUIs.
for name in names:
    file = assets / "guidef" / (name + ".xml")
    text = file.read_text(encoding="utf-8")
    text = "\n".join(line for line in text.splitlines() if "<image " not in line)
    text = text.replace(' style="plain"', '')
    if name == "liquescent_extruder":
        text = text.replace('x="74" y="34" width="29" height="19"', 'x="77" y="37" width="32" height="16"')
        text = text.replace('x="74" y="34" style="progressmetalformer"', 'x="82" y="37" style="progressarrow"')
    if name == "impellerized_roller":
        text = text.replace('x="81" y="34" width="29" height="19"', 'x="82" y="37" width="32" height="16"')
        text = text.replace('x="81" y="34" style="progressmetalformer"', 'x="87" y="37" style="progressarrow"')
    file.write_text(text + "\n", encoding="utf-8", newline="\n")

for tag in ["mineable/pickaxe", "needs_stone_tool"]:
    file = root / "data/minecraft/tags/block" / (tag + ".json")
    file.parent.mkdir(parents=True, exist_ok=True)
    data = json.loads(file.read_text()) if file.exists() else {"replace": False, "values": []}
    data["values"] = list(dict.fromkeys(data["values"] + ["ic2r_addons:" + name for name in names]))
    file.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8", newline="\n")
