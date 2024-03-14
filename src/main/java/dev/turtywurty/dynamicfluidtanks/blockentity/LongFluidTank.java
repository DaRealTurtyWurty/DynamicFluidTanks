package dev.turtywurty.dynamicfluidtanks.blockentity;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

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
        return stack.isFluidEqual(getFluidInTank(tank));
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {

    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {

    }


    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {

    }

    protected void onContentsChanged() {

    }

    public boolean isEmpty() {
        return longAmount <= 0;
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();

        tag.putLong("Capacity", longCapacity);
        tag.putLong("Amount", longAmount);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        longCapacity = nbt.getLong("Capacity");
        longAmount = nbt.getLong("Amount");
    }

    private int getLastStackRemainder() {
        return (int) (longCapacity % Integer.MAX_VALUE);
    }
}
