package com.citeconomy.client.gui;

import com.citeconomy.menu.BankbookMenu;
import com.citeconomy.network.BankbookPayPayload;
import com.citeconomy.data.TransactionLog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class BankbookScreen extends AbstractContainerScreen<BankbookMenu> {
    private EditBox recipientInput;
    private EditBox amountInput;
    private Button confirmButton;
    private String suggestion = "";
    private UUID suggestionUuid = null;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM HH:mm");

    public BankbookScreen(BankbookMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 220;
        this.imageHeight = 220;
    }

    @Override
    protected void init() {
        super.init();
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Recipient input
        this.recipientInput = new EditBox(this.font, x + 20, y + 45, 180, 20, Component.translatable("gui.citeconomy.bankbook.recipient_field"));
        this.recipientInput.setMaxLength(16);
        this.recipientInput.setResponder(this::onRecipientChanged);
        this.addRenderableWidget(this.recipientInput);

        // Amount input
        this.amountInput = new EditBox(this.font, x + 20, y + 85, 180, 20, Component.translatable("gui.citeconomy.bankbook.amount_field"));
        this.amountInput.setMaxLength(9);
        this.amountInput.setFilter(text -> text.isEmpty() || text.matches("\\d+"));
        this.amountInput.setResponder(this::onAmountChanged);
        this.addRenderableWidget(this.amountInput);

        // Confirm button
        this.confirmButton = Button.builder(Component.translatable("gui.citeconomy.bankbook.confirm"), (btn) -> {
            trySendPayment();
        }).bounds(x + 20, y + 115, 180, 20).build();
        this.confirmButton.active = false;
        this.addRenderableWidget(this.confirmButton);
    }

    private void onRecipientChanged(String text) {
        updateSuggestion(text);
        validateInputs();
    }

    private void onAmountChanged(String text) {
        validateInputs();
    }

    private void updateSuggestion(String text) {
        if (text.isEmpty()) {
            suggestion = "";
            suggestionUuid = null;
            return;
        }

        Collection<PlayerInfo> players = Minecraft.getInstance().getConnection().getOnlinePlayers();
        for (PlayerInfo player : players) {
            String name = player.getProfile().getName();
            // Don't suggest self
            if (name.equalsIgnoreCase(Minecraft.getInstance().player.getName().getString())) {
                continue;
            }
            if (name.toLowerCase().startsWith(text.toLowerCase())) {
                suggestion = name;
                suggestionUuid = player.getProfile().getId();
                return;
            }
        }
        suggestion = "";
        suggestionUuid = null;
    }

    private void validateInputs() {
        boolean validAmount = false;
        try {
            int amt = Integer.parseInt(amountInput.getValue());
            validAmount = amt > 0 && amt <= menu.getBalance();
        } catch (NumberFormatException ignored) {}

        boolean validRecipient = suggestionUuid != null || isValidUUID(recipientInput.getValue());
        confirmButton.active = validAmount && validRecipient;
    }

    private boolean isValidUUID(String val) {
        try {
            UUID.fromString(val);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void trySendPayment() {
        UUID target = suggestionUuid;
        if (target == null && isValidUUID(recipientInput.getValue())) {
            target = UUID.fromString(recipientInput.getValue());
        }

        if (target != null) {
            try {
                int amount = Integer.parseInt(amountInput.getValue());
                PacketDistributor.sendToServer(new BankbookPayPayload(target, amount));
                this.minecraft.setScreen(null); // Close screen on send
            } catch (NumberFormatException ignored) {}
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_TAB && !suggestion.isEmpty()) {
            recipientInput.setValue(suggestion);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Background box
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFFC6C6C6);
        graphics.fill(x, y, x + imageWidth, y + 15, 0xFF555555); // Header

        graphics.drawString(this.font, Component.translatable("gui.citeconomy.bankbook.title"), x + 5, y + 4, 0xFFFFFF, false);
        graphics.drawString(this.font, Component.translatable("gui.citeconomy.bankbook.balance", this.menu.getBalance()), x + 20, y + 22, 0x000000, false);
        
        graphics.drawString(this.font, Component.translatable("gui.citeconomy.bankbook.recipient_label"), x + 20, y + 35, 0x404040, false);
        graphics.drawString(this.font, Component.translatable("gui.citeconomy.bankbook.amount_label"), x + 20, y + 75, 0x404040, false);

        // Draw suggestion autocomplete ghost text if applicable
        if (!suggestion.isEmpty() && !recipientInput.getValue().equalsIgnoreCase(suggestion)) {
            int typedWidth = this.font.width(recipientInput.getValue());
            String ghostText = suggestion.substring(recipientInput.getValue().length());
            graphics.drawString(this.font, ghostText, recipientInput.getX() + typedWidth + 4, recipientInput.getY() + 6, 0x888888, false);
        }

        // Draw transaction history (5 most recent)
        graphics.drawString(this.font, Component.translatable("gui.citeconomy.bankbook.history_label"), x + 20, y + 140, 0x333333, false);
        List<TransactionLog> logs = menu.getLogs();
        int drawY = y + 152;
        int count = 0;
        
        // Show in reverse order (newest first)
        for (int i = logs.size() - 1; i >= 0 && count < 5; i--, count++) {
            TransactionLog log = logs.get(i);
            String timeStr = dateFormat.format(new Date(log.timestamp()));
            
            // Format line: [time] [party] [amount]
            String party = log.secondParty() != null && !log.secondParty().isEmpty() ? log.secondParty() : log.type();
            if (party.length() > 12) party = party.substring(0, 10) + "..";
            
            String prefix = log.amount() >= 0 ? "§2+" : "§4";
            graphics.drawString(this.font, Component.translatable("gui.citeconomy.bankbook.history_entry", timeStr, party, prefix, log.amount()), x + 20, drawY, 0x000000, false);
            drawY += 11;
        }

        if (logs.isEmpty()) {
            graphics.drawString(this.font, Component.translatable("gui.citeconomy.bankbook.no_transactions"), x + 20, drawY, 0x000000, false);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Suppress default Minecraft titles (Inventory / Carnet Bancaire)
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
