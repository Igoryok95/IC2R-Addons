package com.igoryok95.ic2raddons.control;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;

class ControlLocaleTest {
  @Test
  void englishNeverContainsMisdecodedCyrillicAndBothEngineLabelsUseAsciiSeparator()
      throws Exception {
    var root =
        Path.of(
            System.getProperty("ic2r_addons.projectDir"),
            "src/main/resources/assets/ic2r_addons/lang");
    var en = JsonParser.parseString(Files.readString(root.resolve("en_us.json"))).getAsJsonObject();
    for (var entry : en.entrySet())
      assertTrue(
          entry
              .getValue()
              .getAsString()
              .codePoints()
              .noneMatch(cp -> cp >= 1024 && cp <= 1279 || cp == 65533),
          entry.getKey());
    var ru = JsonParser.parseString(Files.readString(root.resolve("ru_ru.json"))).getAsJsonObject();
    for (var entry : ru.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue().getAsString();
      if (key.startsWith("item.")
          || key.startsWith("message.ic2r_addons.control.")
          || key.startsWith("control.ic2r_addons.jei.")) {
        if (value.matches(".*[A-Za-z]{3,}.*"))
          assertNotEquals(en.get(key).getAsString(), value, "Untranslated Russian text: " + key);
      }
      assertTrue(value.codePoints().noneMatch(cp -> cp == 65533), key);
      assertFalse(value.contains("в†"), key);
    }
    for (var language : java.util.List.of("en_us", "ru_ru")) {
      var values =
          JsonParser.parseString(Files.readString(root.resolve(language + ".json")))
              .getAsJsonObject();
      var rate = values.get("jade.ic2r_addons.engine.rate").getAsString();
      assertTrue(rate.contains(" | "));
      assertFalse(rate.contains("в†"));
    }
  }
}
