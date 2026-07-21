package com.citeconomy.network;

import com.citeconomy.CiteconomyMod;
import com.citeconomy.block.entity.PersonalShopBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet C2S : fixe le prix d'un slot spécifique dans la boutique du joueur.
 * Envoyé par le propriétaire quand il clique sur un slot et entre un prix.
 * Le prix est clampé entre 0 (pas en vente) et 100 000 crédits.
 * Seul le propriétaire du bloc peut exécuter cette action.
 */
public record SetShopPricePayload(BlockPos pos, int slot, int price) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SetShopPricePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CiteconomyMod.MODID, "set_shop_price"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetShopPricePayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, data) -> {
                        buf.writeBlockPos(data.pos);
                        buf.writeInt(data.slot);
                        buf.writeInt(data.price);
                    },
                    (buf) -> new SetShopPricePayload(buf.readBlockPos(), buf.readInt(), buf.readInt())
            );

    @Override
    public CustomPacketPayload.Type<SetShopPricePayload> type() {
        return TYPE;
    }

    public static void handleData(SetShopPricePayload data, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (!player.level().isLoaded(data.pos)) return;

            BlockEntity be = player.level().getBlockEntity(data.pos);
            if (!(be instanceof PersonalShopBlockEntity shop)) return;
            if (!player.getUUID().equals(shop.getOwnerId())) return;
            if (data.slot < 0 || data.slot >= 27) return;

            shop.setSlotPrice(data.slot, data.price);
            if (data.price > 0) {
                player.sendSystemMessage(Component.translatable("message.shop.price_set", data.slot + 1, data.price));
            } else {
                player.sendSystemMessage(Component.translatable("message.shop.price_removed", data.slot + 1));
            }
        });
    }
}
