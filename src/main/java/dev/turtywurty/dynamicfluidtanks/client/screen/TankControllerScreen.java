package dev.turtywurty.dynamicfluidtanks.client.screen;

import dev.turtywurty.dynamicfluidtanks.DynamicFluidTanks;
import dev.turtywurty.dynamicfluidtanks.blockentity.LongFluidTank;
import dev.turtywurty.dynamicfluidtanks.menu.TankControllerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import org.jetbrains.annotations.NotNull;

public class TankControllerScreen extends AbstractContainerScreen<TankControllerMenu> {
    private static final ResourceLocation TEXTURE
            = new ResourceLocation(DynamicFluidTanks.MODID, "textures/gui/tank_controller.png");

    public TankControllerScreen(TankControllerMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);

        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        pGuiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        pGuiGraphics.fill(this.leftPos + 99, this.topPos + 14, this.leftPos + 117, this.topPos + 64, 0xFF404040);

        LazyOptional<LongFluidTank> tankHolder = this.menu.getBlockEntity().getTank();
        if (!tankHolder.isPresent())
            return;

        LongFluidTank tank = tankHolder.orElseThrow(IllegalStateException::new);
        if(tank.isEmpty())
            return;

        FluidStack fluidStack = tank.getFluid();
        var fluidTypeExtensions = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        ResourceLocation stillTexture = fluidTypeExtensions.getStillTexture(fluidStack);
        if(stillTexture == null)
            return;

        TextureAtlasSprite sprite =
                this.minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stillTexture);
        int tintColor = fluidTypeExtensions.getTintColor(fluidStack);

        float alpha = ((tintColor >> 24) & 0xFF) / 255f;
        float red = ((tintColor >> 16) & 0xFF) / 255f;
        float green = ((tintColor >> 8) & 0xFF) / 255f;
        float blue = (tintColor & 0xFF) / 255f;

        pGuiGraphics.setColor(red, green, blue, alpha);

        int fluidHeight = getFluidHeight(tank);
        pGuiGraphics.blit(
                this.leftPos + 100,
                getFluidY(fluidHeight),
                0,
                16,
                fluidHeight,
                sprite);

        pGuiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        renderTooltip(pGuiGraphics, pMouseX, pMouseY);

        LazyOptional<LongFluidTank> tankHolder = this.menu.getBlockEntity().getTank();
        if (!tankHolder.isPresent())
            return;

        LongFluidTank tank = tankHolder.orElseThrow(IllegalStateException::new);
        if(tank.isEmpty())
            return;

        int fluidHeight = getFluidHeight(tank);
        if(!isHovering(100, getFluidY(fluidHeight) - this.topPos, 16, fluidHeight, pMouseX, pMouseY))
            return;

        Component component = MutableComponent.create(tank.getFluid().getDisplayName().getContents())
                .append(" (%s/%s mB)".formatted(tank.getFluidAmountLong(), tank.getCapacityLong()));
        pGuiGraphics.renderTooltip(this.font, component, pMouseX, pMouseY);
    }

    private static int getFluidHeight(LongFluidTank tank) {
        long amount = tank.getFluidAmountLong();
        long capacity = tank.getCapacityLong();
        double ratio = (double) amount / (double) capacity;

        return (int) (48 * ratio);
    }

    private int getFluidY(int fluidHeight) {
        return this.topPos + 15 + (48 - fluidHeight);
    }
}
