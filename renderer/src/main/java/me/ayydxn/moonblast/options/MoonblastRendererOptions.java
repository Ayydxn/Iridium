package me.ayydxn.moonblast.options;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import me.ayydxn.moonblast.utils.MoonblastConstants;
import me.ayydxn.moonblast.utils.PathUtils;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class MoonblastRendererOptions
{
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
            .create();

    public RendererOptions rendererOptions = new RendererOptions();
    public DebugOptions debugOptions = new DebugOptions();

    public static MoonblastRendererOptions defaults()
    {
        return new MoonblastRendererOptions();
    }

    public static MoonblastRendererOptions load()
    {
        if (Files.exists(PathUtils.getConfigFilePath()))
        {
            StringBuilder configFileContents = new StringBuilder();

            try
            {
                configFileContents.append(FileUtils.readFileToString(PathUtils.getConfigFilePath().toFile(), StandardCharsets.UTF_8));
            }
            catch (IOException exception)
            {
                MoonblastConstants.LOGGER.error(exception);
            }

            MoonblastRendererOptions moonblastRendererOptions = null;

            try
            {
                moonblastRendererOptions = GSON.fromJson(configFileContents.toString(), MoonblastRendererOptions.class);
            }
            catch (JsonSyntaxException exception)
            {
                MoonblastConstants.LOGGER.error(exception);
            }

            return moonblastRendererOptions;
        }
        else
        {
            MoonblastConstants.LOGGER.warn("Failed to load Moonblast's options! Loading defautls...");

            MoonblastRendererOptions defaultRendererOptions = MoonblastRendererOptions.defaults();
            defaultRendererOptions.write();

            return defaultRendererOptions;
        }
    }

    public void write()
    {
        try
        {
            FileUtils.writeStringToFile(PathUtils.getConfigFilePath().toFile(), GSON.toJson(this), StandardCharsets.UTF_8);
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
    }

    public static class RendererOptions
    {
        public boolean enableVSync = true;
        public boolean enableShaderCaching = true;
    }

    public static class DebugOptions
    {
        public boolean enableValidationLayers = true;
    }
}
