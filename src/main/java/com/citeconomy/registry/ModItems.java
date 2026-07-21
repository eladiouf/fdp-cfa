package com.citeconomy.registry;

import com.citeconomy.CiteconomyMod;
import com.citeconomy.item.BankbookItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CiteconomyMod.MODID);

    public static final DeferredItem<BlockItem> BANKER_TABLE = ITEMS.registerSimpleBlockItem("banker_table", ModBlocks.BANKER_TABLE);

    public static final DeferredItem<Item> BANKBOOK = ITEMS.register("bankbook", 
            () -> new BankbookItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<BlockItem> PERSONAL_SHOP = ITEMS.registerSimpleBlockItem("personal_shop", ModBlocks.PERSONAL_SHOP);

    public static final DeferredItem<BlockItem> MERCHANT_COUNTER = ITEMS.registerSimpleBlockItem("merchant_counter", ModBlocks.MERCHANT_COUNTER);
}
