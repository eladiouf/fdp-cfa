package com.citeconomy.client;

import com.citeconomy.CiteconomyMod;
import com.citeconomy.client.gui.AdminShopScreen;
import com.citeconomy.client.gui.BankScreen;
import com.citeconomy.client.gui.BankbookScreen;
import com.citeconomy.client.gui.MarketScreen;
import com.citeconomy.client.gui.NeedsScreen;
import com.citeconomy.client.gui.PersonalShopScreen;
import com.citeconomy.registry.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
@EventBusSubscriber(modid = CiteconomyMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.BANK_MENU.get(), BankScreen::new);
        event.register(ModMenus.PERSONAL_SHOP_MENU.get(), PersonalShopScreen::new);
        event.register(ModMenus.BANKBOOK_MENU.get(), BankbookScreen::new);
        event.register(ModMenus.MARKET_MENU.get(), MarketScreen::new);
        event.register(ModMenus.NEEDS_MENU.get(), NeedsScreen::new);
        event.register(ModMenus.ADMIN_SHOP_MENU.get(), AdminShopScreen::new);
    }
}
