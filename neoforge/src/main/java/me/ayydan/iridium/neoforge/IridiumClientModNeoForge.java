package me.ayydan.iridium.neoforge;

import me.ayydan.iridium.IridiumClientMod;
import me.ayydan.iridium.render.IridiumRenderer;
import me.ayydan.iridium.utils.IridiumConstants;
import net.neoforged.fml.common.Mod;

@Mod(IridiumConstants.MOD_ID)
public class IridiumClientModNeoForge
{
    public IridiumClientModNeoForge()
    {
        IridiumClientMod.initialize();
    }
}
