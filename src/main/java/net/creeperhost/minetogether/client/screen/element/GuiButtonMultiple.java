package net.creeperhost.minetogether.client.screen.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.creeperhost.minetogether.lib.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

public class GuiButtonMultiple extends Button
{
    private static ResourceLocation buttonImg = new ResourceLocation(Constants.MOD_ID, "textures/gui.png");
    private final int index;
    
    public GuiButtonMultiple(int xPos, int yPos, int index, Button.IPressable onPress)
    {
        super(xPos, yPos, 20, 20, new StringTextComponent(""), onPress);
        this.index = index;
    }
    
    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        if (this.visible)
        {
            ResourceLocation buttonImage = buttonImg;
            Minecraft.getInstance().getTextureManager().bindTexture(buttonImage);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.blit(matrixStack, this.x, this.y, index * 20, isHovered ? this.height : 0, this.width, this.height);
        }
    }
}