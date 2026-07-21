package com.citeconomy.block.entity;

import com.citeconomy.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.UUID;

/**
 * BlockEntity de la boutique joueur.
 * Contient l'inventaire (27 slots), les prix par slot, et les infos du propriétaire.
 *
 * Les prix sont stockés dans un int[27] serialisé en NBT via putIntArray/getIntArray.
 * Chaque slot peut avoir un prix indépendant (0 = pas en vente).
 * La synchronisation client-serveur utilise getUpdatePacket/getUpdateTag (pattern standard NeoForge).
 */
public class PersonalShopBlockEntity extends BlockEntity {

    private UUID ownerId;
    private String ownerName = "Inconnu";

    private final int[] slotPrices = new int[27];

    private final ItemStackHandler inventory = new ItemStackHandler(27) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            syncToClient();
        }
    };

    public PersonalShopBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PERSONAL_SHOP.get(), pos, state);
    }

    // --- Propriétaire ---

    public void setOwner(UUID ownerId, String ownerName) {
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        setChanged();
        syncToClient();
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    // --- Prix par slot ---

    public int getSlotPrice(int slot) {
        if (slot < 0 || slot >= slotPrices.length) return 0;
        return slotPrices[slot];
    }

    /**
     * Fixe le prix d'un slot. Le prix est clampé entre 0 et 100 000.
     * price=0 signifie "pas en vente" (le slot peut contenir un item mais il n'est pas achetable).
     */
    public void setSlotPrice(int slot, int price) {
        if (slot < 0 || slot >= slotPrices.length) return;
        slotPrices[slot] = Math.max(0, Math.min(100000, price));
        setChanged();
        syncToClient();
    }

    public int[] getSlotPrices() {
        return slotPrices;
    }

    /** Un slot est en vente s'il a un prix > 0 ET un item non vide. */
    public boolean isSlotOnSale(int slot) {
        return slot >= 0 && slot < slotPrices.length && slotPrices[slot] > 0 && !inventory.getStackInSlot(slot).isEmpty();
    }

    // --- Sync client-serveur ---

    private void syncToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        tag.putIntArray("SlotPrices", slotPrices);
        if (ownerId != null) {
            tag.putUUID("OwnerId", ownerId);
            tag.putString("OwnerName", ownerName);
        }
        tag.put("Inventory", inventory.serializeNBT(provider));
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // --- NBT Persistance ---

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putIntArray("SlotPrices", slotPrices);
        if (ownerId != null) {
            tag.putUUID("OwnerId", ownerId);
            tag.putString("OwnerName", ownerName);
        }
        tag.put("Inventory", inventory.serializeNBT(provider));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("SlotPrices")) {
            int[] loaded = tag.getIntArray("SlotPrices");
            System.arraycopy(loaded, 0, slotPrices, 0, Math.min(loaded.length, slotPrices.length));
        }
        if (tag.hasUUID("OwnerId")) {
            this.ownerId = tag.getUUID("OwnerId");
            this.ownerName = tag.getString("OwnerName");
        }
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(provider, tag.getCompound("Inventory"));
        }
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }
}
