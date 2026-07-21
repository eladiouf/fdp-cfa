package com.citeconomy.client.gui;

import com.citeconomy.client.ClientState;
import com.citeconomy.menu.AdminShopMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class AdminShopScreen extends AbstractContainerScreen<AdminShopMenu> {
    private static final int WINDOW_W = 176;
    private static final int WINDOW_H = 186;

    private static final int OVERLAY_BG  = 0x66000000;
    private static final int PANEL_BG    = 0x7F000000;
    private static final int SLOT_ACTIVE = 0x55FFFFFF;
    private static final int SLOT_INACT  = 0x3C000000;
    private static final int LABEL_BG    = 0x7F000000;
    private static final int DIVIDER     = 0x3FFFFFFF;
    private static final int TEXT_WHITE  = 0xFFFFFF;
    private static final int TEXT_GRAY   = 0xAAAAAA;

    public AdminShopScreen(AdminShopMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = WINDOW_W;
        this.imageHeight = WINDOW_H;
    }

    @Override
    public void render(GuiGraphics graphics, int mx, int my, float delta) {
        renderBackground(graphics, mx, my, delta);
        super.render(graphics, mx, my, delta);
        renderTooltip(graphics, mx, my);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mx, int my) {
        int x = (width - WINDOW_W) / 2;
        int y = (height - WINDOW_H) / 2;

        fill(graphics, 0, 0, width, height, OVERLAY_BG);
        fillRounded(graphics, x, y, WINDOW_W, WINDOW_H, PANEL_BG);

        // Header line
        graphics.fill(x + 2, y + 14, x + WINDOW_W - 2, y + 15, DIVIDER);

        // Separator between shop and player inventory
        graphics.fill(x + 4, y + 68, x + WINDOW_W - 4, y + 69, DIVIDER);

        // Player area label
        graphics.drawString(this.font, Component.literal("§7Inventaire"), x + 8, y + 74, TEXT_GRAY, false);

        // --- Render shop slots with SDM style ---
        int shopSlotsMax = 18;
        for (int i = 0; i < this.menu.slots.size(); i++) {
            Slot slot = this.menu.slots.get(i);
            int sx = x + slot.x;
            int sy = y + slot.y;

            if (i < shopSlotsMax) {
                boolean active = i < menu.getShopItemCount();
                graphics.fill(sx - 1, sy - 1, sx + 17, sy + 17, active ? SLOT_ACTIVE : SLOT_INACT);
                // Subtle inner border
                graphics.fill(sx, sy, sx + 1, sy + 16, active ? 0x22FFFFFF : 0);
                graphics.fill(sx + 15, sy, sx + 16, sy + 16, active ? 0x22000000 : 0);
                graphics.fill(sx, sy, sx + 16, sy + 1, active ? 0x22FFFFFF : 0);
                graphics.fill(sx, sy + 15, sx + 16, sy + 16, active ? 0x22000000 : 0);
            } else {
                // Player inventory slots
                graphics.fill(sx - 1, sy - 1, sx + 17, sy + 17, 0x44000000);
                graphics.fill(sx, sy, sx + 16, sy + 16, 0x33000000);
            }
        }

        // --- Price labels on shop items ---
        for (int i = 0; i < shopSlotsMax && i < menu.getShopItemCount(); i++) {
            Slot slot = this.menu.slots.get(i);
            if (slot.hasItem()) {
                int sx = x + slot.x;
                int sy = y + slot.y;
                int price = menu.getPriceAt(i);

                String priceStr = price + "§r";
                int pW = this.font.width(priceStr);
                int labelX = sx + 8 - pW / 2;
                fill(graphics, labelX - 2, sy + 12, labelX + pW + 2, sy + 17, LABEL_BG);
                graphics.drawString(this.font, Component.literal("§f" + priceStr), labelX, sy + 12, TEXT_WHITE, false);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mx, int my) {
        graphics.drawString(this.font, Component.literal("§7" + this.title.getString()), 8, 4, TEXT_GRAY, false);

        // Balance
        int balance = ClientState.getBalance();
        Component balText = Component.translatable("gui.citeconomy.market.balance", balance);
        graphics.drawString(this.font, balText, WINDOW_W - this.font.width(balText) - 8, 170, TEXT_GRAY, false);
    }

    private void fill(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x, y, w, h, color);
    }

    private void fillRounded(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x + 2, y, x + w - 2, y + h, color);
        graphics.fill(x, y + 2, x + w, y + h - 2, color);
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, color);
    }
}
