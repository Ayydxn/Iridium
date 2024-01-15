package me.ayydan.iridium.platform.fabric;

import me.ayydan.iridium.utils.IridiumConstants;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class IridiumPlatformUtilsImpl
{
    public static String getCurrentVersion()
    {
        return FabricLoader.getInstance().getModContainer(IridiumConstants.MOD_ID).orElseThrow(NullPointerException::new)
                .getMetadata().getVersion().getFriendlyString();
    }

    public static Path getConfigurationDir()
    {
        return FabricLoader.getInstance().getConfigDir();
    }
}
