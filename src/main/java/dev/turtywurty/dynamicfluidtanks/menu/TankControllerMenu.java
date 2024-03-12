package dev.turtywurty.dynamicfluidtanks.menu;

import dev.turtywurty.dynamicfluidtanks.blockentity.TankControllerBlockEntity;
import dev.turtywurty.dynamicfluidtanks.init.BlockInit;
import dev.turtywurty.dynamicfluidtanks.init.MenuTypeInit;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class TankControllerMenu extends AbstractContainerMenu {
    @Getter
    private final TankControllerBlockEntity blockEntity;
    private final ContainerLevelAccess levelAccess;

    public TankControllerMenu(int menuId, Inventory playerInv, FriendlyByteBuf additionalData) {
        this(menuId, playerInv, playerInv.player.level().getBlockEntity(additionalData.readBlockPos()));
    }

    public TankControllerMenu(int menuId, Inventory playerInv, BlockEntity blockEntity) {
        super(MenuTypeInit.TANK_CONTROLLER.get(), menuId);
        if(blockEntity instanceof TankControllerBlockEntity be) {
            this.blockEntity = be;
        } else {
            throw new IllegalStateException("Incorrect block entity class %s passed into TankControllerMenu".formatted(blockEntity.getClass().getSimpleName()));
        }

        this.levelAccess = ContainerLevelAccess.create(playerInv.player.level(), blockEntity.getBlockPos());

        createPlayerHotbar(playerInv);
        createPlayerInventory(playerInv);
        createBlockEntityInventory();
    }

    private void createBlockEntityInventory() {
        this.blockEntity.getInventory().ifPresent(inventory ->
                addSlot(new FluidContainerSlot(inventory, 0, 44, 36)));
    }

    private void createPlayerInventory(Inventory playerInv) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInv,
                        9 + column + (row * 9),
                        8 + (column * 18),
                        84 + (row * 18)));
            }
        }
    }

    private void createPlayerHotbar(Inventory playerInv) {
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInv,
                    column,
                    8 + (column * 18),
                    142));
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player pPlayer, int pIndex) {
        Slot fromSlot = getSlot(pIndex);
        ItemStack fromStack = fromSlot.getItem();

        if (fromStack.getCount() <= 0)
            fromSlot.set(ItemStack.EMPTY);

        if (!fromSlot.hasItem())
            return ItemStack.EMPTY;

        ItemStack copyFromStack = fromStack.copy();

        if (pIndex < 36) {
            // We are inside of the player's inventory
            if (!moveItemStackTo(fromStack, 36, 37, false))
                return ItemStack.EMPTY;
        } else if (pIndex < 37) {
            // We are inside of the block entity inventory
            if (!moveItemStackTo(fromStack, 0, 36, false))
                return ItemStack.EMPTY;
        } else {
            System.err.println("Invalid slot index: " + pIndex);
            return ItemStack.EMPTY;
        }

        fromSlot.setChanged();
        fromSlot.onTake(pPlayer, fromStack);

        return copyFromStack;
    }

    @Override
    public boolean stillValid(@NotNull Player pPlayer) {
        return stillValid(this.levelAccess, pPlayer, BlockInit.TANK_CONTROLLER.get());
    }
}
