package com.citeconomy.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import com.citeconomy.config.CitEconomyConfig;

import java.util.*;

public class EconomyData extends SavedData {
    private static final String DATA_NAME = "citeconomy_data";

    private final Map<UUID, Integer> balances = new HashMap<>();
    private final Map<UUID, Company> companies = new HashMap<>();
    private final Map<UUID, List<TransactionLog>> transactionHistory = new HashMap<>();
    private final List<MarketListing> marketListings = new ArrayList<>();
    private int treasuryBalance = 0;

    private final Map<UUID, List<WeeklyQuest>> playerQuests = new HashMap<>();
    private int currentWeek = 0;
    private int prosperityLevel = 0;
    private long prosperityDecayTick = 0;

    public static EconomyData load(CompoundTag nbt, HolderLookup.Provider provider) {
        EconomyData data = new EconomyData();

        ListTag list = nbt.getList("Balances", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            UUID uuid = entry.getUUID("PlayerUUID");
            int balance = entry.getInt("Balance");
            data.balances.put(uuid, balance);
        }

        ListTag historyList = nbt.getList("TransactionHistory", Tag.TAG_COMPOUND);
        for (int i = 0; i < historyList.size(); i++) {
            CompoundTag entry = historyList.getCompound(i);
            UUID uuid = entry.getUUID("PlayerUUID");
            ListTag logs = entry.getList("Logs", Tag.TAG_COMPOUND);
            data.transactionHistory.put(uuid, TransactionLog.loadList(logs));
        }

        ListTag companyList = nbt.getList("Companies", Tag.TAG_COMPOUND);
        for (int i = 0; i < companyList.size(); i++) {
            Company company = Company.load(companyList.getCompound(i));
            data.companies.put(company.getId(), company);
        }

        ListTag marketList = nbt.getList("MarketListings", Tag.TAG_COMPOUND);
        data.marketListings.addAll(MarketListing.loadList(marketList, provider));

        data.treasuryBalance = nbt.getInt("TreasuryBalance");

        ListTag questList = nbt.getList("PlayerQuests", Tag.TAG_COMPOUND);
        for (int i = 0; i < questList.size(); i++) {
            CompoundTag entry = questList.getCompound(i);
            UUID uuid = entry.getUUID("PlayerUUID");
            List<WeeklyQuest> quests = WeeklyQuest.loadList(entry.getList("Quests", Tag.TAG_COMPOUND));
            data.playerQuests.put(uuid, quests);
        }
        data.currentWeek = nbt.getInt("CurrentWeek");
        data.prosperityLevel = nbt.getInt("ProsperityLevel");
        data.prosperityDecayTick = nbt.getLong("ProsperityDecayTick");

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, Integer> entry : balances.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("PlayerUUID", entry.getKey());
            tag.putInt("Balance", entry.getValue());
            list.add(tag);
        }
        nbt.put("Balances", list);

        ListTag historyList = new ListTag();
        for (Map.Entry<UUID, List<TransactionLog>> entry : transactionHistory.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("PlayerUUID", entry.getKey());
            tag.put("Logs", TransactionLog.saveList(entry.getValue()));
            historyList.add(tag);
        }
        nbt.put("TransactionHistory", historyList);

        ListTag companyList = new ListTag();
        for (Company company : companies.values()) {
            companyList.add(company.save());
        }
        nbt.put("Companies", companyList);

        nbt.put("MarketListings", MarketListing.saveList(marketListings, provider));

        nbt.putInt("TreasuryBalance", treasuryBalance);

        ListTag questList = new ListTag();
        for (Map.Entry<UUID, List<WeeklyQuest>> entry : playerQuests.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("PlayerUUID", entry.getKey());
            tag.put("Quests", WeeklyQuest.saveList(entry.getValue()));
            questList.add(tag);
        }
        nbt.put("PlayerQuests", questList);
        nbt.putInt("CurrentWeek", currentWeek);
        nbt.putInt("ProsperityLevel", prosperityLevel);
        nbt.putLong("ProsperityDecayTick", prosperityDecayTick);

        return nbt;
    }

    public static SavedData.Factory<EconomyData> factory() {
        return new SavedData.Factory<>(EconomyData::new, EconomyData::load, null);
    }

    public static EconomyData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().getLevel(ServerLevel.OVERWORLD);
        if (overworld == null) return new EconomyData();

        return overworld.getDataStorage().computeIfAbsent(
                factory(),
                DATA_NAME
        );
    }

    // Transaction History
    public void logTransaction(UUID playerId, String type, int amount, int balanceAfter, String description) {
        logTransaction(playerId, type, amount, balanceAfter, description, null);
    }

    public void logTransaction(UUID playerId, String type, int amount, int balanceAfter, String description, String secondParty) {
        List<TransactionLog> logs = transactionHistory.computeIfAbsent(playerId, k -> new ArrayList<>());
        logs.add(new TransactionLog(System.currentTimeMillis(), type, amount, balanceAfter, description, secondParty));
        int maxLogs = CitEconomyConfig.SERVER.maxTransactionLogs.get();
        if (logs.size() > maxLogs) {
            logs.remove(0);
        }
        setDirty();
    }

    public List<TransactionLog> getRecentTransactions(UUID playerId, int count) {
        List<TransactionLog> logs = transactionHistory.get(playerId);
        if (logs == null || logs.isEmpty()) return Collections.emptyList();
        int size = logs.size();
        return logs.subList(Math.max(0, size - count), size);
    }

    // Player Accounts
    public int getBalance(UUID uuid) {
        return balances.getOrDefault(uuid, CitEconomyConfig.COMMON.startingBalance.get());
    }

    private int internalSetBalance(UUID uuid, int amount) {
        balances.put(uuid, amount);
        setDirty();
        return amount;
    }

    public void setBalance(UUID uuid, int amount, String reason) {
        int old = getBalance(uuid);
        internalSetBalance(uuid, amount);
        int diff = amount - old;
        logTransaction(uuid, "ADMIN_SET", diff, amount, reason);
    }

    public boolean addBalance(UUID uuid, int amount, String reason) {
        int newBalance = internalSetBalance(uuid, getBalance(uuid) + amount);
        logTransaction(uuid, "CREDIT", amount, newBalance, reason);
        return true;
    }

    public boolean addBalance(UUID uuid, int amount, String reason, String secondParty) {
        int newBalance = internalSetBalance(uuid, getBalance(uuid) + amount);
        logTransaction(uuid, "CREDIT", amount, newBalance, reason, secondParty);
        return true;
    }

    public boolean removeBalance(UUID uuid, int amount, String reason) {
        int current = getBalance(uuid);
        if (current < amount) return false;
        int newBalance = internalSetBalance(uuid, current - amount);
        logTransaction(uuid, "DEBIT", -amount, newBalance, reason);
        return true;
    }

    public boolean removeBalance(UUID uuid, int amount, String reason, String secondParty) {
        int current = getBalance(uuid);
        if (current < amount) return false;
        int newBalance = internalSetBalance(uuid, current - amount);
        logTransaction(uuid, "DEBIT", -amount, newBalance, reason, secondParty);
        return true;
    }

    // Treasury
    public int getTreasuryBalance() {
        return treasuryBalance;
    }

    public void setTreasuryBalance(int amount) {
        this.treasuryBalance = amount;
        setDirty();
    }

    public void addTreasury(int amount) {
        setTreasuryBalance(getTreasuryBalance() + amount);
    }

    public boolean removeTreasury(int amount) {
        int current = getTreasuryBalance();
        if (current >= amount) {
            setTreasuryBalance(current - amount);
            return true;
        }
        return false;
    }

    // Companies
    public Company getCompany(UUID id) {
        return companies.get(id);
    }

    public Company getCompanyByName(String name) {
        for (Company company : companies.values()) {
            if (company.getName().equalsIgnoreCase(name)) {
                return company;
            }
        }
        return null;
    }

    public Company getCompanyByOwner(UUID ownerId) {
        for (Company company : companies.values()) {
            if (company.getOwner().equals(ownerId)) {
                return company;
            }
        }
        return null;
    }

    public void addCompany(Company company) {
        companies.put(company.getId(), company);
        setDirty();
    }

    public void removeCompany(UUID id) {
        companies.remove(id);
        setDirty();
    }

    public Company getCompanyByEmployee(UUID playerId) {
        for (Company c : companies.values()) {
            if (c.isEmployee(playerId)) return c;
        }
        return null;
    }

    public Map<UUID, Company> getCompanies() {
        return companies;
    }

    // Market Listings
    public List<MarketListing> getMarketListings() {
        return marketListings;
    }

    public void addMarketListing(MarketListing listing) {
        marketListings.add(listing);
        setDirty();
    }

    public void removeMarketListing(MarketListing listing) {
        marketListings.remove(listing);
        setDirty();
    }

    public MarketListing getMarketListingById(UUID id) {
        for (MarketListing listing : marketListings) {
            if (listing.getId().equals(id)) return listing;
        }
        return null;
    }

    // Prosperity
    public int getProsperityLevel() { return prosperityLevel; }

    public void setProsperity(int amount) {
        prosperityLevel = Math.max(0, Math.min(100, amount));
        prosperityDecayTick = 0;
        setDirty();
    }

    public void addProsperity(int amount) {
        setProsperity(prosperityLevel + amount);
    }

    public void updateProsperityDecay() {
        prosperityDecayTick++;
        if (prosperityDecayTick >= CitEconomyConfig.SERVER.prosperityDecayInterval.get() && prosperityLevel > 0) {
            prosperityLevel = Math.max(0, prosperityLevel - CitEconomyConfig.SERVER.prosperityDecayAmount.get());
            prosperityDecayTick = 0;
            setDirty();
        }
    }

    public float getTaxMultiplier() {
        return 1.0f - (prosperityLevel / 100.0f * 0.5f);
    }

    public float getSalaryMultiplier() {
        return 1.0f + (prosperityLevel / 100.0f * 0.5f);
    }

    // Weekly Quests
    public int getCurrentWeek() { return currentWeek; }

    public void advanceWeek() {
        currentWeek++;
        playerQuests.clear();
        setDirty();
    }

    public List<WeeklyQuest> getOrCreateQuests(UUID playerId) {
        return playerQuests.computeIfAbsent(playerId, k -> {
            List<WeeklyQuest> quests = WeeklyQuest.generateQuests(currentWeek * 1000);
            setDirty();
            return quests;
        });
    }

    public void markQuestClaimed(UUID playerId, int questId) {
        List<WeeklyQuest> quests = playerQuests.get(playerId);
        if (quests != null) {
            for (WeeklyQuest q : quests) {
                if (q.getId() == questId) {
                    q.setClaimed(true);
                    setDirty();
                    return;
                }
            }
        }
    }

    public void progressQuests(UUID playerId, String itemId, int amount) {
        List<WeeklyQuest> quests = playerQuests.get(playerId);
        if (quests != null) {
            for (WeeklyQuest q : quests) {
                if (!q.isClaimed() && !q.isComplete() && q.getItemId().equals(itemId)) {
                    q.addProgress(amount);
                    setDirty();
                }
            }
        }
    }

    public void runEconomicCycle(ServerLevel level) {
        int totalTaxes = 0;
        int totalSalaries = 0;

        updateProsperityDecay();

        for (Company company : companies.values()) {
            int tax = (int) Math.floor(company.getBalance() * (CitEconomyConfig.SERVER.companyTaxPercent.get() / 100.0) * getTaxMultiplier());
            if (tax > 0) {
                company.removeBalance(tax);
                treasuryBalance += tax;
                totalTaxes += tax;
            }

            int salary = company.getSalary();
            if (salary > 0) {
                int adjustedSalary = (int) (salary * getSalaryMultiplier());
                for (UUID empId : company.getEmployees()) {
                    if (company.removeBalance(adjustedSalary)) {
                        internalSetBalance(empId, getBalance(empId) + adjustedSalary);
                        logTransaction(empId, "SALARY", adjustedSalary, getBalance(empId), "Salaire de '" + company.getName() + "'", company.getName());
                        totalSalaries += adjustedSalary;
                        ServerPlayer p = level.getServer().getPlayerList().getPlayer(empId);
                        if (p != null) {
                            p.sendSystemMessage(Component.translatable("message.company.salary.received", adjustedSalary, company.getName()));
                        }
                    }
                }
            }
        }

        setDirty();
        if (totalTaxes > 0 || totalSalaries > 0) {
            level.getServer().sendSystemMessage(Component.translatable("message.economy.cycle", totalTaxes, totalSalaries));
        }
    }
}
