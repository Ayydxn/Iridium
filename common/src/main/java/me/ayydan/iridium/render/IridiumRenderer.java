package me.ayydan.iridium.render;

import me.ayydan.iridium.render.vulkan.VulkanContext;
import me.ayydan.iridium.utils.logging.IridiumLogger;

public class IridiumRenderer
{
    private static IridiumRenderer INSTANCE;
    private static IridiumLogger LOGGER;

    private final VulkanContext vulkanContext;

    private IridiumRenderer()
    {
        this.vulkanContext = new VulkanContext();
        this.vulkanContext.create();
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

        this.vulkanContext.destroy();

        LOGGER = null;
        INSTANCE = null;
    }

    public static IridiumRenderer getInstance()
    {
        if (INSTANCE == null)
            throw new IllegalStateException("Tried to access an instance of Iridium's renderer when one wasn't available!");

        return INSTANCE;
    }

    public static IridiumLogger getLogger()
    {
        if (LOGGER == null)
            throw new IllegalStateException("Tried to access an instance of Iridium's renderer logger when wasn't available!");

        return LOGGER;
    }
}
