package com.citeconomy.client.gui;

import com.citeconomy.menu.AdminShopMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class AdminShopScreen extends AbstractContainerScreen<AdminShopMenu> {
    private static final int WINDOW_W = 176;
    private static final int WINDOW_H = 144;

    public AdminShopScreen(AdminShopMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = WINDOW_W;
        this.imageHeight = WINDOW_H;
    }

    @Override
    protected void renderBg(GuiGraphics gui, float delta, int mx, int my) {
        int x = (width - WINDOW_W) / 2;
        int y = (height - WINDOW_H) / 2;
        gui.fill(x, y, x + WINDOW_W, y + WINDOW_H, 0xFFC6C6C6);
        gui.fill(x, y, x + WINDOW_W, y + 15, 0xFF555555);

        // Draw slots with dark border and lighter gray background
        for (Slot slot : this.menu.slots) {
            int sx = x + slot.x;
            int sy = y + slot.y;
            gui.fill(sx - 1, sy - 1, sx + 17, sy + 17, 0xFF373737);
            gui.fill(sx, sy, sx + 16, sy + 16, 0xFF8B8B8B);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mx, int my) {
        gui.drawString(this.font, this.title, 8, 4, 0xFFFFFF, false);
        gui.drawString(this.font, Component.translatable("gui.citeconomy.adminshop.click_hint"), 8, 42, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics gui, int mx, int my, float delta) {
        renderBackground(gui, mx, my, delta);
        super.render(gui, mx, my, delta);
        renderTooltip(gui, mx, my);
    }
}
