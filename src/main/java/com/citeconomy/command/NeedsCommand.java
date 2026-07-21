package com.citeconomy.command;

import com.citeconomy.data.EconomyData;
import com.citeconomy.data.WeeklyQuest;
import com.citeconomy.menu.NeedsMenu;
import com.citeconomy.network.NeedsDataPayload;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class NeedsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("besoins")
                .executes(ctx -> {
                    CommandSourceStack src = ctx.getSource();
                    if (!(src.getEntity() instanceof ServerPlayer player)) {
                        src.sendFailure(Component.translatable("command.needs.player_only"));
                        return 0;
                    }

                    EconomyData data = EconomyData.get(player.serverLevel());
                    List<WeeklyQuest> quests = data.getOrCreateQuests(player.getUUID());

                    player.openMenu(new SimpleMenuProvider(
                            (id, inv, p) -> new NeedsMenu(id, inv),
                            Component.translatable("container.citeconomy.needs")
                    ));

                    PacketDistributor.sendToPlayer(player, new NeedsDataPayload(quests));
                    return 1;
                })
        );
    }
}
