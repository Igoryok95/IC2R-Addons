package com.igoryok95.ic2raddons.solar;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class ResourceContractTest {
  private static final Path ROOT = Path.of(System.getProperty("ic2r_addons.projectDir"));
  private static final Path RES = ROOT.resolve("src/main/resources");

  private static JsonObject read(Path p) throws Exception {
    return JsonParser.parseString(Files.readString(p, StandardCharsets.UTF_8)).getAsJsonObject();
  }

  @Test
  void vajraHasNative32PixelDetailAndTransparentOpenings() throws Exception {
    var sprite =
        javax.imageio.ImageIO.read(
            RES.resolve("assets/ic2r_addons/textures/item/vajra.png").toFile());
    assertEquals(32, sprite.getWidth());
    assertEquals(32, sprite.getHeight());
    int detailedCells = 0;
    for (int y = 0; y < 32; y += 2)
      for (int x = 0; x < 32; x += 2) {
        var colors = new HashSet<Integer>();
        for (int dy = 0; dy < 2; dy++)
          for (int dx = 0; dx < 2; dx++) colors.add(sprite.getRGB(x + dx, y + dy));
        if (colors.size() > 1) detailedCells++;
      }
    assertTrue(
        detailedCells >= 64,
        "A doubled 16px sprite has no independent detail within its 2x2 cells");
    var outside = new boolean[32][32];
    var queue = new ArrayDeque<int[]>();
    for (int i = 0; i < 32; i++) {
      queue.add(new int[] {i, 0});
      queue.add(new int[] {i, 31});
      queue.add(new int[] {0, i});
      queue.add(new int[] {31, i});
    }
    while (!queue.isEmpty()) {
      var p = queue.remove();
      int x = p[0], y = p[1];
      if (x < 0
          || x >= 32
          || y < 0
          || y >= 32
          || outside[x][y]
          || (sprite.getRGB(x, y) >>> 24) != 0) continue;
      outside[x][y] = true;
      queue.add(new int[] {x + 1, y});
      queue.add(new int[] {x - 1, y});
      queue.add(new int[] {x, y + 1});
      queue.add(new int[] {x, y - 1});
    }
    int holes = 0;
    for (int x = 0; x < 32; x++)
      for (int y = 0; y < 32; y++) {
        int alpha = sprite.getRGB(x, y) >>> 24;
        assertTrue(alpha == 0 || alpha == 255, "Crisp cutout alpha");
        if (alpha == 0 && !outside[x][y]) holes++;
      }
    assertTrue(holes >= 30, "The open frame must contain real transparent holes");
  }

  @Test
  void electricEngineUsesStandardBlockHandTransforms() throws Exception {
    var model = read(RES.resolve("assets/ic2r_addons/models/item/electric_engine.json"));
    var display = model.getAsJsonObject("display");
    assertEquals("builtin/entity", model.get("parent").getAsString());
    for (var hand : List.of("firstperson_righthand", "firstperson_lefthand"))
      for (var value : display.getAsJsonObject(hand).getAsJsonArray("scale"))
        assertEquals(.4, value.getAsDouble(), .000001, "standard BC/Minecraft block scale");
    for (var value : display.getAsJsonObject("thirdperson_righthand").getAsJsonArray("scale"))
      assertEquals(.375, value.getAsDouble(), .000001, "held engine must not render at world size");
  }

  @Test
  void allStaticPlayerFacingKeysAndPackDescriptionsAreLocalized() throws Exception {
    var en = read(RES.resolve("assets/ic2r_addons/lang/en_us.json"));
    var ru = read(RES.resolve("assets/ic2r_addons/lang/ru_ru.json"));
    var quoted = Pattern.compile("\"([A-Za-z0-9_.]+)\"");
    try (var files = Files.walk(ROOT.resolve("src/main/java"))) {
      for (Path file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
        var matcher = quoted.matcher(Files.readString(file, StandardCharsets.UTF_8));
        while (matcher.find()) {
          String key = matcher.group(1);
          if (key.endsWith(".")) continue;
          if (key.matches(
                  "^(tooltip|message|screen|hud|itemGroup|key|config|jade|status|pack)\\.ic2r_addons\\..*")
              || key.equals("ic2r_addons.on")
              || key.equals("ic2r_addons.off")) {
            assertTrue(en.has(key) && ru.has(key), file + " -> " + key);
          }
        }
      }
    }
    String packKey =
        read(RES.resolve("pack.mcmeta"))
            .getAsJsonObject("pack")
            .getAsJsonObject("description")
            .get("translate")
            .getAsString();
    assertTrue(en.has(packKey) && ru.has(packKey));
    assertTrue(
        en.has("fml.menu.mods.info.description.ic2r_addons")
            && ru.has("fml.menu.mods.info.description.ic2r_addons"));
  }

  @Test
  void onlyTheCombinedNamespaceIsPublished() throws Exception {
    for (String directory : List.of("assets", "data")) {
      assertFalse(Files.exists(RES.resolve(directory + "/advanced_solar_panels")));
      assertFalse(Files.exists(RES.resolve(directory + "/gravisuite")));
      assertTrue(Files.isDirectory(RES.resolve(directory + "/ic2r_addons")));
    }
    String metadata = Files.readString(RES.resolve("META-INF/neoforge.mods.toml"));
    assertTrue(metadata.contains("modId=\"ic2r_addons\""));
    try (var paths = Files.walk(RES)) {
      for (Path p :
          paths
              .filter(Files::isRegularFile)
              .filter(p -> p.toString().endsWith(".json") || p.toString().endsWith(".xml"))
              .toList()) {
        String text = Files.readString(p);
        assertFalse(text.contains("advanced_solar_panels:"), p.toString());
        assertFalse(text.contains("\"gravisuite:"), p.toString());
      }
    }
  }

  @Test
  void russianMatchesEnglishKeysAndArguments() throws Exception {
    JsonObject en = read(RES.resolve("assets/ic2r_addons/lang/en_us.json")),
        ru = read(RES.resolve("assets/ic2r_addons/lang/ru_ru.json"));
    assertEquals(en.keySet(), ru.keySet());
    for (String k : en.keySet()) {
      String text = ru.get(k).getAsString();
      assertFalse(text.isBlank(), k);
      assertTrue(text.codePoints().noneMatch(c -> c == 0xfffd), k);
      assertEquals(formats(en.get(k).getAsString()), formats(text), k);
    }
  }

  private static List<String> formats(String s) {
    var m = Pattern.compile("%(?:[0-9]+\\$)?[sd%]").matcher(s);
    List<String> r = new ArrayList<>();
    while (m.find()) r.add(m.group());
    return r;
  }

  @Test
  void allModelTexturesResolve() throws Exception {
    try (var paths = Files.walk(RES.resolve("assets/ic2r_addons/models"))) {
      for (Path p : paths.filter(x -> x.toString().endsWith(".json")).toList()) {
        JsonObject model = read(p);
        if (!model.has("textures")) continue;
        for (var e : model.getAsJsonObject("textures").entrySet()) {
          String id = e.getValue().getAsString();
          if (!id.startsWith("ic2r_addons:")) continue;
          assertTrue(
              Files.isRegularFile(
                  RES.resolve("assets/ic2r_addons/textures/" + id.split(":", 2)[1] + ".png")),
              p + " -> " + id);
        }
      }
    }
  }

  @Test
  void recipesHaveNoLegacyForgeTags() throws Exception {
    try (var paths = Files.walk(RES.resolve("data/ic2r_addons/recipe"))) {
      for (Path p : paths.filter(x -> x.toString().endsWith(".json")).toList()) {
        JsonObject recipe = read(p);
        assertTrue(recipe.has("type"), p.toString());
        assertFalse(recipe.toString().contains("\"forge:"), p.toString());
      }
    }
  }

  @Test
  void retiredPanelsHaveNoRecipesOrAssets() throws Exception {
    for (String name : List.of("spectral", "singular", "light_absorbing", "photonic")) {
      assertFalse(
          Files.exists(
              RES.resolve("data/ic2r_addons/recipe/shaped/" + name + "_solar_panel.json")));
      assertFalse(
          Files.exists(
              RES.resolve("assets/ic2r_addons/models/block/" + name + "_solar_panel.json")));
      assertFalse(
          Files.exists(
              RES.resolve("assets/ic2r_addons/blockstates/" + name + "_solar_panel.json")));
      assertFalse(
          read(RES.resolve("assets/ic2r_addons/lang/en_us.json"))
              .has("block.ic2r_addons." + name + "_solar_panel"));
    }
  }

  @Test
  void quantumHelmetRetainsExistingPixelSilhouetteAndArmorUvs() throws Exception {
    Path textures = RES.resolve("assets/ic2r_addons/textures");
    var original =
        javax.imageio.ImageIO.read(textures.resolve("item/ultimatesolarhelmet.png").toFile());
    var combined =
        new java.awt.image.BufferedImage(32, 32, java.awt.image.BufferedImage.TYPE_INT_ARGB);
    var g = combined.createGraphics();
    g.drawImage(original, 0, 0, null);
    g.dispose();
    var quantum =
        javax.imageio.ImageIO.read(textures.resolve("item/quantumsolarhelmet.png").toFile());
    assertEquals(32, quantum.getWidth());
    assertEquals(32, quantum.getHeight());
    int changed = 0;
    for (int y = 0; y < 32; y++)
      for (int x = 0; x < 32; x++) {
        assertEquals(combined.getRGB(x, y) >>> 24, quantum.getRGB(x, y) >>> 24, "same outline");
        if (combined.getRGB(x, y) != quantum.getRGB(x, y)) changed++;
      }
    assertTrue(changed > 0, "quantum palette must distinguish the new tier");
    for (String suffix : List.of("", "_overlay")) {
      var oldArmor =
          javax.imageio.ImageIO.read(
              textures
                  .resolve("models/armor/ultimate_solar_helmet_layer_1" + suffix + ".png")
                  .toFile());
      var armor =
          javax.imageio.ImageIO.read(
              textures
                  .resolve("models/armor/quantum_solar_helmet_layer_1" + suffix + ".png")
                  .toFile());
      assertEquals(128, armor.getWidth());
      assertEquals(64, armor.getHeight());
      for (int y = 0; y < 64; y++)
        for (int x = 0; x < 128; x++)
          assertEquals(
              oldArmor.getRGB(x, y) >>> 24, armor.getRGB(x, y) >>> 24, "same armor UV coverage");
    }
  }
}
