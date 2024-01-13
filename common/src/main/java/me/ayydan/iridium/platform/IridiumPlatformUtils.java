package me.ayydan.iridium.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import org.apache.commons.lang3.NotImplementedException;

public class IridiumPlatformUtils
{
    /**
     * Returns the version of Iridium that is currently being used.
     *
     * @throws NotImplementedException If called while running on a mod loader that doesn't have an implementation of this function.
     * @return The version of Iridium that is being used.
     */
    @ExpectPlatform
    public static String getCurrentVersion()
    {
        throw new NotImplementedException("IridiumPlatformUtils::getCurrentVersion is not implemented for the currently running mod loader!");
    }
}
