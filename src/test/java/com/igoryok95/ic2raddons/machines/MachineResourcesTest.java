package com.igoryok95.ic2raddons.machines;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.*;
import java.nio.file.*;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class MachineResourcesTest {
  private static final Path RES =
      Path.of(System.getProperty("ic2r_addons.projectDir"), "src/main/resources");
  private static final List<String> NAMES =
      List.of(
          "rotary_macerator",
          "singularity_compressor",
          "centrifuge_extractor",
          "compacting_recycler",
          "liquescent_extruder",
          "impellerized_roller",
          "water_jet_cutter",
          "thermal_washer");

  @Test
  void completeSetHasCraftingAndActiveModelsInCombinedNamespace() throws Exception {
    for (String name : NAMES) {
      var recipe =
          JsonParser.parseString(
                  Files.readString(RES.resolve("data/ic2r_addons/recipe/shaped/" + name + ".json")))
              .getAsJsonObject();
      assertEquals("ic2r_addons:" + name, recipe.getAsJsonObject("result").get("id").getAsString());
      assertTrue(
          Files.isRegularFile(
              RES.resolve("assets/ic2r_addons/models/block/" + name + "_active.json")),
          name);
      assertTrue(
          Files.isRegularFile(RES.resolve("assets/ic2r_addons/guidef/" + name + ".xml")), name);
    }
    assertTrue(
        Files.isRegularFile(RES.resolve("assets/ic2r_addons/textures/item/sharp_plate.png")));
  }

  @Test
  void modernMachineFacesAreNative32PixelTextures() throws Exception {
    for (String folder :
        List.of(
            "macerator",
            "compressor",
            "extractor",
            "roller",
            "extruder",
            "cutter",
            "recycler",
            "washer")) {
      try (var files = Files.walk(RES.resolve("assets/ic2r_addons/textures/block/" + folder))) {
        for (var file : files.filter(p -> p.toString().endsWith(".png")).toList()) {
          var image = ImageIO.read(file.toFile());
          assertEquals(32, image.getWidth(), file.toString());
          assertEquals(32, image.getHeight(), file.toString());
        }
      }
    }
  }

  @Test
  void progressBackgroundsDoNotIntersectInventorySlots() throws Exception {
    int[] outputCounts = {2, 1, 3, 1, 3, 1, 1, 3};
    for (int index = 0; index < NAMES.size(); index++) {
      String name = NAMES.get(index);
      var document =
          javax.xml.parsers.DocumentBuilderFactory.newInstance()
              .newDocumentBuilder()
              .parse(RES.resolve("assets/ic2r_addons/guidef/" + name + ".xml").toFile());
      var slots = new java.util.ArrayList<java.awt.Rectangle>();
      for (String element : List.of("slot", "slotgrid")) {
        var nodes = document.getElementsByTagName(element);
        for (int i = 0; i < nodes.getLength(); i++) {
          var node = (org.w3c.dom.Element) nodes.item(i);
          int x = Integer.parseInt(node.getAttribute("x")),
              y = Integer.parseInt(node.getAttribute("y"));
          int count =
              node.getAttribute("name").equals("output")
                  ? outputCounts[index]
                  : node.getAttribute("name").equals("upgrade") ? 2 : 1;
          int size = node.getAttribute("style").equals("large") ? 26 : 18;
          int cols =
              node.hasAttribute("cols") ? Integer.parseInt(node.getAttribute("cols")) : count;
          for (int slot = 0; slot < count; slot++)
            slots.add(
                new java.awt.Rectangle(
                    x + (slot % cols) * size, y + (slot / cols) * size, size, size));
        }
      }
      var nodes = document.getElementsByTagName("gauge");
      for (int i = 0; i < nodes.getLength(); i++) {
        var node = (org.w3c.dom.Element) nodes.item(i);
        var style = ic2.core.gui.Gauge.GaugeStyle.get(node.getAttribute("style")).getProperties();
        int x = Integer.parseInt(node.getAttribute("x")),
            y = Integer.parseInt(node.getAttribute("y"));
        var background =
            new java.awt.Rectangle(
                x + style.bgXOffset(), y + style.bgYOffset(), style.bgWidth(), style.bgHeight());
        var fill = new java.awt.Rectangle(x, y, style.innerWidth(), style.innerHeight());
        for (var slot : slots) {
          assertFalse(
              background.intersects(slot),
              name + " gauge background " + background + " overlaps " + slot);
          assertFalse(fill.intersects(slot), name + " progress fill " + fill + " overlaps " + slot);
        }
      }
    }
  }
}
