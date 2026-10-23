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
import org.junit.jupiter.api.Test;

class ResourceContractTest {
  private static final Path ROOT = Path.of(System.getProperty("gravisuite.projectDir"));
  private static final Path RESOURCES = ROOT.resolve("src/main/resources");
  private static final Pattern ARGUMENT =
      Pattern.compile("%(?:\\d+\\$)?[-#+ 0,(<]*\\d*(?:\\.\\d+)?[a-zA-Z%]");

  @Test
  void russianCoversEnglishAndPreservesArguments() throws IOException {
    JsonObject english = json("assets/gravisuite/lang/en_us.json"),
        russian = json("assets/gravisuite/lang/ru_ru.json");
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
    JsonObject english = json("assets/gravisuite/lang/en_us.json");
    try (var files = Files.list(RESOURCES.resolve("data/gravisuite/recipe"))) {
      for (Path file : files.toList()) {
        JsonObject recipe =
            JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8))
                .getAsJsonObject();
        assertEquals("ic2:shaped", recipe.get("type").getAsString(), file.toString());
        String item = recipe.getAsJsonObject("result").get("item").getAsString();
        assertTrue(english.has("item." + item.replace(':', '.')), item);
        JsonObject model =
            json(
                "assets/"
                    + item.replace(':', '/').replace("gravisuite/", "gravisuite/models/item/")
                    + ".json");
        for (var layer : model.getAsJsonObject("textures").entrySet()) {
          String texture = layer.getValue().getAsString();
          assertTrue(
              Files.isRegularFile(
                  RESOURCES.resolve(
                      "assets/"
                          + texture.replace(':', '/').replace("gravisuite/", "gravisuite/textures/")
                          + ".png")),
              texture);
        }
      }
    }
  }

  @Test
  void allStaticUiKeysAreLocalized() throws IOException {
    JsonObject english = json("assets/gravisuite/lang/en_us.json");
    Pattern calls =
        Pattern.compile(
            "(?:translatable|KeyMapping|translation)\\(\\\"([^\\\"]*gravisuite[^\\\"]*)\\\"");
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
        Files.exists(RESOURCES.resolve("data/gravisuite/recipe/advanced_iridium_drill.json")));
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
