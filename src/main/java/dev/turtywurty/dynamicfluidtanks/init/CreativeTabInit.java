package dev.turtywurty.dynamicfluidtanks.init;

import dev.turtywurty.dynamicfluidtanks.DynamicFluidTanks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CreativeTabInit {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DynamicFluidTanks.MODID);

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + DynamicFluidTanks.MODID))
                    .icon(ItemInit.TANK_CONTROLLER.get()::getDefaultInstance)
                    .displayItems((pParameters, pOutput) -> ItemInit.ITEMS.getEntries()
                            .stream()
                            .map(RegistryObject::get)
                            .forEach(pOutput::accept))
                    .noScrollBar()
                    .build());
}
