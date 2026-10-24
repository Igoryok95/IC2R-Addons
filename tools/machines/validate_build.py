"""Validate the 1.3.0 JAR and record completed local server / rendered-client runs."""
from pathlib import Path
from datetime import datetime, timezone
import hashlib
import json
import re
import tomllib
import xml.etree.ElementTree as ET
import zipfile

root = Path(__file__).resolve().parents[2]
build = root / "build"
names = ["rotary_macerator", "singularity_compressor", "centrifuge_extractor",
         "compacting_recycler", "liquescent_extruder", "impellerized_roller",
         "water_jet_cutter", "thermal_washer"]
counts = {"tests": 0, "failures": 0, "errors": 0}
for file in (build / "test-results/test").glob("TEST-*.xml"):
    suite = ET.parse(file).getroot()
    for key in counts:
        counts[key] += int(suite.attrib[key])
assert counts == {"tests": 15, "failures": 0, "errors": 0}, counts
for file, count in [("machines-base.log", 65), ("machines-bc-jade.log", 73)]:
    text = (build / file).read_text(encoding="utf-8")
    assert f"All {count} required tests passed" in text and "BUILD SUCCESSFUL" in text, file
clients = {}
for language in ["en", "ru"]:
    text = (build / ("machines-client-" + language + ".log")).read_text(encoding="utf-8")
    assert "BUILD SUCCESSFUL" in text and "MACHINE_SMOKE_PASS" in text
    assert "MACHINE_SMOKE_TEXTURES_PASS machines=8 states=16 faces=96" in text
    assert "IC2R_ADDONS_JEI_SEARCH_PASS items=57 queries=4" in text
    values = re.findall(r"MACHINE_SMOKE_PROCESSING index=(\d+) progress=(\d+) maximum=(\d+)", text)
    assert len(values) == 8 and {int(v[0]) for v in values} == set(range(8))
    assert all(0 < int(progress) < int(maximum) for _, progress, maximum in values)
    clients[language] = {"menus": 8, "model_faces": 96, "partial_processing": True,
                         "jei_items": 57, "jei_queries": 4}

jar = build / "libs/ic2r-addons-neoforge-1.21.1-1.3.0.jar"
with zipfile.ZipFile(jar) as archive:
    assert archive.testzip() is None
    metadata = tomllib.loads(archive.read("META-INF/neoforge.mods.toml").decode("utf-8"))
    assert metadata["mods"][0]["modId"] == "ic2r_addons"
    assert metadata["mods"][0]["version"] == "1.3.0"
    en = json.loads(archive.read("assets/ic2r_addons/lang/en_us.json"))
    ru = json.loads(archive.read("assets/ic2r_addons/lang/ru_ru.json"))
    assert en.keys() == ru.keys()
    assert not any(chr(0xfffd) in value for value in ru.values())
    for name in names:
        assert "block.ic2r_addons." + name in ru
        assert "ic2r_addons.machines.gui." in archive.read("assets/ic2r_addons/guidef/" + name + ".xml").decode()
        recipe = json.loads(archive.read("data/ic2r_addons/recipe/shaped/" + name + ".json"))
        assert recipe["result"]["id"] == "ic2r_addons:" + name
digest = hashlib.sha256(jar.read_bytes()).hexdigest()
Path(str(jar) + ".sha256").write_text(digest + "  " + jar.name + "\n", encoding="utf-8")
manifest = {
    "version": "1.3.0", "verified_at_utc": datetime.now(timezone.utc).isoformat(),
    "minecraft": "1.21.1", "neoforge": "21.1.264", "ic2r": "21.1.68",
    "advanced_machines": names, "junit": counts,
    "gametests": {"base": 65, "buildcraft_jade": 73}, "clients": clients,
    "gui_regression": "Actual IC2R gauge fill/background bounds checked against all machine slots; extruder/roller replace oversized Metal Former indicator with fitting arrows. All eight GUIs rendered with real partial processing in EN/RU.",
    "legacy_gui_note": "Legacy Image overlays hit the IC2R GlTexture native upload path during initial smoke. Native slot widgets replace those overlays; completed client runs pass.",
    "locale_keys": len(ru), "textures": "Original modern Advanced Machines 32x32 PNGs, unchanged",
    "artifact": {"file": jar.name, "bytes": jar.stat().st_size, "sha256": digest},
    "installed_modpack_changed": False, "multiplayer_server_changed": False,
}
file = root / "docs/validation/1.3.0.json"
file.write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8", newline="\n")
print(json.dumps(manifest, ensure_ascii=False, indent=2))
