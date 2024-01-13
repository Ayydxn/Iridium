package me.ayydan.iridium.platform.neoforge;

import me.ayydan.iridium.platform.IridiumPlatformUtils;
import me.ayydan.iridium.utils.IridiumConstants;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;
import org.apache.maven.artifact.versioning.ArtifactVersion;

public class IridiumPlatformUtilsImpl
{
    /**
     * NeoForge implementation for {@link IridiumPlatformUtils#getCurrentVersion()}
     */
    public static String getCurrentVersion()
    {
        return ModList.get().getModContainerById(IridiumConstants.MOD_ID)
                .map(ModContainer::getModInfo)
                .map(IModInfo::getVersion)
                .map(ArtifactVersion::toString).orElse("Unknown Iridium Version");
    }
}
