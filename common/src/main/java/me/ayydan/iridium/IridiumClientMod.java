package me.ayydan.iridium;

import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.platform.Platform;
import me.ayydan.iridium.client.ClientFramerateTracker;
import me.ayydan.iridium.gui.hud.IridiumHudOverlay;
import me.ayydan.iridium.options.IridiumGameOptions;
import me.ayydan.iridium.utils.VersioningUtils;
import me.ayydan.iridium.utils.logging.IridiumLogger;

public class IridiumClientMod
{
    private static IridiumClientMod INSTANCE;
    private static IridiumLogger LOGGER;

    private final IridiumGameOptions iridiumGameOptions;
    private final ClientFramerateTracker clientFramerateTracker;

    private IridiumClientMod()
    {
        this.iridiumGameOptions = IridiumGameOptions.load();
        this.clientFramerateTracker = new ClientFramerateTracker();

        ClientTickEvent.CLIENT_PRE.register(this.clientFramerateTracker::tick);
        ClientGuiEvent.RENDER_HUD.register((graphics, tickDelta) -> new IridiumHudOverlay().render(graphics));
    }

    public static void initialize()
    {
        if (INSTANCE != null)
        {
            LOGGER.warn("Iridium's core has already been initialized! You cannot initialize Iridium's core more than once!");
            return;
        }

        LOGGER = new IridiumLogger("Iridium Core");
        LOGGER.info("Initializing Iridium... (Version: {})", VersioningUtils.getIridiumVersion());

        INSTANCE = new IridiumClientMod();
    }

    public static IridiumClientMod getInstance()
    {
        if (INSTANCE == null)
            throw new IllegalStateException("Tried to access an instance of Iridium's core when one isn't available!");

        return INSTANCE;
    }

    public static IridiumLogger getLogger()
    {
        return LOGGER;
    }

    public IridiumGameOptions getGameOptions()
    {
        return this.iridiumGameOptions;
    }

    public ClientFramerateTracker getClientFramerateTracker()
    {
        return this.clientFramerateTracker;
    }
}
