package com.citeconomy.registry;

import com.citeconomy.CiteconomyMod;
import com.citeconomy.menu.AdminShopMenu;
import com.citeconomy.menu.BankMenu;
import com.citeconomy.menu.BankbookMenu;
import com.citeconomy.menu.MarketMenu;
import com.citeconomy.menu.NeedsMenu;
import com.citeconomy.menu.PersonalShopMenu;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, CiteconomyMod.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<BankMenu>> BANK_MENU = MENUS.register("bank_menu", 
            () -> new MenuType<>(BankMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final DeferredHolder<MenuType<?>, MenuType<PersonalShopMenu>> PERSONAL_SHOP_MENU = MENUS.register("personal_shop_menu",
            () -> net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(PersonalShopMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<BankbookMenu>> BANKBOOK_MENU = MENUS.register("bankbook_menu",
            () -> net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(BankbookMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MarketMenu>> MARKET_MENU = MENUS.register("market_menu",
            () -> IMenuTypeExtension.create(MarketMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<NeedsMenu>> NEEDS_MENU = MENUS.register("needs_menu",
            () -> new MenuType<>(NeedsMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final DeferredHolder<MenuType<?>, MenuType<AdminShopMenu>> ADMIN_SHOP_MENU = MENUS.register("admin_shop_menu",
            () -> new MenuType<>(AdminShopMenu::new, FeatureFlags.DEFAULT_FLAGS));
}
