package me.ayydan.iridium.neoforge;

import me.ayydan.iridium.IridiumCoreSubsystem;
import me.ayydan.iridium.subsystems.IridiumSubsystemManager;
import me.ayydan.iridium.utils.IridiumConstants;
import net.neoforged.fml.common.Mod;

@Mod(IridiumConstants.MOD_ID)
public class IridiumClientModNeoForge
{
    public IridiumClientModNeoForge()
    {
        IridiumSubsystemManager.getInstance().addSubsystem(new IridiumCoreSubsystem());
    }
}
