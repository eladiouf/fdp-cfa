package com.citeconomy.command;

import com.citeconomy.data.EconomyData;
import com.citeconomy.data.TransactionLog;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class EconomyCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("citeconomy")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("admin")
                        .then(Commands.literal("set")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                .executes(context -> setBalance(context.getSource(), EntityArgument.getPlayers(context, "targets"), IntegerArgumentType.getInteger(context, "amount"))))))
                        .then(Commands.literal("add")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(context -> addBalance(context.getSource(), EntityArgument.getPlayers(context, "targets"), IntegerArgumentType.getInteger(context, "amount"))))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(context -> removeBalance(context.getSource(), EntityArgument.getPlayers(context, "targets"), IntegerArgumentType.getInteger(context, "amount"))))))
                        .then(Commands.literal("history")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(context -> showHistory(context.getSource(), EntityArgument.getPlayers(context, "targets")))))
                )
                .then(Commands.literal("treasury")
                        .then(Commands.literal("set")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(context -> setTreasury(context.getSource(), IntegerArgumentType.getInteger(context, "amount")))))
                        .then(Commands.literal("add")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> addTreasury(context.getSource(), IntegerArgumentType.getInteger(context, "amount")))))
                )
                .then(Commands.literal("prosperity")
                        .then(Commands.literal("set")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0, 100))
                                        .executes(ctx -> {
            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                            EconomyData.get(ctx.getSource().getLevel()).setProsperity(amount);
                            ctx.getSource().sendSuccess(() -> Component.translatable("command.citeconomy.prosperity.set", amount), true);
                                            return 1;
                                        })))
                        .then(Commands.literal("add")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 100))
                                        .executes(ctx -> {
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                            EconomyData data = EconomyData.get(ctx.getSource().getLevel());
                                            data.addProsperity(amount);
                                            ctx.getSource().sendSuccess(() -> Component.translatable("command.citeconomy.prosperity.add", amount, data.getProsperityLevel()), true);
                                            return 1;
                                        })))
                        .then(Commands.literal("get")
                                .executes(ctx -> {
                                    int level = EconomyData.get(ctx.getSource().getLevel()).getProsperityLevel();
                                    ctx.getSource().sendSuccess(() -> Component.translatable("command.citeconomy.prosperity.get", level), true);
                                    return 1;
                                })))
                .then(Commands.literal("week")
                        .then(Commands.literal("advance")
                                .executes(ctx -> {
                                    EconomyData data = EconomyData.get(ctx.getSource().getLevel());
                                    data.advanceWeek();
                                    ctx.getSource().sendSuccess(() -> Component.translatable("command.citeconomy.week.advance"), true);
                                    return 1;
                                })))
        );
        dispatcher.register(Commands.literal("payer")
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(context -> payPlayer(context.getSource(), EntityArgument.getPlayer(context, "target"), IntegerArgumentType.getInteger(context, "amount")))))
        );
        dispatcher.register(Commands.literal("historique")
                .executes(context -> showMyHistory(context.getSource()))
        );
        dispatcher.register(Commands.literal("solde")
                .executes(context -> showBalance(context.getSource()))
        );
    }

    private static int payPlayer(CommandSourceStack source, ServerPlayer target, int amount) {
        if (!source.isPlayer()) {
            source.sendFailure(Component.translatable("command.citeconomy.pay.player_only"));
            return 0;
        }

        ServerPlayer sender = source.getPlayer();
        if (sender.getUUID().equals(target.getUUID())) {
            source.sendFailure(Component.translatable("command.citeconomy.pay.self"));
            return 0;
        }

        ServerLevel level = source.getLevel();
        EconomyData data = EconomyData.get(level);

        if (data.removeBalance(sender.getUUID(), amount, "Paiement à " + target.getName().getString(), target.getName().getString())) {
            data.addBalance(target.getUUID(), amount, "Paiement de " + sender.getName().getString(), sender.getName().getString());
            source.sendSuccess(() -> Component.translatable("command.citeconomy.pay.success", amount, target.getName().getString()), true);
            target.sendSystemMessage(Component.translatable("command.citeconomy.pay.received", amount, sender.getName().getString()));
            return 1;
        } else {
            source.sendFailure(Component.translatable("command.citeconomy.pay.no_funds"));
            return 0;
        }
    }

    private static int setBalance(CommandSourceStack source, Collection<ServerPlayer> targets, int amount) {
        ServerLevel level = source.getLevel();
        EconomyData data = EconomyData.get(level);
        for (ServerPlayer player : targets) {
            data.setBalance(player.getUUID(), amount, "Admin set");
            source.sendSuccess(() -> Component.translatable("command.citeconomy.admin.set", player.getName().getString(), amount), true);
        }
        return targets.size();
    }

    private static int addBalance(CommandSourceStack source, Collection<ServerPlayer> targets, int amount) {
        ServerLevel level = source.getLevel();
        EconomyData data = EconomyData.get(level);
        for (ServerPlayer player : targets) {
            data.addBalance(player.getUUID(), amount, "Admin add");
            int newBalance = data.getBalance(player.getUUID());
            source.sendSuccess(() -> Component.translatable("command.citeconomy.admin.add", amount, player.getName().getString(), newBalance), true);
        }
        return targets.size();
    }

    private static int removeBalance(CommandSourceStack source, Collection<ServerPlayer> targets, int amount) {
        ServerLevel level = source.getLevel();
        EconomyData data = EconomyData.get(level);
        for (ServerPlayer player : targets) {
            data.removeBalance(player.getUUID(), amount, "Admin remove");
            int newBalance = data.getBalance(player.getUUID());
            source.sendSuccess(() -> Component.translatable("command.citeconomy.admin.remove", amount, player.getName().getString(), newBalance), true);
        }
        return targets.size();
    }

    private static int setTreasury(CommandSourceStack source, int amount) {
        ServerLevel level = source.getLevel();
        EconomyData data = EconomyData.get(level);
        data.setTreasuryBalance(amount);
        source.sendSuccess(() -> Component.translatable("command.citeconomy.treasury.set", amount), true);
        return 1;
    }

    private static int addTreasury(CommandSourceStack source, int amount) {
        ServerLevel level = source.getLevel();
        EconomyData data = EconomyData.get(level);
        data.addTreasury(amount);
        source.sendSuccess(() -> Component.translatable("command.citeconomy.treasury.add", amount, data.getTreasuryBalance()), true);
        return 1;
    }

    private static int showHistory(CommandSourceStack source, Collection<ServerPlayer> targets) {
        ServerLevel level = source.getLevel();
        EconomyData data = EconomyData.get(level);
        for (ServerPlayer player : targets) {
            source.sendSuccess(() -> Component.translatable("command.citeconomy.history.title.other", player.getName().getString()), false);
            List<TransactionLog> history = data.getRecentTransactions(player.getUUID(), 20);
            if (history.isEmpty()) {
                source.sendSuccess(() -> Component.translatable("command.citeconomy.history.none"), false);
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                for (TransactionLog log : history) {
                    String time = sdf.format(new Date(log.timestamp()));
                    String sign = log.amount() >= 0 ? "§a+" : "§c";
                    String second = log.secondParty() != null && !log.secondParty().isEmpty() ? " (" + log.secondParty() + ")" : "";
                    source.sendSuccess(() -> Component.translatable("command.citeconomy.history.entry", time, sign, log.amount(), log.balanceAfter(), log.description() + second), false);
                }
            }
        }
        return targets.size();
    }

    private static int showMyHistory(CommandSourceStack source) {
        if (!source.isPlayer()) return 0;
        ServerPlayer player = source.getPlayer();
        EconomyData data = EconomyData.get(source.getLevel());

        source.sendSuccess(() -> Component.translatable("command.citeconomy.history.title", 10), false);
        List<TransactionLog> history = data.getRecentTransactions(player.getUUID(), 10);
        if (history.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("command.citeconomy.history.none"), false);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            for (TransactionLog log : history) {
                String time = sdf.format(new Date(log.timestamp()));
                String sign = log.amount() >= 0 ? "§a+" : "§c";
                String second = log.secondParty() != null && !log.secondParty().isEmpty() ? " (" + log.secondParty() + ")" : "";
                source.sendSuccess(() -> Component.translatable("command.citeconomy.history.entry", time, sign, log.amount(), log.balanceAfter(), log.description() + second), false);
            }
        }
        return 1;
    }

    private static int showBalance(CommandSourceStack source) {
        if (!source.isPlayer()) return 0;
        ServerPlayer player = source.getPlayer();
        EconomyData data = EconomyData.get(source.getLevel());
        int balance = data.getBalance(player.getUUID());
        source.sendSuccess(() -> Component.translatable("command.citeconomy.balance.display", balance), false);
        return 1;
    }
}
