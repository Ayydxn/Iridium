package me.ayydan.iridium.options.minecraft;

import com.google.common.collect.Lists;
import dev.isxander.yacl3.api.*;
import me.ayydan.iridium.gui.screens.IridiumOptionsScreen;
import net.minecraft.client.gui.screen.*;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IridiumExtraOptions extends IridiumMinecraftOptions
{
    private final List<Option<?>> extrasOptionsOptions = new ArrayList<>();
    private final List<Option<?>> creditsOptionsOptions = new ArrayList<>();

    private ConfigCategory extraOptionsCategory;

    public IridiumExtraOptions()
    {
        super(null);
    }

    @Override
    public void create()
    {
        this.createExtraOptions();
        this.createCreditsOptions();

        OptionGroup extraOptionsGroup = OptionGroup.createBuilder()
                .name(Text.literal("Extras"))
                .options(this.extrasOptionsOptions)
                .build();

        OptionGroup creditsOptionsGroup = OptionGroup.createBuilder()
                .name(Text.literal("Credits"))
                .options(this.creditsOptionsOptions)
                .build();

        this.extraOptionsCategory = ConfigCategory.createBuilder()
                .name(Text.translatable("iridium.options.category.extras"))
                .groups(Lists.newArrayList(extraOptionsGroup, creditsOptionsGroup))
                .build();
    }

    @Override
    public ConfigCategory getYACLCategory()
    {
        return this.extraOptionsCategory;
    }

    private void createExtraOptions()
    {
        ButtonOption telemetryDataOption = ButtonOption.createBuilder()
                .name(Text.translatable("iridium.options.extras.telemetryData"))
                .description(OptionDescription.of(Text.translatable("iridium.options.extras.telemetryData.description")))
                .text(Text.literal(""))
                .action((screen, button) -> this.client.setScreen(new TelemetryDataScreen(new IridiumOptionsScreen(null).getHandle(), this.client.options)))
                .build();

        Collections.addAll(this.extrasOptionsOptions, telemetryDataOption);
    }

    private void createCreditsOptions()
    {
        ButtonOption creditsOption = ButtonOption.createBuilder()
                .name(Text.translatable("credits_and_attribution.button.credits"))
                .description(OptionDescription.of(Text.translatable("iridium.options.credits.credits.description")))
                .text(Text.literal(""))
                .action((screen, button) -> this.client.setScreen(new CreditsScreen(false, () -> this.client.setScreen(new TitleScreen()))))
                .build();

        ButtonOption attributionOption = ButtonOption.createBuilder()
                .name(Text.translatable("credits_and_attribution.button.attribution"))
                .description(OptionDescription.of(Text.translatable("iridium.options.credits.attribution.description")))
                .text(Text.literal(""))
                .action(((yaclScreen, buttonOption) -> ConfirmLinkScreen.create(new TitleScreen(), "https://aka.ms/MinecraftJavaAttribution")))
                .build();

        ButtonOption licensesOption = ButtonOption.createBuilder()
                .name(Text.translatable("credits_and_attribution.button.licenses"))
                .description(OptionDescription.of(Text.translatable("iridium.options.credits.licenses.description")))
                .text(Text.literal(""))
                .action(((yaclScreen, buttonOption) -> ConfirmLinkScreen.create(new TitleScreen(), "https://aka.ms/MinecraftJavaLicenses")))
                .build();

        Collections.addAll(this.creditsOptionsOptions, creditsOption, attributionOption, licensesOption);
    }
}
