package com.ayydxn.iridium.hud;

import com.ayydxn.iridium.IridiumClientMod;
import com.ayydxn.iridium.options.IridiumGameOptions;
import com.ayydxn.iridium.util.ClientFramerateTracker;
import com.google.common.collect.Queues;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.client.rendering.v1.LayeredDrawerWrapper;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.util.Deque;
import java.util.List;

public class IridiumHudOverlay implements ClientTickEvents.StartTick, HudLayerRegistrationCallback
{
    private final List<Component> overlayTextQueue = Lists.newArrayList();
    private final Minecraft client = Minecraft.getInstance();
    private final IridiumGameOptions iridiumGameOptions = IridiumClientMod.getInstance().getGameOptions();

    @Override
    public void onStartTick(Minecraft client)
    {
        this.overlayTextQueue.clear();

        if (this.iridiumGameOptions.advancedGraphicsOptions.showFPSOverlay)
        {
            ClientFramerateTracker clientFramerateTracker = ClientFramerateTracker.getInstance();

            Component fpsOverlayText = Component.translatable("iridium.advancedGraphics.fpsOverlay", clientFramerateTracker.getSmoothFPS(),
                    clientFramerateTracker.getAverageFPS(), clientFramerateTracker.getOnePercentLowFPS(), clientFramerateTracker.getPointOnePercentLowFPS());

            this.overlayTextQueue.add(fpsOverlayText);
        }

        if (this.iridiumGameOptions.advancedGraphicsOptions.showCoordinates && client.player != null && !client.showOnlyReducedInfo())
        {
            Vec3 playerPosition = client.player.position();

            Component coordinatesOverlayText = Component.translatable("iridium.advancedGraphics.coordinatesOverlay", String.format("%.2f", playerPosition.x()),
                    String.format("%.2f", playerPosition.y()), String.format("%.2f", playerPosition.z()));

            this.overlayTextQueue.add(coordinatesOverlayText);
        }
    }

    @Override
    public void register(LayeredDrawerWrapper layeredDrawer)
    {
        layeredDrawer.attachLayerAfter(IdentifiedLayer.SUBTITLES, IridiumClientMod.of("hud_overlay"), this::renderOverlay);
    }

    private void renderOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker)
    {
        if (!this.client.getDebugOverlay().showDebugScreen())
        {
            IridiumGameOptions.OverlayPosition overlayPosition = this.iridiumGameOptions.advancedGraphicsOptions.overlayPosition;
            int xPosition;
            int yPosition = 2;

            if (overlayPosition == IridiumGameOptions.OverlayPosition.BottomRight || overlayPosition == IridiumGameOptions.OverlayPosition.BottomLeft)
                yPosition = this.client.getWindow().getGuiScaledHeight() - this.client.font.lineHeight - 2;

            // In order to keep the text order consistent across all positions, we reverse the list
            // or else the FPS and coordinates will swap places in the bottom corners.
            List<Component> overlayTextList = (overlayPosition == IridiumGameOptions.OverlayPosition.BottomRight ||
                    overlayPosition == IridiumGameOptions.OverlayPosition.BottomLeft) ? this.overlayTextQueue.reversed() : this.overlayTextQueue;

            for (Component overlayText : overlayTextList)
            {
                if (overlayPosition == IridiumGameOptions.OverlayPosition.TopRight || overlayPosition == IridiumGameOptions.OverlayPosition.BottomRight)
                {
                    xPosition = this.client.getWindow().getGuiScaledWidth() - this.client.font.width(overlayText) - 2;
                }
                else
                {
                    xPosition = 2;
                }

                this.drawString(guiGraphics, overlayText, xPosition, yPosition);

                if (overlayPosition == IridiumGameOptions.OverlayPosition.BottomLeft || overlayPosition == IridiumGameOptions.OverlayPosition.BottomRight)
                {
                    yPosition -= this.client.font.lineHeight + 2;
                }
                else
                {
                    yPosition += this.client.font.lineHeight + 2;
                }
            }
        }
    }

    private void drawString(GuiGraphics guiGraphics, Component text, int xPosition, int yPosition)
    {
        switch (this.iridiumGameOptions.advancedGraphicsOptions.textContrast)
        {
            case None -> guiGraphics.drawString(this.client.font, text, xPosition, yPosition, Color.WHITE.getRGB(), false);

            case Background ->
            {
                guiGraphics.fill(xPosition - 1, yPosition - 1, xPosition + this.client.font.width(text) + 1,
                        yPosition + this.client.font.lineHeight, -1873784752);

                guiGraphics.drawString(this.client.font, text, xPosition, yPosition, Color.WHITE.getRGB(), false);
            }

            case Shadow -> guiGraphics.drawString(this.client.font, text, xPosition, yPosition, Color.WHITE.getRGB(), true);
        }
    }
}
