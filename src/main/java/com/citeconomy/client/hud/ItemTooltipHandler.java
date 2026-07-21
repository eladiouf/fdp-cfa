package com.citeconomy.client.hud;

import com.citeconomy.CiteconomyMod;
import com.citeconomy.data.AdminShopItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = CiteconomyMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ItemTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        for (AdminShopItem shopItem : AdminShopItem.getDefaults()) {
            if (ItemStack.isSameItem(stack, shopItem.item())) {
                int unitPrice = shopItem.priceCredits();
                event.getToolTip().add(Component.translatable("tooltip.citeconomy.adminshop.unit_price", unitPrice));
                if (stack.getCount() > 1) {
                    int stackPrice = unitPrice * stack.getCount();
                    event.getToolTip().add(Component.translatable("tooltip.citeconomy.adminshop.stack_price", stackPrice));
                }
                break;
            }
        }
    }
}
