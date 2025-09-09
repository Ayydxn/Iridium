package me.ayydxn.iridium.options;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import me.ayydxn.iridium.utils.IridiumConstants;
import me.ayydxn.iridium.utils.PathUtils;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class IridiumRendererOptions
{
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
            .create();

    public RendererOptions rendererOptions = new RendererOptions();
    public DebugOptions debugOptions = new DebugOptions();

    public static IridiumRendererOptions defaults()
    {
        return new IridiumRendererOptions();
    }

    public static IridiumRendererOptions load()
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
                IridiumConstants.LOGGER.error(exception);
            }

            IridiumRendererOptions iridiumRendererOptions = null;

            try
            {
                iridiumRendererOptions = GSON.fromJson(configFileContents.toString(), IridiumRendererOptions.class);
            }
            catch (JsonSyntaxException exception)
            {
                IridiumConstants.LOGGER.error(exception);
            }

            return iridiumRendererOptions;
        }
        else
        {
            IridiumConstants.LOGGER.warn("Failed to load Moonblast's options! Loading defautls...");

            IridiumRendererOptions defaultRendererOptions = IridiumRendererOptions.defaults();
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

        public int framesInFlight = 3;
    }

    public static class DebugOptions
    {
        public boolean enableValidationLayers = true;
    }
}
