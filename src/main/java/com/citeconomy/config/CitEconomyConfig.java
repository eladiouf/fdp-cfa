package com.citeconomy.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class CitEconomyConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final Common COMMON;
    public static final Server SERVER;

    public static final ModConfigSpec COMMON_SPEC;
    public static final ModConfigSpec SERVER_SPEC;

    static {
        BUILDER.push("common");
        COMMON = new Common(BUILDER);
        BUILDER.pop();
        COMMON_SPEC = BUILDER.build();

        BUILDER.push("server");
        SERVER = new Server(BUILDER);
        BUILDER.pop();
        SERVER_SPEC = BUILDER.build();
    }

    public static class Common {
        public final ModConfigSpec.IntValue startingBalance;
        public final ModConfigSpec.IntValue maxBalance;

        Common(ModConfigSpec.Builder builder) {
            startingBalance = builder
                    .comment("Starting credits for new players")
                    .defineInRange("startingBalance", 100, 0, 100000);
            maxBalance = builder
                    .comment("Maximum credits a player can hold")
                    .defineInRange("maxBalance", 1000000, 100, 10000000);
        }
    }

    public static class Server {
        public final ModConfigSpec.DoubleValue shopBuyTaxPercent;
        public final ModConfigSpec.DoubleValue marketTaxPercent;
        public final ModConfigSpec.DoubleValue adminShopTaxPercent;
        public final ModConfigSpec.IntValue economicCycleInterval;
        public final ModConfigSpec.IntValue prosperityDecayInterval;
        public final ModConfigSpec.IntValue prosperityDecayAmount;
        public final ModConfigSpec.DoubleValue companyTaxPercent;
        public final ModConfigSpec.IntValue maxTransactionLogs;
        public final ModConfigSpec.IntValue maxMarketListings;
        public final ModConfigSpec.ConfigValue<String> currencyItem;
        public final ModConfigSpec.IntValue exchangeRate;

        Server(ModConfigSpec.Builder builder) {
            shopBuyTaxPercent = builder
                    .comment("Tax percentage on personal shop purchases")
                    .defineInRange("shopBuyTaxPercent", 2.0, 0.0, 50.0);
            marketTaxPercent = builder
                    .comment("Tax percentage on market purchases")
                    .defineInRange("marketTaxPercent", 5.0, 0.0, 50.0);
            adminShopTaxPercent = builder
                    .comment("Tax percentage on admin shop purchases")
                    .defineInRange("adminShopTaxPercent", 0.0, 0.0, 50.0);
            economicCycleInterval = builder
                    .comment("Ticks between economic cycles (20 min default)")
                    .defineInRange("economicCycleInterval", 24000, 100, 72000);
            prosperityDecayInterval = builder
                    .comment("Ticks of no activity before prosperity decays by 1")
                    .defineInRange("prosperityDecayInterval", 10, 1, 24000);
            prosperityDecayAmount = builder
                    .comment("How much prosperity decays per interval")
                    .defineInRange("prosperityDecayAmount", 1, 1, 10);
            companyTaxPercent = builder
                    .comment("Tax percentage on company balance per cycle")
                    .defineInRange("companyTaxPercent", 10.0, 0.0, 100.0);
            maxTransactionLogs = builder
                    .comment("Max transaction logs kept per player")
                    .defineInRange("maxTransactionLogs", 50, 10, 500);
            maxMarketListings = builder
                    .comment("Max active market listings")
                    .defineInRange("maxMarketListings", 100, 10, 1000);
            currencyItem = builder
                    .comment("Item used for physical currency")
                    .define("currencyItem", "minecraft:emerald");
            exchangeRate = builder
                    .comment("How many credits per currency item")
                    .defineInRange("exchangeRate", 100, 1, 10000);
        }
    }
}
