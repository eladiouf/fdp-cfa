package com.citeconomy.event;

import com.citeconomy.CiteconomyMod;
import com.citeconomy.config.CitEconomyConfig;
import com.citeconomy.data.EconomyData;
import com.citeconomy.data.MarketListing;
import com.citeconomy.menu.BankMenu;
import com.citeconomy.network.MarketListingsPayload;
import com.citeconomy.registry.ModVillagers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = CiteconomyMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ServerEvents {
    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        tickCounter++;
        if (tickCounter < CitEconomyConfig.SERVER.economicCycleInterval.get()) return;
        tickCounter = 0;

        ServerLevel overworld = event.getServer().getLevel(ServerLevel.OVERWORLD);
        if (overworld == null) return;

        EconomyData data = EconomyData.get(overworld);
        data.runEconomicCycle(overworld);
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof Villager villager) {
            if (villager.getVillagerData().getProfession() == ModVillagers.BANKER_PROFESSION.get()) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);

                if (!event.getLevel().isClientSide() && event.getEntity() instanceof ServerPlayer player) {
                    player.openMenu(new SimpleMenuProvider(
                            (id, inv, p) -> new BankMenu(id, inv),
                            Component.translatable("container.citeconomy.bank")
                    ));
                }
            } else if (villager.getVillagerData().getProfession() == ModVillagers.MERCHANT_PROFESSION.get()) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);

                if (!event.getLevel().isClientSide() && event.getEntity() instanceof ServerPlayer player) {
                    player.openMenu(new SimpleMenuProvider(
                            (id, inv, p) -> new com.citeconomy.menu.MarketMenu(id, inv),
                            Component.translatable("container.citeconomy.market")
                    ));

                    EconomyData data = EconomyData.get(player.serverLevel());
                    List<MarketListingsPayload.Entry> entries = new ArrayList<>();
                    for (MarketListing listing : data.getMarketListings()) {
                        entries.add(new MarketListingsPayload.Entry(
                                listing.getId(), listing.getItemStack(), listing.getPrice(), listing.getSellerName()
                        ));
                    }
                    PacketDistributor.sendToPlayer(player, new MarketListingsPayload(entries));
                }
            }
        }
    }
}
