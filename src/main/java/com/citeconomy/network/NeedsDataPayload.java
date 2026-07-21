package com.citeconomy.network;

import com.citeconomy.CiteconomyMod;
import com.citeconomy.client.ClientNeedsData;
import com.citeconomy.data.WeeklyQuest;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record NeedsDataPayload(List<WeeklyQuest> quests) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<NeedsDataPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CiteconomyMod.MODID, "needs_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, NeedsDataPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, data) -> {
                        buf.writeInt(data.quests.size());
                        for (WeeklyQuest q : data.quests) {
                            buf.writeInt(q.getId());
                            buf.writeUtf(q.getItemId());
                            buf.writeInt(q.getTargetAmount());
                            buf.writeInt(q.getProgress());
                            buf.writeInt(q.getRewardCredits());
                            buf.writeBoolean(q.isClaimed());
                        }
                    },
                    (buf) -> {
                        int size = buf.readInt();
                        List<WeeklyQuest> list = new ArrayList<>();
                        for (int i = 0; i < size; i++) {
                            list.add(new WeeklyQuest(
                                    buf.readInt(), buf.readUtf(), buf.readInt(),
                                    buf.readInt(), buf.readInt(), buf.readBoolean()
                            ));
                        }
                        return new NeedsDataPayload(list);
                    }
            );

    @Override
    public CustomPacketPayload.Type<NeedsDataPayload> type() {
        return TYPE;
    }

    public static void handleData(NeedsDataPayload data, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientNeedsData.quests = data.quests;
        });
    }
}
