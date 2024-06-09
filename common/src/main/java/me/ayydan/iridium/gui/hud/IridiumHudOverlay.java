package me.ayydan.iridium.gui.hud;

import me.ayydan.iridium.IridiumClientMod;
import me.ayydan.iridium.options.IridiumGameOptions;
import me.ayydan.iridium.client.ClientFramerateTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.awt.*;

public class IridiumHudOverlay
{
    private final Minecraft minecraftClient = Minecraft.getInstance();
    private final IridiumGameOptions iridiumGameOptions = IridiumClientMod.getInstance().getGameOptions();

    public void render(GuiGraphics guiGraphics)
    {
        if (!this.minecraftClient.getDebugOverlay().showDebugScreen())
        {
            if (this.iridiumGameOptions.advancedGraphicsOptions.showFPSOverlay)
                this.renderFramerateOverlay(guiGraphics);

            if (this.iridiumGameOptions.advancedGraphicsOptions.showCoordinates)
                this.renderCoordinatesOverlay(guiGraphics);
        }
    }

    private void renderFramerateOverlay(GuiGraphics guiGraphics)
    {
        int currentClientFPS = this.minecraftClient.getFps();
        ClientFramerateTracker clientFramerateTracker = IridiumClientMod.getInstance().getClientFramerateTracker();

        Component fpsOverlayText = Component.translatable("iridium.advancedGraphics.fpsOverlay", currentClientFPS, clientFramerateTracker.getAverageFPS(),
                clientFramerateTracker.getHighestFPS(), clientFramerateTracker.getLowestFPS());

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
                xPosition = this.minecraftClient.getWindow().getGuiScaledWidth() - this.minecraftClient.font.width(fpsOverlayText) - 2;
                yPosition = 2;
            }

            case BottomLeft ->
            {
                xPosition = 2;
                yPosition = this.minecraftClient.getWindow().getGuiScaledWidth() - this.minecraftClient.font.lineHeight - 2;
            }

            case BottomRight ->
            {
                xPosition = this.minecraftClient.getWindow().getGuiScaledWidth() - this.minecraftClient.font.width(fpsOverlayText) - 2;
                yPosition = this.minecraftClient.getWindow().getGuiScaledWidth() - this.minecraftClient.font.lineHeight - 2;
            }
        }

        this.drawString(guiGraphics, fpsOverlayText, xPosition, yPosition);
    }

    private void renderCoordinatesOverlay(GuiGraphics guiGraphics)
    {
        if (this.minecraftClient.player == null)
            return;

        if (this.minecraftClient.showOnlyReducedInfo())
            return;

        Vec3 playerPosition = this.minecraftClient.player.position();

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
                xPosition = this.minecraftClient.getWindow().getGuiScaledWidth() - this.minecraftClient.font.width(coordinatesOverlay) - 2;
                yPosition = 12;
            }

            case BottomLeft ->
            {
                xPosition = 2;
                yPosition = this.minecraftClient.getWindow().getGuiScaledWidth() - this.minecraftClient.font.lineHeight - 12;
            }

            case BottomRight ->
            {
                xPosition = this.minecraftClient.getWindow().getGuiScaledWidth() - this.minecraftClient.font.width(coordinatesOverlay) - 2;
                yPosition = this.minecraftClient.getWindow().getGuiScaledWidth() - this.minecraftClient.font.lineHeight - 12;
            }
        }

        this.drawString(guiGraphics, coordinatesOverlay, xPosition, yPosition);
    }

    private void drawString(GuiGraphics guiGraphics, Component text, int xPosition, int yPosition)
    {
        switch (this.iridiumGameOptions.advancedGraphicsOptions.textContrast)
        {
            case None -> guiGraphics.drawString(this.minecraftClient.font, text, xPosition, yPosition, Color.WHITE.getRGB(), false);

            case Background ->
            {
                guiGraphics.fill(xPosition - 1, yPosition - 1, xPosition + this.minecraftClient.font.width(text) + 1, yPosition + this.minecraftClient.font.lineHeight, -1873784752);
                guiGraphics.drawString(this.minecraftClient.font, text, xPosition, yPosition, Color.WHITE.getRGB(), false);
            }

            case Shadow -> guiGraphics.drawString(this.minecraftClient.font, text, xPosition, yPosition, Color.WHITE.getRGB(), true);
        }
    }
}
