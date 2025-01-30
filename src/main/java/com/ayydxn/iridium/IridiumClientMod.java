package com.ayydxn.iridium;

import com.ayydxn.iridium.options.IridiumGameOptions;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

@Environment(EnvType.CLIENT)
public class IridiumClientMod implements ClientModInitializer
{
    private static IridiumClientMod INSTANCE;

    private static final Logger LOGGER = (Logger) LogManager.getLogger("Iridium");

    private IridiumGameOptions iridiumGameOptions;
    private String modVersion = "Unknown";

    @Override
    public void onInitializeClient()
    {
        INSTANCE = this;

        this.modVersion = FabricLoader.getInstance().getModContainer("iridium").orElseThrow()
                        .getMetadata().getVersion().getFriendlyString();

        LOGGER.info("Initializing Iridium... (Version: {})", this.modVersion);

        this.iridiumGameOptions = IridiumGameOptions.load();
    }

    public static IridiumClientMod getInstance()
    {
        if (INSTANCE == null)
            throw new IllegalStateException("Tried to access an instance of Iridium before one was available!");

        return INSTANCE;
    }

    public static Logger getLogger()
    {
        return LOGGER;
    }

    public IridiumGameOptions getGameOptions()
    {
        return this.iridiumGameOptions;
    }

    public String getModVersion()
    {
        return this.modVersion;
    }
}
