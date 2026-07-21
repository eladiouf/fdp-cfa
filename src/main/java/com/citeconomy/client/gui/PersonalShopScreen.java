package com.citeconomy.client.gui;

import com.citeconomy.client.ClientState;
import com.citeconomy.menu.PersonalShopMenu;
import com.citeconomy.network.SetShopPricePayload;
import com.citeconomy.network.ShopBuyPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class PersonalShopScreen extends AbstractContainerScreen<PersonalShopMenu> {

    // --- SDM-style colors ---
    private static final int DARK_BG = 0xCC0A0A0A;
    private static final int HEADER_BG = 0xAA0A0A0A;
    private static final int PANEL_FILL = 0x99141414;
    private static final int PANEL_BORDER = 0x442A2A2A;
    private static final int SLOT_FILL = 0xCC1E1E1E;
    private static final int SLOT_BORDER = 0x44333333;
    private static final int GOLD = 0xFFB3954E;
    private static final int GOLD_DIM = 0x44B3954E;
    private static final int GOLD_SELECTED = 0x66B3954E;
    private static final int WHITE_SELECTED = 0x55FFFFFF;
    private static final int GREEN_BUY = 0xFF3D8C40;
    private static final int GREEN_BUY_HOVER = 0xFF4CAF50;
    private static final int BUTTON_GRAY = 0x55444444;
    private static final int BUTTON_GRAY_HOVER = 0x66555555;
    private static final int TEXT_WHITE = 0xFFFFFF;
    private static final int TEXT_GRAY = 0xAAAAAA;
    private static final int TEXT_GOLD = 0xB3954E;
    private static final int TEXT_GREEN = 0x55FF55;
    private static final int TEXT_RED = 0xFF5555;

    private static final int SHOP_SLOTS = 27;

    private EditBox priceInput;
    private int selectedSlot = -1;

    private boolean showConfirmOverlay = false;
    private int confirmSlot = -1;
    private ItemStack confirmStack = ItemStack.EMPTY;
    private int confirmPrice = 0;

    public PersonalShopScreen(PersonalShopMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 190;
    }

    @Override
    protected void init() {
        super.init();
        if (this.menu.isOwner() && this.menu.getBlockEntity() != null) {
            int x = (width - imageWidth) / 2;
            int y = (height - imageHeight) / 2;
            this.priceInput = new EditBox(this.font, x + 8, y + 172, 80, 12, Component.translatable("gui.citeconomy.shop.price_field"));
            this.priceInput.setMaxLength(6);
            this.priceInput.setResponder(s -> {
                if (!s.matches("\\d*")) {
                    this.priceInput.setValue(s.replaceAll("\\D", ""));
                }
            });
            addRenderableWidget(this.priceInput);
        }
    }

    // ======================= RENDER =======================

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        if (showConfirmOverlay) {
            renderBuyConfirmOverlay(graphics, mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Main background
        fillPanel(graphics, x, y, imageWidth, imageHeight, DARK_BG);

        // Header bar
        fillPanel(graphics, x, y, imageWidth, 16, HEADER_BG);
        graphics.fill(x, y + 16, x + imageWidth, y + 17, GOLD);

        // Shop area panel
        fillPanel(graphics, x + 2, y + 18, 172, 56, PANEL_FILL);
        graphics.fill(x + 2, y + 18, x + 174, y + 19, PANEL_BORDER);

        // Separator between shop and player inventory
        graphics.fill(x + 4, y + 78, x + imageWidth - 4, y + 79, GOLD_DIM);

        // Player inventory panel
        fillPanel(graphics, x + 2, y + 82, 172, 78, PANEL_FILL);

        // Render slots with custom backgrounds
        if (this.menu.getBlockEntity() != null) {
            for (Slot slot : this.menu.slots) {
                int sx = x + slot.x;
                int sy = y + slot.y;

                if (slot.index < SHOP_SLOTS) {
                    renderShopSlotBg(graphics, sx, sy, slot.index);
                } else {
                    renderPlayerSlotBg(graphics, sx, sy);
                }
            }
        }
    }

    private void renderShopSlotBg(GuiGraphics graphics, int x, int y, int slot) {
        boolean isSelected = slot == selectedSlot;
        boolean onSale = this.menu.getSlotPrice(slot) > 0
                && this.menu.getBlockEntity().getInventory().getStackInSlot(slot).isEmpty() == false;
        boolean hasItem = this.menu.getBlockEntity().getInventory().getStackInSlot(slot).isEmpty() == false;

        if (isSelected) {
            graphics.fill(x - 1, y - 1, x + 17, y + 17, WHITE_SELECTED);
            graphics.fill(x - 1, y - 1, x + 17, y + 17, GOLD_SELECTED);
        } else if (onSale) {
            graphics.fill(x - 1, y - 1, x + 17, y + 17, GOLD_DIM);
        } else if (hasItem) {
            graphics.fill(x - 1, y - 1, x + 17, y + 17, PANEL_BORDER);
        } else {
            graphics.fill(x - 1, y - 1, x + 17, y + 17, SLOT_BORDER);
        }

        graphics.fill(x, y, x + 16, y + 16, SLOT_FILL);

        // Small indicator for on-sale items
        if (onSale && !isSelected) {
            graphics.fill(x + 1, y + 1, x + 4, y + 4, GOLD);
        }
    }

    private void renderPlayerSlotBg(GuiGraphics graphics, int x, int y) {
        graphics.fill(x - 1, y - 1, x + 17, y + 17, PANEL_BORDER);
        graphics.fill(x, y, x + 16, y + 16, SLOT_FILL);
    }

    private void fillPanel(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x, y, x + w, y + h, color);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Header
        graphics.drawString(this.font, Component.literal("§6✦ §f") .append(this.title), 8, 5, TEXT_WHITE, false);

        if (!showConfirmOverlay && this.menu.getBlockEntity() != null) {
            if (this.menu.isOwner()) {
                Component modeText = selectedSlot >= 0
                        ? Component.translatable("gui.citeconomy.shop.slot_info", selectedSlot + 1, this.menu.getSlotPrice(selectedSlot))
                        : Component.translatable("gui.citeconomy.shop.click_hint");
                graphics.drawString(this.font, modeText, 8, 162, TEXT_WHITE, false);
            }

            // Balance display
            int balance = ClientState.getBalance();
            Component balanceText = Component.translatable("gui.citeconomy.market.balance", balance);
            graphics.drawString(this.font, balanceText, imageWidth - this.font.width(balanceText) - 8, 172, TEXT_GRAY, false);
        }
    }

    // ======================= BUY CONFIRM OVERLAY =======================

    private void renderBuyConfirmOverlay(GuiGraphics graphics, int mouseX, int mouseY) {
        int cx = (width - imageWidth) / 2 + imageWidth / 2;
        int cy = (height - imageHeight) / 2 + imageHeight / 2;

        int pw = 150;
        int ph = 90;
        int px = cx - pw / 2;
        int py = cy - ph / 2;

        // Dark overlay background
        fillPanel(graphics, px - 1, py - 1, pw + 2, ph + 2, 0x66000000);

        // Main panel
        fillPanel(graphics, px, py, pw, ph, 0xDD141414);

        // Gold accent border
        graphics.fill(px, py, px + pw, py + 1, GOLD);
        graphics.fill(px, py + ph - 1, px + pw, py + ph, GOLD);
        graphics.fill(px, py, px + 1, py + ph, GOLD);
        graphics.fill(px + pw - 1, py, px + pw, py + ph, GOLD);

        // Header line
        graphics.fill(px + 4, py + 22, px + pw - 4, py + 23, GOLD_DIM);

        // Item icon
        if (!confirmStack.isEmpty()) {
            graphics.renderItem(confirmStack, px + 10, py + 6);

            // Item name
            graphics.drawString(this.font, confirmStack.getHoverName(), px + 30, py + 7, TEXT_WHITE, false);

            // Price
            Component priceText = Component.translatable("gui.citeconomy.shop.buy_confirm_price", confirmPrice);
            graphics.drawString(this.font, priceText, px + 30, py + 17, TEXT_GOLD, false);
        }

        // Your balance
        int balance = ClientState.getBalance();
        boolean canAfford = balance >= confirmPrice;
        String balColor = canAfford ? "§a" : "§c";
        Component balanceLabel = Component.translatable("gui.citeconomy.shop.buy_confirm_balance", balColor, balance);
        graphics.drawString(this.font, balanceLabel, px + 10, py + 30, TEXT_WHITE, false);

        // Buy button
        int buyBtnX = px + 15;
        int buyBtnY = py + 50;
        int btnW = 50;
        int btnH = 16;
        boolean hoverBuy = mouseX >= buyBtnX && mouseX <= buyBtnX + btnW
                && mouseY >= buyBtnY && mouseY <= buyBtnY + btnH;

        if (canAfford) {
            fillPanel(graphics, buyBtnX, buyBtnY, btnW, btnH, hoverBuy ? GREEN_BUY_HOVER : GREEN_BUY);
        } else {
            fillPanel(graphics, buyBtnX, buyBtnY, btnW, btnH, 0x66333333);
        }
        graphics.fill(buyBtnX, buyBtnY, buyBtnX + btnW, buyBtnY + 1, GOLD_DIM);
        graphics.drawString(this.font, Component.translatable("gui.citeconomy.shop.buy_button.plain"),
                buyBtnX + (btnW - font.width(Component.translatable("gui.citeconomy.shop.buy_button.plain"))) / 2,
                buyBtnY + 4, canAfford ? TEXT_WHITE : TEXT_GRAY, false);

        // Cancel button
        int cancelBtnX = px + pw - 15 - btnW;
        boolean hoverCancel = mouseX >= cancelBtnX && mouseX <= cancelBtnX + btnW
                && mouseY >= buyBtnY && mouseY <= buyBtnY + btnH;

        fillPanel(graphics, cancelBtnX, buyBtnY, btnW, btnH, hoverCancel ? BUTTON_GRAY_HOVER : BUTTON_GRAY);
        graphics.fill(cancelBtnX, buyBtnY, cancelBtnX + btnW, buyBtnY + 1, PANEL_BORDER);
        graphics.drawString(this.font, Component.translatable("gui.citeconomy.shop.cancel_button.plain"),
                cancelBtnX + (btnW - font.width(Component.translatable("gui.citeconomy.shop.cancel_button.plain"))) / 2,
                buyBtnY + 4, TEXT_WHITE, false);
    }

    // ======================= TOOLTIP =======================

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!showConfirmOverlay && this.menu.getBlockEntity() != null) {
            Slot slot = this.getSlotUnderMouse();
            if (slot != null && slot.index < SHOP_SLOTS && slot.hasItem()) {
                int price = this.menu.getSlotPrice(slot.index);
                if (price > 0) {
                    graphics.renderTooltip(this.font,
                            Component.translatable("gui.citeconomy.shop.tooltip", slot.getItem().getHoverName(), price),
                            mouseX, mouseY);
                    return;
                }
            }
        }
        super.renderTooltip(graphics, mouseX, mouseY);
    }

    // ======================= KEYBOARD =======================

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (this.priceInput != null && this.priceInput.isFocused()) {
            if (key == 257 || key == 335) {
                applyPrice();
                return true;
            }
            return this.priceInput.keyPressed(key, scanCode, modifiers);
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.priceInput != null && this.priceInput.isFocused()) {
            return this.priceInput.charTyped(codePoint, modifiers);
        }
        return super.charTyped(codePoint, modifiers);
    }

    private void applyPrice() {
        String text = this.priceInput.getValue().trim();
        if (text.isEmpty() || this.menu.getBlockEntity() == null || selectedSlot < 0) return;
        try {
            int price = Integer.parseInt(text);
            PacketDistributor.sendToServer(new SetShopPricePayload(
                    this.menu.getBlockEntity().getBlockPos(), selectedSlot, price));
            this.priceInput.setFocused(false);
        } catch (NumberFormatException ignored) {}
    }

    // ======================= MOUSE =======================

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.priceInput != null && this.priceInput.isFocused() && !this.priceInput.isMouseOver(mouseX, mouseY)) {
            applyPrice();
        }
        if (this.menu.getBlockEntity() == null) return super.mouseClicked(mouseX, mouseY, button);

        if (showConfirmOverlay) {
            return handleConfirmClick(mouseX, mouseY);
        }

        Slot slot = this.getSlotUnderMouse();
        if (slot != null && slot.index < SHOP_SLOTS) {
            if (this.menu.isOwner()) {
                selectedSlot = slot.index;
                this.priceInput.setValue(String.valueOf(this.menu.getSlotPrice(slot.index)));
                this.priceInput.setFocused(true);
                return true;
            } else if (slot.hasItem() && this.menu.getSlotPrice(slot.index) > 0) {
                confirmSlot = slot.index;
                confirmStack = slot.getItem().copy();
                confirmPrice = this.menu.getSlotPrice(slot.index);
                showConfirmOverlay = true;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleConfirmClick(double mouseX, double mouseY) {
        int cx = (width - imageWidth) / 2 + imageWidth / 2;
        int cy = (height - imageHeight) / 2 + imageHeight / 2;

        int pw = 150;
        int ph = 90;
        int px = cx - pw / 2;
        int py = cy - ph / 2;

        int btnW = 50;
        int btnH = 16;
        int buyBtnY = py + 50;

        // Buy button
        int buyBtnX = px + 15;
        if (mouseX >= buyBtnX && mouseX <= buyBtnX + btnW
                && mouseY >= buyBtnY && mouseY <= buyBtnY + btnH) {
            int balance = ClientState.getBalance();
            if (balance >= confirmPrice) {
                PacketDistributor.sendToServer(new ShopBuyPayload(
                        this.menu.getBlockEntity().getBlockPos(), confirmSlot));
            }
            showConfirmOverlay = false;
            return true;
        }

        // Cancel button
        int cancelBtnX = px + pw - 15 - btnW;
        if (mouseX >= cancelBtnX && mouseX <= cancelBtnX + btnW
                && mouseY >= buyBtnY && mouseY <= buyBtnY + btnH) {
            showConfirmOverlay = false;
            return true;
        }

        return false;
    }
}
