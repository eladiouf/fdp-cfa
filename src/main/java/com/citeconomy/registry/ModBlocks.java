package com.citeconomy.registry;

import com.citeconomy.CiteconomyMod;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CiteconomyMod.MODID);

    public static final DeferredBlock<Block> BANKER_TABLE = BLOCKS.registerSimpleBlock("banker_table", 
            BlockBehaviour.Properties.of()
                    .strength(2.5f)
                    .sound(SoundType.WOOD));

    public static final DeferredBlock<Block> PERSONAL_SHOP = BLOCKS.register("personal_shop",
            () -> new com.citeconomy.block.PersonalShopBlock(BlockBehaviour.Properties.of()
                    .strength(2.5f)
                    .sound(SoundType.WOOD)));

    public static final DeferredBlock<Block> MERCHANT_COUNTER = BLOCKS.registerSimpleBlock("merchant_counter",
            BlockBehaviour.Properties.of()
                    .strength(2.5f)
                    .sound(SoundType.STONE));
}
