package me.ayydan.iridium.options.categories;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import me.ayydan.iridium.gui.screens.IridiumOptionsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.apache.commons.compress.utils.Lists;

import java.util.List;
import java.util.Objects;

public class IridiumLanguageOptionsCategory extends IridiumOptionCategory
{
    public IridiumLanguageOptionsCategory()
    {
        super(Component.translatable("options.language.title"));
    }

    @Override
    public List<Option<?>> getCategoryOptions()
    {
        List<Option<?>> languageCategoryOptions = Lists.newArrayList();
        Minecraft client = Minecraft.getInstance();

        client.getLanguageManager().getLanguages().forEach((languageCode, languageDefinition) ->
        {
            Component currentSelectedLanguage = Objects.equals(languageCode, client.getLanguageManager().getLanguage(languageCode).name()) ?
                    Component.translatable("iridium.options.language.language.selected", languageCode, languageDefinition.toComponent().getString()) :
                    Component.translatable("iridium.options.language.language", languageCode, languageDefinition.toComponent().getString());

            ButtonOption languageButtonOption = ButtonOption.createBuilder()
                    .name(currentSelectedLanguage)
                    .description(OptionDescription.EMPTY)
                    .text(Component.literal(""))
                    .action((screen, button) ->
                    {
                        client.getLanguageManager().setSelected(languageCode);
                        client.reloadResourcePacks();

                        // (Ayydxn) Hack to refresh the screen.
                        client.setScreen(new IridiumOptionsScreen(null).getHandle());
                    })
                    .build();

            languageCategoryOptions.add(languageButtonOption);
        });

        return languageCategoryOptions;
    }

    @Override
    public List<OptionGroup> getCategoryGroups()
    {
        return null;
    }
}
