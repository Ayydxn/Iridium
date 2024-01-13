package me.ayydan.iridium;

import me.ayydan.iridium.platform.IridiumPlatformUtils;
import me.ayydan.iridium.utils.logging.IridiumLogger;

/**
 * The common entrypoint to Iridium used by all mod loaders that it supports.
 * <br/>
 * <br/>
 * This is referred to as Iridium's "core" and is an entirely separate thing from its renderer.
 */
public class IridiumClientMod
{
    private static IridiumClientMod INSTANCE;

    private static IridiumLogger LOGGER;

    /**
     * Initializes and creates an instance of Iridium. This is separate from the renderer and that must be initialized separately.
     * <br/>
     * <br/>
     * This function won't do anything if Iridium is already initialized and an instance is available.
     */
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

    /**
     * Shuts down and destroys the currently available instance of Iridium. Just like {@link IridiumClientMod#initialize()}, this is separate from the renderer
     * and that must be shutdown separately.
     * <br/>
     * <br/>
     * This function won't do anything if there is no currently available instance of Iridium to destroy.
     */
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

    /**
     * Returns the current instance of Iridium.
     *
     * @throws IllegalStateException If called before Iridium was initialized.
     * @return The current instance of Iridium.
     */
    public static IridiumClientMod getInstance()
    {
        if (INSTANCE == null)
            throw new IllegalStateException("Tried to access an instance of Iridium when one wasn't available!");

        return INSTANCE;
    }

    /**
     * Returns the instance of Iridium's core logger.
     *
     * @throws IllegalStateException If called before Iridium's core logger was created.
     * @return The instance of Iridium's core logger.
     */
    public static IridiumLogger getLogger()
    {
        if (LOGGER == null)
            throw new IllegalStateException("Tried to access an instance of Iridium's core logger when wasn't available!");

        return LOGGER;
    }
}
