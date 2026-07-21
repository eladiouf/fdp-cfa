package com.citeconomy.network;

import com.citeconomy.CiteconomyMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.citeconomy.client.ClientState;

public record SyncBalancePayload(int balance) implements CustomPacketPayload {
    public static final Type<SyncBalancePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CiteconomyMod.MODID, "sync_balance"));

    public static final StreamCodec<FriendlyByteBuf, SyncBalancePayload> STREAM_CODEC = StreamCodec.composite(
            net.minecraft.network.codec.ByteBufCodecs.INT, SyncBalancePayload::balance,
            SyncBalancePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(final SyncBalancePayload data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientState.setBalance(data.balance());
        });
    }
}
