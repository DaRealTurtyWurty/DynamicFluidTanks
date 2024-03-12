package dev.turtywurty.dynamicfluidtanks.init;

import dev.turtywurty.dynamicfluidtanks.DynamicFluidTanks;
import dev.turtywurty.dynamicfluidtanks.menu.TankControllerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MenuTypeInit {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, DynamicFluidTanks.MODID);

    public static final RegistryObject<MenuType<TankControllerMenu>> TANK_CONTROLLER =
            MENU_TYPES.register("tank_controller", () -> IForgeMenuType.create(TankControllerMenu::new));
}
