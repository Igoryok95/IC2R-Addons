package reforged.mods.gravisuite;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class ResourceContractTest {
  private static final Path ROOT = Path.of(System.getProperty("solar_gravisuite.projectDir"));
  private static final Path RESOURCES = ROOT.resolve("src/main/resources");
  private static final Pattern ARGUMENT =
      Pattern.compile("%(?:\\d+\\$)?[-#+ 0,(<]*\\d*(?:\\.\\d+)?[a-zA-Z%]");

  @Test
  void upgradedLegsAndBootsUseBlueDetailsAndValidArmorLayers() throws IOException {
    for (String part :
        new String[] {
          "advanced_nano_leggings",
          "advanced_nano_boots",
          "advanced_quantum_leggings",
          "advanced_quantum_boots"
        }) {
      var image =
          ImageIO.read(
              RESOURCES.resolve("assets/solar_gravisuite/textures/item/" + part + ".png").toFile());
      int blue = 0;
      for (int y = 0; y < image.getHeight(); y++)
        for (int x = 0; x < image.getWidth(); x++) {
          int pixel = image.getRGB(x, y);
          if ((pixel >>> 24) == 0) continue;
          int r = pixel >> 16 & 255, g = pixel >> 8 & 255, b = pixel & 255;
          assertFalse(g > r + 25 && g > b + 25, "green indicator remains: " + part);
          if (b > r + 25 && b > g) blue++;
        }
      assertTrue(blue > 0, "missing blue energy details: " + part);
    }
    for (String material : new String[] {"advanced_nano", "advanced_quantum"})
      for (int layer = 1; layer <= 2; layer++) {
        var path =
            RESOURCES.resolve(
                "assets/solar_gravisuite/textures/models/armor/"
                    + material
                    + "_layer_"
                    + layer
                    + ".png");
        var image = ImageIO.read(path.toFile());
        assertEquals(64, image.getWidth(), path.toString());
        assertEquals(32, image.getHeight(), path.toString());
      }
  }

  @Test
  void russianCoversEnglishAndPreservesArguments() throws IOException {
    JsonObject english = json("assets/solar_gravisuite/lang/en_us.json"),
        russian = json("assets/solar_gravisuite/lang/ru_ru.json");
    assertEquals(english.keySet(), russian.keySet());
    for (String key : english.keySet()) {
      String translated = russian.get(key).getAsString();
      assertFalse(translated.isBlank(), key);
      assertTrue(translated.codePoints().noneMatch(c -> c == 0xfffd), key);
      assertEquals(arguments(english.get(key).getAsString()), arguments(translated), key);
    }
  }

  @Test
  void everyRecipeOutputHasNameModelAndTexture() throws IOException {
    JsonObject english = json("assets/solar_gravisuite/lang/en_us.json");
    try (var files = Files.walk(RESOURCES.resolve("data/solar_gravisuite/recipe"))) {
      for (Path file : files.filter(Files::isRegularFile).toList()) {
        JsonObject recipe =
            JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8))
                .getAsJsonObject();
        if (!recipe.get("type").getAsString().equals("ic2:shaped")) continue;
        String item = recipe.getAsJsonObject("result").get("item").getAsString();
        assertTrue(
            english.has("item." + item.replace(':', '.'))
                || english.has("block." + item.replace(':', '.')),
            item);
        JsonObject model =
            json(
                "assets/"
                    + item.replace(':', '/')
                        .replace("solar_gravisuite/", "solar_gravisuite/models/item/")
                    + ".json");
        if (!model.has("textures")) {
          assertTrue(model.has("parent") || model.has("loader"), item);
          if (model.has("parent")
              && model.get("parent").getAsString().startsWith("solar_gravisuite:"))
            assertTrue(
                Files.isRegularFile(
                    RESOURCES.resolve(
                        "assets/solar_gravisuite/models/"
                            + model.get("parent").getAsString().split(":", 2)[1]
                            + ".json")),
                item);
          continue;
        }
        for (var layer : model.getAsJsonObject("textures").entrySet()) {
          String texture = layer.getValue().getAsString();
          assertTrue(
              Files.isRegularFile(
                  RESOURCES.resolve(
                      "assets/"
                          + texture
                              .replace(':', '/')
                              .replace("solar_gravisuite/", "solar_gravisuite/textures/")
                          + ".png")),
              texture);
        }
      }
    }
  }

  @Test
  void allStaticUiKeysAreLocalized() throws IOException {
    JsonObject english = json("assets/solar_gravisuite/lang/en_us.json");
    Pattern calls =
        Pattern.compile(
            "(?:translatable|KeyMapping|translation)\\(\\\"([^\\\"]*solar_gravisuite[^\\\"]*)\\\"");
    try (var files = Files.walk(ROOT.resolve("src/main/java"))) {
      for (Path file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
        var matcher = calls.matcher(Files.readString(file, StandardCharsets.UTF_8));
        while (matcher.find()) {
          String key = matcher.group(1);
          if (!key.endsWith(".")) assertTrue(english.has(key), key + " in " + file);
        }
      }
    }
  }

  @Test
  void addonDoesNotOverrideIc2RecipeFiles() throws IOException {
    assertFalse(Files.exists(RESOURCES.resolve("data/ic2/recipe")));
    assertTrue(
        Files.exists(
            RESOURCES.resolve("data/solar_gravisuite/recipe/advanced_iridium_drill.json")));
  }

  private static Map<String, Integer> arguments(String value) {
    Map<String, Integer> result = new HashMap<>();
    var matcher = ARGUMENT.matcher(value);
    while (matcher.find()) result.merge(matcher.group(), 1, Integer::sum);
    return result;
  }

  private static JsonObject json(String path) throws IOException {
    return JsonParser.parseString(Files.readString(RESOURCES.resolve(path), StandardCharsets.UTF_8))
        .getAsJsonObject();
  }
}
