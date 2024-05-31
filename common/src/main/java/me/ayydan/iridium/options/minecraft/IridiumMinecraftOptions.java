package me.ayydan.iridium.options.minecraft;

import dev.isxander.yacl3.api.ConfigCategory;
import me.ayydan.iridium.gui.screens.IridiumOptionsScreen;
import me.ayydan.iridium.options.IridiumGameOptions;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

public abstract class IridiumMinecraftOptions
{
    protected final IridiumGameOptions iridiumGameOptions;
    protected final Minecraft client;

    protected IridiumMinecraftOptions(@Nullable IridiumGameOptions iridiumGameOptions)
    {
        this.iridiumGameOptions = iridiumGameOptions;
        this.client = Minecraft.getInstance();
    }

    public abstract void create();

    public abstract ConfigCategory getYACLCategory();

    public final void refreshOptionsScreen()
    {
        this.client.setScreen(new IridiumOptionsScreen(null).getHandle());
    }
}
