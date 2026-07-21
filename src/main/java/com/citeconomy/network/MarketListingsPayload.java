package com.citeconomy.network;

import com.citeconomy.CiteconomyMod;
import com.citeconomy.data.MarketListing;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record MarketListingsPayload(List<Entry> entries) implements CustomPacketPayload {
    public record Entry(UUID listingId, ItemStack item, int price, String sellerName) {}

    public static final Type<MarketListingsPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CiteconomyMod.MODID, "market_listings"));

    public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, MarketListingsPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeVarInt(payload.entries().size());
                for (Entry e : payload.entries()) {
                    buf.writeUUID(e.listingId());
                    ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, e.item());
                    buf.writeVarInt(e.price());
                    buf.writeUtf(e.sellerName());
                }
            },
            (buf) -> {
                int size = buf.readVarInt();
                List<Entry> list = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    UUID id = buf.readUUID();
                    ItemStack item = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
                    int price = buf.readVarInt();
                    String name = buf.readUtf();
                    list.add(new Entry(id, item, price, name));
                }
                return new MarketListingsPayload(list);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(final MarketListingsPayload data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            com.citeconomy.client.ClientMarketListings.setListings(data.entries());
        });
    }
}
