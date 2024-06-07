package me.ayydan.iridium.options;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import dev.architectury.platform.Platform;
import me.ayydan.iridium.IridiumClientMod;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class IridiumGameOptions
{
    private static final Path CONFIG_FILE = Platform.getConfigFolder().resolve("iridium-settings.json");

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
            .create();

    private static boolean isConfigCorrupted = false;

    public GraphicsQuality leavesQuality = GraphicsQuality.High;
    public GraphicsQuality weatherQuality = GraphicsQuality.High;
    public boolean enableVignette = true;
    public OverlayPosition overlayPosition = OverlayPosition.TopLeft;
    public TextContrast textContrast = TextContrast.Shadow;
    public boolean showFPSOverlay = false;
    public boolean showCoordinates = false;
    public boolean enableShaderCaching = !Platform.isDevelopmentEnvironment();
    public int framesInFlight = 3;

    public static IridiumGameOptions defaults()
    {
        return new IridiumGameOptions();
    }

    public static IridiumGameOptions load()
    {
        if (Files.exists(CONFIG_FILE))
        {
            StringBuilder configFileContents = new StringBuilder();

            try
            {
                configFileContents.append(FileUtils.readFileToString(CONFIG_FILE.toFile(), StandardCharsets.UTF_8));
            }
            catch (IOException exception)
            {
                exception.printStackTrace();
            }

            IridiumGameOptions iridiumGameOptions = null;

            try
            {
                iridiumGameOptions = GSON.fromJson(configFileContents.toString(), IridiumGameOptions.class);
                isConfigCorrupted = isConfigCorrupted(iridiumGameOptions);// If an option within the config file is set to an invalid value.
            }
            catch (JsonSyntaxException exception) // If the config file is corrupted on disk.
            {
                isConfigCorrupted = true;

                exception.printStackTrace();
            }

            return iridiumGameOptions;
        }
        else
        {
            IridiumClientMod.getLogger().warn("Failed to load Iridium's options! Loading defaults...");

            IridiumGameOptions defaultIridiumGameOptions = IridiumGameOptions.defaults();
            defaultIridiumGameOptions.write();

            return defaultIridiumGameOptions;
        }
    }

    public void write()
    {
        try
        {
            FileUtils.writeStringToFile(CONFIG_FILE.toFile(), GSON.toJson(this), StandardCharsets.UTF_8);
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
    }

    private static boolean isConfigCorrupted(IridiumGameOptions iridiumGameOptions)
    {
        for (Field field : iridiumGameOptions.getClass().getDeclaredFields())
        {
            if (Modifier.isPrivate(field.getModifiers()))
                field.setAccessible(true);

            try
            {
                Object fieldValue = field.get(iridiumGameOptions);

                if (Objects.isNull(fieldValue))
                    return true;
            }
            catch (IllegalAccessException exception)
            {
                exception.printStackTrace();
            }
        }

        return false;
    }

    public static boolean isConfigCorrupted()
    {
        return isConfigCorrupted;
    }

    public enum GraphicsQuality
    {
        Low,
        Medium,
        High;

        public boolean isMediumOrBetter()
        {
            return this == Medium || this == High;
        }
    }

    public enum OverlayPosition
    {
        TopLeft,
        TopRight,
        BottomLeft,
        BottomRight;

        public static OverlayPosition[] toArray()
        {
            return new OverlayPosition[]{ TopLeft, TopRight, BottomLeft, BottomRight };
        }
    }

    public enum TextContrast
    {
        None,
        Background,
        Shadow
    }
}
