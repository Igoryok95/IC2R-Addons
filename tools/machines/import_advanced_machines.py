"""Import attributed Advanced Machines resources and leaf classes from the reference checkout.

Usage: python tools/machines/import_advanced_machines.py <reference checkout>
The modern 32x32 texture overlay is selected; PNGs are copied without modification.
"""
from pathlib import Path
import json
import shutil
import sys

source = Path(sys.argv[1]) / "src/main"
root = Path(__file__).resolve().parents[2]
target = root / "src/main"
old = source / "resources/assets/advanced_machines"
modern = source / "resources/modern_overlay/assets/advanced_machines"
assets = target / "resources/assets/ic2r_addons"
names = ["rotary_macerator", "singularity_compressor", "centrifuge_extractor",
         "compacting_recycler", "liquescent_extruder", "impellerized_roller",
         "water_jet_cutter", "thermal_washer"]

def write(path, text):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8", newline="\n")

def migrated(text):
    return text.replace("advanced_machines.gui.", "ic2r_addons.machines.gui.").replace(
        "advanced_machines:", "ic2r_addons:")

for name in names + ["sharp_plate"]:
    for sub in ["models/item", "models/block", "blockstates", "guidef"]:
        extension = ".xml" if sub == "guidef" else ".json"
        for variant in [name, name + "_active"] if sub == "models/block" else [name]:
            rel = Path(sub) / (variant + extension)
            file = modern / rel if (modern / rel).exists() else old / rel
            if file.exists():
                text = migrated(file.read_text(encoding="utf-8"))
                # The recycler has its own JEI category, available through its catalyst.
                if name == "compacting_recycler" and sub == "guidef":
                    text = "\n".join(line for line in text.splitlines() if '<button ' not in line)
                # The base IC2 overlay is supplied by the dependency.
                text = text.replace('src="textures/gui/', 'src="ic2:textures/gui/')
                write(assets / rel, text)
    recipe = json.loads((source / "resources/data/advanced_machines/recipes" / (name + ".json")).read_text())
    recipe["result"]["id"] = recipe["result"].pop("item").replace("advanced_machines:", "ic2r_addons:")
    for ingredient in recipe["key"].values():
        ingredient["item"] = ingredient["item"].replace("advanced_machines:", "ic2r_addons:")
    write(target / "resources/data/ic2r_addons/recipe/shaped" / (name + ".json"), json.dumps(recipe, indent=2) + "\n")

for directory in [modern / "textures/block", old / "textures/item"]:
    for file in directory.rglob("*"):
        if file.is_file():
            rel = file.relative_to(directory)
            dest = assets / "textures" / ("gui/overlay" if directory.name == "overlay" else directory.name) / rel
            dest.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(file, dest)

# Leaf types retain the original machine-specific speed, sound and display behavior.
for name in names:
    if name in ["compacting_recycler", "thermal_washer"]:
        continue
    cls = "TileEntity" + "".join(part.title() for part in name.split("_"))
    text = (source / "java/com/chocohead/AdvMachines/te" / (cls + ".java")).read_text()
    text = text.replace("com.chocohead.AdvMachines.te", "com.igoryok95.ic2raddons.machines.te")
    text = text.replace("com.chocohead.AdvMachines.AdvMachinesBlocks", "com.igoryok95.ic2raddons.machines.AdvMachinesBlocks")
    text = text.replace("net.minecraftforge.api.distmarker", "net.neoforged.api.distmarker")
    write(target / "java/com/igoryok95/ic2raddons/machines/te" / (cls + ".java"), text)

# Reuse the established registration convention and place machines in the combined tab.
text = (source / "java/com/chocohead/AdvMachines/AdvMachinesBlocks.java").read_text()
text = text.replace("package com.chocohead.AdvMachines;", "package com.igoryok95.ic2raddons.machines;\nimport com.igoryok95.ic2raddons.solar.IC2RAddons;\nimport java.util.function.Supplier;\nimport net.neoforged.neoforge.registries.DeferredRegister;")
text = text.replace("com.chocohead.AdvMachines.te", "com.igoryok95.ic2raddons/machines/te".replace("/", "."))
text = text.replace("import com.chocohead.AdvMachines.item.ItemSharpPlate;", "")
text = "\n".join(line for line in text.splitlines() if "net.minecraftforge.registries" not in line)
text = text.replace("AdvancedMachines.MODID", "IC2RAddons.MODID").replace("RegistryObject<", "Supplier<")
text = text.replace("ForgeRegistries.BLOCKS", "Registries.BLOCK").replace("ForgeRegistries.ITEMS", "Registries.ITEM").replace("ForgeRegistries.BLOCK_ENTITY_TYPES", "Registries.BLOCK_ENTITY_TYPE")
text = text.replace('ITEMS.register("sharp_plate", ItemSharpPlate::new)', 'ITEMS.register("sharp_plate", () -> new Item(new Item.Properties()))')
start = text.index("\tpublic static final DeferredRegister<CreativeModeTab>")
end = text.index(";", start) + 1
text = text[:start] + text[end:]
start = text.index("\t// --- Creative tab ---")
end = text.index("\tprivate AdvMachinesBlocks()", start)
all_items = ", ".join("ITEM_" + name.upper() for name in names) + ", SHARP_PLATE"
text = text[:start] + "\tpublic static final java.util.List<Supplier<Item>> ALL_ITEMS = java.util.List.of(" + all_items + ");\n\n" + text[end:]
text = text.replace("net.minecraftforge.eventbus.api.IEventBus", "net.neoforged.bus.api.IEventBus").replace("\n\t\tTABS.register(bus);", "")
write(target / "java/com/igoryok95/ic2raddons/machines/AdvMachinesBlocks.java", text)
print("Imported 8 machines, 9 crafting recipes and original modern texture resources")
