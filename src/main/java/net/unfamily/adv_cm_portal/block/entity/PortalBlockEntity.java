package net.unfamily.adv_cm_portal.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Stores optional return dimension: when set, this block is a "return" portal and teleports to that dimension.
 */
public class PortalBlockEntity extends BlockEntity {

    private static final String TAG_RETURN_TO = "ReturnTo";

    @Nullable
    private String returnTo;

    public PortalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PORTAL.get(), pos, state);
    }

    @Nullable
    public String getReturnTo() {
        return returnTo;
    }

    public void setReturnTo(@Nullable String dimensionKey) {
        this.returnTo = dimensionKey;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (returnTo != null && !returnTo.isEmpty()) {
            tag.putString(TAG_RETURN_TO, returnTo);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        returnTo = tag.contains(TAG_RETURN_TO) ? tag.getString(TAG_RETURN_TO) : null;
    }

    @Override
    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
