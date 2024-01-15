package me.ayydan.iridium.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import org.apache.commons.lang3.NotImplementedException;

import java.nio.file.Path;

public class IridiumPlatformUtils
{
    @ExpectPlatform
    public static String getCurrentVersion()
    {
        throw new NotImplementedException("IridiumPlatformUtils::getCurrentVersion is not implemented for the currently running mod loader!");
    }

    @ExpectPlatform
    public static Path getConfigurationDir()
    {
        throw new NotImplementedException("IridiumPlatformUtils::getConfigurationDir is not implemented for the currently running mod loader!");
    }
}
