package com.citeconomy.menu;

import com.citeconomy.data.TransactionLog;
import com.citeconomy.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BankbookMenu extends AbstractContainerMenu {
    private final int balance;
    private final List<TransactionLog> logs;

    // Client-side constructor
    public BankbookMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        super(ModMenus.BANKBOOK_MENU.get(), id);
        this.balance = extraData.readInt();
        int size = extraData.readInt();
        this.logs = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            long timestamp = extraData.readLong();
            String type = extraData.readUtf();
            int amount = extraData.readInt();
            int balanceAfter = extraData.readInt();
            String description = extraData.readUtf();
            String secondParty = extraData.readUtf();
            logs.add(new TransactionLog(timestamp, type, amount, balanceAfter, description, secondParty));
        }
    }

    // Server-side constructor
    public BankbookMenu(int id, int balance, List<TransactionLog> logs) {
        super(ModMenus.BANKBOOK_MENU.get(), id);
        this.balance = balance;
        this.logs = logs;
    }

    public int getBalance() {
        return balance;
    }

    public List<TransactionLog> getLogs() {
        return logs;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
