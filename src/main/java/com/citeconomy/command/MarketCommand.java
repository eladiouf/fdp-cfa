package com.citeconomy.command;

import com.citeconomy.data.EconomyData;
import com.citeconomy.data.MarketListing;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class MarketCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("marche")
                .then(Commands.literal("vendre")
                        .then(Commands.argument("prix", IntegerArgumentType.integer(1))
                                .executes(context -> sellItem(context.getSource(), IntegerArgumentType.getInteger(context, "prix")))))
        );
    }

    private static int sellItem(CommandSourceStack source, int price) {
        if (!source.isPlayer()) return 0;
        ServerPlayer player = source.getPlayer();
        ItemStack held = player.getMainHandItem();

        if (held.isEmpty()) {
            source.sendFailure(Component.translatable("command.market.sell.hold_item"));
            return 0;
        }

        ServerLevel level = source.getLevel();
        EconomyData data = EconomyData.get(level);

        ItemStack toSell = held.copyWithCount(1);
        String itemName = toSell.getHoverName().getString();

        MarketListing listing = new MarketListing(
                UUID.randomUUID(),
                player.getUUID(),
                player.getName().getString(),
                toSell,
                price,
                System.currentTimeMillis()
        );

        held.shrink(1);
        data.addMarketListing(listing);

        source.sendSuccess(() -> Component.translatable("command.market.sell.success", itemName, price), true);
        return 1;
    }
}
