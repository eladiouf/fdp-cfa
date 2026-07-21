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

    private static final int SHOP_SLOTS = 27;

    // --- SDM Shop modern colors (RGBA semi-transparent) ---
    private static final int OVERLAY_BG    = 0xBB000000;
    private static final int PANEL_BG      = 0x7F000000;
    private static final int SLOT_ON_SALE  = 0x55FFFFFF;
    private static final int SLOT_HAS_ITEM = 0x55000000;
    private static final int SLOT_EMPTY    = 0x3C000000;
    private static final int SLOT_SELECTED = 0x78FFFFFF;
    private static final int LABEL_BG      = 0x7F000000;
    private static final int DIVIDER       = 0x3FFFFFFF;
    private static final int TEXT_WHITE    = 0xFFFFFF;
    private static final int TEXT_GRAY     = 0xAAAAAA;
    private static final int TEXT_GREEN    = 0x55FF55;
    private static final int TEXT_RED      = 0xFF5555;
    private static final int BTN_CONFIRM   = 0x7F555555;
    private static final int BTN_CANCEL    = 0x7F333333;

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
            this.priceInput = new EditBox(this.font, x + 8, y + 60, 80, 12,
                    Component.translatable("gui.citeconomy.shop.price_field"));
            this.priceInput.setMaxLength(6);
            this.priceInput.setResponder(s -> {
                if (!s.matches("\\d*")) {
                    this.priceInput.setValue(s.replaceAll("\\D", ""));
                }
            });
            addRenderableWidget(this.priceInput);
        }
    }

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

        // Full screen dark overlay
        fill(graphics, 0, 0, width, height, 0x66000000);

        // Main panel background — SDM style dark semi-transparent
        fillRounded(graphics, x, y, imageWidth, imageHeight, PANEL_BG);

        // Top accent line
        graphics.fill(x + 2, y + 14, x + imageWidth - 2, y + 15, DIVIDER);

        // Shop area label
        graphics.drawString(this.font, Component.literal("§7" + this.title.getString()), x + 8, y + 4, TEXT_GRAY, false);

        // Separator between shop area and edit/player area
        graphics.fill(x + 4, y + 74, x + imageWidth - 4, y + 75, DIVIDER);

        // Player inventory label
        graphics.drawString(this.font, Component.literal("§7Inventaire"), x + 8, y + 80, TEXT_GRAY, false);

        // --- Render slots with SDM-style backgrounds ---
        if (this.menu.getBlockEntity() != null) {
            for (Slot slot : this.menu.slots) {
                int sx = x + slot.x;
                int sy = y + slot.y;

                if (slot.index < SHOP_SLOTS) {
                    renderSdmSlot(graphics, sx, sy, slot.index);
                } else {
                    // Player inventory slots: subtle dark
                    graphics.fill(sx - 1, sy - 1, sx + 17, sy + 17, 0x44000000);
                    graphics.fill(sx, sy, sx + 16, sy + 16, 0x33000000);
                }
            }
        }

        // --- SDM-style price labels under shop slots ---
        if (this.menu.getBlockEntity() != null) {
            for (Slot slot : this.menu.slots) {
                if (slot.index < SHOP_SLOTS) {
                    int sx = x + slot.x;
                    int sy = y + slot.y;
                    int price = this.menu.getSlotPrice(slot.index);
                    boolean onSale = price > 0 && !slot.getItem().isEmpty();

                    if (onSale) {
                        String priceStr = price + "§r";
                        int pW = this.font.width(priceStr);
                        int labelX = sx + 8 - pW / 2;
                        fill(graphics, labelX - 2, sy + 12, labelX + pW + 2, sy + 17, LABEL_BG);
                        graphics.drawString(this.font, Component.literal("§f" + priceStr), labelX, sy + 12, TEXT_WHITE, false);
                    }
                }
            }
        }
    }

    private void renderSdmSlot(GuiGraphics graphics, int x, int y, int slot) {
        boolean isSelected = slot == selectedSlot;
        boolean onSale = this.menu.getSlotPrice(slot) > 0
                && this.menu.getBlockEntity().getInventory().getStackInSlot(slot).isEmpty() == false;
        boolean hasItem = this.menu.getBlockEntity().getInventory().getStackInSlot(slot).isEmpty() == false;

        int color;
        if (isSelected) {
            color = SLOT_SELECTED;
        } else if (onSale) {
            color = SLOT_ON_SALE;
        } else if (hasItem) {
            color = SLOT_HAS_ITEM;
        } else {
            color = SLOT_EMPTY;
        }

        graphics.fill(x - 1, y - 1, x + 17, y + 17, color);

        // Subtle inner border
        graphics.fill(x, y, x + 1, y + 16, 0x22FFFFFF);
        graphics.fill(x + 15, y, x + 16, y + 16, 0x22000000);
        graphics.fill(x, y, x + 16, y + 1, 0x22FFFFFF);
        graphics.fill(x, y + 15, x + 16, y + 16, 0x22000000);
    }

    private void fill(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x, y, w, h, color);
    }

    private void fillRounded(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        // Approximate rounded corners with layered fills
        graphics.fill(x + 2, y, x + w - 2, y + h, color);
        graphics.fill(x, y + 2, x + w, y + h - 2, color);
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, color);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.menu.getBlockEntity() == null) return;

        // Owner mode: show price info
        if (this.menu.isOwner()) {
            if (selectedSlot >= 0) {
                String info = "§7Slot §f" + (selectedSlot + 1) + "§7: §f" + this.menu.getSlotPrice(selectedSlot) + " Cr";
                graphics.drawString(this.font, Component.literal(info), 8, 62, TEXT_GRAY, false);
            }
        }

        // Balance at bottom
        int balance = ClientState.getBalance();
        Component balText = Component.translatable("gui.citeconomy.market.balance", balance);
        graphics.drawString(this.font, balText, imageWidth - this.font.width(balText) - 8, 174, TEXT_GRAY, false);
    }

    // ================================================================== //
    //  BUY CONFIRM OVERLAY — Modern SDM BuyerScreen style                //
    // ================================================================== //

    private void renderBuyConfirmOverlay(GuiGraphics graphics, int mouseX, int mouseY) {
        int cx = (width - imageWidth) / 2 + imageWidth / 2;
        int cy = (height - imageHeight) / 2 + imageHeight / 2;

        int pw = 150;
        int ph = 110;
        int px = cx - pw / 2;
        int py = cy - ph / 2;

        // Outer dark overlay
        fill(graphics, 0, 0, width, height, 0x88000000);

        // Main panel — dark rounded (SDM style)
        fillRounded(graphics, px, py, pw, ph, 0xDD141414);
        graphics.fill(px + 1, py + 1, px + pw - 1, py + ph - 1, 0xDD141414);

        // --- Item section ---
        // Icon
        if (!confirmStack.isEmpty()) {
            graphics.renderItem(confirmStack, px + 10, py + 8);
            graphics.renderItemDecorations(this.font, confirmStack, px + 10, py + 8);
        }

        // Item name bar
        String name = confirmStack.isEmpty() ? "" : confirmStack.getHoverName().getString();
        int nameW = this.font.width(name);
        int nameBarW = Math.min(nameW + 12, pw - 80);
        fill(graphics, px + 34, py + 8, px + 34 + nameBarW, py + 20, LABEL_BG);
        graphics.drawString(this.font, Component.literal("§f" + name), px + 40, py + 10, TEXT_WHITE, false);

        // Price bar
        String priceStr = "§7Prix: §f" + confirmPrice + " Cr";
        int priceW = this.font.width(Component.literal(priceStr));
        fill(graphics, px + 34, py + 22, px + 34 + priceW + 8, py + 34, LABEL_BG);
        graphics.drawString(this.font, Component.literal(priceStr), px + 40, py + 24, TEXT_WHITE, false);

        // --- Divider ---
        graphics.fill(px + 10, py + 40, px + pw - 10, py + 41, DIVIDER);

        // --- Two-column stats (ModernBuyerScreen style) ---
        int colW = (pw - 30) / 2;
        int statY = py + 46;

        // Col 1: Your money
        int balance = ClientState.getBalance();
        fill(graphics, px + 10, statY, px + 10 + colW, statY + 14, LABEL_BG);
        graphics.drawString(this.font, Component.literal("§7Votre argent"), px + 14, statY + 3, TEXT_GRAY, false);
        fill(graphics, px + 12 + colW, statY, px + 10 + colW * 2, statY + 14, LABEL_BG);
        graphics.drawString(this.font, Component.literal("§" + (balance >= confirmPrice ? "a" : "c") + balance + " Cr"),
                px + 16 + colW, statY + 3, TEXT_WHITE, false);

        // Col 2: Can be buy
        fill(graphics, px + 10, statY + 16, px + 10 + colW, statY + 30, LABEL_BG);
        graphics.drawString(this.font, Component.literal("§7Achetable"), px + 14, statY + 19, TEXT_GRAY, false);
        fill(graphics, px + 12 + colW, statY + 16, px + 10 + colW * 2, statY + 30, LABEL_BG);
        boolean canAfford = balance >= confirmPrice;
        graphics.drawString(this.font, Component.literal("§" + (canAfford ? "aOui" : "cNon")),
                px + 16 + colW, statY + 19, TEXT_WHITE, false);

        // --- You will spend ---
        int spendY = statY + 34;
        fill(graphics, px + 10, spendY, px + pw - 10, spendY + 14, LABEL_BG);
        graphics.drawString(this.font, Component.literal("§7Vous allez dépenser : §f" + confirmPrice + " Cr"),
                px + 14, spendY + 3, TEXT_WHITE, false);

        // --- Count text input (simulated: just show "x1" for now) ---
        int countY = spendY + 18;
        fill(graphics, px + 10, countY, px + pw - 10, countY + 16, LABEL_BG);
        graphics.drawString(this.font, Component.literal("§7Quantité : §f1"), px + 14, countY + 3, TEXT_GRAY, false);

        // --- Buttons: Cancel (left), Confirm (right) ---
        int btnW = 55;
        int btnH = 16;
        int btnY = py + ph - 22;

        // Cancel
        int cancelX = px + (pw / 2) - btnW - 6;
        boolean hoverCancel = mouseX >= cancelX && mouseX <= cancelX + btnW
                && mouseY >= btnY && mouseY <= btnY + btnH;
        fillRounded(graphics, cancelX, btnY, btnW, btnH, hoverCancel ? 0x7F555555 : BTN_CANCEL);
        graphics.drawString(this.font, Component.translatable("gui.citeconomy.shop.cancel_button.plain"),
                cancelX + (btnW - font.width(Component.translatable("gui.citeconomy.shop.cancel_button.plain"))) / 2,
                btnY + 4, TEXT_WHITE, false);

        // Confirm
        int confirmX = px + (pw / 2) + 6;
        boolean hoverConfirm = mouseX >= confirmX && mouseX <= confirmX + btnW
                && mouseY >= btnY && mouseY <= btnY + btnH;
        fillRounded(graphics, confirmX, btnY, btnW, btnH, hoverConfirm ? 0x7F777777 : BTN_CONFIRM);
        graphics.drawString(this.font, Component.translatable("gui.citeconomy.shop.buy_button.plain"),
                confirmX + (btnW - font.width(Component.translatable("gui.citeconomy.shop.buy_button.plain"))) / 2,
                btnY + 4, canAfford ? TEXT_WHITE : TEXT_GRAY, false);
    }

    // ================================================================== //
    //  TOOLTIP                                                            //
    // ================================================================== //

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

    // ================================================================== //
    //  KEYBOARD                                                           //
    // ================================================================== //

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

    // ================================================================== //
    //  MOUSE                                                              //
    // ================================================================== //

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
        int ph = 110;
        int px = cx - pw / 2;
        int py = cy - ph / 2;

        int btnW = 55;
        int btnH = 16;
        int btnY = py + ph - 22;

        // Confirm button
        int confirmX = px + (pw / 2) + 6;
        if (mouseX >= confirmX && mouseX <= confirmX + btnW
                && mouseY >= btnY && mouseY <= btnY + btnH) {
            int balance = ClientState.getBalance();
            if (balance >= confirmPrice) {
                PacketDistributor.sendToServer(new ShopBuyPayload(
                        this.menu.getBlockEntity().getBlockPos(), confirmSlot));
            }
            showConfirmOverlay = false;
            return true;
        }

        // Cancel button
        int cancelX = px + (pw / 2) - btnW - 6;
        if (mouseX >= cancelX && mouseX <= cancelX + btnW
                && mouseY >= btnY && mouseY <= btnY + btnH) {
            showConfirmOverlay = false;
            return true;
        }

        return false;
    }
}
