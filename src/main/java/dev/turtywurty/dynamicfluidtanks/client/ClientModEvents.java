package dev.turtywurty.dynamicfluidtanks.client;

import dev.turtywurty.dynamicfluidtanks.DynamicFluidTanks;
import dev.turtywurty.dynamicfluidtanks.client.renderer.FluidTankBERenderer;
import dev.turtywurty.dynamicfluidtanks.client.screen.TankControllerScreen;
import dev.turtywurty.dynamicfluidtanks.init.BlockEntityTypeInit;
import dev.turtywurty.dynamicfluidtanks.init.MenuTypeInit;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = DynamicFluidTanks.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntityTypeInit.TANK_CONTROLLER.get(), FluidTankBERenderer::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->
                MenuScreens.register(MenuTypeInit.TANK_CONTROLLER.get(), TankControllerScreen::new));
    }
}
