package me.ayydan.iridium.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PathUtils
{
    public static Path getShaderCacheDirectory()
    {
        Path shaderCacheDirectory = Path.of(System.getProperty("user.home") + "/.iridium/cache/shaders");

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
}
