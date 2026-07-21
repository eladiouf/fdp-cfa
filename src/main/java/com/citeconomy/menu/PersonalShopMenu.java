package com.citeconomy.menu;

import com.citeconomy.block.entity.PersonalShopBlockEntity;
import com.citeconomy.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * Menu container de la boutique personnelle.
 * 27 slots shop (accès propriétaire uniquement) + inventaire joueur.
 * Les slots shop sont protégés : seul le proprio peut poser/prendre des items.
 * Utilise ContainerLevelAccess pour fermer le menu si le joueur s'éloigne (> 8 blocs).
 */
public class PersonalShopMenu extends AbstractContainerMenu {

    private final PersonalShopBlockEntity blockEntity;
    private final Player playerEntity;
    private final ContainerLevelAccess access;

    public PersonalShopMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public PersonalShopMenu(int id, Inventory inv, BlockEntity entity) {
        super(ModMenus.PERSONAL_SHOP_MENU.get(), id);
        this.playerEntity = inv.player;
        this.blockEntity = entity instanceof PersonalShopBlockEntity be ? be : null;
        this.access = blockEntity != null
                ? ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos())
                : ContainerLevelAccess.NULL;

        if (this.blockEntity != null) {
            int index = 0;
            for (int i = 0; i < 3; ++i) {
                for (int j = 0; j < 9; ++j) {
                    this.addSlot(new SlotItemHandler(this.blockEntity.getInventory(), index++, 8 + j * 18, 18 + i * 18) {
                        @Override
                        public boolean mayPlace(ItemStack stack) { return isOwner(); }
                        @Override
                        public boolean mayPickup(Player player) { return isOwner(); }
                    });
                }
            }
        }

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(inv, k, 8 + k * 18, 142));
        }
    }

    public PersonalShopBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public boolean isOwner() {
        return blockEntity != null && playerEntity.getUUID().equals(blockEntity.getOwnerId());
    }

    public int getSlotPrice(int slot) {
        return blockEntity != null ? blockEntity.getSlotPrice(slot) : 0;
    }

    @Override
    public boolean stillValid(Player player) {
        if (blockEntity == null) return false;
        return access.evaluate((level, pos) ->
                level.getBlockEntity(pos) instanceof PersonalShopBlockEntity
                        && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0,
                true
        );
    }

    /**
     * Shift-click : déplace entre shop (0-26) et inventaire (27+).
     * Seul le propriétaire peut déplacer les items du shop.
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (blockEntity == null) return ItemStack.EMPTY;
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 27) {
                if (!this.moveItemStackTo(itemstack1, 27, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 27, false)) {
                return ItemStack.EMPTY;
            }
            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }
}
