package com.citeconomy.network;

import com.citeconomy.CiteconomyMod;
import com.citeconomy.config.CitEconomyConfig;
import com.citeconomy.data.EconomyData;
import com.citeconomy.data.MarketListing;
import com.citeconomy.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record MarketBuyPayload(UUID listingId) implements CustomPacketPayload {
    public static final Type<MarketBuyPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CiteconomyMod.MODID, "market_buy"));

    public static final StreamCodec<FriendlyByteBuf, MarketBuyPayload> STREAM_CODEC = StreamCodec.composite(
            net.minecraft.core.UUIDUtil.STREAM_CODEC, MarketBuyPayload::listingId,
            MarketBuyPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(final MarketBuyPayload data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                EconomyData ecoData = EconomyData.get(player.serverLevel());
                MarketListing listing = ecoData.getMarketListingById(data.listingId());

                if (listing == null) {
                    player.sendSystemMessage(Component.translatable("message.market.buy.not_found"));
                    return;
                }

                if (listing.getSellerId().equals(player.getUUID())) {
                    player.sendSystemMessage(Component.translatable("message.market.buy.self"));
                    return;
                }

                int price = listing.getPrice();
                int tax = (int) Math.floor(price * CitEconomyConfig.SERVER.marketTaxPercent.get() / 100.0);
                int earnings = price - tax;

                if (ecoData.removeBalance(player.getUUID(), price, "Achat marché à " + listing.getSellerName(), listing.getSellerName())) {
                    ecoData.addBalance(listing.getSellerId(), earnings, "Vente marché à " + player.getName().getString() + " (taxe " + tax + ")", player.getName().getString());
                    ecoData.addTreasury(tax);

                    ItemStack bought = listing.getItemStack().copy();
                    ecoData.removeMarketListing(listing);

                    if (!player.getInventory().add(bought)) {
                        player.drop(bought, false);
                    }

                    player.sendSystemMessage(Component.translatable("message.market.buy.success", price));
                    player.playNotifySound(ModSounds.SHOP_BUY.get(), SoundSource.PLAYERS, 1.0f, 1.0f);

                    ServerLevel serverLevel = player.serverLevel();
                    var buyerPos = player.position();
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, buyerPos.x, buyerPos.y + 1.0, buyerPos.z, 10, 0.5, 0.5, 0.5, 0.1);

                    ServerPlayer seller = player.serverLevel().getServer().getPlayerList().getPlayer(listing.getSellerId());
                    if (seller != null) {
                        seller.sendSystemMessage(Component.translatable("message.market.sell.notification", player.getName().getString(), earnings, tax));
                        seller.playNotifySound(ModSounds.COIN_RECEIVE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                        var sellerPos = seller.position();
                        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, sellerPos.x, sellerPos.y + 1.0, sellerPos.z, 10, 0.5, 0.5, 0.5, 0.1);
                    }
                } else {
                    player.playNotifySound(ModSounds.ECONOMY_ERROR.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                    player.sendSystemMessage(Component.translatable("message.market.buy.no_funds", price));
                    var errPos = player.position();
                    player.serverLevel().sendParticles(ParticleTypes.SMOKE, errPos.x, errPos.y + 1.0, errPos.z, 10, 0.5, 0.5, 0.5, 0.1);
                }
            }
        });
    }
}
