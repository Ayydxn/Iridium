package me.ayydxn.iridium.utils;

import me.ayydxn.iridium.IridiumRenderer;
import net.minecraft.DetectedVersion;

public class VersioningUtils
{
    private static final SemanticVersion MINECRAFT_ARTIFACT_VERSION = SemanticVersion.parse(DetectedVersion.BUILT_IN.getName());
    private static final SemanticVersion IRIDIUM_ARTIFACT_VERSION = SemanticVersion.parse(IridiumRenderer.getVersion());

    public static int getMinecraftMajorVersion()
    {
        return MINECRAFT_ARTIFACT_VERSION.majorVersion;
    }

    public static int getMinecraftMinorVersion()
    {
        return MINECRAFT_ARTIFACT_VERSION.minorVersion;
    }

    public static int getMinecraftPatchVersion()
    {
        return MINECRAFT_ARTIFACT_VERSION.patchVersion;
    }

    public static int getIridiumMajorVersion()
    {
        return IRIDIUM_ARTIFACT_VERSION.majorVersion;
    }

    public static int getIridiumMinorVersion()
    {
        return IRIDIUM_ARTIFACT_VERSION.minorVersion;
    }

    public static int getIridiumPatchVersion()
    {
        return IRIDIUM_ARTIFACT_VERSION.patchVersion;
    }

    private static class SemanticVersion
    {
        private final int majorVersion;
        private final int minorVersion;
        private final int patchVersion;

        private SemanticVersion(int majorVersion, int minorVersion, int patchVersion)
        {
            this.majorVersion = majorVersion;
            this.minorVersion = minorVersion;
            this.patchVersion = patchVersion;
        }

        /**
         * Parses a version string like "1.20.1" into major, minor, patch.
         * Missing parts default to 0, and non-numeric parts are ignored.
         */
        static SemanticVersion parse(String versionString)
        {
            String[] parts = versionString.split("\\.");
            int major = parts.length > 0 ? parseIntSafe(parts[0]) : 0;
            int minor = parts.length > 1 ? parseIntSafe(parts[1]) : 0;
            int patch = parts.length > 2 ? parseIntSafe(parts[2]) : 0;

            return new SemanticVersion(major, minor, patch);
        }

        private static int parseIntSafe(String str)
        {
            try
            {
                return Integer.parseInt(str.replaceAll("[^0-9]", ""));
            }
            catch (NumberFormatException e)
            {
                return 0;
            }
        }
    }
}
