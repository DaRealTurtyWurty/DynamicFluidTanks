package dev.turtywurty.dynamicfluidtanks.init;

import dev.turtywurty.dynamicfluidtanks.DynamicFluidTanks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemInit {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, DynamicFluidTanks.MODID);

    public static final RegistryObject<BlockItem> TANK_CASING = ITEMS.register("tank_casing",
            () -> new BlockItem(BlockInit.TANK_CASING.get(), new Item.Properties()));

    public static final RegistryObject<BlockItem> TANK_GLASS = ITEMS.register("tank_glass",
            () -> new BlockItem(BlockInit.TANK_GLASS.get(), new Item.Properties()));

    public static final RegistryObject<BlockItem> TANK_VALVE = ITEMS.register("tank_valve",
            () -> new BlockItem(BlockInit.TANK_VALVE.get(), new Item.Properties()));

    public static final RegistryObject<BlockItem> TANK_CONTROLLER = ITEMS.register("tank_controller",
            () -> new BlockItem(BlockInit.TANK_CONTROLLER.get(), new Item.Properties()));
}
