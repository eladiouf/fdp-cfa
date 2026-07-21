package com.citeconomy.registry;

import com.citeconomy.CiteconomyMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, CiteconomyMod.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> COIN_SPEND =
            SOUND_EVENTS.register("coin_spend", () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(CiteconomyMod.MODID, "coin_spend")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COIN_RECEIVE =
            SOUND_EVENTS.register("coin_receive", () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(CiteconomyMod.MODID, "coin_receive")));

    public static final DeferredHolder<SoundEvent, SoundEvent> SHOP_BUY =
            SOUND_EVENTS.register("shop_buy", () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(CiteconomyMod.MODID, "shop_buy")));

    public static final DeferredHolder<SoundEvent, SoundEvent> SHOP_SELL =
            SOUND_EVENTS.register("shop_sell", () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(CiteconomyMod.MODID, "shop_sell")));

    public static final DeferredHolder<SoundEvent, SoundEvent> ECONOMY_SUCCESS =
            SOUND_EVENTS.register("economy_success", () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(CiteconomyMod.MODID, "economy_success")));

    public static final DeferredHolder<SoundEvent, SoundEvent> ECONOMY_ERROR =
            SOUND_EVENTS.register("economy_error", () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(CiteconomyMod.MODID, "economy_error")));

    public static final DeferredHolder<SoundEvent, SoundEvent> UI_CLICK =
            SOUND_EVENTS.register("ui_click", () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(CiteconomyMod.MODID, "ui_click")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANY_CREATE =
            SOUND_EVENTS.register("company_create", () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(CiteconomyMod.MODID, "company_create")));

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPANY_DISSOLVE =
            SOUND_EVENTS.register("company_dissolve", () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(CiteconomyMod.MODID, "company_dissolve")));
}
