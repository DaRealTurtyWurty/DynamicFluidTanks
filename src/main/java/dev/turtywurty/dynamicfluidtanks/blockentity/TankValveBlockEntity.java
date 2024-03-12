package dev.turtywurty.dynamicfluidtanks.blockentity;

import dev.turtywurty.dynamicfluidtanks.init.BlockEntityTypeInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

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

    public void setController(BlockPos controllerPos) {
        this.controllerPos = controllerPos;
    }
}
