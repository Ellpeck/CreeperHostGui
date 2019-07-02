package net.creeperhost.minetogether.gui.chat.ingame;

import com.google.common.collect.Lists;
import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.gui.chat.GuiMTChat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.List;

public class GuiNewChatOurs extends GuiNewChat
{
    
    public boolean base = true;
    
    @Override
    public void printChatMessageWithOptionalDeletion(ITextComponent chatComponent, int chatLineId)
    {
        if (!base)
            unread = true;
        super.printChatMessageWithOptionalDeletion(chatComponent, chatLineId);
    }
    
    private final Minecraft mc;
    
    private final List<ChatLine> chatLines = Lists.<ChatLine>newArrayList();
    /**
     * List of the ChatLines currently drawn
     */
    private final List<ChatLine> drawnChatLines = Lists.<ChatLine>newArrayList();
    private int scrollPos;
    private boolean isScrolled;
    
    private final List<String> sentMessages = Lists.<String>newArrayList();
    
    public GuiNewChatOurs(Minecraft mcIn)
    {
        super(mcIn);
        mc = mcIn;
    }
    
    @Override
    public int getChatWidth()
    {
        return (int) (super.getChatWidth() - (16 * 0.75));
    }
    
    private static Field drawnChatLinesField = null;
    private List<ChatLine> vanillaDrawnChatLines = null;
    
    @Override
    public void drawChat(int updateCounter)
    {
        if (base)
            super.drawChat(updateCounter);
        else
        {
            if (this.mc.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN)
            {
                int i = this.getLineCount();
                int j = this.drawnChatLines.size();
                float f = this.mc.gameSettings.chatOpacity * 0.9F + 0.1F;
                
                if (j > 0)
                {
                    boolean flag = false;
                    
                    if (this.getChatOpen())
                    {
                        flag = true;
                    }
                    
                    float f1 = this.getChatScale();
                    int k = MathHelper.ceil((float) this.getChatWidth() / f1);
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(2.0F, 8.0F, 0.0F);
                    GlStateManager.scale(f1, f1, 1.0F);
                    int l = 0;
                    
                    for (int i1 = 0; i1 + this.scrollPos < this.drawnChatLines.size() && i1 < i; ++i1)
                    {
                        ChatLine chatline = this.drawnChatLines.get(i1 + this.scrollPos);
                        
                        if (chatline != null)
                        {
                            int j1 = updateCounter - chatline.getUpdatedCounter();
                            
                            if (j1 < 200 || flag)
                            {
                                double d0 = (double) j1 / 200.0D;
                                d0 = 1.0D - d0;
                                d0 = d0 * 10.0D;
                                d0 = MathHelper.clamp(d0, 0.0D, 1.0D);
                                d0 = d0 * d0;
                                int l1 = (int) (255.0D * d0);
                                
                                if (flag)
                                {
                                    l1 = 255;
                                }
                                
                                l1 = (int) ((float) l1 * f);
                                ++l;
                                
                                if (l1 > 3)
                                {
                                    int i2 = 0;
                                    int j2 = -i1 * 9;
                                    drawRect(-2, j2 - 9, 0 + k + 4, j2, l1 / 2 << 24);
                                    String s = chatline.getChatComponent().getFormattedText();
                                    GlStateManager.enableBlend();
                                    this.mc.fontRendererObj.drawStringWithShadow(s, 0.0F, (float) (j2 - 8), 16777215 + (l1 << 24));
                                    GlStateManager.disableAlpha();
                                    GlStateManager.disableBlend();
                                }
                            }
                        }
                    }
                    
                    if (flag)
                    {
                        int k2 = this.mc.fontRendererObj.FONT_HEIGHT;
                        GlStateManager.translate(-3.0F, 0.0F, 0.0F);
                        int l2 = j * k2 + j;
                        int i3 = l * k2 + l;
                        int j3 = this.scrollPos * i3 / j;
                        int k1 = i3 * i3 / l2;
                        
                        if (l2 != i3)
                        {
                            int k3 = j3 > 0 ? 170 : 96;
                            int l3 = this.isScrolled ? 13382451 : 3355562;
                            drawRect(0, -j3, 2, -j3 - k1, l3 + (k3 << 24));
                            drawRect(2, -j3, 1, -j3 - k1, 13421772 + (k3 << 24));
                        }
                    }
                    
                    GlStateManager.popMatrix();
                }
            }
        }
        
        List<ChatLine> tempDrawnChatLines = drawnChatLines;
        
        if (base)
        {
            if (vanillaDrawnChatLines == null)
            {
                if (drawnChatLinesField == null)
                {
                    drawnChatLinesField = ReflectionHelper.findField(GuiNewChat.class, "field_146253_i", "drawnChatLines");
                }
                
                try
                {
                    vanillaDrawnChatLines = (List<ChatLine>) drawnChatLinesField.get(this);
                } catch (IllegalAccessException e)
                {
                }
            }
            
            tempDrawnChatLines = vanillaDrawnChatLines;
        }
        
        if (getChatOpen() && !CreeperHost.instance.ingameChat.hasDisabledIngameChat())
        {
            float f1 = this.getChatScale();
            GlStateManager.pushMatrix();
            GlStateManager.translate(2.0F, 8.0F, 0.0F);
            GlStateManager.scale(f1, f1, 1.0F);

            int minLines = base ? 8 : 14;

            int k = MathHelper.ceil((float) this.getChatWidth() / f1);

            for (int line = tempDrawnChatLines.size(); line < minLines; line++)
            {
                int l1 = 255;
                int j2 = -line * 9;
                drawRect(-2, j2 - 9, 0 + k + 4, j2, l1 / 2 << 24);
            }

            int lines = Math.max(minLines, tempDrawnChatLines.size());

            lines = 1;

            if (!base)
                GuiMTChat.drawLogo(mc.fontRendererObj, 0 + k + 4 + 2, lines * 9, -2, -lines * 9, 0.75F);
            
            GlStateManager.popMatrix();
        }
    }
    
    @Override
    public List<String> getSentMessages()
    {
        return base ? super.getSentMessages() : sentMessages;
    }
    
    @Override
    public void addToSentMessages(String message)
    {
        if (base)
            super.addToSentMessages(message);
        else
        {
            if (this.sentMessages.isEmpty() || !(this.sentMessages.get(this.sentMessages.size() - 1)).equals(message))
            {
                this.sentMessages.add(message);
            }
        }
    }
    
    public boolean unread;
    
    public void setChatLine(ITextComponent chatComponent, int chatLineId, int updateCounter, boolean displayOnly)
    {
        if (base)
        {
            unread = true;
        }
        if (chatLineId != 0)
        {
            this.deleteChatLine(chatLineId);
        }
        
        int i = MathHelper.floor((float) this.getChatWidth() / this.getChatScale());
        List<ITextComponent> list = GuiUtilRenderComponents.splitText(chatComponent, i, this.mc.fontRendererObj, false, false);
        boolean flag = this.getChatOpen();
        
        for (ITextComponent itextcomponent : list)
        {
            if (flag && this.scrollPos > 0)
            {
                this.isScrolled = true;
                this.scroll(1);
            }
            
            this.drawnChatLines.add(0, new ChatLine(updateCounter, itextcomponent, chatLineId));
        }
        
        while (this.drawnChatLines.size() > 100)
        {
            this.drawnChatLines.remove(this.drawnChatLines.size() - 1);
        }
        
        if (!displayOnly)
        {
            this.chatLines.add(0, new ChatLine(updateCounter, chatComponent, chatLineId));
            
            while (this.chatLines.size() > 100)
            {
                this.chatLines.remove(this.chatLines.size() - 1);
            }
        }
    }
    
    @Override
    public void resetScroll()
    {
        if (base)
            super.resetScroll();
        else
        {
            this.scrollPos = 0;
            this.isScrolled = false;
        }
    }
    
    @Override
    public void scroll(int amount)
    {
        if (base)
            super.scroll(amount);
        else
        {
            this.scrollPos += amount;
            int i = this.drawnChatLines.size();
            
            if (this.scrollPos > i - this.getLineCount())
            {
                this.scrollPos = i - this.getLineCount();
            }
            
            if (this.scrollPos <= 0)
            {
                this.scrollPos = 0;
                this.isScrolled = false;
            }
        }
    }
    
    @Nullable
    @Override
    public ITextComponent getChatComponent(int mouseX, int mouseY)
    {
        if (base)
            return super.getChatComponent(mouseX, mouseY);
        else
        {
            if (!this.getChatOpen())
            {
                return null;
            } else
            {
                ScaledResolution scaledresolution = new ScaledResolution(this.mc);
                int i = scaledresolution.getScaleFactor();
                float f = this.getChatScale();
                int j = mouseX / i - 2;
                int k = mouseY / i - 40;
                j = MathHelper.floor((float) j / f);
                k = MathHelper.floor((float) k / f);
                
                if (j >= 0 && k >= 0)
                {
                    int l = Math.min(this.getLineCount(), this.drawnChatLines.size());
                    
                    if (j <= MathHelper.floor((float) this.getChatWidth() / this.getChatScale()) && k < this.mc.fontRendererObj.FONT_HEIGHT * l + l)
                    {
                        int i1 = k / this.mc.fontRendererObj.FONT_HEIGHT + this.scrollPos;
                        
                        if (i1 >= 0 && i1 < this.drawnChatLines.size())
                        {
                            ChatLine chatline = this.drawnChatLines.get(i1);
                            int j1 = 0;
                            
                            for (ITextComponent itextcomponent : chatline.getChatComponent())
                            {
                                if (itextcomponent instanceof TextComponentString)
                                {
                                    j1 += this.mc.fontRendererObj.getStringWidth(GuiUtilRenderComponents.removeTextColorsIfConfigured(((TextComponentString) itextcomponent).getText(), false));
                                    
                                    if (j1 > j)
                                    {
                                        return itextcomponent;
                                    }
                                }
                            }
                        }
                        
                        return null;
                    } else
                    {
                        return null;
                    }
                } else
                {
                    return null;
                }
            }
        }
    }
}