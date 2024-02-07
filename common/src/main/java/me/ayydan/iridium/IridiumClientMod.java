package me.ayydan.iridium;

import me.ayydan.iridium.options.IridiumGameOptions;
import me.ayydan.iridium.platform.IridiumPlatformUtils;
import me.ayydan.iridium.render.IridiumRendererSubsystem;
import me.ayydan.iridium.subsystems.IridiumSubsystemManager;
import me.ayydan.iridium.utils.ClientFramerateTracker;
import me.ayydan.iridium.utils.logging.IridiumLogger;

public class IridiumClientMod
{
    public static void initialize()
    {
        IridiumSubsystemManager.initialize();
        IridiumSubsystemManager.getInstance().addSubsystem(new IridiumCoreSubsystem());
    }

    public static IridiumLogger getLogger()
    {
        return ((IridiumCoreSubsystem) IridiumSubsystemManager.getInstance().getSubsystemInstance(IridiumCoreSubsystem.class)).getLogger();
    }

    public static IridiumGameOptions getGameOptions()
    {
        return ((IridiumCoreSubsystem) IridiumSubsystemManager.getInstance().getSubsystemInstance(IridiumCoreSubsystem.class)).getGameOptions();
    }

    public static ClientFramerateTracker getClientFramerateTracker()
    {
        return ((IridiumCoreSubsystem) IridiumSubsystemManager.getInstance().getSubsystemInstance(IridiumCoreSubsystem.class)).getClientFramerateTracker();
    }
}
