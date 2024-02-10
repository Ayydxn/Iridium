package me.ayydan.iridium;

import dev.architectury.platform.Platform;
import me.ayydan.iridium.options.IridiumGameOptions;
import me.ayydan.iridium.subsystems.IridiumSubsystemManager;
import me.ayydan.iridium.client.ClientFramerateTracker;
import me.ayydan.iridium.utils.logging.IridiumLogger;

public class IridiumClientMod
{
    public static void initialize()
    {
        // (Ayydan) By the time this function is called in the NeoForge, the subsystem manager would've already been initialized.
        if (!Platform.isNeoForge())
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
