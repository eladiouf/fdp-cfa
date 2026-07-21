package com.citeconomy.registry;

import com.citeconomy.CiteconomyMod;
import com.citeconomy.block.entity.PersonalShopBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CiteconomyMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PersonalShopBlockEntity>> PERSONAL_SHOP = BLOCK_ENTITIES.register("personal_shop", 
            () -> BlockEntityType.Builder.of(PersonalShopBlockEntity::new, ModBlocks.PERSONAL_SHOP.get()).build(null));
}
