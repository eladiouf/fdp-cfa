package com.citeconomy.command;

import com.citeconomy.menu.AdminShopMenu;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;

public class AdminShopCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("adminshop")
                .executes(ctx -> {
                    CommandSourceStack src = ctx.getSource();
                    if (!(src.getEntity() instanceof ServerPlayer player)) {
                        src.sendFailure(Component.translatable("command.adminshop.player_only"));
                        return 0;
                    }

                    player.openMenu(new SimpleMenuProvider(
                            (id, inv, p) -> new AdminShopMenu(id, inv),
                            Component.translatable("container.citeconomy.admin_shop")
                    ));
                    return 1;
                })
        );
    }
}
