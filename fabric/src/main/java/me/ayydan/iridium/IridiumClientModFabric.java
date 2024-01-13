package me.ayydan.iridium;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class IridiumClientModFabric implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        IridiumClientMod.initialize();
    }
}
