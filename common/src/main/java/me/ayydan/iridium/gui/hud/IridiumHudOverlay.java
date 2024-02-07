package me.ayydan.iridium.gui.hud;

import me.ayydan.iridium.IridiumClientMod;
import me.ayydan.iridium.options.IridiumGameOptions;
import me.ayydan.iridium.utils.ClientFramerateTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

public class IridiumHudOverlay
{
    private final MinecraftClient minecraftClient = MinecraftClient.getInstance();
    private final IridiumGameOptions iridiumGameOptions = IridiumClientMod.getGameOptions();

    public void render(GuiGraphics guiGraphics)
    {
        if (!this.minecraftClient.method_53526().chartsVisible())
        {
            if (this.iridiumGameOptions.isFPSOverlayEnabled())
                this.renderFramerateOverlay(guiGraphics);

            if (this.iridiumGameOptions.isCoordinatesOverlayEnabled())
                this.renderCoordinatesOverlay(guiGraphics);
        }
    }

    private void renderFramerateOverlay(GuiGraphics guiGraphics)
    {
        int currentClientFPS = this.minecraftClient.getCurrentFps();
        ClientFramerateTracker clientFramerateTracker = IridiumClientMod.getClientFramerateTracker();

        Text fpsOverlayText = Text.translatable("iridium.advancedGraphics.fpsOverlay", currentClientFPS, clientFramerateTracker.getAverageFPS(),
                clientFramerateTracker.getHighestFPS(), clientFramerateTracker.getLowestFPS());

        int xPosition = 0;
        int yPosition = 0;
        switch (this.iridiumGameOptions.getOverlayPosition())
        {
            case TopLeft ->
            {
                xPosition = 2;
                yPosition = 2;
            }

            case TopRight ->
            {
                xPosition = this.minecraftClient.getWindow().getScaledWidth() - this.minecraftClient.textRenderer.getWidth(fpsOverlayText) - 2;
                yPosition = 2;
            }

            case BottomLeft ->
            {
                xPosition = 2;
                yPosition = this.minecraftClient.getWindow().getScaledWidth() - this.minecraftClient.textRenderer.fontHeight - 2;
            }

            case BottomRight ->
            {
                xPosition = this.minecraftClient.getWindow().getScaledWidth() - this.minecraftClient.textRenderer.getWidth(fpsOverlayText) - 2;
                yPosition = this.minecraftClient.getWindow().getScaledWidth() - this.minecraftClient.textRenderer.fontHeight - 2;
            }
        }

        this.drawString(guiGraphics, fpsOverlayText, xPosition, yPosition);
    }

    private void renderCoordinatesOverlay(GuiGraphics guiGraphics)
    {
        if (this.minecraftClient.player == null)
            return;

        if (this.minecraftClient.hasReducedDebugInfo())
            return;

        Vec3d playerPosition = this.minecraftClient.player.getPos();

        Text coordinatesOverlay = Text.translatable("iridium.advancedGraphics.coordinatesOverlay", String.format("%.2f", playerPosition.getX()),
                String.format("%.2f", playerPosition.getY()), String.format("%.2f", playerPosition.getZ()));

        int xPosition = 0;
        int yPosition = 0;
        switch (this.iridiumGameOptions.getOverlayPosition())
        {
            case TopLeft ->
            {
                xPosition = 2;
                yPosition = 12;
            }

            case TopRight ->
            {
                xPosition = this.minecraftClient.getWindow().getScaledWidth() - this.minecraftClient.textRenderer.getWidth(coordinatesOverlay) - 2;
                yPosition = 12;
            }

            case BottomLeft ->
            {
                xPosition = 2;
                yPosition = this.minecraftClient.getWindow().getScaledWidth() - this.minecraftClient.textRenderer.fontHeight - 12;
            }

            case BottomRight ->
            {
                xPosition = this.minecraftClient.getWindow().getScaledWidth() - this.minecraftClient.textRenderer.getWidth(coordinatesOverlay) - 2;
                yPosition = this.minecraftClient.getWindow().getScaledWidth() - this.minecraftClient.textRenderer.fontHeight - 12;
            }
        }

        this.drawString(guiGraphics, coordinatesOverlay, xPosition, yPosition);
    }

    private void drawString(GuiGraphics guiGraphics, Text text, int xPosition, int yPosition)
    {
        switch (this.iridiumGameOptions.getTextContrast())
        {
            case None -> guiGraphics.drawText(this.minecraftClient.textRenderer, text, xPosition, yPosition, Color.WHITE.getRGB(), false);

            case Background ->
            {
                guiGraphics.fill(xPosition - 1, yPosition - 1, xPosition + this.minecraftClient.textRenderer.getWidth(text) + 1, yPosition + this.minecraftClient.textRenderer.fontHeight, -1873784752);
                guiGraphics.drawText(this.minecraftClient.textRenderer, text, xPosition, yPosition, Color.WHITE.getRGB(), false);
            }

            case Shadow -> guiGraphics.drawText(this.minecraftClient.textRenderer, text, xPosition, yPosition, Color.WHITE.getRGB(), true);
        }
    }
}
