package com.citeconomy.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class WeeklyQuest {
    private static final Random RANDOM = new Random();

    private final int id;
    private final String itemId;
    private final int targetAmount;
    private int progress;
    private final int rewardCredits;
    private boolean claimed;

    public WeeklyQuest(int id, String itemId, int targetAmount, int rewardCredits) {
        this(id, itemId, targetAmount, 0, rewardCredits, false);
    }

    public WeeklyQuest(int id, String itemId, int targetAmount, int progress, int rewardCredits, boolean claimed) {
        this.id = id;
        this.itemId = itemId;
        this.targetAmount = targetAmount;
        this.progress = progress;
        this.rewardCredits = rewardCredits;
        this.claimed = claimed;
    }

    public static List<WeeklyQuest> generateQuests(int startId) {
        List<QuestTemplate> pool = getQuestPool();
        Collections.shuffle(pool, RANDOM);
        List<WeeklyQuest> quests = new ArrayList<>();
        int count = Math.min(3, pool.size());
        for (int i = 0; i < count; i++) {
            QuestTemplate t = pool.get(i);
            quests.add(new WeeklyQuest(startId + i, t.itemId(), t.amount(), t.reward()));
        }
        return quests;
    }

    private record QuestTemplate(String itemId, int amount, int reward) {}

    private static List<QuestTemplate> getQuestPool() {
        return List.of(
            new QuestTemplate("minecraft:wheat", 16, 20),
            new QuestTemplate("minecraft:carrot", 16, 20),
            new QuestTemplate("minecraft:potato", 16, 20),
            new QuestTemplate("minecraft:bread", 8, 15),
            new QuestTemplate("minecraft:cooked_beef", 8, 25),
            new QuestTemplate("minecraft:bone", 16, 15),
            new QuestTemplate("minecraft:string", 16, 15),
            new QuestTemplate("minecraft:coal", 16, 20),
            new QuestTemplate("minecraft:iron_ingot", 8, 30),
            new QuestTemplate("minecraft:gold_ingot", 4, 40),
            new QuestTemplate("minecraft:stone", 32, 15),
            new QuestTemplate("minecraft:oak_log", 32, 15),
            new QuestTemplate("minecraft:cobblestone", 32, 15)
        );
    }

    public static Item getItemById(String id) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
    }

    public int getId() { return id; }
    public String getItemId() { return itemId; }
    public int getTargetAmount() { return targetAmount; }
    public int getProgress() { return progress; }
    public int getRewardCredits() { return rewardCredits; }
    public boolean isClaimed() { return claimed; }
    public boolean isComplete() { return progress >= targetAmount; }

    public void setProgress(int progress) { this.progress = Math.min(progress, targetAmount); }
    public void addProgress(int amount) { this.progress = Math.min(this.progress + amount, targetAmount); }
    public void setClaimed(boolean claimed) { this.claimed = claimed; }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Id", id);
        tag.putString("ItemId", itemId);
        tag.putInt("TargetAmount", targetAmount);
        tag.putInt("Progress", progress);
        tag.putInt("RewardCredits", rewardCredits);
        tag.putBoolean("Claimed", claimed);
        return tag;
    }

    public static WeeklyQuest load(CompoundTag tag) {
        return new WeeklyQuest(
            tag.getInt("Id"),
            tag.getString("ItemId"),
            tag.getInt("TargetAmount"),
            tag.getInt("Progress"),
            tag.getInt("RewardCredits"),
            tag.getBoolean("Claimed")
        );
    }

    public static ListTag saveList(List<WeeklyQuest> quests) {
        ListTag list = new ListTag();
        for (WeeklyQuest q : quests) list.add(q.save());
        return list;
    }

    public static List<WeeklyQuest> loadList(ListTag list) {
        List<WeeklyQuest> quests = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            quests.add(load(list.getCompound(i)));
        }
        return quests;
    }
}
