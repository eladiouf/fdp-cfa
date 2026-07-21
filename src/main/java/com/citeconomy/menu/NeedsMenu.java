package com.citeconomy.menu;

import com.citeconomy.registry.ModMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;

public class NeedsMenu extends AbstractContainerMenu {
    private final List<Integer> dummy;

    public NeedsMenu(int id, Inventory inv) {
        super(ModMenus.NEEDS_MENU.get(), id);
        this.dummy = Collections.emptyList();

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, 48 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, 48 + col * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }

    @Override
    public boolean stillValid(Player player) { return true; }
}
