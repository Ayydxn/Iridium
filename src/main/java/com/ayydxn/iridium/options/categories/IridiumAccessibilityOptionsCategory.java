package com.ayydxn.iridium.options.categories;

import com.ayydxn.iridium.options.util.OptionsUtil;
import dev.isxander.yacl3.api.Binding;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.gui.controllers.BooleanController;
import dev.isxander.yacl3.gui.controllers.cycling.EnumController;
import dev.isxander.yacl3.gui.controllers.slider.DoubleSliderController;
import dev.isxander.yacl3.gui.controllers.slider.IntegerSliderController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.NarratorStatus;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.PackRepository;

import java.util.List;

public class IridiumAccessibilityOptionsCategory extends IridiumOptionCategory
{
    public IridiumAccessibilityOptionsCategory()
    {
        super("Accessibility", Component.translatable("iridium.options.category.accessibility"));
    }

    @Override
    public List<Option<?>> getCategoryOptions()
    {
        Minecraft client = Minecraft.getInstance();
        
        Option<NarratorStatus> narrartorOption = Option.<NarratorStatus>createBuilder()
                .name(Component.translatable("options.narrator"))
                .description(OptionDescription.of(Component.translatable("iridium.options.chat.narrator.description")))
                .binding(Binding.minecraft(client.options.narrator()))
                .customController(option -> new EnumController<>(option, NarratorStatus::getName, NarratorStatus.values()))
                .build();

        Option<Boolean> narrartorHotkeyOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.accessibility.narrator_hotkey"))
                .description(OptionDescription.of(Component.translatable("iridium.options.accessibility.narratorHotkey.description")))
                .binding(Binding.minecraft(client.options.narratorHotkey())) // (Ayydan) This function currently doesn't have a proper name in Quilt mappings.
                .customController(BooleanController::new)
                .build();

        Option<Boolean> highContrastOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.accessibility.high_contrast"))
                .description(OptionDescription.of(Component.translatable("iridium.options.accessibility.highContrast.description")))
                .binding(false, () -> client.options.highContrast().get(), newValue ->
                {
                    final String highContrastPackName = "high_contrast";

                    PackRepository resourcePackManager = client.getResourcePackRepository();
                    boolean isHighContrastPackEnabled = resourcePackManager.getSelectedIds().contains(highContrastPackName);

                    if (!isHighContrastPackEnabled && newValue)
                    {
                        if (resourcePackManager.addPack("high_contrast"))
                            client.options.updateResourcePacks(resourcePackManager);
                    }

                    if (isHighContrastPackEnabled && !newValue && resourcePackManager.removePack(highContrastPackName))
                    {
                        client.options.updateResourcePacks(resourcePackManager);
                    }
                })
                .customController(BooleanController::new)
                .build();

        Option<Integer> menuBackgroundBlurOption = Option.<Integer>createBuilder()
                .name(Component.translatable("options.accessibility.menu_background_blurriness"))
                .description(OptionDescription.of(Component.translatable("iridium.options.accessibility.menuBackgroundBlur.description")))
                .binding(Binding.minecraft(client.options.menuBackgroundBlurriness()))
                .customController(option -> new IntegerSliderController(option, 0, 10, 1, value ->
                {
                    if (value == 0)
                        return Component.translatable("options.off");

                    return Component.literal(String.valueOf(value));
                }))
                .build();

        Option<Double> darknessPulsingOption = Option.<Double>createBuilder()
                .name(Component.translatable("options.darknessEffectScale"))
                .description(OptionDescription.of(Component.translatable("iridium.options.accessibility.darknessPulsing.description")))
                .binding(Binding.minecraft(client.options.darknessEffectScale()))
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
                .binding(Binding.minecraft(client.options.damageTiltStrength()))
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
                .binding(Binding.minecraft(client.options.glintSpeed()))
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
                .binding(Binding.minecraft(client.options.glintStrength()))
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
                .binding(Binding.minecraft(client.options.hideLightningFlash()))
                .customController(BooleanController::new)
                .build();

        Option<Boolean> monochromeLogoOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.darkMojangStudiosBackgroundColor"))
                .description(OptionDescription.of(Component.translatable("iridium.options.accessibility.darkMojangStudiosBackgroundColor.description")))
                .binding(Binding.minecraft(client.options.darkMojangStudiosBackground()))
                .customController(BooleanController::new)
                .build();

        Option<Double> panoramaScrollSpeedOption = Option.<Double>createBuilder()
                .name(Component.translatable("options.accessibility.panorama_speed"))
                .description(OptionDescription.of(Component.translatable("iridium.options.accessibility.panoramaScrollSpeed.description")))
                .binding(Binding.minecraft(client.options.panoramaSpeed()))
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
                .binding(Binding.minecraft(client.options.hideSplashTexts()))
                .customController(BooleanController::new)
                .build();

        return List.of(narrartorOption, narrartorHotkeyOption, highContrastOption, menuBackgroundBlurOption, darknessPulsingOption, damageTiltOption, glintSpeedOption,
                glintStrengthOption, hideLightingFlashesOption, monochromeLogoOption, panoramaScrollSpeedOption, hideSplashTextsOption);
    }

    @Override
    public List<OptionGroup> getCategoryGroups()
    {
        return null;
    }
}
