package com.ayydxn.iridium;

import com.ayydxn.iridium.gui.screens.IridiumOptionsScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screens.Screen;

public class IridiumModMenuEntrypoint implements ModMenuApi
{
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory()
    {
        return (ConfigScreenFactory<Screen>) screen -> new IridiumOptionsScreen(null).getHandle();
    }
}
