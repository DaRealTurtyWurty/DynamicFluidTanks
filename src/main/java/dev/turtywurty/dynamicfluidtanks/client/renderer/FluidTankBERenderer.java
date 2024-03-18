package dev.turtywurty.dynamicfluidtanks.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.turtywurty.dynamicfluidtanks.blockentity.LongFluidTank;
import dev.turtywurty.dynamicfluidtanks.blockentity.MultiblockData;
import dev.turtywurty.dynamicfluidtanks.blockentity.TankControllerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class FluidTankBERenderer implements BlockEntityRenderer<TankControllerBlockEntity> {
    public FluidTankBERenderer(BlockEntityRendererProvider.Context ctx) {

    }

    private static void drawVertex(VertexConsumer builder, PoseStack poseStack, float x, float y, float z, float u, float v, int packedLight, int color) {
        builder.vertex(poseStack.last().pose(), x, y, z)
                .color(color)
                .uv(u, v)
                .uv2(packedLight)
                .normal(1, 0, 0)
                .endVertex();
    }

    private static void drawQuad(VertexConsumer builder, PoseStack poseStack, float x0, float y0, float z0, float x1, float y1, float z1, float u0, float v0, float u1, float v1, int packedLight, int color) {
        drawVertex(builder, poseStack, x0, y0, z0, u0, v0, packedLight, color);
        drawVertex(builder, poseStack, x0, y1, z1, u0, v1, packedLight, color);
        drawVertex(builder, poseStack, x1, y1, z1, u1, v1, packedLight, color);
        drawVertex(builder, poseStack, x1, y0, z0, u1, v0, packedLight, color);
    }

    private static void drawQuadReversed(VertexConsumer builder, PoseStack poseStack, float x0, float y0, float z0, float x1, float y1, float z1, float u0, float v0, float u1, float v1, int packedLight, int color) {
        drawVertex(builder, poseStack, x0, y0, z0, u0, v0, packedLight, color);
        drawVertex(builder, poseStack, x1, y0, z1, u1, v0, packedLight, color);
        drawVertex(builder, poseStack, x1, y1, z1, u1, v1, packedLight, color);
        drawVertex(builder, poseStack, x0, y1, z0, u0, v1, packedLight, color);
    }

    private static void drawCuboid(VertexConsumer builder, PoseStack poseStack, BlockPos fromPos, Vector3f start, Vector3f end, TextureAtlasSprite sprite, int packedLight, int color) {
        var relativeStart = Vec3.atLowerCornerWithOffset(fromPos, -start.x(), -start.y(), -start.z());

        float width = end.x() - start.x();
        float height = end.y() - start.y();
        float depth = end.z() - start.z();

        float u0 = sprite.getU0() * width;
        float v0 = sprite.getV0() * height;
        float u1 = sprite.getU1() * width;
        float v1 = sprite.getV1() * height;

        poseStack.pushPose();
        poseStack.translate(-relativeStart.x(), -relativeStart.y(), -relativeStart.z());

        // Draw the front face
        drawQuad(builder, poseStack, 0, 0, 0, width, height, 0, u0, v0, u1, v1, packedLight, color);

        // Draw the back face
        drawQuad(builder, poseStack, width, 0, depth, 0, height, depth, u0, v0, u1, v1, packedLight, color);

        // Draw the top face
        drawQuad(builder, poseStack, 0, height, 0, width, height, depth, u0, v0, u1, v1, packedLight, color);

        // Draw the bottom face
        drawQuad(builder, poseStack, 0, 0, depth, width, 0, 0, u0, v0, u1, v1, packedLight, color);

        // Draw the right face
        drawQuadReversed(builder, poseStack, 0, 0, 0, 0, height, depth, u0, v0, u1, v1, packedLight, color);

        // Draw the left face
        drawQuadReversed(builder, poseStack, width, 0, depth, width, height, 0, u0, v0, u1, v1, packedLight, color);

        poseStack.popPose();
    }

    @Override
    public void render(@NotNull TankControllerBlockEntity pBlockEntity, float pPartialTick, @NotNull PoseStack pPoseStack,
                       @NotNull MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        LazyOptional<LongFluidTank> tankHolder = pBlockEntity.getTank();
        if (!tankHolder.isPresent())
            return;

        LongFluidTank fluidTank = tankHolder.orElseThrow(IllegalStateException::new);
        MultiblockData multiblockData = pBlockEntity.getMultiblockData();
        if (fluidTank.isEmpty() || !multiblockData.isComplete())
            return;

        FluidStack fluidStack = fluidTank.getFluid();
        var extensions = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        if (extensions == null)
            return;

        ResourceLocation stillTexture = extensions.getStillTexture(fluidStack);
        if (stillTexture == null)
            return;

        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stillTexture);

        int tintColor = extensions.getTintColor(fluidStack);
        RenderType renderType = ItemBlockRenderTypes.getRenderLayer(fluidStack.getFluid().defaultFluidState());

        long amount = fluidTank.getLongAmount();
        long capacity = fluidTank.getLongCapacity();

        VertexConsumer builder = pBuffer.getBuffer(renderType);

        BlockPos pos = pBlockEntity.getBlockPos();
        float percent = ((float) amount / (float) capacity);
        float startX = multiblockData.getMinX() + 1;
        float startY = multiblockData.getMinY() + 1;
        float startZ = multiblockData.getMinZ() + 1;
        float endX = multiblockData.getMaxX();
        float endY = startY + (percent * (multiblockData.getMaxY() - startY));
        float endZ = multiblockData.getMaxZ();

        drawCuboid(builder, pPoseStack, pos, new Vector3f(startX, startY, startZ), new Vector3f(endX, endY, endZ), sprite, 15728880, tintColor);
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull TankControllerBlockEntity pBlockEntity) {
        return true;
    }

    @Override
    public boolean shouldRender(@NotNull TankControllerBlockEntity pBlockEntity, @NotNull Vec3 pCameraPos) {
        return true;
    }
}
