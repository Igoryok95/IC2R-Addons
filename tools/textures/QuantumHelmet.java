import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import javax.imageio.ImageIO;

/** Minimal palette variant of the existing helmet; geometry and UVs are never regenerated. */
public class QuantumHelmet {
  public static void main(String[] args) throws Exception {
    Path root = Path.of("src/main/resources/assets/ic2r_addons/textures");
    var reference = ImageIO.read(Path.of("tools/textures/1.2.1-helmet-palette.png").toFile());
    long green = 0, blue = 0, count = 0;
    for (int y = 0; y < reference.getHeight() / 2; y++)
      for (int x = 0; x < reference.getWidth(); x++) {
        int c = reference.getRGB(x, y), r = c >> 16 & 255, g = c >> 8 & 255, b = c & 255;
        if ((c >>> 24) > 200 && b > 150 && b > r + 80 && g > 30) {
          green += g; blue += b; count++;
        }
      }
    if (count == 0) throw new IllegalStateException("Missing generated blue palette");
    int paletteGreen = (int) (green / count), paletteBlue = (int) (blue / count);
    var original = ImageIO.read(root.resolve("item/ultimatesolarhelmet.png").toFile());
    var icon = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = icon.createGraphics();
    g.drawImage(original, 0, 0, null); g.dispose();
    for (int y = 0; y < 32; y++) for (int x = 0; x < 32; x++)
      icon.setRGB(x, y, tint(icon.getRGB(x, y), y < 12, paletteGreen, paletteBlue));
    ImageIO.write(icon, "png", root.resolve("item/quantumsolarhelmet.png").toFile());
    for (String suffix : new String[] {"", "_overlay"}) {
      var armor = ImageIO.read(root.resolve("models/armor/ultimate_solar_helmet_layer_1" + suffix + ".png").toFile());
      for (int y = 0; y < 32; y++) for (int x = 0; x < 64; x++)
        armor.setRGB(x, y, tint(armor.getRGB(x, y), true, paletteGreen, paletteBlue));
      ImageIO.write(armor, "png", root.resolve("models/armor/quantum_solar_helmet_layer_1" + suffix + ".png").toFile());
    }
    // Nearest-neighbor comparison is a preview only; runtime icons stay 32x32.
    var preview = new BufferedImage(1024, 256, BufferedImage.TYPE_INT_ARGB);
    Graphics2D pg = preview.createGraphics();
    pg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    int i = 0;
    for (String name : new String[] {"advsolarhelmet", "hybridsolarhelmet", "ultimatesolarhelmet", "quantumsolarhelmet"}) {
      pg.drawImage(ImageIO.read(root.resolve("item/" + name + ".png").toFile()), i * 256, 0, 256, 256, null);
      i++;
    }
    pg.dispose(); ImageIO.write(preview, "png", Path.of("tools/textures/1.2.1-helmets-preview.png").toFile());
  }

  private static int tint(int c, boolean crown, int paletteGreen, int paletteBlue) {
    if ((c >>> 24) == 0) return c;
    int r = c >> 16 & 255, g = c >> 8 & 255, b = c & 255;
    if (crown && b > r + 8 && b >= g) {
      double shade = Math.min(1, .55 + b / 512.0);
      return (c & 0xff000000) | (r / 2 << 16) | ((int) (paletteGreen * shade) << 8) | (int) (paletteBlue * shade);
    }
    if (r > 40 && Math.max(r, Math.max(g, b)) - Math.min(r, Math.min(g, b)) < 20) {
      r = Math.min(255, (int) (r * 1.08) + 3); g = Math.min(255, (int) (g * 1.08) + 3); b = Math.min(255, (int) (b * 1.08) + 3);
      return (c & 0xff000000) | r << 16 | g << 8 | b;
    }
    return c;
  }
}
