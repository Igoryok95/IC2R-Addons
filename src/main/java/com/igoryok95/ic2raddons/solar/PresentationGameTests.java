package com.igoryok95.ic2raddons.solar;

import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ic2r_addons")
@PrefixGameTestTemplate(false)
public final class PresentationGameTests {
  @GameTest(template = "gametest/empty3x3x3")
  public static void everyRegisteredDisplayNameHasEnglishAndRussian(GameTestHelper h)
      throws Exception {
    for (String locale : List.of("en_us", "ru_ru")) {
      try (var stream =
          PresentationGameTests.class.getResourceAsStream(
              "/assets/ic2r_addons/lang/" + locale + ".json")) {
        var names =
            JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                .getAsJsonObject();
        for (var id : BuiltInRegistries.ITEM.keySet()) {
          if (!id.getNamespace().equals("ic2r_addons")) continue;
          var item = BuiltInRegistries.ITEM.get(id);
          var key = item.getDescriptionId(new ItemStack(item));
          h.assertTrue(
              names.has(key) && !names.get(key).getAsString().isBlank(),
              locale + " missing " + id + " -> " + key);
        }
      }
    }
    h.succeed();
  }

  @GameTest(template = "gametest/empty3x3x3")
  public static void originalTierColorsSurviveInheritedArmorDefaults(GameTestHelper h) {
    String[] tiers = {"advanced", "hybrid", "ultimate", "quantum"};
    Rarity[] rarity = {Rarity.UNCOMMON, Rarity.RARE, Rarity.EPIC, Rarity.EPIC};
    for (int i = 0; i < tiers.length; i++)
      for (String suffix : List.of("_solar_panel", "_solar_helmet")) {
        var stack = new ItemStack(BuiltInRegistries.ITEM.get(IC2RAddons.id(tiers[i] + suffix)));
        h.assertTrue(stack.getRarity() == rarity[i], tiers[i] + suffix + " original ASP rarity");
      }
    for (var machine : com.igoryok95.ic2raddons.machines.AdvMachinesBlocks.BLOCKS.getEntries())
      h.assertTrue(
          new ItemStack(machine.get()).getRarity() == Rarity.UNCOMMON,
          "Advanced machine is yellow");
    for (String name :
        List.of(
            "advanced_jetpack",
            "advanced_nano",
            "advanced_nano_leggings",
            "advanced_nano_boots",
            "gravitool"))
      h.assertTrue(
          new ItemStack(BuiltInRegistries.ITEM.get(IC2RAddons.id(name))).getRarity()
              == Rarity.UNCOMMON,
          name + " GraviSuite rarity");
    for (String name :
        List.of(
            "vajra",
            "ultimate_lappack",
            "advanced_quant",
            "advanced_quantum_leggings",
            "advanced_quantum_boots"))
      h.assertTrue(
          new ItemStack(BuiltInRegistries.ITEM.get(IC2RAddons.id(name))).getRarity() == Rarity.EPIC,
          name + " GraviSuite rarity");
    h.succeed();
  }
}
