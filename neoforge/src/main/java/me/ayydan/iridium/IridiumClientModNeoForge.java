package me.ayydan.iridium;

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
