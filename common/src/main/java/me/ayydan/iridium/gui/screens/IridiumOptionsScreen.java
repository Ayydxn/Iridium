package me.ayydan.iridium.gui.screens;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import me.ayydan.iridium.IridiumClientMod;
import me.ayydan.iridium.options.IridiumGameOptions;
import me.ayydan.iridium.options.categories.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
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
        this.iridiumGameOptions = IridiumClientMod.getInstance().getGameOptions();
        this.previousScreen = previousScreen;

        IridiumVideoOptionsCategory videoOptionsCategory = new IridiumVideoOptionsCategory();
        IridiumAudioOptionsCategory audioOptionsCategory = new IridiumAudioOptionsCategory();
        IridiumControlsOptionsCategory controlsOptionsCategory = new IridiumControlsOptionsCategory();
        IridiumSkinOptionsCategory skinOptionsCategory = new IridiumSkinOptionsCategory();
        IridiumLanguageOptionsCategory languageOptionsCategory = new IridiumLanguageOptionsCategory();
        IridiumChatOptionsCategory chatOptionsCategory = new IridiumChatOptionsCategory();
        IridiumAccessibilityOptionsCategory accessibilityOptionsCategory = new IridiumAccessibilityOptionsCategory();
        IridiumOnlineOptionsCategory onlineOptionsCategory = new IridiumOnlineOptionsCategory();
        IridiumRendererOptionsCategory rendererOptionsCategory = new IridiumRendererOptionsCategory();
        IridiumExtraOptionsCategory extraOptionsCategory = new IridiumExtraOptionsCategory();

        Collections.addAll(this.optionCategories, videoOptionsCategory.getYACLCategory(), audioOptionsCategory.getYACLCategory(),
                controlsOptionsCategory.getYACLCategory(), skinOptionsCategory.getYACLCategory(), languageOptionsCategory.getYACLCategory(),
                chatOptionsCategory.getYACLCategory(), accessibilityOptionsCategory.getYACLCategory(), onlineOptionsCategory.getYACLCategory(),
                rendererOptionsCategory.getYACLCategory(), extraOptionsCategory.getYACLCategory());
    }

    public Screen getHandle()
    {
        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("iridium.options.gui_title"))
                .categories(this.optionCategories)
                .save(() ->
                {
                    Minecraft.getInstance().options.save();
                    this.iridiumGameOptions.write();
                })
                .build()
                .generateScreen(this.previousScreen);
    }
}
