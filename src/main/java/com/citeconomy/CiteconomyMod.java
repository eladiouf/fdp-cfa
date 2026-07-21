package com.citeconomy;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import com.citeconomy.command.AdminShopCommand;
import com.citeconomy.command.CompanyCommand;
import com.citeconomy.command.EconomyCommand;
import com.citeconomy.command.MarketCommand;
import com.citeconomy.command.NeedsCommand;
import com.citeconomy.config.CitEconomyConfig;
import com.citeconomy.registry.ModBlocks;
import com.citeconomy.registry.ModBlockEntities;
import com.citeconomy.registry.ModCreativeTabs;
import com.citeconomy.registry.ModItems;
import com.citeconomy.registry.ModMenus;
import com.citeconomy.registry.ModSounds;
import com.citeconomy.registry.ModVillagers;
import net.neoforged.fml.config.ModConfig;

@Mod(CiteconomyMod.MODID)
public class CiteconomyMod {
    public static final String MODID = "citeconomy";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CiteconomyMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);

        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModVillagers.POI_TYPES.register(modEventBus);
        ModVillagers.VILLAGER_PROFESSIONS.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, CitEconomyConfig.COMMON_SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, CitEconomyConfig.SERVER_SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Citeconomy Mod Setup Complete.");
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        EconomyCommand.register(event.getDispatcher());
        CompanyCommand.register(event.getDispatcher());
        MarketCommand.register(event.getDispatcher());
        NeedsCommand.register(event.getDispatcher());
        AdminShopCommand.register(event.getDispatcher());
    }
}
