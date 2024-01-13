package me.ayydan.iridium.platform.fabric;

import me.ayydan.iridium.platform.IridiumPlatformUtils;
import me.ayydan.iridium.utils.IridiumConstants;
import net.fabricmc.loader.api.FabricLoader;

public class IridiumPlatformUtilsImpl
{
    /**
     * Fabric implementation for {@link IridiumPlatformUtils#getCurrentVersion()}
     */
    public static String getCurrentVersion()
    {
        return FabricLoader.getInstance().getModContainer(IridiumConstants.MOD_ID).orElseThrow(NullPointerException::new)
                .getMetadata().getVersion().getFriendlyString();
    }
}
