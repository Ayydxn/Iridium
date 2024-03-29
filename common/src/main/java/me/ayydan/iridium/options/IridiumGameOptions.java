package me.ayydan.iridium.options;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import me.ayydan.iridium.IridiumClientMod;
import me.ayydan.iridium.platform.IridiumPlatformUtils;
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
    private static final Path CONFIG_FILE = IridiumPlatformUtils.getConfigurationDir().resolve("iridium-settings.json");

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
            .setExclusionStrategies(new GameOptionExclusionStrategy())
            .create();

    private static boolean isConfigCorrupted = false;

    @GameOption
    public GraphicsQuality leavesQuality = GraphicsQuality.High;

    @GameOption
    public GraphicsQuality weatherQuality = GraphicsQuality.High;

    @GameOption
    public boolean enableVignette = true;

    @GameOption
    public OverlayPosition overlayPosition = OverlayPosition.TopLeft;

    @GameOption
    public TextContrast textContrast = TextContrast.Shadow;

    @GameOption
    public boolean showFPSOverlay = false;

    @GameOption
    public boolean showCoordinates = false;

    @GameOption
    public boolean enableShaderCaching = true;

    @GameOption
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
            if (!field.isAnnotationPresent(GameOption.class))
                continue;

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
