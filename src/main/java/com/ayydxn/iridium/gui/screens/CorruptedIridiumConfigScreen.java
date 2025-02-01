package com.ayydxn.iridium.gui.screens;

import com.ayydxn.iridium.options.IridiumGameOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.NotNull;

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
            // (Ayydxn) Set the music volume back to what it was before we muted it.
            this.client.getSoundManager().updateSourceVolume(SoundSource.MUSIC, client.options.getSoundSourceVolume(SoundSource.MUSIC));

            IridiumGameOptions.defaults().write();
            this.client.stop();
        }).bounds(this.width / 2 - 200 / 2, this.height - 40, 205, 20).build());
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta)
    {
        super.render(graphics, mouseX, mouseY, delta);

        MultiLineLabel errorMessage = MultiLineLabel.create(this.client.font, Component.translatable("iridium.options.texts.corrupt_config", "\n\n"),
                this.width - 50);

        // (Ayydxn) Mute the game's music for aura points because why not LOL.
        client.getSoundManager().updateSourceVolume(SoundSource.MUSIC, 0.0f);

        graphics.pose().pushPose();
        graphics.pose().scale(1.5f, 1.5f, 1.5f);

        graphics.drawString(this.client.font, Component.translatable("iridium.texts.fatal_error"),
                17, 15, Color.RED.getRGB());

        graphics.pose().popPose();

        errorMessage.renderLeftAligned(graphics, 25, 50, this.client.font.lineHeight * 2, Color.WHITE.getRGB());
    }

    @Override
    public boolean shouldCloseOnEsc()
    {
        return false;
    }
}
