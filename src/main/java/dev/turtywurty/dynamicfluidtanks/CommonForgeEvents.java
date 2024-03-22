package dev.turtywurty.dynamicfluidtanks;

import dev.turtywurty.dynamicfluidtanks.blockentity.MultiblockData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = DynamicFluidTanks.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonForgeEvents {
    @SubscribeEvent
    public static void blockPlace(BlockEvent.EntityPlaceEvent event) {
        LevelAccessor levelAccessor = event.getLevel();
        if (!(levelAccessor instanceof Level level))
            return;

        BlockPos pos = event.getPos();
        MultiblockData.INSTANCES.entrySet().stream()
                .filter(entry -> entry.getKey().equals(level.dimension()))
                .map(Map.Entry::getValue)
                .flatMap(List::stream)
                .filter(MultiblockData::isComplete)
                .filter(data -> data.isWithinMultiblock(pos))
                .forEach(MultiblockData::markForValidation);
    }

    @SubscribeEvent
    public static void blockBreak(BlockEvent.BreakEvent event) {
        LevelAccessor levelAccessor = event.getLevel();
        if (!(levelAccessor instanceof Level level))
            return;

        BlockPos pos = event.getPos();
        MultiblockData.INSTANCES.entrySet().stream()
                .filter(entry -> entry.getKey().equals(level.dimension()))
                .map(Map.Entry::getValue)
                .flatMap(List::stream)
                .filter(data -> data.isWithinMultiblock(pos))
                .filter(data -> !pos.equals(data.getControllerPos()))
                .forEach(MultiblockData::markForValidation);
    }
}
