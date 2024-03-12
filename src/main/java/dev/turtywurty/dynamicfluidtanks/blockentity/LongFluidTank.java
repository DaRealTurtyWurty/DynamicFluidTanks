package dev.turtywurty.dynamicfluidtanks.blockentity;

import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LongFluidTank implements IFluidTank, IFluidHandler, INBTSerializable<CompoundTag> {
    private final List<FluidStack> fluidStacks = new ArrayList<>();
    @Setter
    private long capacity;

    public LongFluidTank(long capacity) {
        this.capacity = capacity;
    }

    public LongFluidTank() {}

    @Override
    public @NotNull FluidStack getFluid() {
        return this.fluidStacks.isEmpty() ? FluidStack.EMPTY : this.fluidStacks.get(0);
    }

    @Override
    public int getFluidAmount() {
        return getFluid().getAmount();
    }

    @Override
    public int getCapacity() {
        return (int) this.capacity;
    }

    @Override
    public boolean isFluidValid(FluidStack stack) {
        return stack.isFluidEqual(getFluid()) || isEmpty();
    }

    @Override
    public int getTanks() {
        return this.fluidStacks.size();
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return this.fluidStacks.get(tank);
    }

    @Override
    public int getTankCapacity(int tank) {
        return getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return stack.isFluidEqual(getFluidInTank(tank));
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !isFluidValid(resource)) {
            return 0;
        }

        long totalFilled = 0;

        if (action.simulate()) {
            for (FluidStack fluid : fluidStacks) {
                if (fluid.isFluidEqual(resource)) {
                    totalFilled += Math.min(getCapacityLong() - fluid.getAmount(), resource.getAmount());
                }
            }
            return (int) totalFilled;
        }

        for (FluidStack fluid : fluidStacks) {
            if (fluid.isFluidEqual(resource)) {
                long filled = Math.min(getCapacityLong() - fluid.getAmount(), resource.getAmount());
                fluid.grow((int) filled);
                totalFilled += filled;
                resource.shrink((int) filled);
                if (resource.isEmpty()) {
                    break;
                }
            }
        }

        if (!resource.isEmpty()) {
            FluidStack newFluid = new FluidStack(resource, (int) Math.min(getCapacityLong(), resource.getAmount()));
            fluidStacks.add(newFluid);
            totalFilled += newFluid.getAmount();
        }

        if (totalFilled > 0) {
            onContentsChanged();
        }

        return (int) totalFilled;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        long totalDrained = 0;
        FluidStack drained = FluidStack.EMPTY;

        if (action.simulate()) {
            for (FluidStack fluid : fluidStacks) {
                totalDrained += Math.min(fluid.getAmount(), maxDrain);
                if (totalDrained >= maxDrain) {
                    break;
                }
            }
            return new FluidStack(drained, (int) totalDrained);
        }

        Iterator<FluidStack> iterator = fluidStacks.iterator();
        while (iterator.hasNext()) {
            FluidStack fluid = iterator.next();
            long drainable = Math.min(fluid.getAmount(), maxDrain - totalDrained);
            if (drainable > 0) {
                fluid.shrink((int) drainable);
                totalDrained += drainable;
                if (fluid.isEmpty()) {
                    iterator.remove();
                }
                if (totalDrained >= maxDrain) {
                    break;
                }
            }
        }

        if (totalDrained > 0) {
            onContentsChanged();
        }

        return new FluidStack(drained, (int) totalDrained);
    }


    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        FluidStack fluid = getFluid();
        if (resource.isEmpty() || !resource.isFluidEqual(fluid)) {
            return FluidStack.EMPTY;
        }

        return drain(resource.getAmount(), action);
    }

    protected void onContentsChanged() {

    }

    public boolean isEmpty() {
        return fluidStacks.isEmpty();
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();

        var fluidStacksTag = new ListTag();
        for (FluidStack fluid : fluidStacks) {
            fluidStacksTag.add(fluid.writeToNBT(new CompoundTag()));
        }

        tag.put("FluidStacks", fluidStacksTag);
        tag.putLong("Capacity", capacity);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        var fluidStacksTag = nbt.getList("FluidStacks", 10);
        for (int i = 0; i < fluidStacksTag.size(); i++) {
            fluidStacks.add(FluidStack.loadFluidStackFromNBT(fluidStacksTag.getCompound(i)));
        }

        capacity = nbt.getLong("Capacity");
    }

    public long getCapacityLong() {
        return capacity;
    }

    public long getFluidAmountLong() {
        long amount = 0;
        for (FluidStack fluid : fluidStacks) {
            amount += fluid.getAmount();
        }
        return amount;
    }
}
