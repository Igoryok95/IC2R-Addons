import java.awt.image.BufferedImage;
import java.nio.file.*;
import javax.imageio.ImageIO;

/** Copies the two user-provided classic icons without generating a replacement design. */
public class ClassicHelmets {
  public static void main(String[] args) throws Exception {
    var source = ImageIO.read(Path.of("tools/textures/source/classic-solar-helmets-reference.png").toFile());
    String[] names = {"hybridsolarhelmet", "ultimatesolarhelmet"};
    for (int i = 0; i < names.length; i++) {
      var icon = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
      int left = i == 0 ? 32 : 198;
      for (int y = 0; y < 32; y++) for (int x = 0; x < 32; x++) {
        int c = source.getRGB(left + x * 4 + 2, 20 + y * 4 + 2);
        int r = c >> 16 & 255, g = c >> 8 & 255, b = c & 255;
        if (r >= 18 && r <= 34 && g >= 26 && g <= 46 && b >= 22 && b <= 42) c = 0;
        icon.setRGB(x, y, c);
      }
      ImageIO.write(icon, "png", Path.of("src/main/resources/assets/ic2r_addons/textures/item/" + names[i] + ".png").toFile());
    }
  }
}
