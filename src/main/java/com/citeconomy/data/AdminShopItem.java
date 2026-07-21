package com.citeconomy.data;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public record AdminShopItem(ItemStack item, int priceCredits) {
    public static List<AdminShopItem> getDefaults() {
        return List.of(
            // Building Materials
            new AdminShopItem(new ItemStack(Items.STONE, 16), 2),
            new AdminShopItem(new ItemStack(Items.COBBLESTONE, 16), 1),
            new AdminShopItem(new ItemStack(Items.OAK_LOG, 16), 2),
            new AdminShopItem(new ItemStack(Items.OAK_PLANKS, 16), 2),
            new AdminShopItem(new ItemStack(Items.BRICKS, 8), 4),
            new AdminShopItem(new ItemStack(Items.STONE_BRICKS, 8), 3),
            new AdminShopItem(new ItemStack(Items.GLASS, 8), 3),
            new AdminShopItem(new ItemStack(Items.SAND, 16), 1),
            new AdminShopItem(new ItemStack(Items.GRAVEL, 16), 1),

            // Decorative
            new AdminShopItem(new ItemStack(Items.TORCH, 16), 1),
            new AdminShopItem(new ItemStack(Items.LANTERN, 4), 3),
            new AdminShopItem(new ItemStack(Items.WHITE_WOOL, 8), 3),
            new AdminShopItem(new ItemStack(Items.OAK_FENCE, 16), 2),
            new AdminShopItem(new ItemStack(Items.OAK_STAIRS, 8), 2),

            // Farming & Food
            new AdminShopItem(new ItemStack(Items.WHEAT_SEEDS, 8), 1),
            new AdminShopItem(new ItemStack(Items.BREAD, 8), 1),
            new AdminShopItem(new ItemStack(Items.COOKED_BEEF, 4), 2),
            new AdminShopItem(new ItemStack(Items.BAKED_POTATO, 8), 2),
            new AdminShopItem(new ItemStack(Items.CARROT, 8), 1),
            new AdminShopItem(new ItemStack(Items.BONE_MEAL, 8), 2),

            // Utility
            new AdminShopItem(new ItemStack(Items.CHEST, 4), 3),
            new AdminShopItem(new ItemStack(Items.LADDER, 8), 1),
            new AdminShopItem(new ItemStack(Items.WATER_BUCKET, 1), 5),
            new AdminShopItem(new ItemStack(Items.BUCKET, 1), 3),
            new AdminShopItem(new ItemStack(Items.COMPASS, 1), 3),
            new AdminShopItem(new ItemStack(Items.CLOCK, 1), 5),

            // Ores & Resources
            new AdminShopItem(new ItemStack(Items.COAL, 8), 3),
            new AdminShopItem(new ItemStack(Items.IRON_INGOT, 4), 6),
            new AdminShopItem(new ItemStack(Items.GOLD_INGOT, 2), 8),
            new AdminShopItem(new ItemStack(Items.REDSTONE, 8), 4),
            new AdminShopItem(new ItemStack(Items.LAPIS_LAZULI, 4), 3)
        );
    }
}
