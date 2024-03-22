package dev.turtywurty.dynamicfluidtanks;

import com.mojang.blaze3d.shaders.FogShape;
import dev.turtywurty.dynamicfluidtanks.blockentity.LongFluidTank;
import dev.turtywurty.dynamicfluidtanks.blockentity.MultiblockData;
import dev.turtywurty.dynamicfluidtanks.blockentity.TankControllerBlockEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = DynamicFluidTanks.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEvents {
    @SubscribeEvent
    public static void fogColor(ViewportEvent.ComputeFogColor event) {
//        Minecraft minecraft = Minecraft.getInstance();
//        ClientLevel level = minecraft.level;
//        MultiblockData.INSTANCES.entrySet().stream()
//                .filter(entry -> entry.getKey().equals(level.dimension()))
//                .map(Map.Entry::getValue)
//                .flatMap(List::stream)
//                .filter(MultiblockData::isComplete)
//                .forEach(data -> {
//                    BlockPos controllerPos = data.getControllerPos();
//                    BlockEntity blockEntity = level.getBlockEntity(controllerPos);
//                    if (blockEntity instanceof TankControllerBlockEntity be) {
//                        LazyOptional<LongFluidTank> tank = be.getTank();
//                        if (!tank.isPresent())
//                            return;
//
//                        FluidStack fluid = tank.orElseThrow(IllegalStateException::new).getFluidData();
//                        if (fluid.isEmpty())
//                            return;
//
//                        var fluidTypeExtensions = IClientFluidTypeExtensions.of(fluid.getFluid());
//                        if (fluidTypeExtensions == null)
//                            return;
//
//                        Camera camera = event.getCamera();
//                        float partialTick = (float) event.getPartialTick();
//                        int renderDistance = minecraft.options.getEffectiveRenderDistance();
//                        float darkenAmount = minecraft.gameRenderer.getDarkenWorldAmount(partialTick);
//                        Vector3f currentFogColor = new Vector3f(event.getRed(), event.getGreen(), event.getBlue());
//                        currentFogColor = fluidTypeExtensions.modifyFogColor(camera, partialTick, level, renderDistance, darkenAmount, currentFogColor);
//
//                        event.setRed(currentFogColor.x);
//                        event.setGreen(currentFogColor.y);
//                        event.setBlue(currentFogColor.z);
//                    }
//                });
    }

    @SubscribeEvent
    public static void modifyFogRender(ViewportEvent.RenderFog event) {
//        Minecraft minecraft = Minecraft.getInstance();
//        ClientLevel level = minecraft.level;
//        MultiblockData.INSTANCES.entrySet().stream()
//                .filter(entry -> entry.getKey().equals(level.dimension()))
//                .map(Map.Entry::getValue)
//                .flatMap(List::stream)
//                .filter(MultiblockData::isComplete)
//                .forEach(data -> {
//                    BlockPos controllerPos = data.getControllerPos();
//                    BlockEntity blockEntity = level.getBlockEntity(controllerPos);
//                    if (blockEntity instanceof TankControllerBlockEntity be) {
//                        LazyOptional<LongFluidTank> tank = be.getTank();
//                        if (!tank.isPresent())
//                            return;
//
//                        FluidStack fluid = tank.orElseThrow(IllegalStateException::new).getFluidData();
//                        if (fluid.isEmpty())
//                            return;
//
//                        var fluidTypeExtensions = IClientFluidTypeExtensions.of(fluid.getFluid());
//                        if (fluidTypeExtensions == null)
//                            return;
//
//                        Camera camera = event.getCamera();
//                        FogRenderer.FogMode mode = event.getMode();
//                        float partialTick = (float) event.getPartialTick();
//                        float renderDistance = minecraft.options.getEffectiveRenderDistance();
//                        float nearDistance = event.getNearPlaneDistance();
//                        float farDistance = event.getFarPlaneDistance();
//                        FogShape shape = event.getFogShape();
//
//                        fluidTypeExtensions.modifyFogRender(camera, mode, renderDistance, partialTick, nearDistance, farDistance, shape);
//                    }
//                });
    }
}
