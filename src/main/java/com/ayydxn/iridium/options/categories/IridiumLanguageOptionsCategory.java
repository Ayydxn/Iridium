package com.ayydxn.iridium.options.categories;

import com.ayydxn.iridium.gui.screens.IridiumOptionsScreen;
import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.network.chat.Component;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class IridiumLanguageOptionsCategory extends IridiumOptionCategory
{
    public IridiumLanguageOptionsCategory()
    {
        super("Language", Component.translatable("options.language.title"));
    }

    @Override
    public @NotNull List<Option<?>> getCategoryOptions()
    {
        Minecraft client = Minecraft.getInstance();

        List<Option<?>> languageCategoryOptions = Lists.newArrayList();
        languageCategoryOptions.add(ButtonOption.createBuilder()
                .name(Component.translatable("iridium.options.language.customizeLanguage"))
                .description(OptionDescription.of(Component.translatable("iridium.options.language.customizeLanguage.description")))
                .text(Component.literal(""))
                .action((screen, button) -> client.setScreen(new LanguageSelectScreen(new IridiumOptionsScreen(null).getHandle(), client.options,
                        client.getLanguageManager())))
                .build());

        return languageCategoryOptions;
    }

    @Override
    public @NotNull List<OptionGroup> getCategoryGroups()
    {
        return Lists.newArrayList();
    }
}
