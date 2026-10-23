import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/** Transfers the generated blue palette without allowing image generation to alter Minecraft UVs. */
class BlueArmor {
  public static void main(String[] args) throws Exception {
    Path dir = Path.of("tools/textures");
    BufferedImage reference = ImageIO.read(dir.resolve("blue-reference.png").toFile());
    Map<Integer, Integer> colors = new HashMap<>();
    for (int y = 0; y < reference.getHeight(); y++)
      for (int x = 0; x < reference.getWidth(); x++) {
        int color = reference.getRGB(x, y);
        int r = color >> 16 & 255, g = color >> 8 & 255, b = color & 255;
        if ((color >>> 24) > 200 && b > r + 60 && g > r + 60)
          colors.merge(color, 1, Integer::sum);
      }
    int selected = colors.entrySet().stream().max(Map.Entry.comparingByValue()).orElseThrow().getKey();
    float hue = Color.RGBtoHSB(selected >> 16 & 255, selected >> 8 & 255, selected & 255, null)[0];
    System.out.printf("Generated reference palette: #%06X, hue %.3f%n", selected & 0xffffff, hue);
    float nanoHue = 0.9f * Color.RGBtoHSB(119, 187, 255, null)[0] + 0.1f * hue;
    float quantumHue = 0.9f * Color.RGBtoHSB(105, 211, 255, null)[0] + 0.1f * hue;
    Path target = Path.of("src/main/resources/assets/solar_gravisuite/textures");
    for (String item : new String[] {"advanced_nano_leggings", "advanced_nano_boots",
        "advanced_quantum_leggings", "advanced_quantum_boots",
        "advanced_quantum_leggings_overlay", "advanced_quantum_boots_overlay"})
      write(recolor(read(dir, item + ".png"), item.contains("nano") ? nanoHue : quantumHue), target.resolve("item/" + item + ".png"));
    for (int layer = 1; layer <= 2; layer++) {
      BufferedImage nano = recolor(read(dir, "ic2_nano_layer_" + layer + ".png"), nanoHue);
      if (layer == 1) {
        var g = nano.createGraphics();
        g.setClip(16, 16, 48, 16);
        g.drawImage(read(dir, "nano-chest.png"), 0, 0, null);
        g.dispose();
      }
      write(nano, target.resolve("models/armor/advanced_nano_layer_" + layer + ".png"));
      BufferedImage quantum = read(dir, "ic2_quantum_layer_" + layer + ".png");
      overlay(quantum, read(dir, "ic2_quantum_layer_" + layer + "_overlay.png"));
      write(recolor(quantum, quantumHue), target.resolve("models/armor/advanced_quantum_layer_" + layer + ".png"));
    }
  }

  static BufferedImage read(Path dir, String name) throws Exception {
    return ImageIO.read(dir.resolve("source").resolve(name).toFile());
  }

  static BufferedImage recolor(BufferedImage source, float hue) {
    BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
    for (int y = 0; y < source.getHeight(); y++)
      for (int x = 0; x < source.getWidth(); x++) {
        int pixel = source.getRGB(x, y);
        int r = pixel >> 16 & 255, g = pixel >> 8 & 255, b = pixel & 255;
        if ((pixel >>> 24) != 0 && g > r * 1.15 && g > b * 1.15 && g - Math.min(r, b) > 16) {
          float[] hsb = Color.RGBtoHSB(r, g, b, null);
          pixel = (pixel & 0xff000000) | (Color.HSBtoRGB(hue, hsb[1], hsb[2]) & 0xffffff);
        }
        result.setRGB(x, y, pixel);
      }
    return result;
  }

  static void overlay(BufferedImage base, BufferedImage overlay) {
    var g = base.createGraphics();
    g.drawImage(overlay, 0, 0, null);
    g.dispose();
  }

  static void write(BufferedImage image, Path path) throws Exception {
    Files.createDirectories(path.getParent());
    ImageIO.write(image, "PNG", path.toFile());
  }
}
