package com.citeconomy.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.HolderLookup;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MarketListing {
    private final UUID id;
    private final UUID sellerId;
    private final String sellerName;
    private ItemStack itemStack;
    private int price;
    private final long timestamp;

    public MarketListing(UUID id, UUID sellerId, String sellerName, ItemStack itemStack, int price, long timestamp) {
        this.id = id;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.itemStack = itemStack;
        this.price = price;
        this.timestamp = timestamp;
    }

    public UUID getId() { return id; }
    public UUID getSellerId() { return sellerId; }
    public String getSellerName() { return sellerName; }
    public ItemStack getItemStack() { return itemStack; }
    public int getPrice() { return price; }
    public long getTimestamp() { return timestamp; }

    public CompoundTag save(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", id);
        tag.putUUID("SellerId", sellerId);
        tag.putString("SellerName", sellerName);
        tag.put("Item", itemStack.save(provider));
        tag.putInt("Price", price);
        tag.putLong("Timestamp", timestamp);
        return tag;
    }

    public static MarketListing load(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        UUID id = tag.getUUID("Id");
        UUID sellerId = tag.getUUID("SellerId");
        String sellerName = tag.getString("SellerName");
        ItemStack item = ItemStack.parseOptional(provider, tag.getCompound("Item"));
        int price = tag.getInt("Price");
        long timestamp = tag.getLong("Timestamp");
        return new MarketListing(id, sellerId, sellerName, item, price, timestamp);
    }

    public static ListTag saveList(List<MarketListing> listings, net.minecraft.core.HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (MarketListing listing : listings) {
            list.add(listing.save(provider));
        }
        return list;
    }

    public static List<MarketListing> loadList(ListTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        List<MarketListing> listings = new ArrayList<>();
        for (int i = 0; i < tag.size(); i++) {
            listings.add(load(tag.getCompound(i), provider));
        }
        return listings;
    }
}
