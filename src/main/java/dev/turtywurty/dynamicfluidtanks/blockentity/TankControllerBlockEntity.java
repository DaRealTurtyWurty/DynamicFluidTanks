package dev.turtywurty.dynamicfluidtanks.blockentity;

import dev.turtywurty.dynamicfluidtanks.DynamicFluidTanks;
import dev.turtywurty.dynamicfluidtanks.init.BlockEntityTypeInit;
import dev.turtywurty.dynamicfluidtanks.menu.TankControllerMenu;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TankControllerBlockEntity extends BlockEntity implements TickableBlockEntity, MenuProvider {
    private static final Component TITLE =
            Component.translatable("container." + DynamicFluidTanks.MODID + ".tank_controller");

    @Getter
    private final MultiblockData multiblockData = new MultiblockData(this.worldPosition);

    private final LongFluidTank tank = new LongFluidTank() {
        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            sendUpdate();
        }
    };
    private final LazyOptional<LongFluidTank> tankHolder = LazyOptional.of(() -> this.tank);

    private final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            sendUpdate();
        }
    };

    private void sendUpdate() {
        setChanged();

        if (this.level != null) {
            BlockState state = getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_ALL);
        }
    }

    private final LazyOptional<IItemHandler> inventoryHolder = LazyOptional.of(() -> this.inventory);

    private int ticks;

    public TankControllerBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityTypeInit.TANK_CONTROLLER.get(), pPos, pBlockState);

        this.multiblockData.setOnComplete(this::onMultiblockComplete);
        this.multiblockData.setOnInvalid(this::onMultiblockInvalid);
    }

    @Override
    public void tick() {
        if (this.level == null)
            return;

        if ((!this.multiblockData.isComplete() && this.ticks % 20 == 0) || this.ticks % 200 == 0) {
            long start = System.nanoTime();
            this.multiblockData.construct(this.level);
            long end = System.nanoTime();
            DynamicFluidTanks.LOGGER.info("Multiblock construction took: " + (end - start) + "ns");
        }

        if(this.ticks++ == Integer.MAX_VALUE) {
            this.ticks = 0;
        }

        if(this.multiblockData.isComplete()) {
            ItemStack stack = this.inventory.getStackInSlot(0);
            if(stack.isEmpty())
                return;

            if(this.tank.getFluidAmount() >= this.tank.getCapacity())
                return;

            LazyOptional<IFluidHandlerItem> fluidHandler = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
            fluidHandler.ifPresent(iFluidHandlerItem -> {
                if(!this.tank.getFluid().isFluidEqual(iFluidHandlerItem.getFluidInTank(0)) && !this.tank.isEmpty())
                    return;

                int amountToDrain = this.tank.getCapacity() - this.tank.getFluidAmount();
                int amount = iFluidHandlerItem.drain(amountToDrain, IFluidHandler.FluidAction.SIMULATE).getAmount();
                if(amount > 0) {
                    this.tank.fill(iFluidHandlerItem.drain(amountToDrain, IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);

                    if(amount <= amountToDrain) {
                        this.inventory.setStackInSlot(0, iFluidHandlerItem.getContainer());
                    }
                }
            });
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        super.saveAdditional(pTag);

        var modidTag = new CompoundTag();

        CompoundTag multiblockDataTag = this.multiblockData.serializeNBT();
        modidTag.put("MultiblockData", multiblockDataTag);

        var tankTag = this.tank.serializeNBT();
        modidTag.put("Tank", tankTag);

        var inventoryTag = this.inventory.serializeNBT();
        modidTag.put("Inventory", inventoryTag);

        pTag.put(DynamicFluidTanks.MODID, modidTag);
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);

        if (pTag.contains(DynamicFluidTanks.MODID)) {
            var modidTag = pTag.getCompound(DynamicFluidTanks.MODID);

            if (modidTag.contains("MultiblockData")) {
                CompoundTag multiblockDataTag = modidTag.getCompound("MultiblockData");
                this.multiblockData.deserializeNBT(multiblockDataTag);
            }

            if (modidTag.contains("Tank")) {
                CompoundTag tankTag = modidTag.getCompound("Tank");
                this.tank.deserializeNBT(tankTag);
            }

            if (modidTag.contains("Inventory")) {
                CompoundTag inventoryTag = modidTag.getCompound("Inventory");
                this.inventory.deserializeNBT(inventoryTag);
            }
        }
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.tankHolder.invalidate();
        this.inventoryHolder.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return this.tankHolder.cast();
        }

        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return this.inventoryHolder.cast();
        }

        return super.getCapability(cap, side);
    }

    public LazyOptional<LongFluidTank> getTank() {
        return this.tankHolder;
    }

    public void onMultiblockComplete() {
        long volume = this.multiblockData.getVolume();
        this.tank.setCapacity(volume * 1_000L); // volume * 1000 (b -> mb)
        System.out.println("Multiblock complete! Volume: " + volume + "b, Capacity: " + this.tank.getCapacity() + "mb");

        if(this.level == null)
            return;

        for (BlockPos valvePosition : this.multiblockData.getValvePositions()) {
            BlockEntity blockEntity = this.level.getBlockEntity(valvePosition);
            if (blockEntity instanceof TankValveBlockEntity valve) {
                valve.setController(this.worldPosition);
            }
        }
    }

    public void onMultiblockInvalid() {
        this.tank.setCapacity(0L);
    }

    public LazyOptional<IItemHandler> getInventory() {
        return this.inventoryHolder;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return TITLE;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pPlayerInventory, @NotNull Player pPlayer) {
        return new TankControllerMenu(pContainerId, pPlayerInventory, this);
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

    @Override
    public AABB getRenderBoundingBox() {
        return this.multiblockData.getBoundingBox();
    }
}
