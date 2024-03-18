package dev.turtywurty.dynamicfluidtanks.blockentity;

import dev.turtywurty.dynamicfluidtanks.DynamicFluidTanks;
import dev.turtywurty.dynamicfluidtanks.block.TankValveBlock;
import dev.turtywurty.dynamicfluidtanks.init.BlockInit;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
public class MultiblockData implements INBTSerializable<CompoundTag> {
    private final BlockPos controllerPos;
    private final Set<BlockPos> tankPositions = new HashSet<>();
    private final Set<BlockPos> valvePositions = new HashSet<>();
    private final Set<BlockPos> gaugePositions = new HashSet<>();

    private int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
    private int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
    private AABB boundingBox = new AABB(0, 0, 0, 0, 0, 0);

    private boolean isComplete = false;
    private int volume = 0;

    @Setter
    private Runnable onComplete, onInvalid;

    public MultiblockData(BlockPos controllerPos) {
        this.controllerPos = controllerPos;
    }

    public void construct(@NotNull Level level) {
        if (this.isComplete) {
            if (!validateMultiblock(level).isEmpty()) {
                reset();
                if (this.onInvalid != null) {
                    this.onInvalid.run();
                }
            } else
                return;
        }

        Set<BlockPos> visited = new HashSet<>();
        Stack<BlockPos> stack = new Stack<>();
        stack.push(this.controllerPos);

        while (!stack.isEmpty()) {
            BlockPos pos = stack.pop();
            visited.add(pos);

            BlockState state = level.getBlockState(pos);
            Block block = state.getBlock();

            if (this.tankPositions.contains(pos))
                continue;

            if (state.is(BlockInit.TANK_CONTROLLER.get())) {
                this.tankPositions.add(pos);
            } else if (block instanceof TankValveBlock) {
                this.tankPositions.add(pos);
                this.valvePositions.add(pos);
            } else if ((state.is(BlockInit.TANK_CASING.get()) || state.is(BlockInit.TANK_GLASS.get()))) {
                this.tankPositions.add(pos);
            } /*else if(state.is(BlockInit.TANK_GAUGE.get())) {
                this.tankPositions.add(pos);
                this.gaugePositions.add(pos);
             }*/ else
                continue;

            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = pos.relative(direction);
                if (!visited.contains(neighborPos)) {
                    stack.push(neighborPos);
                }
            }
        }

        for (BlockPos position : this.tankPositions) {
            this.minX = Math.min(this.minX, position.getX());
            this.minY = Math.min(this.minY, position.getY());
            this.minZ = Math.min(this.minZ, position.getZ());
            this.maxX = Math.max(this.maxX, position.getX());
            this.maxY = Math.max(this.maxY, position.getY());
            this.maxZ = Math.max(this.maxZ, position.getZ());
        }

        // validate multiblock
        List<BlockPos> invalidPositions = validateMultiblock(level);
        if (invalidPositions.isEmpty()) {
            this.isComplete = true;
            this.boundingBox = new AABB(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
            this.volume = (this.maxX - this.minX + 1) * (this.maxY - this.minY + 1) * (this.maxZ - this.minZ + 1);

            spawnParticles(level, invalidPositions);

            if (this.onComplete != null) {
                this.onComplete.run();
            }
        } else {
            this.isComplete = false;
            spawnParticles(level, invalidPositions);
            reset();

            if (this.onInvalid != null) {
                this.onInvalid.run();
            }
        }
    }

    // TODO: If there are multiple controllers, it should be considered invalid
    private List<BlockPos> validateMultiblock(@NotNull Level level) {
        // Check if there's at least one valve and one controller
        if (this.valvePositions.isEmpty() || this.tankPositions.isEmpty())
            return List.of(this.controllerPos);

        boolean outerFailed = false;
        boolean innerFailed = false;
        boolean foundController = false;
        List<BlockPos> invalidPositions = new ArrayList<>();

        // Check if the blocks form a cuboid with an empty interior
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (int x = this.minX; x <= this.maxX; x++) {
            for (int y = this.minY; y <= this.maxY; y++) {
                for (int z = this.minZ; z <= this.maxZ; z++) {
                    mutablePos.set(x, y, z);
                    BlockState state = level.getBlockState(mutablePos);

                    // Check the outer layer
                    if (x == this.minX || x == this.maxX || y == this.minY || y == this.maxY || z == this.minZ || z == this.maxZ) {
                        if (!state.is(BlockInit.TANK_CASING.get()) && !state.is(BlockInit.TANK_GLASS.get()) && !state.is(BlockInit.TANK_VALVE.get())) {
                            if(state.is(BlockInit.TANK_CONTROLLER.get())) {
                                if(foundController) {
                                    DynamicFluidTanks.LOGGER.warn("Found multiple controllers!");
                                    invalidPositions.add(mutablePos.immutable());

                                    if(!invalidPositions.contains(this.controllerPos)) {
                                        invalidPositions.add(this.controllerPos);
                                    }
                                } else {
                                    foundController = true;
                                }

                                continue;
                            }

                            if (!outerFailed) {
                                DynamicFluidTanks.LOGGER.warn("Outer blocks failed!");
                                outerFailed = true;
                            }

                            invalidPositions.add(mutablePos.immutable());
                        }
                    } else if (!level.isEmptyBlock(mutablePos)) { // Check the inner blocks
                        if (!innerFailed) {
                            DynamicFluidTanks.LOGGER.warn("Inner blocks failed!");
                            innerFailed = true;
                        }

                        invalidPositions.add(mutablePos.immutable());
                    }
                }
            }
        }

        if (invalidPositions.isEmpty()) {
            DynamicFluidTanks.LOGGER.info("Multiblock validation successful!");
        } else {

        }

        return invalidPositions;
    }

    private void reset() {
        this.tankPositions.clear();
        this.valvePositions.clear();
        this.gaugePositions.clear();

        this.isComplete = false;
        this.volume = 0;

        this.minX = Integer.MAX_VALUE;
        this.minY = Integer.MAX_VALUE;
        this.minZ = Integer.MAX_VALUE;
        this.maxX = Integer.MIN_VALUE;
        this.maxY = Integer.MIN_VALUE;
        this.maxZ = Integer.MIN_VALUE;

        this.boundingBox = new AABB(0, 0, 0, 0, 0, 0);
    }

    private void spawnParticles(@NotNull Level level, List<BlockPos> invalidPositions) {
        if (level.isClientSide)
            return;

        var serverLevel = (ServerLevel) level;
        // spawn particles along the edges of the multiblock
        if (this.isComplete) {
            for (int x = this.minX; x <= this.maxX; x++) {
                for (int y = this.minY; y <= this.maxY; y++) {
                    for (int z = this.minZ; z <= this.maxZ; z++) {
                        if (x == this.minX || x == this.maxX || y == this.minY || y == this.maxY || z == this.minZ || z == this.maxZ) {
                            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, x + 0.5, y + 0.5, z + 0.5, 1, 0, 0, 0, 0);
                        }
                    }
                }
            }
        } else {
            for (BlockPos pos : invalidPositions) {
                serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, 0, 0, 0, 0);
            }
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();

        var tankPositionsTag = new ListTag();
        for (BlockPos pos : this.tankPositions) {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("X", pos.getX());
            posTag.putInt("Y", pos.getY());
            posTag.putInt("Z", pos.getZ());
            tankPositionsTag.add(posTag);
        }
        nbt.put("TankPositions", tankPositionsTag);

        var valvePositionsTag = new ListTag();
        for (BlockPos pos : this.valvePositions) {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("X", pos.getX());
            posTag.putInt("Y", pos.getY());
            posTag.putInt("Z", pos.getZ());
            valvePositionsTag.add(posTag);
        }
        nbt.put("ValvePositions", valvePositionsTag);

        var gaugePositionsTag = new ListTag();
        for (BlockPos pos : this.gaugePositions) {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("X", pos.getX());
            posTag.putInt("Y", pos.getY());
            posTag.putInt("Z", pos.getZ());
            gaugePositionsTag.add(posTag);
        }
        nbt.put("GaugePositions", gaugePositionsTag);

        nbt.putInt("MinX", this.minX);
        nbt.putInt("MinY", this.minY);
        nbt.putInt("MinZ", this.minZ);
        nbt.putInt("MaxX", this.maxX);
        nbt.putInt("MaxY", this.maxY);
        nbt.putInt("MaxZ", this.maxZ);
        nbt.putBoolean("IsComplete", this.isComplete);
        nbt.putInt("Volume", this.volume);

        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.tankPositions.clear();
        this.valvePositions.clear();
        this.gaugePositions.clear();

        if (nbt.contains("TankPositions", Tag.TAG_LIST)) {
            ListTag tankPositionsTag = nbt.getList("TankPositions", Tag.TAG_COMPOUND);
            for (int i = 0; i < tankPositionsTag.size(); i++) {
                CompoundTag posTag = tankPositionsTag.getCompound(i);
                this.tankPositions.add(new BlockPos(posTag.getInt("X"), posTag.getInt("Y"), posTag.getInt("Z")));
            }
        }

        if (nbt.contains("ValvePositions", Tag.TAG_LIST)) {
            ListTag valvePositionsTag = nbt.getList("ValvePositions", Tag.TAG_COMPOUND);
            for (int i = 0; i < valvePositionsTag.size(); i++) {
                CompoundTag posTag = valvePositionsTag.getCompound(i);
                this.valvePositions.add(new BlockPos(posTag.getInt("X"), posTag.getInt("Y"), posTag.getInt("Z")));
            }
        }

        if (nbt.contains("GaugePositions", Tag.TAG_LIST)) {
            ListTag gaugePositionsTag = nbt.getList("GaugePositions", Tag.TAG_COMPOUND);
            for (int i = 0; i < gaugePositionsTag.size(); i++) {
                CompoundTag posTag = gaugePositionsTag.getCompound(i);
                this.gaugePositions.add(new BlockPos(posTag.getInt("X"), posTag.getInt("Y"), posTag.getInt("Z")));
            }
        }

        if (nbt.contains("MinX", Tag.TAG_INT)) {
            this.minX = nbt.getInt("MinX");
        }

        if (nbt.contains("MinY", Tag.TAG_INT)) {
            this.minY = nbt.getInt("MinY");
        }

        if (nbt.contains("MinZ", Tag.TAG_INT)) {
            this.minZ = nbt.getInt("MinZ");
        }

        if (nbt.contains("MaxX", Tag.TAG_INT)) {
            this.maxX = nbt.getInt("MaxX");
        }

        if (nbt.contains("MaxY", Tag.TAG_INT)) {
            this.maxY = nbt.getInt("MaxY");
        }

        if (nbt.contains("MaxZ", Tag.TAG_INT)) {
            this.maxZ = nbt.getInt("MaxZ");
        }

        this.boundingBox = new AABB(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);

        if (nbt.contains("IsComplete", Tag.TAG_BYTE)) {
            this.isComplete = nbt.getBoolean("IsComplete");
        }

        if (nbt.contains("Volume", Tag.TAG_INT)) {
            this.volume = nbt.getInt("Volume");
        }
    }
}
