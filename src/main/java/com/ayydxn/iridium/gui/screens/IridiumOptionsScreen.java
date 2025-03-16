package com.ayydxn.iridium.gui.screens;

import com.ayydxn.iridium.IridiumClientMod;
import com.ayydxn.iridium.options.IridiumGameOptions;
import com.ayydxn.iridium.options.categories.IridiumOptionCategory;
import com.ayydxn.iridium.options.categories.util.OptionCategoryRegistry;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IridiumOptionsScreen
{
    private final IridiumGameOptions iridiumGameOptions;
    private final Screen previousScreen;

    public IridiumOptionsScreen(@Nullable Screen previousScreen)
    {
        this.iridiumGameOptions = IridiumClientMod.getInstance().getGameOptions();
        this.previousScreen = previousScreen;
    }

    public Screen getHandle()
    {
        List<IridiumOptionCategory> optionCategories = OptionCategoryRegistry.getCategories()
                .stream()
                .toList();

        List<ConfigCategory> configCategories = optionCategories.stream()
                .map(IridiumOptionCategory::getYACLCategory)
                .toList();

        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("iridium.options.gui_title"))
                .categories(configCategories)
                .save(() ->
                {
                    Minecraft.getInstance().options.save();
                    this.iridiumGameOptions.write();
                })
                .build()
                .generateScreen(this.previousScreen);
    }
}