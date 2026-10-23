package reforged.mods.gravisuite.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import reforged.mods.gravisuite.ItemState;
import reforged.mods.gravisuite.items.Relocator;
import reforged.mods.gravisuite.network.RelocatorMenu;

public final class RelocatorScreen extends AbstractContainerScreen<RelocatorMenu> {
  private EditBox name;

  public RelocatorScreen(RelocatorMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
    imageWidth = 276;
    imageHeight = 232;
  }

  private ItemStack stack() {
    return minecraft.player.getItemInHand(menu.hand);
  }

  protected void init() {
    super.init();
    name =
        new EditBox(
            font,
            leftPos + 10,
            topPos + 150,
            256,
            18,
            Component.translatable("screen.gravisuite.point_name"));
    name.setMaxLength(48);
    name.setHint(Component.translatable("screen.gravisuite.point_name"));
    addRenderableWidget(name);
    addButton(
        "add",
        10,
        174,
        123,
        () -> {
          GraviClient.send(10, 0, name.getValue());
          name.setValue("");
        });
    addButton(
        "remove",
        143,
        174,
        123,
        () -> GraviClient.send(11, ItemState.read(stack()).getInt("selected"), ""));
    addButton("teleport", 10, 200, 123, () -> GraviClient.send(13, 0, ""));
    addButton("mode", 143, 200, 123, () -> GraviClient.send(14, 0, ""));
  }

  private void addButton(String key, int x, int y, int width, Runnable run) {
    addRenderableWidget(
        Button.builder(Component.translatable("screen.gravisuite." + key), button -> run.run())
            .bounds(leftPos + x, topPos + y, width, 20)
            .build());
  }

  protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
    graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xf016202e);
    graphics.fill(leftPos + 5, topPos + 20, leftPos + imageWidth - 5, topPos + 145, 0xff0b131e);
    var points = Relocator.points(stack());
    int selected = ItemState.mode(stack(), "selected", Math.max(1, points.size()));
    for (int i = 0; i < points.size(); i++) {
      if (i == selected)
        graphics.fill(
            leftPos + 7,
            topPos + 23 + i * 12,
            leftPos + imageWidth - 7,
            topPos + 35 + i * 12,
            0xff234768);
      graphics.drawString(
          font,
          font.plainSubstrByWidth(points.get(i).title(i).getString(), 250),
          leftPos + 10,
          topPos + 25 + i * 12,
          0xffffff);
    }
  }

  protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    String[] modes = {"personal", "translocator", "portal"};
    graphics.drawString(
        font,
        Component.translatable(
            "screen.gravisuite.title",
            Component.translatable(
                "mode.gravisuite." + modes[ItemState.mode(stack(), "relocator", 3)])),
        8,
        6,
        0xe0f4ff);
  }

  public boolean mouseClicked(double x, double y, int button) {
    if (button == 0
        && x >= leftPos + 7
        && x < leftPos + imageWidth - 7
        && y >= topPos + 23
        && y < topPos + 143) {
      int index = (int) (y - topPos - 23) / 12;
      if (index < Relocator.points(stack()).size()) {
        GraviClient.send(12, index, "");
        return true;
      }
    }
    return super.mouseClicked(x, y, button);
  }

  public boolean keyPressed(int key, int scanCode, int modifiers) {
    if (name.isFocused() && key != org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE)
      return name.keyPressed(key, scanCode, modifiers) || name.canConsumeInput();
    return super.keyPressed(key, scanCode, modifiers);
  }

  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    super.render(graphics, mouseX, mouseY, partialTick);
    renderTooltip(graphics, mouseX, mouseY);
  }
}
