package dev.turtywurty.dynamicfluidtanks.init;

import ca.weblite.objc.Proxy;
import dev.turtywurty.dynamicfluidtanks.DynamicFluidTanks;
import dev.turtywurty.dynamicfluidtanks.blockentity.TankControllerBlockEntity;
import dev.turtywurty.dynamicfluidtanks.blockentity.TankValveBlockEntity;
import net.minecraft.Util;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntityTypeInit {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, DynamicFluidTanks.MODID);

    public static final RegistryObject<BlockEntityType<TankControllerBlockEntity>> TANK_CONTROLLER =
            BLOCK_ENTITY_TYPES.register("tank_controller",
                    () -> BlockEntityType.Builder.of(TankControllerBlockEntity::new, BlockInit.TANK_CONTROLLER.get())
                            .build(Util.fetchChoiceType(References.BLOCK_ENTITY, DynamicFluidTanks.MODID + ":tank_controller")));

    public static final RegistryObject<BlockEntityType<TankValveBlockEntity>> TANK_VALVE =
            BLOCK_ENTITY_TYPES.register("tank_valve",
                    () -> BlockEntityType.Builder.of(TankValveBlockEntity::new, BlockInit.TANK_VALVE.get())
                            .build(Util.fetchChoiceType(References.BLOCK_ENTITY, DynamicFluidTanks.MODID + ":tank_valve")));
}
