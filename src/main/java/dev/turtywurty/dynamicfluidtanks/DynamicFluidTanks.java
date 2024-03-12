package dev.turtywurty.dynamicfluidtanks;

import dev.turtywurty.dynamicfluidtanks.init.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(DynamicFluidTanks.MODID)
public class DynamicFluidTanks {
    public static final String MODID = "dynamicfluidtanks";
    public static final Logger LOGGER = LoggerFactory.getLogger(DynamicFluidTanks.class);

    public DynamicFluidTanks() {
        LOGGER.info("Hello from DynamicFluidTanks!");

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BlockInit.BLOCKS.register(bus);
        BlockEntityTypeInit.BLOCK_ENTITY_TYPES.register(bus);
        MenuTypeInit.MENU_TYPES.register(bus);
        ItemInit.ITEMS.register(bus);
        CreativeTabInit.TABS.register(bus);
    }
}
