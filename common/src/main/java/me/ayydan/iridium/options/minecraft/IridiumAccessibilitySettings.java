package me.ayydan.iridium.options.minecraft;

import dev.isxander.yacl3.api.Binding;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.gui.controllers.BooleanController;
import dev.isxander.yacl3.gui.controllers.cycling.EnumController;
import dev.isxander.yacl3.gui.controllers.slider.DoubleSliderController;
import me.ayydan.iridium.options.util.OptionsUtil;
import net.minecraft.client.option.NarratorMode;
import net.minecraft.resource.pack.PackManager;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;

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
                .name(Text.translatable("iridium.options.category.accessibility"))
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
        Option<NarratorMode> narrartorOption = Option.<NarratorMode>createBuilder()
                .name(Text.translatable("options.narrator"))
                .description(OptionDescription.of(Text.translatable("iridium.options.chat.narrator.description")))
                .binding(Binding.minecraft(this.client.options.getNarrator()))
                .customController(option -> new EnumController<>(option, NarratorMode::getName, NarratorMode.values()))
                .build();

        Option<Boolean> narrartorHotkeyOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("options.accessibility.narrator_hotkey"))
                .description(OptionDescription.of(Text.translatable("iridium.options.accessibility.narratorHotkey.description")))
                .binding(Binding.minecraft(this.client.options.method_53530())) // (Ayydan) This function currently doesn't have a proper name in Quilt mappings.
                .customController(BooleanController::new)
                .build();

        Option<Boolean> highContrastOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("options.accessibility.high_contrast"))
                .description(OptionDescription.of(Text.translatable("iridium.options.accessibility.highContrast.description")))
                .binding(false, () -> this.client.options.getHighContrast().get(), newValue ->
                {
                    final String highContrastPackName = "high_contrast";

                    PackManager resourcePackManager = this.client.getResourcePackManager();
                    boolean isHighContrastPackEnabled = resourcePackManager.getEnabledNames().contains(highContrastPackName);

                    if (!isHighContrastPackEnabled && newValue)
                    {
                        if (resourcePackManager.enablePackProfile("high_contrast"))
                            this.client.options.updateResourcePacks(resourcePackManager);
                    }

                    if (isHighContrastPackEnabled && !newValue && resourcePackManager.disablePackProfile(highContrastPackName))
                    {
                        this.client.options.updateResourcePacks(resourcePackManager);
                    }
                })
                .customController(BooleanController::new)
                .build();

        Option<Double> darknessPulsingOption = Option.<Double>createBuilder()
                .name(Text.translatable("options.darknessEffectScale"))
                .description(OptionDescription.of(Text.translatable("iridium.options.accessibility.darknessPulsing.description")))
                .binding(Binding.minecraft(this.client.options.getDarknessEffectScale()))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, value ->
                {
                    if (value == 0.0d)
                        return CommonTexts.OFF;

                    return OptionsUtil.getPercentValueText(value);
                }))
                .build();

        Option<Double> damageTiltOption = Option.<Double>createBuilder()
                .name(Text.translatable("options.damageTiltStrength"))
                .description(OptionDescription.of(Text.translatable("iridium.options.accessibility.damageTilt.description")))
                .binding(Binding.minecraft(this.client.options.getDamageTilt()))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, value ->
                {
                    if (value == 0.0d)
                        return CommonTexts.OFF;

                    return OptionsUtil.getPercentValueText(value);
                }))
                .build();

        Option<Double> glintSpeedOption = Option.<Double>createBuilder()
                .name(Text.translatable("options.glintSpeed"))
                .description(OptionDescription.of(Text.translatable("iridium.options.accessibility.glintSpeed.description")))
                .binding(Binding.minecraft(this.client.options.getGlintSpeed()))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, value ->
                {
                    if (value == 0.0d)
                        return CommonTexts.OFF;

                    return OptionsUtil.getPercentValueText(value);
                }))
                .build();

        Option<Double> glintStrengthOption = Option.<Double>createBuilder()
                .name(Text.translatable("options.glintStrength"))
                .description(OptionDescription.of(Text.translatable("iridium.options.accessibility.glintStrength.description")))
                .binding(Binding.minecraft(this.client.options.getGlintStrength()))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, value ->
                {
                    if (value == 0.0d)
                        return CommonTexts.OFF;

                    return OptionsUtil.getPercentValueText(value);
                }))
                .build();

        Option<Boolean> hideLightingFlashesOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("options.hideLightningFlashes"))
                .description(OptionDescription.of(Text.translatable("iridium.options.accessibility.hideLightningFlashes.description")))
                .binding(Binding.minecraft(this.client.options.getHideLightningFlashes()))
                .customController(BooleanController::new)
                .build();

        Option<Boolean> monochromeLogoOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("options.darkMojangStudiosBackgroundColor"))
                .description(OptionDescription.of(Text.translatable("iridium.options.accessibility.darkMojangStudiosBackgroundColor.description")))
                .binding(Binding.minecraft(this.client.options.getMonochromeLogo()))
                .customController(BooleanController::new)
                .build();

        Option<Double> panoramaScrollSpeedOption = Option.<Double>createBuilder()
                .name(Text.translatable("options.accessibility.panorama_speed"))
                .description(OptionDescription.of(Text.translatable("iridium.options.accessibility.panoramaScrollSpeed.description")))
                .binding(Binding.minecraft(this.client.options.getPanoramaScrollSpeed()))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, value ->
                {
                    if (value == 0.0d)
                        return CommonTexts.OFF;

                    return OptionsUtil.getPercentValueText(value);
                }))
                .build();

        Option<Boolean> hideSplashTextsOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("options.hideSplashTexts"))
                .description(OptionDescription.of(Text.translatable("iridium.options.accessibility.hideSplashTexts.description")))
                .binding(Binding.minecraft(this.client.options.method_54581())) // (Ayydan) This function currently doesn't have a proper name in Quilt mappings.
                .customController(BooleanController::new)
                .build();

        Collections.addAll(this.accessibilityOptions, narrartorOption, narrartorHotkeyOption, highContrastOption, darknessPulsingOption, damageTiltOption,
                glintSpeedOption, glintStrengthOption, hideLightingFlashesOption, monochromeLogoOption, panoramaScrollSpeedOption, hideSplashTextsOption);
    }
}
