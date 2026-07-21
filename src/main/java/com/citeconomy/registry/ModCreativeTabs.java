package com.citeconomy.registry;

import com.citeconomy.CiteconomyMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CiteconomyMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CITECONOMY_TAB = CREATIVE_MODE_TABS.register("citeconomy_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.citeconomy"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> ModItems.BANKBOOK.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.BANKBOOK.get());
                        output.accept(ModItems.BANKER_TABLE.get());
                        output.accept(ModItems.PERSONAL_SHOP.get());
                        output.accept(ModItems.MERCHANT_COUNTER.get());
                    })
                    .build());
}
