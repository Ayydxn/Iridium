package me.ayydan.iridium.options.minecraft;

import dev.isxander.yacl3.api.Binding;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.gui.controllers.BooleanController;
import dev.isxander.yacl3.gui.controllers.cycling.EnumController;
import dev.isxander.yacl3.gui.controllers.slider.DoubleSliderController;
import me.ayydan.iridium.options.util.OptionsUtil;
import net.minecraft.client.NarratorStatus;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.PackRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IridiumAccessibilitySettings extends IridiumMinecraftOptions
{
    private final List<Option<?>> accessibilityOptions = new ArrayList<>();

    private ConfigCategory accessibilityOptionsCategory;

    public IridiumAccessibilitySettings()
    {
        super(null);
    }

    @Override
    public void create()
    {
        this.createAccessibilityOptions();

        this.accessibilityOptionsCategory = ConfigCategory.createBuilder()
                .name(Component.translatable("iridium.options.category.accessibility"))
                .options(this.accessibilityOptions)
                .build();
    }

    @Override
    public ConfigCategory getYACLCategory()
    {
        return this.accessibilityOptionsCategory;
    }

    private void createAccessibilityOptions()
    {
        Option<NarratorStatus> narrartorOption = Option.<NarratorStatus>createBuilder()
                .name(Component.translatable("options.narrator"))
                .description(OptionDescription.of(Component.translatable("iridium.options.chat.narrator.description")))
                .binding(Binding.minecraft(this.client.options.narrator()))
                .customController(option -> new EnumController<>(option, NarratorStatus::getName, NarratorStatus.values()))
                .build();

        Option<Boolean> narrartorHotkeyOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.accessibility.narrator_hotkey"))
                .description(OptionDescription.of(Component.translatable("iridium.options.accessibility.narratorHotkey.description")))
                .binding(Binding.minecraft(this.client.options.narratorHotkey())) // (Ayydan) This function currently doesn't have a proper name in Quilt mappings.
                .customController(BooleanController::new)
                .build();

        Option<Boolean> highContrastOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.accessibility.high_contrast"))
                .description(OptionDescription.of(Component.translatable("iridium.options.accessibility.highContrast.description")))
                .binding(false, () -> this.client.options.highContrast().get(), newValue ->
                {
                    final String highContrastPackName = "high_contrast";

                    PackRepository resourcePackManager = this.client.getResourcePackRepository();
                    boolean isHighContrastPackEnabled = resourcePackManager.getSelectedIds().contains(highContrastPackName);

                    if (!isHighContrastPackEnabled && newValue)
                    {
                        if (resourcePackManager.addPack("high_contrast"))
                            this.client.options.updateResourcePacks(resourcePackManager);
                    }

                    if (isHighContrastPackEnabled && !newValue && resourcePackManager.removePack(highContrastPackName))
                    {
                        this.client.options.updateResourcePacks(resourcePackManager);
                    }
                })
                .customController(BooleanController::new)
                .build();

        Option<Double> darknessPulsingOption = Option.<Double>createBuilder()
                .name(Component.translatable("options.darknessEffectScale"))
                .description(OptionDescription.of(Component.translatable("iridium.options.accessibility.darknessPulsing.description")))
                .binding(Binding.minecraft(this.client.options.darknessEffectScale()))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, value ->
                {
                    if (value == 0.0d)
                        return CommonComponents.OPTION_OFF;

                    return OptionsUtil.getPercentValueText(value);
                }))
                .build();

        Option<Double> damageTiltOption = Option.<Double>createBuilder()
                .name(Component.translatable("options.damageTiltStrength"))
                .description(OptionDescription.of(Component.translatable("iridium.options.accessibility.damageTilt.description")))
                .binding(Binding.minecraft(this.client.options.damageTiltStrength()))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, value ->
                {
                    if (value == 0.0d)
                        return CommonComponents.OPTION_OFF;

                    return OptionsUtil.getPercentValueText(value);
                }))
                .build();

        Option<Double> glintSpeedOption = Option.<Double>createBuilder()
                .name(Component.translatable("options.glintSpeed"))
                .description(OptionDescription.of(Component.translatable("iridium.options.accessibility.glintSpeed.description")))
                .binding(Binding.minecraft(this.client.options.glintSpeed()))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, value ->
                {
                    if (value == 0.0d)
                        return CommonComponents.OPTION_OFF;

                    return OptionsUtil.getPercentValueText(value);
                }))
                .build();

        Option<Double> glintStrengthOption = Option.<Double>createBuilder()
                .name(Component.translatable("options.glintStrength"))
                .description(OptionDescription.of(Component.translatable("iridium.options.accessibility.glintStrength.description")))
                .binding(Binding.minecraft(this.client.options.glintStrength()))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, value ->
                {
                    if (value == 0.0d)
                        return CommonComponents.OPTION_OFF;

                    return OptionsUtil.getPercentValueText(value);
                }))
                .build();

        Option<Boolean> hideLightingFlashesOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.hideLightningFlashes"))
                .description(OptionDescription.of(Component.translatable("iridium.options.accessibility.hideLightningFlashes.description")))
                .binding(Binding.minecraft(this.client.options.hideLightningFlash()))
                .customController(BooleanController::new)
                .build();

        Option<Boolean> monochromeLogoOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.darkMojangStudiosBackgroundColor"))
                .description(OptionDescription.of(Component.translatable("iridium.options.accessibility.darkMojangStudiosBackgroundColor.description")))
                .binding(Binding.minecraft(this.client.options.darkMojangStudiosBackground()))
                .customController(BooleanController::new)
                .build();

        Option<Double> panoramaScrollSpeedOption = Option.<Double>createBuilder()
                .name(Component.translatable("options.accessibility.panorama_speed"))
                .description(OptionDescription.of(Component.translatable("iridium.options.accessibility.panoramaScrollSpeed.description")))
                .binding(Binding.minecraft(this.client.options.panoramaSpeed()))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, value ->
                {
                    if (value == 0.0d)
                        return CommonComponents.OPTION_OFF;

                    return OptionsUtil.getPercentValueText(value);
                }))
                .build();

        Option<Boolean> hideSplashTextsOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.hideSplashTexts"))
                .description(OptionDescription.of(Component.translatable("iridium.options.accessibility.hideSplashTexts.description")))
                .binding(Binding.minecraft(this.client.options.hideSplashTexts())) // (Ayydan) This function currently doesn't have a proper name in Quilt mappings.
                .customController(BooleanController::new)
                .build();

        Collections.addAll(this.accessibilityOptions, narrartorOption, narrartorHotkeyOption, highContrastOption, darknessPulsingOption, damageTiltOption,
                glintSpeedOption, glintStrengthOption, hideLightingFlashesOption, monochromeLogoOption, panoramaScrollSpeedOption, hideSplashTextsOption);
    }
}
