package com.citeconomy.registry;

import com.citeconomy.CiteconomyMod;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModVillagers {
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, CiteconomyMod.MODID);
    public static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS = DeferredRegister.create(Registries.VILLAGER_PROFESSION, CiteconomyMod.MODID);

    public static final DeferredHolder<PoiType, PoiType> BANKER_POI = POI_TYPES.register("banker_poi", 
            () -> new PoiType(ImmutableSet.copyOf(ModBlocks.BANKER_TABLE.get().getStateDefinition().getPossibleStates()), 1, 1));

    public static final DeferredHolder<VillagerProfession, VillagerProfession> BANKER_PROFESSION = VILLAGER_PROFESSIONS.register("banker", 
            () -> new VillagerProfession("banker", 
                    holder -> holder.is(BANKER_POI.getId()), 
                    holder -> holder.is(BANKER_POI.getId()), 
                    ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_CLERIC));

    public static final DeferredHolder<PoiType, PoiType> MERCHANT_POI = POI_TYPES.register("merchant_poi", 
            () -> new PoiType(ImmutableSet.copyOf(ModBlocks.MERCHANT_COUNTER.get().getStateDefinition().getPossibleStates()), 1, 1));

    public static final DeferredHolder<VillagerProfession, VillagerProfession> MERCHANT_PROFESSION = VILLAGER_PROFESSIONS.register("merchant", 
            () -> new VillagerProfession("merchant", 
                    holder -> holder.is(MERCHANT_POI.getId()), 
                    holder -> holder.is(MERCHANT_POI.getId()), 
                    ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_CLERIC));
}
