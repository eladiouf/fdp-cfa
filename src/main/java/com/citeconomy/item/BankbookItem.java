package com.citeconomy.item;

import com.citeconomy.data.EconomyData;
import com.citeconomy.data.TransactionLog;
import com.citeconomy.menu.BankbookMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import java.util.List;

public class BankbookItem extends Item {
    public BankbookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemstack = player.getItemInHand(usedHand);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            EconomyData data = EconomyData.get(serverPlayer.serverLevel());
            int balance = data.getBalance(serverPlayer.getUUID());
            List<TransactionLog> logs = data.getRecentTransactions(serverPlayer.getUUID(), 5);

            serverPlayer.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new BankbookMenu(id, balance, logs),
                    Component.translatable("container.citeconomy.bankbook")
            ), buf -> {
                buf.writeInt(balance);
                buf.writeInt(logs.size());
                for (TransactionLog log : logs) {
                    buf.writeLong(log.timestamp());
                    buf.writeUtf(log.type() != null ? log.type() : "");
                    buf.writeInt(log.amount());
                    buf.writeInt(log.balanceAfter());
                    buf.writeUtf(log.description() != null ? log.description() : "");
                    buf.writeUtf(log.secondParty() != null ? log.secondParty() : "");
                }
            });
        }
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
}
