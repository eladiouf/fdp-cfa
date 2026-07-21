package com.citeconomy.client.gui;

import com.citeconomy.menu.BankMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import com.citeconomy.network.BankTransactionPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import com.citeconomy.client.ClientState;

public class BankScreen extends AbstractContainerScreen<BankMenu> {
    private EditBox amountField;

    public BankScreen(BankMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        amountField = new EditBox(this.font, x + 10, y + 20, 80, 18, Component.translatable("gui.citeconomy.bank.amount_field"));
        amountField.setValue("1");
        amountField.setFilter(s -> {
            if (s.isEmpty()) return true;
            try { return Integer.parseInt(s) > 0; } catch (NumberFormatException e) { return false; }
        });
        this.addRenderableWidget(amountField);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.citeconomy.bank.deposit"), (button) -> {
            sendTransaction(true);
        }).bounds(x + 95, y + 18, 60, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.citeconomy.bank.withdraw"), (button) -> {
            sendTransaction(false);
        }).bounds(x + 95, y + 42, 60, 20).build());
    }

    private void sendTransaction(boolean isDeposit) {
        int amount = 1;
        try {
            amount = Integer.parseInt(amountField.getValue());
        } catch (NumberFormatException ignored) {}
        if (amount < 1) amount = 1;
        PacketDistributor.sendToServer(new BankTransactionPayload(isDeposit, amount));
    }


    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, Component.translatable("gui.citeconomy.bank.title"), 5, 4, 0xFFFFFF, false);
        graphics.drawString(this.font, Component.translatable("gui.citeconomy.bank.balance", ClientState.getBalance()), 10, 70, 0x000000, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFFC6C6C6);
        graphics.fill(x, y, x + imageWidth, y + 15, 0xFF555555);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
