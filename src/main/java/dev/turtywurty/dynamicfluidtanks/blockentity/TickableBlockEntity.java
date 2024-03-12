package dev.turtywurty.dynamicfluidtanks.blockentity;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;

public interface TickableBlockEntity {
    void tick();

    static <T extends BlockEntity> BlockEntityTicker<T> createTicker(Level level, boolean allowClient) {
        return level.isClientSide() && !allowClient ? null : (pLevel, pPos, pState, pBlockEntity) -> ((TickableBlockEntity) pBlockEntity).tick();
    }

    static <T extends BlockEntity> BlockEntityTicker<T> createTicker(Level level) {
        return createTicker(level, false);
    }
}
