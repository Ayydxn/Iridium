package me.ayydan.iridium.platform.neoforge;

import me.ayydan.iridium.utils.IridiumConstants;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforgespi.language.IModInfo;
import org.apache.maven.artifact.versioning.ArtifactVersion;

import java.nio.file.Path;

public class IridiumPlatformUtilsImpl
{
    public static String getCurrentVersion()
    {
        if (ModList.get() == null)
        {
            return FMLLoader.getLoadingModList().getModFileById(IridiumConstants.MOD_ID)
                    .versionString();
        }
        else
        {
            return ModList.get().getModContainerById(IridiumConstants.MOD_ID)
                    .map(ModContainer::getModInfo)
                    .map(IModInfo::getVersion)
                    .map(ArtifactVersion::toString).orElse("Unknown Iridium Version");
        }
    }

    public static Path getConfigurationDir()
    {
        return FMLPaths.CONFIGDIR.get();
    }
}
