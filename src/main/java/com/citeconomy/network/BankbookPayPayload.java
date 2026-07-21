package com.citeconomy.network;

import com.citeconomy.CiteconomyMod;
import com.citeconomy.data.EconomyData;
import com.citeconomy.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;

import java.util.UUID;

public record BankbookPayPayload(UUID targetId, int amount) implements CustomPacketPayload {
    public static final Type<BankbookPayPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CiteconomyMod.MODID, "bankbook_pay"));

    public static final StreamCodec<FriendlyByteBuf, BankbookPayPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, BankbookPayPayload::targetId,
            net.minecraft.network.codec.ByteBufCodecs.INT, BankbookPayPayload::amount,
            BankbookPayPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(final BankbookPayPayload data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sender) {
                if (data.amount() <= 0) {
                    sender.sendSystemMessage(Component.translatable("message.bankbook.invalid_amount"));
                    return;
                }

                EconomyData ecoData = EconomyData.get(sender.serverLevel());
                int senderBalance = ecoData.getBalance(sender.getUUID());

                if (senderBalance < data.amount()) {
                    sender.playNotifySound(ModSounds.ECONOMY_ERROR.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                    sender.sendSystemMessage(Component.translatable("message.bankbook.insufficient_balance"));
                    var errPos = sender.position();
                    sender.serverLevel().sendParticles(ParticleTypes.SMOKE, errPos.x, errPos.y + 1.0, errPos.z, 10, 0.5, 0.5, 0.5, 0.1);
                    return;
                }

                ServerPlayer target = sender.server.getPlayerList().getPlayer(data.targetId());
                if (target == null) {
                    sender.sendSystemMessage(Component.translatable("message.bankbook.recipient_offline"));
                    return;
                }

                if (target.getUUID().equals(sender.getUUID())) {
                    sender.sendSystemMessage(Component.translatable("message.bankbook.self_transfer"));
                    return;
                }

                // Perform transfer
                ecoData.removeBalance(sender.getUUID(), data.amount(), "Virement envoyé à " + target.getName().getString(), target.getName().getString());
                ecoData.addBalance(target.getUUID(), data.amount(), "Virement reçu de " + sender.getName().getString(), sender.getName().getString());

                sender.sendSystemMessage(Component.translatable("message.bankbook.transfer.success", data.amount(), target.getName().getString()));
                target.sendSystemMessage(Component.translatable("message.bankbook.transfer.received", data.amount(), sender.getName().getString()));

                sender.playNotifySound(ModSounds.COIN_SPEND.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                target.playNotifySound(ModSounds.COIN_RECEIVE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);

                ServerLevel serverLevel = sender.serverLevel();
                var senderPos = sender.position();
                serverLevel.sendParticles(ParticleTypes.SMOKE, senderPos.x, senderPos.y + 1.0, senderPos.z, 10, 0.5, 0.5, 0.5, 0.1);
                var targetPos = target.position();
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, targetPos.x, targetPos.y + 1.0, targetPos.z, 10, 0.5, 0.5, 0.5, 0.1);

                // Sync balance to both
                context.reply(new SyncBalancePayload(ecoData.getBalance(sender.getUUID())));
                target.connection.send(new SyncBalancePayload(ecoData.getBalance(target.getUUID())));
            }
        });
    }
}
