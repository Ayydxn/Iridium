package me.ayydan.iridium;

import me.ayydan.iridium.platform.IridiumPlatformUtils;
import me.ayydan.iridium.utils.logging.IridiumLogger;

public class IridiumClientMod
{
    private static IridiumClientMod INSTANCE;

    private static IridiumLogger LOGGER;

    public static void initialize()
    {
        if (INSTANCE != null)
        {
            LOGGER.warn("Iridium's core has already been initialized! You cannot initialize Iridium's core more than once!");
            return;
        }

        LOGGER = new IridiumLogger("Iridium Core");
        LOGGER.info("Initializing Iridium... (Version: {})", IridiumPlatformUtils.getCurrentVersion());

        INSTANCE = new IridiumClientMod();
    }

    public void shutdown()
    {
        if (INSTANCE == null)
        {
            LOGGER.warn("You cannot shutdown Iridium's core when there isn't an available instance!");
            return;
        }

        LOGGER.info("Shutting down Iridium...");

        LOGGER = null;
        INSTANCE = null;
    }

    public static IridiumClientMod getInstance()
    {
        if (INSTANCE == null)
            throw new IllegalStateException("Tried to access an instance of Iridium when one wasn't available!");

        return INSTANCE;
    }

    public static IridiumLogger getLogger()
    {
        if (LOGGER == null)
            throw new IllegalStateException("Tried to access an instance of Iridium's core logger when wasn't available!");

        return LOGGER;
    }
}
