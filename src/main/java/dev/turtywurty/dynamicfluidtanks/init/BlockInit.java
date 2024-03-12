package dev.turtywurty.dynamicfluidtanks.init;

import dev.turtywurty.dynamicfluidtanks.DynamicFluidTanks;
import dev.turtywurty.dynamicfluidtanks.block.TankControllerBlock;
import dev.turtywurty.dynamicfluidtanks.block.TankValveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockInit {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, DynamicFluidTanks.MODID);

    public static final RegistryObject<Block> TANK_CASING = BLOCKS.register("tank_casing",
            () -> new Block(Block.Properties.copy(Blocks.IRON_BLOCK)));

    public static final RegistryObject<Block> TANK_GLASS = BLOCKS.register("tank_glass",
            () -> new GlassBlock(Block.Properties.copy(Blocks.GLASS)));

    public static final RegistryObject<Block> TANK_VALVE = BLOCKS.register("tank_valve",
            () -> new TankValveBlock(Block.Properties.copy(Blocks.IRON_BLOCK)));

    public static final RegistryObject<Block> TANK_CONTROLLER = BLOCKS.register("tank_controller", TankControllerBlock::new);
}
