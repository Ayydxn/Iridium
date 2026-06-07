package me.ayydxn.iridium;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

@Environment(EnvType.CLIENT)
public class IridiumClientMod implements ClientModInitializer
{
    public static final Logger LOGGER = (Logger) LogManager.getLogger("Iridium");
    public static final String MOD_ID = "iridium";

    @Override
    public void onInitializeClient()
    {
        LOGGER.info("Initializing Iridium... (Version: {})", FabricLoader.getInstance().getModContainer(MOD_ID)
                .orElseThrow().getMetadata().getVersion().getFriendlyString());
    }
}