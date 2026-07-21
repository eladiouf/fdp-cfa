package com.citeconomy.client.gui;

import com.citeconomy.client.ClientMarketListings;
import com.citeconomy.menu.MarketMenu;
import com.citeconomy.network.MarketBuyPayload;
import com.citeconomy.network.MarketListingsPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class MarketScreen extends AbstractContainerScreen<MarketMenu> {
    private static final int LISTING_HEIGHT = 20;
    private int scrollOffset = 0;

    public MarketScreen(MarketMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 222;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        List<MarketListingsPayload.Entry> listings = ClientMarketListings.getListings();
        int maxScroll = Math.max(0, listings.size() - 5);
        scrollOffset = Math.clamp(scrollOffset - (int) scrollY, 0, maxScroll);
        return true;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFFC6C6C6);
        graphics.fill(x, y, x + imageWidth, y + 15, 0xFF555555);

        int panelY = y + 18;
        graphics.fill(x + 5, panelY, x + imageWidth - 5, panelY + 105, 0xFF8B8B8B);

        List<MarketListingsPayload.Entry> listings = ClientMarketListings.getListings();
        int startIdx = scrollOffset;
        int endIdx = Math.min(startIdx + 5, listings.size());

        for (int i = startIdx; i < endIdx; i++) {
            MarketListingsPayload.Entry entry = listings.get(i);
            int ly = panelY + 3 + (i - startIdx) * LISTING_HEIGHT;
            graphics.fill(x + 8, ly, x + imageWidth - 8, ly + LISTING_HEIGHT - 2, 0xFF6B6B6B);
            graphics.renderItem(entry.item(), x + 10, ly + 2);
            graphics.drawString(this.font, entry.item().getHoverName().getString(), x + 30, ly + 2, 0xFFFFFF, false);
            graphics.drawString(this.font, entry.sellerName(), x + 30, ly + 11, 0xAAAAAA, false);
            graphics.drawString(this.font, Component.translatable("gui.citeconomy.market.price", entry.price()), x + imageWidth - 60, ly + 5, 0xFFFF55, false);
        }

        graphics.drawString(this.font, Component.translatable("gui.citeconomy.market.balance", com.citeconomy.client.ClientState.getBalance()), x + 10, y + 128, 0x000000, false);

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                int sx = x + 8 + j * 18;
                int sy = y + 140 + i * 18;
                graphics.fill(sx - 1, sy - 1, sx + 17, sy + 17, 0xFF373737);
                graphics.fill(sx, sy, sx + 16, sy + 16, 0xFF8B8B8B);
            }
        }
        for (int k = 0; k < 9; ++k) {
            int sx = x + 8 + k * 18;
            int sy = y + 198;
            graphics.fill(sx - 1, sy - 1, sx + 17, sy + 17, 0xFF373737);
            graphics.fill(sx, sy, sx + 16, sy + 16, 0xFF8B8B8B);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        int panelY = y + 18;

        List<MarketListingsPayload.Entry> listings = ClientMarketListings.getListings();
        int startIdx = scrollOffset;
        int endIdx = Math.min(startIdx + 5, listings.size());

        for (int i = startIdx; i < endIdx; i++) {
            int ly = panelY + 3 + (i - startIdx) * LISTING_HEIGHT;
            if (mouseX >= x + 8 && mouseX <= x + imageWidth - 8 && mouseY >= ly && mouseY <= ly + LISTING_HEIGHT - 2) {
                MarketListingsPayload.Entry entry = listings.get(i);
                PacketDistributor.sendToServer(new MarketBuyPayload(entry.listingId()));
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Draw title inside renderLabels using local coordinates (no x/y offset)
        graphics.drawString(this.font, this.title, 8, 4, 0xFFFFFF, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
