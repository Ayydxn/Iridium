package me.ayydan.iridium.options.minecraft;

import dev.isxander.yacl3.api.ConfigCategory;
import me.ayydan.iridium.gui.screens.IridiumOptionsScreen;
import me.ayydan.iridium.options.IridiumGameOptions;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.Nullable;

public abstract class IridiumMinecraftOptions
{
    protected final IridiumGameOptions iridiumGameOptions;
    protected final MinecraftClient client;

    protected IridiumMinecraftOptions(@Nullable IridiumGameOptions iridiumGameOptions)
    {
        this.iridiumGameOptions = iridiumGameOptions;
        this.client = MinecraftClient.getInstance();
    }

    public abstract void create();

    public abstract ConfigCategory getYACLCategory();

    public final void refreshOptionsScreen()
    {
        this.client.setScreen(new IridiumOptionsScreen(null).getHandle());
    }
}
