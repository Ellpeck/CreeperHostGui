package net.creeperhost.minetogether.events;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.api.Order;
import net.creeperhost.minetogether.client.screen.OfflineScreen;
import net.creeperhost.minetogether.client.screen.SettingsScreen;
import net.creeperhost.minetogether.client.screen.chat.MTChatScreen;
import net.creeperhost.minetogether.client.screen.chat.ingame.GuiChatOurs;
import net.creeperhost.minetogether.client.screen.chat.ingame.GuiNewChatOurs;
import net.creeperhost.minetogether.client.screen.element.GuiButtonCreeper;
import net.creeperhost.minetogether.client.screen.element.GuiButtonMultiple;
import net.creeperhost.minetogether.client.screen.minigames.MinigamesScreen;
import net.creeperhost.minetogether.client.screen.order.GuiGetServer;
import net.creeperhost.minetogether.client.screen.serverlist.data.Server;
import net.creeperhost.minetogether.client.screen.serverlist.gui.MultiplayerPublicScreen;
import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.paul.Callbacks;
import net.creeperhost.minetogether.util.ScreenUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.RenderComponentsUtil;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class ScreenEvents
{
    private static final Logger logger = LogManager.getLogger();
    boolean first = true;
    Screen fakeGui = new Screen(new StringTextComponent("")) {};
    int u = 0;
    int v = 0;

    private boolean firstConnect = true;

    @SubscribeEvent
    public void openScreen(GuiScreenEvent.InitGuiEvent.Post event)
    {
        if(!MineTogether.isOnline) return;
        boolean buttonDrawn = false;
        
        if (event.getGui() instanceof MainMenuScreen)
        {
            if(Config.getInstance().getReplaceRealms())
            {
                Widget realmsButton = ScreenUtils.removeButton("menu.online", event.getWidgetList());
                Button realmsReplacmentButton = new Button(realmsButton.x, realmsButton.y, realmsButton.getWidth(), 20, new StringTextComponent(I18n.format("minetogether.realms.replace")), (button) ->
                {
                    Minecraft.getInstance().displayGuiScreen(GuiGetServer.getByStep(0, new Order()));
                });
                event.addWidget(realmsReplacmentButton);
            }
            if (MineTogether.instance.gdpr.hasAcceptedGDPR() && first)
            {
                first = false;
                MineTogether.proxy.startChat();
                String server = System.getProperty("mt.server");
                int serverId = -1;
                if (server != null)
                {
                    try {
                        serverId = Integer.parseInt(server);
                    } catch (Throwable t) {
                        logger.error("Unable to auto connect to server as unable to parse server ID");
                    }

                    Server serverObj = Callbacks.getServer(serverId);

                    if (serverObj != null)
                    {
                        String[] serverSplit = serverObj.host.split(":");

                        int realPort = -1;
                        try {
                            realPort = Integer.parseInt(serverSplit[1]);
                        } catch (Throwable t) {
                            logger.error("Unable to auto connect to server as unable to parse server port for ID " + serverId);
                        }

                        if (realPort != -1) Minecraft.getInstance().displayGuiScreen(new ConnectingScreen(event.getGui(), Minecraft.getInstance(), server, realPort));
                    }
                }
            }
            if (Config.getInstance().isServerListEnabled() || Config.getInstance().isChatEnabled())
            {
                MineTogether.instance.setRandomImplementation();
                if(!Config.instance.getReplaceRealms())
                {
                    event.addWidget(new GuiButtonCreeper(event.getGui().width / 2 - 124, event.getGui().height / 4 + 96, p ->
                    {
                        Minecraft.getInstance().displayGuiScreen(GuiGetServer.getByStep(0, new Order()));
                    }));
                }
            }
            //Replace Multiplayer Button
            AtomicInteger width = new AtomicInteger();
            AtomicInteger height = new AtomicInteger();
            AtomicInteger x = new AtomicInteger();
            AtomicInteger y = new AtomicInteger();
            
            event.getWidgetList().forEach(widget ->
            {
                if (widget instanceof Button)
                {
                    Button button = (Button) widget;
                    //Get the translated name so we can make sure to remove the correct button
                    String name = I18n.format("menu.multiplayer");
                    
                    if (button.getMessage().getString().equalsIgnoreCase(name))
                    {
                        width.set(button.getWidth());
                        height.set(button.getHeightRealms());
                        x.set(button.x);
                        y.set(button.y);
                        
                        button.active = false;
                        button.visible = false;
                    }
                }
            });
            
            event.addWidget(new Button(x.get(), y.get(), width.get(), height.get(), new StringTextComponent(I18n.format("menu.multiplayer")), p ->
            {
                if (MineTogether.instance.gdpr.hasAcceptedGDPR())
                {
                    Minecraft.getInstance().displayGuiScreen(new MultiplayerPublicScreen(event.getGui()));
                } else
                {
                    Minecraft.getInstance().displayGuiScreen(new MultiplayerScreen(event.getGui()));
                }
            }));
        }
        
        if (event.getGui() instanceof MultiplayerScreen || event.getGui() instanceof MultiplayerPublicScreen)
        {
            event.getWidgetList().forEach(b ->
            {
                if (b instanceof Button)
                {
                    if(b.getMessage().getString().equalsIgnoreCase(I18n.format("selectServer.select")))
                    {
                        b.x +=1;
                    }
                    if(b.getMessage().getString().equalsIgnoreCase(I18n.format("selectServer.edit")))
                    {
                        b.setWidth(b.getWidth() -9);
                        b.x +=1;
                    }
                    if (b.getMessage().getString().equalsIgnoreCase(I18n.format("selectServer.refresh")))
                    {
                        b.active = false;
                        b.visible = false;
                    }
                    if (b.getMessage().getString().equalsIgnoreCase(I18n.format("selectServer.delete")))
                    {
                        b.x -= 16;
                        b.setWidth(b.getWidth() - 6);
                    }
                    if (b.getMessage().getString().equalsIgnoreCase(I18n.format("selectServer.direct")))
                    {
                        b.x = event.getGui().width / 2 - 23;
                        b.y = event.getGui().height - 28;
                        b.setWidth(b.getWidth());
                    }
                    if (b.getMessage().getString().equalsIgnoreCase(I18n.format("selectServer.add")))
                    {
                        b.x -= 23;
                    }
                    if (b.getMessage().getString().equalsIgnoreCase(I18n.format("selectServer.cancel")))
                    {
                        b.x += 1;
                        b.setWidth(b.getWidth() - 2);
                    }
                }
            });
            
            event.addWidget(new GuiButtonMultiple(event.getGui().width / 2 + 134, event.getGui().height - 52, 2, p ->
            {
                Minecraft.getInstance().displayGuiScreen(new MultiplayerPublicScreen(((MultiplayerPublicScreen) event.getGui()).parent, ((MultiplayerPublicScreen) event.getGui()).listType, ((MultiplayerPublicScreen) event.getGui()).sortOrder));
            }));
            
            event.addWidget(new Button(event.getGui().width / 2 - 50, event.getGui().height - 52, 78, 20, new StringTextComponent("Minigames"), p ->
            {
                Minecraft.getInstance().displayGuiScreen(new MinigamesScreen(event.getGui()));
            }));
        }
        
        if (event.getGui() instanceof MainMenuScreen)
        {
            if (Config.getInstance().isEnableMainMenuFriends())
            {
                buttonDrawn = true;
                event.addWidget(new Button(event.getGui().width - 100 - 5, 5, 100, 20, new StringTextComponent(I18n.format("creeperhost.multiplayer.friends")), p ->
                {
                    MineTogether.proxy.openFriendsGui();
                }));
                
                int x = event.getGui().width - 20 - 5;
                if (buttonDrawn) x -= 99;
                
                event.addWidget(new GuiButtonMultiple(x, 5, 1, p ->
                {
                    if (Config.getInstance().isChatEnabled()) {
                        Minecraft.getInstance().displayGuiScreen(new MTChatScreen(event.getGui()));
                    } else {
                        Minecraft.getInstance().displayGuiScreen(new SettingsScreen(event.getGui()));
                    }
                }));
            }
        }
        
        if (event.getGui() instanceof IngameMenuScreen)
        {
            buttonDrawn = true;
            event.addWidget(new Button(event.getGui().width - 100 - 5, 5, 100, 20, new StringTextComponent(I18n.format("creeperhost.multiplayer.friends")), p ->
            {
                MineTogether.proxy.openFriendsGui();
            }));
            
            int x = event.getGui().width - 20 - 5;
            if (buttonDrawn) x -= 99;
            
            event.addWidget(new GuiButtonMultiple(x, 5, 1, p ->
            {
                Minecraft.getInstance().displayGuiScreen(new MTChatScreen(event.getGui()));
            }));
        }
    }
    
    boolean firstOpen = true;

    @SubscribeEvent
    public void openScreen(GuiOpenEvent event)
    {
        final Screen gui = event.getGui();

        if (firstConnect && gui instanceof MainMenuScreen)
        {
            File offline = new File("local/minetogether/offline.txt");

            if(!MineTogether.isOnline && !offline.exists())
            {
                firstConnect = false;
                event.setGui(new OfflineScreen(event.getGui()));
            }

            if(!MineTogether.isOnline) return;
            firstConnect = false;
            String server = System.getProperty("mt.server");
            int serverId = -1;
            if (server != null)
            {
                try {
                    serverId = Integer.parseInt(server);
                } catch (Throwable t) {
                    logger.error("Unable to auto connect to server as unable to parse server ID");
                }

                Server serverObj = Callbacks.getServer(serverId);


                if (serverObj != null)
                {
                    String[] serverSplit = serverObj.host.split(":");

                    int realPort = -1;
                    try {
                        realPort = Integer.parseInt(serverSplit[1]);
                    } catch (Throwable t) {
                        logger.error("Unable to auto connect to server as unable to parse server port for ID " + serverId);
                    }
                    ServerData serverData = new ServerData(serverObj.displayName, serverObj.host, false);

                    if(realPort != -1)
                        Minecraft.getInstance().displayGuiScreen(new ConnectingScreen(gui, Minecraft.getInstance(), serverData));
                }
            }
        }

        if(!MineTogether.isOnline) return;
        Screen screen = event.getGui();
        
        if (screen instanceof ChatScreen && Config.getInstance().isChatEnabled() && !MineTogether.instance.ingameChat.hasDisabledIngameChat() && MineTogether.instance.gdpr.hasAcceptedGDPR())
        {
            String presetString = "";
            boolean sleep = false;
            if (screen instanceof SleepInMultiplayerScreen)
            {
                sleep = true;
            }
            
            ChatScreen chatScreen = (ChatScreen) event.getGui();
            presetString = chatScreen.defaultInputFieldText;
            MinecraftServer minecraftServerInstance = MineTogether.server;
            
            if (Config.getInstance().isAutoMT() && minecraftServerInstance != null && minecraftServerInstance.isSinglePlayer() && Minecraft.getInstance().ingameGUI.getChatGUI() instanceof GuiNewChatOurs && firstOpen)
            {
                firstOpen = false;
                ((GuiNewChatOurs) Minecraft.getInstance().ingameGUI.getChatGUI()).setBase(!MineTogether.instance.gdpr.hasAcceptedGDPR());
            }
            try
            {
                event.setGui(new GuiChatOurs(presetString, sleep));
            } catch (Exception ignored)
            {
            }
        }
    }

    @SubscribeEvent
    public void guiRendered(TickEvent.RenderTickEvent evt)
    {
        if (!MineTogether.isOnline) return;
        if (!Config.getInstance().isFriendOnlineToastsEnabled()) return;

        if (MineTogether.instance.toastHandler.toastText != null)
        {
            long curTime = System.currentTimeMillis();
            if (MineTogether.instance.toastHandler.fadeTime > curTime)
            {
                long fadeDiff = MineTogether.instance.toastHandler.fadeTime - MineTogether.instance.toastHandler.endTime;
                long curFade = Math.min(MineTogether.instance.toastHandler.fadeTime - curTime, fadeDiff);
                float alpha = (float) curFade / (float) fadeDiff;

                RenderHelper.disableStandardItemLighting();
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, alpha);
                Minecraft.getInstance().getRenderManager().textureManager.bindTexture(MineTogether.instance.toastHandler.TEXTURE_TOASTS);

                MatrixStack matrixStack = new MatrixStack();

                drawTexturedModalRect(matrixStack, Minecraft.getInstance().getMainWindow().getScaledWidth() - 160, 0, u, v, 160, 32);
                GlStateManager.enableBlend();
                int textColour = (0xFFFFFF << 32) | ((int) (alpha * 255) << 24);
                List<IReorderingProcessor> s = RenderComponentsUtil.func_238505_a_(MineTogether.instance.toastHandler.toastText, 140, Minecraft.getInstance().fontRenderer);
                int start = 2;
                for(IReorderingProcessor properties : s)
                {
                    Minecraft.getInstance().fontRenderer.func_238407_a_(matrixStack, properties, Minecraft.getInstance().getMainWindow().getScaledWidth() - 155, start +=8, textColour);
                }

            } else
            {
                MineTogether.instance.toastHandler.clearToast(false);
            }
        }
    }

    private void drawTexturedModalRect(MatrixStack matrixStack, int x, int y, int textureX, int textureY, int width, int height)
    {
        fakeGui.blit(matrixStack, x, y, textureX, textureY, width, height);
    }
}
