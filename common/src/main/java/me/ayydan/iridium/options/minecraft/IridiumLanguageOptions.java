package me.ayydan.iridium.options.minecraft;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class IridiumLanguageOptions extends IridiumMinecraftOptions
{
    private final List<Option<?>> languageOptions = new ArrayList<>();

    private ConfigCategory languageOptionsCategory;

    public IridiumLanguageOptions()
    {
        super(null);
    }

    @Override
    public void create()
    {
        this.createLanguageOptions();

        this.languageOptionsCategory = ConfigCategory.createBuilder()
                .name(Text.literal("Language"))
                .options(this.languageOptions)
                .build();
    }

    @Override
    public ConfigCategory getYACLCategory()
    {
        return this.languageOptionsCategory;
    }

    private void createLanguageOptions()
    {
        this.client.getLanguageManager().getAllLanguages().forEach((languageCode, languageDefinition) ->
        {
            Text currentSelectedLanguage = Objects.equals(languageCode, this.client.getLanguageManager().getLanguage()) ?
                    Text.translatable("iridium.options.language.language.selected", languageCode, languageDefinition.getDisplayText().getString()) :
                    Text.translatable("iridium.options.language.language", languageCode, languageDefinition.getDisplayText().getString());

            ButtonOption languageButtonOption = ButtonOption.createBuilder()
                    .name(currentSelectedLanguage)
                    .description(OptionDescription.EMPTY)
                    .text(Text.literal(""))
                    .action((screen, button) ->
                    {
                        this.client.getLanguageManager().setLanguage(languageCode);
                        this.client.reloadResources();
                    })
                    .build();

            Collections.addAll(this.languageOptions, languageButtonOption);
        });
    }
}
