package com.citeconomy.network;

import com.citeconomy.CiteconomyMod;
import com.citeconomy.block.entity.PersonalShopBlockEntity;
import com.citeconomy.config.CitEconomyConfig;
import com.citeconomy.data.EconomyData;
import com.citeconomy.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet C2S : achat d'un item dans une boutique personnelle.
 * Envoyé par l'acheteur quand il confirme l'achat.
 * Le serveur valide : propriétaire ≠ acheteur, slot en vente, fonds suffisants.
 * Taxe de 2% prélevée sur la vente et reversée à la trésorerie de la ville.
 */
public record ShopBuyPayload(BlockPos pos, int slot) implements CustomPacketPayload {

    public static final Type<ShopBuyPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CiteconomyMod.MODID, "shop_buy"));

    public static final StreamCodec<FriendlyByteBuf, ShopBuyPayload> STREAM_CODEC = StreamCodec.composite(
            net.minecraft.core.BlockPos.STREAM_CODEC, ShopBuyPayload::pos,
            net.minecraft.network.codec.ByteBufCodecs.INT, ShopBuyPayload::slot,
            ShopBuyPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(final ShopBuyPayload data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (!player.serverLevel().isLoaded(data.pos())) return;

                BlockEntity be = player.serverLevel().getBlockEntity(data.pos());
                if (be instanceof PersonalShopBlockEntity shop) {
                    // Sécurité : le propriétaire ne peut pas s'acheter à lui-même
                    if (player.getUUID().equals(shop.getOwnerId())) {
                        player.sendSystemMessage(Component.translatable("message.shop.buy.self"));
                        return;
                    }

                    int slot = data.slot();
                    if (slot < 0 || slot >= 27) return;

                    if (!shop.isSlotOnSale(slot)) {
                        player.sendSystemMessage(Component.translatable("message.shop.buy.not_for_sale"));
                        return;
                    }

                    ItemStackHandler inv = shop.getInventory();
                    ItemStack stackToBuy = inv.getStackInSlot(slot);
                    if (stackToBuy.isEmpty()) return;

                    int price = shop.getSlotPrice(slot);

                    EconomyData ecoData = EconomyData.get(player.serverLevel());

                    if (ecoData.getBalance(player.getUUID()) >= price) {
                        if (ecoData.removeBalance(player.getUUID(), price, "Achat boutique", shop.getOwnerName())) {
                            int tax = (int) Math.floor(price * CitEconomyConfig.SERVER.shopBuyTaxPercent.get() / 100.0);
                            int earnings = price - tax;

                            ecoData.addBalance(shop.getOwnerId(), earnings,
                                    "Vente boutique (taxe " + tax + ")", player.getName().getString());
                            ecoData.addTreasury(tax);

                            ItemStack extracted = inv.extractItem(slot, stackToBuy.getCount(), false);
                            if (!player.getInventory().add(extracted)) {
                                player.drop(extracted, false);
                            }

                            player.sendSystemMessage(Component.translatable("message.shop.buy.success", price, tax));

                            // Notification au vendeur s'il est connecté
                            ServerPlayer owner = player.serverLevel().getServer()
                                    .getPlayerList().getPlayer(shop.getOwnerId());
                            ServerLevel serverLevel = player.serverLevel();
                            var buyerPos = player.position();
                            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, buyerPos.x, buyerPos.y + 1.0, buyerPos.z, 10, 0.5, 0.5, 0.5, 0.1);
                            if (owner != null) {
                                owner.sendSystemMessage(Component.translatable("message.shop.sell.notification",
                                        stackToBuy.getHoverName().getString(), player.getName().getString(), price, tax));
                                owner.playNotifySound(ModSounds.COIN_RECEIVE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                                var sellerPos = owner.position();
                                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, sellerPos.x, sellerPos.y + 1.0, sellerPos.z, 10, 0.5, 0.5, 0.5, 0.1);
                            }

                            player.playNotifySound(ModSounds.SHOP_BUY.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                        }
                    } else {
                        player.playNotifySound(ModSounds.ECONOMY_ERROR.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                        player.sendSystemMessage(Component.translatable("message.shop.buy.no_funds", price));
                        var errPos = player.position();
                        player.serverLevel().sendParticles(ParticleTypes.SMOKE, errPos.x, errPos.y + 1.0, errPos.z, 10, 0.5, 0.5, 0.5, 0.1);
                    }
                }
            }
        });
    }
}
