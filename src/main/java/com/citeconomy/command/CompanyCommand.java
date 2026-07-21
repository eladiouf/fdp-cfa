package com.citeconomy.command;

import com.citeconomy.data.Company;
import com.citeconomy.data.EconomyData;
import com.citeconomy.registry.ModSounds;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CompanyCommand {
    private static final Map<UUID, UUID> pendingInvites = new HashMap<>();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("entreprise")
                .then(Commands.literal("creer")
                        .then(Commands.argument("nom", StringArgumentType.string())
                                .executes(context -> createCompany(context.getSource(), StringArgumentType.getString(context, "nom")))))
                .then(Commands.literal("inviter")
                        .then(Commands.argument("joueur", EntityArgument.player())
                                .executes(context -> invitePlayer(context.getSource(), EntityArgument.getPlayer(context, "joueur")))))
                .then(Commands.literal("accepter")
                        .executes(context -> acceptInvite(context.getSource())))
                .then(Commands.literal("deposer")
                        .then(Commands.argument("montant", IntegerArgumentType.integer(1))
                                .executes(context -> depositCompany(context.getSource(), IntegerArgumentType.getInteger(context, "montant")))))
                .then(Commands.literal("retirer")
                        .then(Commands.argument("montant", IntegerArgumentType.integer(1))
                                .executes(context -> withdrawCompany(context.getSource(), IntegerArgumentType.getInteger(context, "montant")))))
                .then(Commands.literal("salaire")
                        .then(Commands.argument("montant", IntegerArgumentType.integer(0))
                                .executes(context -> setSalary(context.getSource(), IntegerArgumentType.getInteger(context, "montant")))))
                .then(Commands.literal("quitter")
                        .executes(context -> leaveCompany(context.getSource())))
                .then(Commands.literal("dissoudre")
                        .executes(context -> dissolveCompany(context.getSource())))
                .then(Commands.literal("infos")
                        .executes(context -> infoCompany(context.getSource())))
        );
    }

    private static int createCompany(CommandSourceStack source, String name) {
        if (!source.isPlayer()) return 0;
        ServerPlayer player = source.getPlayer();
        EconomyData data = EconomyData.get(source.getLevel());

        if (data.getCompanyByOwner(player.getUUID()) != null) {
            source.sendFailure(Component.translatable("command.company.create.already_owned"));
            return 0;
        }
        if (data.getCompanyByName(name) != null) {
            source.sendFailure(Component.translatable("command.company.create.name_taken"));
            return 0;
        }

        Company company = new Company(UUID.randomUUID(), name, player.getUUID());
        company.addEmployee(player.getUUID());
        data.addCompany(company);

        player.playNotifySound(ModSounds.COMPANY_CREATE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        source.sendSuccess(() -> Component.translatable("command.company.create.success", name), true);
        return 1;
    }

    private static Company requireOwnCompany(EconomyData data, ServerPlayer player, CommandSourceStack source) {
        Company company = data.getCompanyByOwner(player.getUUID());
        if (company == null) {
            source.sendFailure(Component.translatable("command.company.not_owned"));
        }
        return company;
    }

    private static Company requireCompany(EconomyData data, ServerPlayer player, CommandSourceStack source) {
        for (Company c : data.getCompanies().values()) {
            if (c.isEmployee(player.getUUID())) return c;
        }
        source.sendFailure(Component.translatable("command.company.not_member"));
        return null;
    }

    private static int invitePlayer(CommandSourceStack source, ServerPlayer target) {
        if (!source.isPlayer()) return 0;
        ServerPlayer player = source.getPlayer();
        EconomyData data = EconomyData.get(source.getLevel());

        Company company = requireOwnCompany(data, player, source);
        if (company == null) return 0;

        if (company.isEmployee(target.getUUID())) {
            source.sendFailure(Component.translatable("command.company.invite.already_employee"));
            return 0;
        }

        pendingInvites.entrySet().removeIf(e -> !e.getValue().equals(company.getId()));
        pendingInvites.put(target.getUUID(), company.getId());
        source.sendSuccess(() -> Component.translatable("command.company.invite.sent", target.getName().getString()), true);
        target.sendSystemMessage(Component.translatable("command.company.invite.received", player.getName().getString(), company.getName()));
        return 1;
    }

    private static int acceptInvite(CommandSourceStack source) {
        if (!source.isPlayer()) return 0;
        ServerPlayer player = source.getPlayer();
        UUID companyId = pendingInvites.get(player.getUUID());

        if (companyId == null) {
            source.sendFailure(Component.translatable("command.company.invite.none"));
            return 0;
        }

        EconomyData data = EconomyData.get(source.getLevel());
        Company company = data.getCompany(companyId);

        if (company != null) {
            company.addEmployee(player.getUUID());
            data.setDirty();
            source.sendSuccess(() -> Component.translatable("command.company.join.success", company.getName()), true);
        }

        pendingInvites.remove(player.getUUID());
        return 1;
    }

    private static int depositCompany(CommandSourceStack source, int amount) {
        if (!source.isPlayer()) return 0;
        ServerPlayer player = source.getPlayer();
        EconomyData data = EconomyData.get(source.getLevel());

        Company company = requireCompany(data, player, source);
        if (company == null) return 0;

        if (data.removeBalance(player.getUUID(), amount, "Dépôt entreprise '" + company.getName() + "'", company.getName())) {
            company.deposit(amount);
            data.setDirty();
            source.sendSuccess(() -> Component.translatable("command.company.deposit.success", amount, company.getName(), company.getBalance()), true);
            return 1;
        } else {
            source.sendFailure(Component.translatable("command.company.deposit.no_funds"));
            return 0;
        }
    }

    private static int withdrawCompany(CommandSourceStack source, int amount) {
        if (!source.isPlayer()) return 0;
        ServerPlayer player = source.getPlayer();
        EconomyData data = EconomyData.get(source.getLevel());

        Company company = requireOwnCompany(data, player, source);
        if (company == null) return 0;

        if (company.withdraw(amount)) {
            data.addBalance(player.getUUID(), amount, "Retrait entreprise '" + company.getName() + "'", company.getName());
            data.setDirty();
            source.sendSuccess(() -> Component.translatable("command.company.withdraw.success", amount, company.getBalance()), true);
            return 1;
        } else {
            source.sendFailure(Component.translatable("command.company.withdraw.no_funds"));
            return 0;
        }
    }

    private static int setSalary(CommandSourceStack source, int amount) {
        if (!source.isPlayer()) return 0;
        ServerPlayer player = source.getPlayer();
        EconomyData data = EconomyData.get(source.getLevel());

        Company company = requireOwnCompany(data, player, source);
        if (company == null) return 0;

        company.setSalary(amount);
        data.setDirty();
        source.sendSuccess(() -> Component.translatable("command.company.salary.set", amount), true);
        return 1;
    }

    private static int leaveCompany(CommandSourceStack source) {
        if (!source.isPlayer()) return 0;
        ServerPlayer player = source.getPlayer();
        EconomyData data = EconomyData.get(source.getLevel());

        Company company = requireCompany(data, player, source);
        if (company == null) return 0;

        if (company.isOwner(player.getUUID())) {
            source.sendFailure(Component.translatable("command.company.leave.owner"));
            return 0;
        }

        company.removeEmployee(player.getUUID());
        data.setDirty();
        source.sendSuccess(() -> Component.translatable("command.company.leave.success", company.getName()), true);
        return 1;
    }

    private static int dissolveCompany(CommandSourceStack source) {
        if (!source.isPlayer()) return 0;
        ServerPlayer player = source.getPlayer();
        EconomyData data = EconomyData.get(source.getLevel());

        Company company = requireOwnCompany(data, player, source);
        if (company == null) return 0;

        int balance = company.getBalance();
        int perEmployee = company.getEmployeeCount() > 0 ? balance / company.getEmployeeCount() : 0;

        for (UUID empId : company.getEmployees()) {
            data.addBalance(empId, perEmployee, "Dissolution de '" + company.getName() + "'", company.getName());
            ServerPlayer emp = source.getServer().getPlayerList().getPlayer(empId);
            if (emp != null) {
                emp.playNotifySound(ModSounds.COMPANY_DISSOLVE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                emp.sendSystemMessage(Component.translatable("command.company.dissolve.notification", company.getName(), perEmployee));
            }
        }

        data.removeCompany(company.getId());
        data.setDirty();
        source.sendSuccess(() -> Component.translatable("command.company.dissolve.success", company.getName(), perEmployee), true);
        return 1;
    }

    private static int infoCompany(CommandSourceStack source) {
        if (!source.isPlayer()) return 0;
        ServerPlayer player = source.getPlayer();
        EconomyData data = EconomyData.get(source.getLevel());

        Company company = requireCompany(data, player, source);
        if (company == null) return 0;

        net.minecraft.server.level.ServerPlayer ownerPlayer = source.getServer().getPlayerList().getPlayer(company.getOwner());
        String ownerName = ownerPlayer != null ? ownerPlayer.getName().getString() : Component.translatable("command.company.info.unknown").getString();

        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.translatable("command.company.info.title", company.getName()), false);
        source.sendSuccess(() -> Component.translatable("command.company.info.owner", ownerName), false);
        source.sendSuccess(() -> Component.translatable("command.company.info.balance", company.getBalance()), false);
        source.sendSuccess(() -> Component.translatable("command.company.info.salary", company.getSalary()), false);
        source.sendSuccess(() -> Component.translatable("command.company.info.employees", company.getEmployeeCount()), false);
        return 1;
    }
}
