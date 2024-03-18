package dev.turtywurty.dynamicfluidtanks.blockentity;

import dev.turtywurty.dynamicfluidtanks.init.BlockEntityTypeInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TankValveBlockEntity extends BlockEntity {
    private BlockPos controllerPos;

    public TankValveBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityTypeInit.TANK_VALVE.get(), pPos, pBlockState);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER && this.controllerPos != null && this.level != null) {
            BlockEntity controller = this.level.getBlockEntity(controllerPos);
            if(controller instanceof TankControllerBlockEntity tankControllerBlockEntity) {
                return tankControllerBlockEntity.getTank().cast();
            }
        }

        return super.getCapability(cap, side);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        super.saveAdditional(pTag);
        if (this.controllerPos != null) {
            pTag.putLong("ControllerPos", this.controllerPos.asLong());
        }
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
        if (pTag.contains("ControllerPos", Tag.TAG_LONG)) {
            this.controllerPos = BlockPos.of(pTag.getLong("ControllerPos"));
        }
    }

    public void setController(BlockPos controllerPos) {
        this.controllerPos = controllerPos;
        sendUpdate();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        var tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    private void sendUpdate() {
        setChanged();

        if (this.level != null) {
            BlockState state = getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_ALL);
        }
    }
}
