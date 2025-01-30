package com.ayydxn.iridium.gui.screens;

import com.ayydxn.iridium.IridiumClientMod;
import com.ayydxn.iridium.options.IridiumGameOptions;
import com.ayydxn.iridium.options.categories.IridiumOptionCategory;
import com.google.common.collect.Lists;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

import java.io.ObjectInputFilter;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.List;

public class IridiumOptionsScreen
{
    private final IridiumGameOptions iridiumGameOptions;
    private final Screen previousScreen;

    public IridiumOptionsScreen(@Nullable  Screen previousScreen)
    {
        this.iridiumGameOptions = IridiumClientMod.getInstance().getGameOptions();
        this.previousScreen = previousScreen;
    }

    public Screen getHandle()
    {
        List<IridiumOptionCategory> iridiumOptionsCategories = Lists.newArrayList();
        List<ConfigCategory> configCategories = Lists.newArrayList();
        List<String> categoryOrder = Lists.newArrayList("Video", "Audio", "Controls", "Skin Customization", "Language", "Chat", "Accessibility",
                "Online", "Renderer", "Extras");

        for (Class<?> categoryClass : new Reflections(IridiumOptionCategory.class.getPackageName()).getSubTypesOf(IridiumOptionCategory.class))
        {
            try
            {
                IridiumOptionCategory optionCategory = (IridiumOptionCategory) categoryClass.getDeclaredConstructor().newInstance();
                iridiumOptionsCategories.add(optionCategory);
            }
            catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
            {
                throw new RuntimeException(e);
            }
        }

        // (Ayydxn) Sort the categories according the categoryOrder list above so that they appear in that order in-game.
        iridiumOptionsCategories.sort(Comparator.comparingInt(category ->
        {
            int index = categoryOrder.indexOf(category.getName());
            return index != -1 ? index : Integer.MAX_VALUE;

        }));
        iridiumOptionsCategories.forEach(iridiumOptionCategory -> configCategories.add(iridiumOptionCategory.getYACLCategory()));

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