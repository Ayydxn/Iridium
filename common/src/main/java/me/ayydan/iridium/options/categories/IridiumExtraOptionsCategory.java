package me.ayydan.iridium.options.categories;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import me.ayydan.iridium.gui.screens.IridiumOptionsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.gui.screens.telemetry.TelemetryInfoScreen;
import net.minecraft.network.chat.Component;
import org.apache.commons.compress.utils.Lists;

import java.util.Collections;
import java.util.List;

public class IridiumExtraOptionsCategory extends IridiumOptionCategory
{
    private List<Option<?>> extrasOptionsOptions;
    private List<Option<?>> creditsOptionsOptions;

    public IridiumExtraOptionsCategory()
    {
        super(Component.translatable("iridium.options.category.extras"));
    }

    @Override
    public List<Option<?>> getCategoryOptions()
    {
        return null;
    }

    @Override
    public List<OptionGroup> getCategoryGroups()
    {
        this.createExtraOptions();
        this.createCreditsOptions();

        OptionGroup extraOptionsGroup = OptionGroup.createBuilder()
                .name(Component.literal("Extras"))
                .options(this.extrasOptionsOptions)
                .build();

        OptionGroup creditsOptionsGroup = OptionGroup.createBuilder()
                .name(Component.literal("Credits"))
                .options(this.creditsOptionsOptions)
                .build();

        return List.of(extraOptionsGroup, creditsOptionsGroup);
    }

    private void createExtraOptions()
    {
        this.extrasOptionsOptions = Lists.newArrayList();

        Minecraft client = Minecraft.getInstance();

        ButtonOption telemetryDataOption = ButtonOption.createBuilder()
                .name(Component.translatable("iridium.options.extras.telemetryData"))
                .description(OptionDescription.of(Component.translatable("iridium.options.extras.telemetryData.description")))
                .text(Component.literal(""))
                .action((screen, button) -> client.setScreen(new TelemetryInfoScreen(new IridiumOptionsScreen(null).getHandle(), client.options)))
                .build();

        Collections.addAll(this.extrasOptionsOptions, telemetryDataOption);
    }

    private void createCreditsOptions()
    {
        this.creditsOptionsOptions = Lists.newArrayList();

        Minecraft client = Minecraft.getInstance();
        
        ButtonOption creditsOption = ButtonOption.createBuilder()
                .name(Component.translatable("credits_and_attribution.button.credits"))
                .description(OptionDescription.of(Component.translatable("iridium.options.credits.credits.description")))
                .text(Component.literal(""))
                .action((screen, button) -> client.setScreen(new WinScreen(false, () -> client.setScreen(new TitleScreen()))))
                .build();

        ButtonOption attributionOption = ButtonOption.createBuilder()
                .name(Component.translatable("credits_and_attribution.button.attribution"))
                .description(OptionDescription.of(Component.translatable("iridium.options.credits.attribution.description")))
                .text(Component.literal(""))
                .action(((yaclScreen, buttonOption) -> ConfirmLinkScreen.confirmLinkNow(new TitleScreen(), "https://aka.ms/MinecraftJavaAttribution")))
                .build();

        ButtonOption licensesOption = ButtonOption.createBuilder()
                .name(Component.translatable("credits_and_attribution.button.licenses"))
                .description(OptionDescription.of(Component.translatable("iridium.options.credits.licenses.description")))
                .text(Component.literal(""))
                .action(((yaclScreen, buttonOption) -> ConfirmLinkScreen.confirmLinkNow(new TitleScreen(), "https://aka.ms/MinecraftJavaLicenses")))
                .build();

        Collections.addAll(this.creditsOptionsOptions, creditsOption, attributionOption, licensesOption);
    }
}
