package me.ayydan.iridium.render;

import me.ayydan.iridium.IridiumClientMod;
import me.ayydan.iridium.utils.logging.IridiumLogger;

public class IridiumRenderer
{
    private static IridiumRenderer INSTANCE;
    private static IridiumLogger LOGGER;

    private IridiumRenderer()
    {

    }

    public static void initialize()
    {
        if (INSTANCE != null)
        {
            LOGGER.warn("Iridium's renderer has already been initialized! You cannot initialize Iridium's renderer more than once!");
            return;
        }

        LOGGER = new IridiumLogger("Iridium Renderer");
        LOGGER.info("Initializing Iridium Renderer..");

        INSTANCE = new IridiumRenderer();
    }

    public void shutdown()
    {
        if (INSTANCE == null)
        {
            LOGGER.warn("You cannot shutdown Iridium's renderer when there isn't an available instance!");
            return;
        }

        LOGGER.info("Shutting down Iridium Renderer...");

        LOGGER = null;
        INSTANCE = null;
    }

    public static IridiumRenderer getInstance()
    {
        if (INSTANCE == null)
            throw new IllegalStateException("Tried to access an instance of Iridium's renderer when one wasn't available!");

        return INSTANCE;
    }
}
