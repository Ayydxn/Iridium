package me.ayydxn.iridium.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PathUtils
{
    public static Path getShaderCacheDirectory()
    {
        Path shaderCacheDirectory = Path.of(System.getProperty("user.home") + "/.moonblast/cache/shaders");

        try
        {
            if (!Files.exists(shaderCacheDirectory))
                Files.createDirectories(shaderCacheDirectory);
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }

        return shaderCacheDirectory;
    }

    public static Path getConfigFilePath()
    {
        return Path.of(System.getProperty("user.home") + "/.moonblast/options.json");
    }
}
