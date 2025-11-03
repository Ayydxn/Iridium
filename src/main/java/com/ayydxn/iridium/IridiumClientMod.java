package com.ayydxn.iridium;

import com.ayydxn.iridium.hud.IridiumHudOverlay;
import com.ayydxn.iridium.options.IridiumGameOptions;
import com.ayydxn.iridium.options.categories.*;
import com.ayydxn.iridium.options.categories.util.OptionCategoryRegistry;
import com.ayydxn.iridium.util.ClientFramerateTracker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

@Environment(EnvType.CLIENT)
public class IridiumClientMod implements ClientModInitializer
{
    private static IridiumClientMod INSTANCE;

    private static final Logger LOGGER = (Logger) LogManager.getLogger("Iridium");
    private static final String MOD_ID = "iridium";

    private IridiumGameOptions iridiumGameOptions;
    private String modVersion = "Unknown";

    @Override
    public void onInitializeClient()
    {
        INSTANCE = this;

        this.modVersion = FabricLoader.getInstance().getModContainer(IridiumClientMod.MOD_ID).orElseThrow()
                        .getMetadata().getVersion().getFriendlyString();

        LOGGER.info("Initializing Iridium... (Version: {})", this.modVersion);

        this.iridiumGameOptions = IridiumGameOptions.load();

        IridiumHudOverlay iridiumHudOverlay = new IridiumHudOverlay();

        OptionCategoryRegistry.register("video", IridiumVideoOptionsCategory::new, 1);
        OptionCategoryRegistry.register("audio", IridiumAudioOptionsCategory::new, 2);
        OptionCategoryRegistry.register("controls", IridiumControlsOptionsCategory::new, 3);
        OptionCategoryRegistry.register("skin", IridiumSkinOptionsCategory::new, 4);
        OptionCategoryRegistry.register("language", IridiumLanguageOptionsCategory::new, 5);
        OptionCategoryRegistry.register("chat", IridiumChatOptionsCategory::new, 6);
        OptionCategoryRegistry.register("online", IridiumOnlineOptionsCategory::new, 7);
        OptionCategoryRegistry.register("extras", IridiumExtraOptionsCategory::new, 8);

        /* -- Event Registration -- */
        WorldRenderEvents.START.register(ClientFramerateTracker.getInstance());

        ClientTickEvents.START_CLIENT_TICK.register(iridiumHudOverlay);

        HudLayerRegistrationCallback.EVENT.register(iridiumHudOverlay);
    }

    public static IridiumClientMod getInstance()
    {
        if (INSTANCE == null)
            throw new IllegalStateException("Tried to access an instance of Iridium before one was available!");

        return INSTANCE;
    }

    public static Logger getLogger()
    {
        return LOGGER;
    }

    public static ResourceLocation of(String path)
    {
        return ResourceLocation.fromNamespaceAndPath(IridiumClientMod.MOD_ID, path);
    }

    public IridiumGameOptions getGameOptions()
    {
        return this.iridiumGameOptions;
    }

    public String getModVersion()
    {
        return this.modVersion;
    }
}
