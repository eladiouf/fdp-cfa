package com.citeconomy.network;

import com.citeconomy.CiteconomyMod;
import com.citeconomy.data.EconomyData;
import com.citeconomy.data.WeeklyQuest;
import com.citeconomy.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record QuestCompletePayload(int questId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<QuestCompletePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CiteconomyMod.MODID, "quest_complete"));

    public static final StreamCodec<RegistryFriendlyByteBuf, QuestCompletePayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, data) -> buf.writeInt(data.questId),
                    (buf) -> new QuestCompletePayload(buf.readInt())
            );

    @Override
    public CustomPacketPayload.Type<QuestCompletePayload> type() {
        return TYPE;
    }

    public static void handleData(QuestCompletePayload data, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            EconomyData economyData = EconomyData.get(player.serverLevel());
            List<WeeklyQuest> quests = economyData.getOrCreateQuests(player.getUUID());

            for (WeeklyQuest quest : quests) {
                if (quest.getId() != data.questId || quest.isClaimed()) continue;

                if (quest.isComplete()) {
                    economyData.addBalance(player.getUUID(), quest.getRewardCredits(), "Besoin complété: " + quest.getItemId());
                    economyData.addProsperity(3);
                    economyData.markQuestClaimed(player.getUUID(), quest.getId());
                    player.playNotifySound(ModSounds.ECONOMY_SUCCESS.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                    player.sendSystemMessage(Component.translatable("message.quest.complete", quest.getRewardCredits()));
                    var qPos = player.position();
                    player.serverLevel().sendParticles(ParticleTypes.TOTEM_OF_UNDYING, qPos.x, qPos.y + 1.0, qPos.z, 20, 1.0, 1.0, 1.0, 0.5);
                    return;
                }

                ItemStack expectedItem = new ItemStack(WeeklyQuest.getItemById(quest.getItemId()), 1);
                int held = 0;
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack slot = player.getInventory().getItem(i);
                    if (ItemStack.isSameItem(expectedItem, slot)) {
                        held += slot.getCount();
                    }
                }

                int needed = quest.getTargetAmount() - quest.getProgress();
                int toRemove = Math.min(needed, held);
                if (toRemove <= 0) {
                    player.sendSystemMessage(Component.translatable("message.quest.no_resources"));
                    var errPos = player.position();
                    player.serverLevel().sendParticles(ParticleTypes.SMOKE, errPos.x, errPos.y + 1.0, errPos.z, 10, 0.5, 0.5, 0.5, 0.1);
                    return;
                }

                int remaining = toRemove;
                for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
                    ItemStack slot = player.getInventory().getItem(i);
                    if (ItemStack.isSameItem(expectedItem, slot)) {
                        int take = Math.min(remaining, slot.getCount());
                        slot.shrink(take);
                        remaining -= take;
                    }
                }

                quest.addProgress(toRemove);
                economyData.setDirty();

                if (quest.isComplete()) {
                    economyData.addBalance(player.getUUID(), quest.getRewardCredits(), "Besoin complété: " + quest.getItemId());
                    economyData.addProsperity(3);
                    economyData.markQuestClaimed(player.getUUID(), quest.getId());
                    player.playNotifySound(ModSounds.ECONOMY_SUCCESS.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                    player.sendSystemMessage(Component.translatable("message.quest.complete", quest.getRewardCredits()));
                    var qPos = player.position();
                    player.serverLevel().sendParticles(ParticleTypes.TOTEM_OF_UNDYING, qPos.x, qPos.y + 1.0, qPos.z, 20, 1.0, 1.0, 1.0, 0.5);
                } else {
                    player.sendSystemMessage(Component.translatable("message.quest.progress", quest.getProgress(), quest.getTargetAmount()));
                }
                return;
            }
        });
    }
}
