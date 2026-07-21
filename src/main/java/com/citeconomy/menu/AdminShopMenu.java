package com.citeconomy.menu;

import com.citeconomy.config.CitEconomyConfig;
import com.citeconomy.data.AdminShopItem;
import com.citeconomy.data.EconomyData;
import com.citeconomy.registry.ModMenus;
import com.citeconomy.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class AdminShopMenu extends AbstractContainerMenu {
    private static final int SHOP_SLOTS_PER_PAGE = 18;

    private final List<AdminShopItem> shopItems;

    public AdminShopMenu(int id, Inventory inv) {
        super(ModMenus.ADMIN_SHOP_MENU.get(), id);
        this.shopItems = AdminShopItem.getDefaults();

        SimpleContainer dummy = new SimpleContainer(SHOP_SLOTS_PER_PAGE);
        for (int i = 0; i < shopItems.size() && i < SHOP_SLOTS_PER_PAGE; i++) {
            AdminShopItem shopItem = shopItems.get(i);
            int fi = i;
            int col = i % 9;
            int row = i / 9;
            addSlot(new Slot(dummy, i, 8 + col * 18, 18 + row * 20) {
                @Override
                public boolean mayPickup(Player player) { return false; }
                @Override
                public boolean mayPlace(ItemStack stack) { return false; }
                @Override
                public ItemStack getItem() { return shopItem.item().copy(); }
                @Override
                public boolean isActive() { return fi < shopItems.size(); }
            });
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 62 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, 8 + col * 18, 120));
        }
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < SHOP_SLOTS_PER_PAGE && slotId < shopItems.size()) {
            if (!player.level().isClientSide() && player instanceof ServerPlayer sp) {
                AdminShopItem item = shopItems.get(slotId);
                EconomyData data = EconomyData.get(sp.serverLevel());
                int price = item.priceCredits();
                int tax = (int) Math.floor(price * CitEconomyConfig.SERVER.adminShopTaxPercent.get() / 100.0);
                if (data.removeBalance(sp.getUUID(), price, "Achat Boutique: " + item.item().getHoverName().getString())) {
                    ItemStack bought = item.item().copy();
                    if (!sp.getInventory().add(bought)) {
                        sp.drop(bought, false);
                    }
                    data.addTreasury(price - tax);
                    sp.playNotifySound(ModSounds.SHOP_BUY.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                    sp.sendSystemMessage(Component.translatable("message.adminshop.buy.success", bought.getHoverName().getString()));
                    var pos = sp.position();
                    sp.serverLevel().sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.x, pos.y + 1.0, pos.z, 10, 0.5, 0.5, 0.5, 0.1);
                } else {
                    sp.playNotifySound(ModSounds.ECONOMY_ERROR.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                    sp.sendSystemMessage(Component.translatable("message.adminshop.buy.no_funds"));
                    var errPos = sp.position();
                    sp.serverLevel().sendParticles(ParticleTypes.SMOKE, errPos.x, errPos.y + 1.0, errPos.z, 10, 0.5, 0.5, 0.5, 0.1);
                }
            }
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }

    @Override
    public boolean stillValid(Player player) { return true; }
}
