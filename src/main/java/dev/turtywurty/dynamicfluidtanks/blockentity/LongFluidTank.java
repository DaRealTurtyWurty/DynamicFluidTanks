package dev.turtywurty.dynamicfluidtanks.blockentity;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import oshi.annotation.concurrent.ThreadSafe;

@Setter
@Getter
public class LongFluidTank implements IFluidTank, IFluidHandler, INBTSerializable<CompoundTag> {
    private long longCapacity, longAmount;
    private FluidStack fluidData;

    public LongFluidTank(long longCapacity) {
        this.longCapacity = longCapacity;
    }

    public LongFluidTank() {}

    @Override
    public @NotNull FluidStack getFluid() {
        if (fluidData == null)
            return FluidStack.EMPTY;

        return this.longAmount <= 0 ? FluidStack.EMPTY : new FluidStack(fluidData, (int) this.longAmount);
    }

    @Override
    public int getFluidAmount() {
        return getFluid().getAmount();
    }

    @Override
    public int getCapacity() {
        return (int) this.longCapacity;
    }

    @Override
    public boolean isFluidValid(FluidStack stack) {
        return stack.isFluidEqual(getFluid()) || isEmpty();
    }

    @Override
    public int getTanks() {
        // Calculate the amount of Integer.MAX_VALUE's that can fit into the longCapacity
        return (int) (longCapacity / (float) Integer.MAX_VALUE);
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        if (tank < 0 || tank >= getTanks()) {
            return FluidStack.EMPTY;
        }

        if(tank == getTanks()) {
            return new FluidStack(fluidData, getLastStackRemainder());
        }

        return new FluidStack(fluidData, Integer.MAX_VALUE);
    }

    @Override
    public int getTankCapacity(int tank) {
        if (tank < 0 || tank >= getTanks()) {
            return 0;
        }

        if(tank == getTanks()) {
            return getLastStackRemainder();
        }

        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return isFluidEqual(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if(resource.isEmpty() || !isFluidValid(resource))
            return 0;

        if(action.simulate()) {
            if(isEmpty())
                return (int) Math.min(resource.getAmount(), longCapacity);

            if(!isFluidEqual(resource))
                return 0;

            return (int) Math.min(resource.getAmount(), longCapacity - longAmount);
        }

        if(isEmpty()) {
            long amount = Math.min(resource.getAmount(), longCapacity);
            fluidData = new FluidStack(resource, (int) amount);
            longAmount = amount;
            onContentsChanged();
            return (int) amount;
        }

        if(!isFluidEqual(resource))
            return 0;

        long filled = longCapacity - longAmount;
        if(resource.getAmount() < filled) {
            longAmount += resource.getAmount();
            filled = resource.getAmount();
            fluidData.grow((int) filled);
        } else {
            longAmount = longCapacity;
            fluidData.setAmount((int) longAmount);
        }

        if (filled > 0) {
            onContentsChanged();
        }

        return (int) filled;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        long drained = maxDrain;
        if (longAmount < drained) {
            drained = longAmount;
        }

        var stack = new FluidStack(fluidData, (int) drained);
        if (action.execute() && drained > 0) {
            longAmount -= drained;
            fluidData.shrink((int) drained);
            onContentsChanged();
        }

        return stack;
    }


    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !isFluidEqual(resource)) {
            return FluidStack.EMPTY;
        }

        return drain(resource.getAmount(), action);
    }

    protected void onContentsChanged() {

    }

    public boolean isEmpty() {
        return longAmount <= 0;
    }

    public boolean isFluidEqual(FluidStack stack) {
        return getFluid().isFluidEqual(stack);
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();

        tag.putLong("Capacity", longCapacity);
        tag.putLong("Amount", longAmount);
        if (this.fluidData != null) {
            tag.put("Fluid", this.fluidData.writeToNBT(new CompoundTag()));
        }

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        longCapacity = nbt.getLong("Capacity");
        longAmount = nbt.getLong("Amount");
        if (nbt.contains("Fluid")) {
            this.fluidData = FluidStack.loadFluidStackFromNBT(nbt.getCompound("Fluid"));
        }
    }

    private int getLastStackRemainder() {
        return (int) (longCapacity % Integer.MAX_VALUE);
    }
}
