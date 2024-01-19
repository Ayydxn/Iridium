package me.ayydan.iridium.gui.screens;

import me.ayydan.iridium.options.IridiumGameOptions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.text.Text;

import java.awt.*;

public class CorruptedIridiumConfigScreen extends Screen
{
    private final MinecraftClient client;

    public CorruptedIridiumConfigScreen()
    {
        super(Text.literal("Corrupted Iridium Config"));

        this.client = MinecraftClient.getInstance();
    }

    @Override
    protected void init()
    {
        this.addDrawable(ButtonWidget.builder(Text.translatable("iridium.options.button.close_game_and_reset_corrupt_config"), button ->
        {
            IridiumGameOptions.defaults().write();
            this.client.scheduleStop();
        }).positionAndSize(this.width / 2 - 200 / 2, this.height - 40, 205, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta)
    {
        this.renderBackground(graphics, mouseX, mouseY, delta);

        MultilineText screenTitle = MultilineText.create(this.client.textRenderer, Text.translatable("iridium.texts.fatal_error"), this.width - 50);
        MultilineText errorMessage = MultilineText.create(this.client.textRenderer, Text.translatable("iridium.options.texts.corrupt_config", "\n\n"),
                this.width - 50);

        graphics.getMatrices().push();
        graphics.getMatrices().scale(1.5f, 1.5f, 1.5f);

        // (Ayydan) I'd rather render this using GuiGraphics, but that doesn't work for some reason.
        screenTitle.drawWithShadow(graphics, 17, 15, this.client.textRenderer.fontHeight * 2, Color.RED.getRGB());

        graphics.getMatrices().pop();

        errorMessage.drawWithShadow(graphics, 25, 50, this.client.textRenderer.fontHeight * 2, Color.WHITE.getRGB());

        super.render(graphics, mouseX, mouseY, delta);
    }
}
