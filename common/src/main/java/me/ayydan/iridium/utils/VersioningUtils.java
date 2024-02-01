package me.ayydan.iridium.utils;

import me.ayydan.iridium.platform.IridiumPlatformUtils;
import net.minecraft.MinecraftVersion;
import org.apache.commons.lang3.StringUtils;

public class VersioningUtils
{
    private static final String CURRENT_MINECRAFT_VERSION_NAME = MinecraftVersion.GAME_VERSION.getName();
    private static final String CURRENT_IRIDIUM_VERSION = IridiumPlatformUtils.getCurrentVersion();

    public static int getMinecraftMajorVersion()
    {
        String majorVersionString = StringUtils.substringBefore(CURRENT_MINECRAFT_VERSION_NAME, ".");
        return Integer.parseInt(majorVersionString);
    }

    public static int getMinecraftMinorVersion()
    {
        // The string of the current Minecraft version without the major number. (Example: 1.19.4 -> 19.4)
        String versionNameWithoutMajor = StringUtils.substringAfter(CURRENT_MINECRAFT_VERSION_NAME, ".");

        // Then, remove everything after the dot to get the minor version. (Example: 19.4 -> 19)
        String minorVersionString = StringUtils.substringBefore(versionNameWithoutMajor, ".");

        return Integer.parseInt(minorVersionString);
    }

    public static int getMinecraftPatchVersion()
    {
        String patchVersionString = StringUtils.substringAfterLast(CURRENT_MINECRAFT_VERSION_NAME, ".");
        return Integer.parseInt(patchVersionString);
    }

    public static int getIridiumMajorVersion()
    {
        String majorVersionString = StringUtils.substringBefore(CURRENT_IRIDIUM_VERSION, ".");
        return Integer.parseInt(majorVersionString);
    }

    public static int getIridiumMinorVersion()
    {
        // The same thing we did in the function for getting Minecraft's minor version number.
        String versionNameWithoutMajor = StringUtils.substringAfter(CURRENT_IRIDIUM_VERSION, ".");
        String minorVersionString = StringUtils.substringBefore(versionNameWithoutMajor, ".");

        return Integer.parseInt(minorVersionString);
    }

    public static int getIridiumPatchVersion()
    {
        // The version string without the Git metadata stuff. (Example: 1.0.0+revision.74918ec-dirty -> 1.0.0)
        String versionWithoutGitMetadata = StringUtils.substringBefore(CURRENT_IRIDIUM_VERSION, "-");

        // We then get the patch version number from the metadata-stripped version string.
        String patchVersionString = StringUtils.substringAfterLast(versionWithoutGitMetadata, ".");

        return Integer.parseInt(patchVersionString);
    }
}
