package me.ayydan.iridium.gui.screens;

import me.ayydan.iridium.options.IridiumGameOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.awt.*;

public class CorruptedIridiumConfigScreen extends Screen
{
    private final Minecraft client;

    public CorruptedIridiumConfigScreen()
    {
        super(Component.literal("Corrupted Iridium Config"));

        this.client = Minecraft.getInstance();
    }

    @Override
    protected void init()
    {
        this.addRenderableWidget(Button.builder(Component.translatable("iridium.options.button.close_game_and_reset_corrupt_config"), button ->
        {
            IridiumGameOptions.defaults().write();
            this.client.stop();
        }).bounds(this.width / 2 - 200 / 2, this.height - 40, 205, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta)
    {
        this.renderBackground(graphics, mouseX, mouseY, delta);

        MultiLineLabel errorMessage = MultiLineLabel.create(this.client.font, Component.translatable("iridium.options.texts.corrupt_config", "\n\n"),
                this.width - 50);

        graphics.pose().pushPose();
        graphics.pose().scale(1.5f, 1.5f, 1.5f);

        graphics.drawString(this.client.font, Component.translatable("iridium.texts.fatal_error"),
                17, 15, Color.RED.getRGB());

        graphics.pose().popPose();

        errorMessage.renderLeftAligned(graphics, 25, 50, this.client.font.lineHeight * 2, Color.WHITE.getRGB());

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc()
    {
        return false;
    }
}
