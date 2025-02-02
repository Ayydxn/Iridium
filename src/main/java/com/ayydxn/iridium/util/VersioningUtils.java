package com.ayydxn.iridium.util;

import com.ayydxn.iridium.IridiumClientMod;
import net.minecraft.DetectedVersion;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

public class VersioningUtils
{
    private static final ArtifactVersion MINECRAFT_ARTIFACT_VERSION = new DefaultArtifactVersion(DetectedVersion.BUILT_IN.getName());
    private static final ArtifactVersion IRIDIUM_ARTIFACT_VERSION = new DefaultArtifactVersion(IridiumClientMod.getInstance().getModVersion());

    public static int getMinecraftMajorVersion()
    {
        return MINECRAFT_ARTIFACT_VERSION.getMajorVersion();
    }

    public static int getMinecraftMinorVersion()
    {
        return MINECRAFT_ARTIFACT_VERSION.getMinorVersion();
    }

    public static int getMinecraftPatchVersion()
    {
        return MINECRAFT_ARTIFACT_VERSION.getIncrementalVersion();
    }

    public static int getIridiumMajorVersion()
    {
        return IRIDIUM_ARTIFACT_VERSION.getMajorVersion();
    }

    public static int getIridiumMinorVersion()
    {
        return IRIDIUM_ARTIFACT_VERSION.getMinorVersion();
    }

    public static int getIridiumPatchVersion()
    {
        return IRIDIUM_ARTIFACT_VERSION.getIncrementalVersion();
    }
}
