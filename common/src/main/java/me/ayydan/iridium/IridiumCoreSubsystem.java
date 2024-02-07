package me.ayydan.iridium;

import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import me.ayydan.iridium.gui.hud.IridiumHudOverlay;
import me.ayydan.iridium.options.IridiumGameOptions;
import me.ayydan.iridium.platform.IridiumPlatformUtils;
import me.ayydan.iridium.subsystems.IridiumSubsystem;
import me.ayydan.iridium.utils.ClientFramerateTracker;
import me.ayydan.iridium.utils.logging.IridiumLogger;

public class IridiumCoreSubsystem extends IridiumSubsystem
{
    private IridiumLogger logger;

    private IridiumGameOptions iridiumGameOptions;
    private ClientFramerateTracker clientFramerateTracker;

    @Override
    public void initialize()
    {
        this.logger = new IridiumLogger("Iridium Core");
        this.logger.info("Initializing Iridium... (Version: {})", IridiumPlatformUtils.getCurrentVersion());

        this.iridiumGameOptions = IridiumGameOptions.load();
        this.clientFramerateTracker = new ClientFramerateTracker();

        ClientTickEvent.CLIENT_PRE.register(this.clientFramerateTracker::tick);
        ClientGuiEvent.RENDER_HUD.register((graphics, tickDelta) -> new IridiumHudOverlay().render(graphics));
    }

    @Override
    public void shutdown()
    {
    }

    @Override
    public boolean shouldInitializeSubsystem()
    {
        return true;
    }

    @Override
    public String getName()
    {
        return "Iridium Core";
    }

    public IridiumLogger getLogger()
    {
        return this.logger;
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
