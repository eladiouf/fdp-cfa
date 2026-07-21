package com.citeconomy.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record TransactionLog(long timestamp, String type, int amount, int balanceAfter, String description, String secondParty) {

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("Timestamp", timestamp);
        tag.putString("Type", type);
        tag.putInt("Amount", amount);
        tag.putInt("BalanceAfter", balanceAfter);
        tag.putString("Description", description != null ? description : "");
        tag.putString("SecondParty", secondParty != null ? secondParty : "");
        return tag;
    }

    public static TransactionLog load(CompoundTag tag) {
        return new TransactionLog(
                tag.getLong("Timestamp"),
                tag.getString("Type"),
                tag.getInt("Amount"),
                tag.getInt("BalanceAfter"),
                tag.getString("Description"),
                tag.getString("SecondParty")
        );
    }

    public static ListTag saveList(List<TransactionLog> logs) {
        ListTag list = new ListTag();
        for (TransactionLog log : logs) {
            list.add(log.save());
        }
        return list;
    }

    public static List<TransactionLog> loadList(ListTag tag) {
        List<TransactionLog> logs = new ArrayList<>();
        for (int i = 0; i < tag.size(); i++) {
            logs.add(load(tag.getCompound(i)));
        }
        return logs;
    }
}
