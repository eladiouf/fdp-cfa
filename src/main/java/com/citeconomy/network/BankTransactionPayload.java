package com.citeconomy.network;

import com.citeconomy.CiteconomyMod;
import com.citeconomy.config.CitEconomyConfig;
import com.citeconomy.data.EconomyData;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BankTransactionPayload(boolean isDeposit, int amount) implements CustomPacketPayload {
    public static final Type<BankTransactionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CiteconomyMod.MODID, "bank_transaction"));

    public static final StreamCodec<FriendlyByteBuf, BankTransactionPayload> STREAM_CODEC = StreamCodec.composite(
            net.minecraft.network.codec.ByteBufCodecs.BOOL, BankTransactionPayload::isDeposit,
            net.minecraft.network.codec.ByteBufCodecs.INT, BankTransactionPayload::amount,
            BankTransactionPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(final BankTransactionPayload data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                EconomyData ecoData = EconomyData.get(player.serverLevel());
                int credits = ecoData.getBalance(player.getUUID());

                Item currencyItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(CitEconomyConfig.SERVER.currencyItem.get()));
                if (currencyItem == Items.AIR) currencyItem = Items.EMERALD;
                int exchangeRate = CitEconomyConfig.SERVER.exchangeRate.get();

                if (data.isDeposit()) {
                    int itemsToDeposit = data.amount();
                    int found = 0;
                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        ItemStack stack = player.getInventory().getItem(i);
                        if (stack.is(currencyItem)) {
                            found += stack.getCount();
                        }
                    }

                    if (found >= itemsToDeposit) {
                        int remaining = itemsToDeposit;
                        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                            ItemStack stack = player.getInventory().getItem(i);
                            if (stack.is(currencyItem)) {
                                int toTake = Math.min(stack.getCount(), remaining);
                                stack.shrink(toTake);
                                remaining -= toTake;
                                if (remaining == 0) break;
                            }
                        }
                        ecoData.addBalance(player.getUUID(), itemsToDeposit * exchangeRate, "Dépôt banque");
                        player.playNotifySound(ModSounds.COIN_RECEIVE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                        var depPos = player.position();
                        player.serverLevel().sendParticles(ParticleTypes.HAPPY_VILLAGER, depPos.x, depPos.y + 1.0, depPos.z, 10, 0.5, 0.5, 0.5, 0.1);
                    } else {
                        player.playNotifySound(ModSounds.ECONOMY_ERROR.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                        player.sendSystemMessage(Component.translatable("message.bank.deposit.no_items", itemsToDeposit));
                        var errPos = player.position();
                        player.serverLevel().sendParticles(ParticleTypes.SMOKE, errPos.x, errPos.y + 1.0, errPos.z, 10, 0.5, 0.5, 0.5, 0.1);
                    }
                } else {
                    int itemsToWithdraw = data.amount();
                    int creditsNeeded = itemsToWithdraw * exchangeRate;
                    if (credits < creditsNeeded) {
                        player.playNotifySound(ModSounds.ECONOMY_ERROR.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                        player.sendSystemMessage(Component.translatable("message.bank.withdraw.no_funds", creditsNeeded));
                        var errPos = player.position();
                        player.serverLevel().sendParticles(ParticleTypes.SMOKE, errPos.x, errPos.y + 1.0, errPos.z, 10, 0.5, 0.5, 0.5, 0.1);
                        return;
                    }

                    int maxStack = 64;
                    int existingSlots = 0;
                    int freeSlots = 0;
                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        ItemStack stack = player.getInventory().getItem(i);
                        if (stack.isEmpty()) {
                            freeSlots++;
                        } else if (stack.is(currencyItem) && stack.getCount() < maxStack) {
                            existingSlots += maxStack - stack.getCount();
                        }
                    }
                    int available = freeSlots * maxStack + existingSlots;
                    if (available < itemsToWithdraw) {
                        player.sendSystemMessage(Component.translatable("message.bank.withdraw.no_space", available));
                        var errPos = player.position();
                        player.serverLevel().sendParticles(ParticleTypes.SMOKE, errPos.x, errPos.y + 1.0, errPos.z, 10, 0.5, 0.5, 0.5, 0.1);
                        return;
                    }

                    ecoData.removeBalance(player.getUUID(), creditsNeeded, "Retrait banque");
                    ItemStack currencyStack = new ItemStack(currencyItem, itemsToWithdraw);
                    if (!player.getInventory().add(currencyStack)) {
                        player.drop(currencyStack, false);
                    }
                    player.playNotifySound(ModSounds.COIN_SPEND.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                    var wdPos = player.position();
                    player.serverLevel().sendParticles(ParticleTypes.ENCHANTED_HIT, wdPos.x, wdPos.y + 1.0, wdPos.z, 10, 0.5, 0.5, 0.5, 0.1);
                }

                // sync balance back to client
                context.reply(new SyncBalancePayload(ecoData.getBalance(player.getUUID())));
            }
        });
    }
}
