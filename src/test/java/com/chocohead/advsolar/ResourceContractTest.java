package com.chocohead.advsolar;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class ResourceContractTest {
  private static final Path ROOT = Path.of(System.getProperty("asp.projectDir"));
  private static final Path RES = ROOT.resolve("src/main/resources");

  private static JsonObject read(Path p) throws Exception {
    return JsonParser.parseString(Files.readString(p, StandardCharsets.UTF_8)).getAsJsonObject();
  }

  @Test
  void onlyTheCombinedNamespaceIsPublished() throws Exception {
    for (String directory : List.of("assets", "data")) {
      assertFalse(Files.exists(RES.resolve(directory + "/advanced_solar_panels")));
      assertFalse(Files.exists(RES.resolve(directory + "/gravisuite")));
      assertTrue(Files.isDirectory(RES.resolve(directory + "/solar_gravisuite")));
    }
    String metadata = Files.readString(RES.resolve("META-INF/neoforge.mods.toml"));
    assertTrue(metadata.contains("modId=\"solar_gravisuite\""));
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
    JsonObject en = read(RES.resolve("assets/solar_gravisuite/lang/en_us.json")),
        ru = read(RES.resolve("assets/solar_gravisuite/lang/ru_ru.json"));
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
    try (var paths = Files.walk(RES.resolve("assets/solar_gravisuite/models"))) {
      for (Path p : paths.filter(x -> x.toString().endsWith(".json")).toList()) {
        JsonObject model = read(p);
        if (!model.has("textures")) continue;
        for (var e : model.getAsJsonObject("textures").entrySet()) {
          String id = e.getValue().getAsString();
          if (!id.startsWith("solar_gravisuite:")) continue;
          assertTrue(
              Files.isRegularFile(
                  RES.resolve("assets/solar_gravisuite/textures/" + id.split(":", 2)[1] + ".png")),
              p + " -> " + id);
        }
      }
    }
  }

  @Test
  void recipesHaveNoLegacyForgeTags() throws Exception {
    try (var paths = Files.walk(RES.resolve("data/solar_gravisuite/recipe"))) {
      for (Path p : paths.filter(x -> x.toString().endsWith(".json")).toList()) {
        JsonObject recipe = read(p);
        assertTrue(recipe.has("type"), p.toString());
        assertFalse(recipe.toString().contains("forge:"), p.toString());
      }
    }
  }

  @Test
  void extendedPanelsUseOneAtlasTilePerFace() throws Exception {
    for (String name : List.of("spectral", "singular", "light_absorbing", "photonic")) {
      var image =
          javax.imageio.ImageIO.read(
              RES.resolve("assets/solar_gravisuite/textures/block/" + name + "_solar_top.png")
                  .toFile());
      assertEquals(96, image.getWidth());
      assertEquals(16, image.getHeight());
      var model =
          read(RES.resolve("assets/solar_gravisuite/models/block/" + name + "_solar_panel.json"));
      var faces =
          model.getAsJsonArray("elements").get(0).getAsJsonObject().getAsJsonObject("faces");
      assertEquals(6, faces.size());
      for (var entry : faces.entrySet()) {
        var uv = entry.getValue().getAsJsonObject().getAsJsonArray("uv");
        assertEquals(
            16.0 / 6,
            uv.get(2).getAsDouble() - uv.get(0).getAsDouble(),
            0.000001,
            name + " " + entry.getKey());
      }
    }
  }
}
