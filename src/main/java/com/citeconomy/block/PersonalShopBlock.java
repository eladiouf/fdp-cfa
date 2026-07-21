package com.citeconomy.block;

import com.citeconomy.block.entity.PersonalShopBlockEntity;
import com.citeconomy.menu.PersonalShopMenu;
import com.citeconomy.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/**
 * Bloc de boutique joueur (PersonalShop).
 * Posé par un joueur, il stocke jusqu'à 27 items avec des prix individuels.
 * Les autres joueurs peuvent acheter les items mis en vente.
 * Les items sont dropés à la destruction du bloc.
 *
 * Architecture inspirée de SDM Shop Rework (SagaDeoMissTeam/SDMShop).
 */
public class PersonalShopBlock extends BaseEntityBlock {

    public static final MapCodec<PersonalShopBlock> CODEC = simpleCodec(PersonalShopBlock::new);

    public PersonalShopBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModBlockEntities.PERSONAL_SHOP.get().create(pos, state);
    }

    /**
     * Sauvegarde le propriétaire (UUID + nom) quand le bloc est placé par un joueur.
     */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (placer instanceof Player player) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof PersonalShopBlockEntity shop) {
                shop.setOwner(player.getUUID(), player.getName().getString());
            }
        }
    }

    /**
     * Drop tout l'inventaire dans le monde quand le bloc est cassé.
     * Anti-perte : aucun item n'est détruit.
     */
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof PersonalShopBlockEntity shop) {
                for (int i = 0; i < shop.getInventory().getSlots(); i++) {
                    ItemStack stack = shop.getInventory().getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
                    }
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /**
     * Ouvre le menu de la boutique quand un joueur interagit avec le bloc.
     * Côté serveur uniquement ; le client reçoit le packet d'ouverture automatiquement.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof PersonalShopBlockEntity shop && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(new SimpleMenuProvider(
                        (id, inv, p) -> new PersonalShopMenu(id, inv, shop),
                        Component.translatable("gui.citeconomy.personalshop.title", shop.getOwnerName())
                ), pos);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
