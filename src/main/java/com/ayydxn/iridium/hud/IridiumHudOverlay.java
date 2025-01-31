package com.ayydxn.iridium.hud;

import com.ayydxn.iridium.IridiumClientMod;
import com.ayydxn.iridium.options.IridiumGameOptions;
import com.ayydxn.iridium.util.ClientFramerateTracker;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.awt.*;

public class IridiumHudOverlay implements HudRenderCallback
{
    private final Minecraft client = Minecraft.getInstance();
    private final IridiumGameOptions iridiumGameOptions = IridiumClientMod.getInstance().getGameOptions();

    @Override
    public void onHudRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker)
    {
        if (!this.client.getDebugOverlay().showDebugScreen())
        {
            if (this.iridiumGameOptions.advancedGraphicsOptions.showFPSOverlay)
                this.renderFramerateOverlay(guiGraphics);

            if (this.iridiumGameOptions.advancedGraphicsOptions.showCoordinates)
                this.renderCoordinatesOverlay(guiGraphics);
        }
    }

    private void renderFramerateOverlay(GuiGraphics guiGraphics)
    {
        int currentClientFPS = this.client.getFps();
        ClientFramerateTracker clientFramerateTracker = IridiumClientMod.getInstance().getClientFramerateTracker();
        Component fpsOverlayText = Component.translatable("iridium.advancedGraphics.fpsOverlay", currentClientFPS,
                clientFramerateTracker.getAverageFPS(), clientFramerateTracker.getHighestFPS(), clientFramerateTracker.getLowestFPS());
        int xPosition = 0;
        int yPosition = 0;

        switch (this.iridiumGameOptions.advancedGraphicsOptions.overlayPosition)
        {
            case TopLeft ->
            {
                xPosition = 2;
                yPosition = 2;
            }
            case TopRight ->
            {
                xPosition = this.client.getWindow().getGuiScaledWidth() - this.client.font.width(fpsOverlayText) - 2;
                yPosition = 2;
            }
            case BottomLeft ->
            {
                xPosition = 2;
                yPosition = this.client.getWindow().getGuiScaledWidth() - this.client.font.lineHeight - 2;
            }
            case BottomRight ->
            {
                xPosition = this.client.getWindow().getGuiScaledWidth() - this.client.font.width(fpsOverlayText) - 2;
                yPosition = this.client.getWindow().getGuiScaledWidth() - this.client.font.lineHeight - 2;
            }
        }

        this.drawString(guiGraphics, fpsOverlayText, xPosition, yPosition);
    }

    private void renderCoordinatesOverlay(GuiGraphics guiGraphics)
    {
        if (this.client.player == null)
            return;

        if (this.client.showOnlyReducedInfo())
            return;

        Vec3 playerPosition = this.client.player.position();
        Component coordinatesOverlay = Component.translatable("iridium.advancedGraphics.coordinatesOverlay", String.format("%.2f", playerPosition.x()),
                String.format("%.2f", playerPosition.y()), String.format("%.2f", playerPosition.z()));
        int xPosition = 0;
        int yPosition = 0;

        switch (this.iridiumGameOptions.advancedGraphicsOptions.overlayPosition)
        {
            case TopLeft ->
            {
                xPosition = 2;
                yPosition = 12;
            }

            case TopRight ->
            {
                xPosition = this.client.getWindow().getGuiScaledWidth() - this.client.font.width(coordinatesOverlay) - 2;
                yPosition = 12;
            }

            case BottomLeft ->
            {
                xPosition = 2;
                yPosition = this.client.getWindow().getGuiScaledWidth() - this.client.font.lineHeight - 12;
            }

            case BottomRight ->
            {
                xPosition = this.client.getWindow().getGuiScaledWidth() - this.client.font.width(coordinatesOverlay) - 2;
                yPosition = this.client.getWindow().getGuiScaledWidth() - this.client.font.lineHeight - 12;
            }
        }

        this.drawString(guiGraphics, coordinatesOverlay, xPosition, yPosition);
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
