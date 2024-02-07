package me.ayydan.iridium.gui.screens;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import me.ayydan.iridium.IridiumClientMod;
import me.ayydan.iridium.options.minecraft.*;
import me.ayydan.iridium.options.IridiumGameOptions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IridiumOptionsScreen
{
    private final List<ConfigCategory> optionCategories = new ArrayList<>();

    private final IridiumGameOptions iridiumGameOptions;
    private final Screen previousScreen;

    public IridiumOptionsScreen(@Nullable Screen previousScreen)
    {
        this.iridiumGameOptions = IridiumClientMod.getGameOptions();
        this.previousScreen = previousScreen;

        IridiumVideoOptions iridiumVideoOptions = new IridiumVideoOptions(this.iridiumGameOptions);
        iridiumVideoOptions.create();

        IridiumAudioOptions iridiumAudioOptions = new IridiumAudioOptions();
        iridiumAudioOptions.create();

        IridiumControlsOptions iridiumControlsOptions = new IridiumControlsOptions();
        iridiumControlsOptions.create();

        IridiumSkinOptions iridiumSkinOptions = new IridiumSkinOptions();
        iridiumSkinOptions.create();

        IridiumLanguageOptions iridiumLanguageOptions = new IridiumLanguageOptions();
        iridiumLanguageOptions.create();

        IridiumChatSettings iridiumChatSettings = new IridiumChatSettings();
        iridiumChatSettings.create();

        IridiumAccessibilitySettings iridiumAccessibilitySettings = new IridiumAccessibilitySettings();
        iridiumAccessibilitySettings.create();

        IridiumOnlineOptions iridiumOnlineOptions = new IridiumOnlineOptions();
        iridiumOnlineOptions.create();

        IridiumExtraOptions iridiumExtraOptions = new IridiumExtraOptions();
        iridiumExtraOptions.create();

        Collections.addAll(this.optionCategories, iridiumVideoOptions.getYACLCategory(), iridiumAudioOptions.getYACLCategory(),
                iridiumControlsOptions.getYACLCategory(), iridiumSkinOptions.getYACLCategory(), iridiumLanguageOptions.getYACLCategory(),
                iridiumChatSettings.getYACLCategory(), iridiumAccessibilitySettings.getYACLCategory(), iridiumOnlineOptions.getYACLCategory(),
                iridiumExtraOptions.getYACLCategory());
    }

    public Screen getHandle()
    {
        return YetAnotherConfigLib.createBuilder()
                .title(Text.translatable("iridium.options.gui_title"))
                .categories(this.optionCategories)
                .save(() ->
                {
                   MinecraftClient.getInstance().options.write();
                    this.iridiumGameOptions.write();
                })
                .build()
                .generateScreen(this.previousScreen);
    }
}
