package me.ayydan.iridium.options;

import com.mojang.blaze3d.glfw.VideoMode;
import com.mojang.blaze3d.glfw.Window;
import com.mojang.blaze3d.glfw.monitor.Monitor;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.gui.controllers.BooleanController;
import dev.isxander.yacl3.gui.controllers.cycling.EnumController;
import dev.isxander.yacl3.gui.controllers.slider.DoubleSliderController;
import dev.isxander.yacl3.gui.controllers.slider.IntegerSliderController;
import me.ayydan.iridium.IridiumClientMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class IridiumOptionsScreen
{
    private final List<Option<?>> generalOptions = new ArrayList<>();
    private final List<Option<?>> qualityOptions = new ArrayList<>();
    private final List<Option<?>> extraOptions = new ArrayList<>();
    private final List<ConfigCategory> optionCategories = new ArrayList<>();

    private OptionGroup generalOptionsGroup;
    private OptionGroup qualityOptionsGroup;
    private OptionGroup extraOptionsGroup;

    private final IridiumGameOptions iridiumGameOptions;
    private final MinecraftClient client;
    private final Screen parentScreen;

    public IridiumOptionsScreen(Screen parentScreen)
    {
        this.iridiumGameOptions = IridiumClientMod.getInstance().getGameOptions();
        this.client = MinecraftClient.getInstance();
        this.parentScreen = parentScreen;

        this.createOptions();
        this.createGroups();
        this.createCategories();
    }

    public Screen getScreen()
    {
        return YetAnotherConfigLib.createBuilder()
                .title(Text.translatable("iridium.options.gui_title"))
                .categories(this.optionCategories)
                .save(() ->
                {
                    this.client.options.write();
                    this.iridiumGameOptions.write();

                    // TODO: (Ayydan) Remove this when YACL has fixed the issue with the "Save Changes" button not working properly.
                    //  Refer to this GitHub issue on its repo for more details: https://github.com/isXander/YetAnotherConfigLib/issues/142
                    this.client.setScreen(this.parentScreen);
                })
                .build()
                .generateScreen(this.parentScreen);
    }

    private void createOptions()
    {
        this.createGeneralOptions();
        this.createQualityOptions();
        this.createExtraOptions();
    }

    private void createGroups()
    {
        this.generalOptionsGroup = OptionGroup.createBuilder()
                .name(Text.translatable("iridium.options.group.general"))
                .options(this.generalOptions)
                .build();

        this.qualityOptionsGroup = OptionGroup.createBuilder()
                .name(Text.translatable("iridium.options.group.quality"))
                .options(this.qualityOptions)
                .build();

        this.extraOptionsGroup = OptionGroup.createBuilder()
                .name(Text.translatable("iridium.options.group.extras"))
                .options(this.extraOptions)
                .build();
    }

    private void createCategories()
    {
        ConfigCategory generalOptionCategory = ConfigCategory.createBuilder()
                .name(Text.translatable("iridium.options.category.general"))
                .group(this.generalOptionsGroup)
                .build();

        ConfigCategory qualityOptionCategory = ConfigCategory.createBuilder()
                .name(Text.translatable("iridium.options.category.quality"))
                .group(this.qualityOptionsGroup)
                .build();

        ConfigCategory extraOptionCategory = ConfigCategory.createBuilder()
                .name(Text.translatable("iridium.options.category.extras"))
                .group(this.extraOptionsGroup)
                .build();

        Collections.addAll(this.optionCategories, generalOptionCategory, qualityOptionCategory, extraOptionCategory);
    }

    private void createGeneralOptions()
    {
        Window window = this.client.getWindow();
        int videoModeCount = window.getMonitor() != null ? window.getMonitor().getVideoModeCount() : 0;

        Option<Integer> renderDistanceOption = Option.<Integer>createBuilder()
                .name(Text.translatable("options.renderDistance"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.general.renderDistance.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.High.getText()))
                        .build())
                .binding(Binding.minecraft(this.client.options.getViewDistance()))
                .customController(option -> new IntegerSliderController(option, 2, 32, 1))
                .build();

        Option<Integer> simulationDistanceOption = Option.<Integer>createBuilder()
                .name(Text.translatable("options.simulationDistance"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.general.simulationDistance.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.High.getText()))
                        .build())
                .binding(Binding.minecraft(this.client.options.getSimulationDistance()))
                .customController(option -> new IntegerSliderController(option, 5, 32, 1))
                .build();

        Option<Double> brightnessOption = Option.<Double>createBuilder()
                .name(Text.translatable("options.gamma"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.general.brightness.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.None.getText()))
                        .build())
                .binding(50.0d, () -> this.client.options.getGamma().get() / 0.01d, newValue -> this.client.options.getGamma().set(newValue * 0.01d))
                .customController(option -> new DoubleSliderController(option, 0, 100, 1, value ->
                        value == 100.0d ? Text.translatable("options.gamma.max") : Text.of(Double.toString(value))))
                .build();

        Option<Integer> fullscreenResolutionOption = Option.<Integer>createBuilder()
                .name(Text.translatable("options.fullscreen.resolution"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.general.fullscreenResolution.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.High.getText()))
                        .build())
                .binding(0, () ->
                {
                    if (window.getMonitor() == null)
                    {
                        return 0;
                    }
                    else
                    {
                        Optional<VideoMode> videoMode = window.getVideoMode();
                        return videoMode.map((vidMode) -> window.getMonitor().findClosestVideoModeIndex(vidMode) + 1).orElse(0);
                    }
                }, (newValue) ->
                {
                    if (window.getMonitor() != null)
                    {
                        if (newValue == 0)
                        {
                            window.setVideoMode(Optional.empty());
                        }
                        else
                        {
                            window.setVideoMode(Optional.of(window.getMonitor().getVideoMode(newValue - 1)));
                        }
                    }

                    window.applyVideoMode();
                })
                .customController(option -> new IntegerSliderController(option, 0, videoModeCount, 1, value ->
                {
                    Monitor monitor = window.getMonitor();

                    if (monitor == null)
                    {
                        return Text.translatable("options.fullscreen.unavailable");
                    }
                    else
                    {
                        if (value == 0)
                        {
                            return Text.translatable("options.fullscreen.current");
                        }
                        else
                        {
                            VideoMode videoMode = monitor.getVideoMode(value - 1);
                            int width = videoMode.getWidth();
                            int height = videoMode.getHeight();
                            int refreshRate = videoMode.getRefreshRate();
                            int colorDepth = videoMode.getRedBits() + videoMode.getGreenBits() + videoMode.getBlueBits();

                            return Text.literal(String.format("%d x %d (%dhz, %d bits)", width, height, refreshRate, colorDepth));
                        }
                    }
                }))
                .build();

        Option<Integer> guiScaleOption = Option.<Integer>createBuilder()
                .name(Text.translatable("options.guiScale"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.general.guiScale.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.None.getText()))
                        .build())
                .binding(0, () -> this.client.options.getGuiScale().get(), (newValue) ->
                {
                    this.client.options.getGuiScale().set(newValue);
                    this.client.onResolutionChanged();
                })
                .customController(option -> new IntegerSliderController(option, 0, MinecraftClient.getInstance().getWindow().calculateScaleFactor(0, MinecraftClient.getInstance().forcesUnicodeFont()), 1, value ->
                        value == 0 ? Text.translatable("options.guiScale.auto") : Text.of(value + "x")))
                .build();

        Option<Boolean> fullscreenOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("options.fullscreen"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.general.fullscreen.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.None.getText()))
                        .build())
                .binding(false, () -> this.client.options.getFullscreen().get(), (newValue) ->
                {
                    this.client.options.getFullscreen().set(newValue);

                    if (window.isFullscreen() != this.client.options.getFullscreen().get())
                    {
                        window.toggleFullscreen();

                        // The client might not be able to enter fullscreen, so we do this just in-case that happens.
                        this.client.options.getFullscreen().set(window.isFullscreen());
                    }
                })
                .customController(BooleanController::new)
                .build();

        Option<Boolean> vSyncOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("options.vsync"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.general.vSync.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Varies.getText()))
                        .build())
                .binding(true, () -> this.client.options.getEnableVsync().get(), newValue ->
                {
                    this.client.options.getEnableVsync().set(newValue);
                })
                .customController(BooleanController::new)
                .build();

        Option<Integer> maxFramerateOption = Option.<Integer>createBuilder()
                .name(Text.translatable("options.framerateLimit"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.general.maxFramerate.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.None.getText()))
                        .build())
                .binding(120, () -> this.client.options.getMaxFps().get(), newValue ->
                {
                    this.client.options.getMaxFps().set(newValue);
                    this.client.getWindow().setFramerateLimit(newValue);
                })
                .customController(option -> new IntegerSliderController(option, 10, 260, 10, value ->
                        value == 260 ? Text.translatable("options.framerateLimit.max") : Text.of(Integer.toString(value))))
                .build();

        Option<Boolean> viewBobbingOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("options.viewBobbing"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.general.viewBobbing.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Varies.getText()))
                        .build())
                .binding(Binding.minecraft(this.client.options.getBobView()))
                .customController(BooleanController::new)
                .build();

        Option<AttackIndicator> attackIndicatorOption = Option.<AttackIndicator>createBuilder()
                .name(Text.translatable("options.attackIndicator"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.general.attackIndicator.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.None.getText()))
                        .build())
                .binding(Binding.minecraft(this.client.options.getAttackIndicator()))
                .customController(option -> new EnumController<>(option, AttackIndicator.class))
                .build();

        Collections.addAll(this.generalOptions, renderDistanceOption, simulationDistanceOption, brightnessOption, fullscreenResolutionOption,
                guiScaleOption, fullscreenOption, vSyncOption, maxFramerateOption, viewBobbingOption, attackIndicatorOption);
    }

    private void createQualityOptions()
    {
        Option<GraphicsMode> graphicsModeOption = Option.<GraphicsMode>createBuilder()
                .name(Text.translatable("iridium.options.quality.graphicsMode"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.quality.graphicsMode.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.High.getText()))
                        .build())
                .binding(Binding.minecraft(this.client.options.getGraphicsMode()))
                .customController(option -> new EnumController<>(option, GraphicsMode.class))
                .flag(OptionFlag.RELOAD_CHUNKS)
                .build();

        Option<CloudRenderMode> cloudsOption = Option.<CloudRenderMode>createBuilder()
                .name(Text.translatable("iridium.options.quality.clouds"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.quality.clouds.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Low.getText()))
                        .build())
                .binding(Binding.minecraft(this.client.options.getRenderClouds()))
                .customController(option -> new EnumController<>(option, CloudRenderMode.class))
                .build();

        Option<IridiumGameOptions.GraphicsQuality> weatherQualityOption = Option.<IridiumGameOptions.GraphicsQuality>createBuilder()
                .name(Text.translatable("iridium.options.quality.weather"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.quality.weather.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Medium.getText()))
                        .build())
                .binding(IridiumGameOptions.defaults().getWeatherQuality(), this.iridiumGameOptions::getWeatherQuality, this.iridiumGameOptions::setWeatherQuality)
                .customController(option -> new EnumController<>(option, IridiumGameOptions.GraphicsQuality.class))
                .flag(OptionFlag.RELOAD_CHUNKS)
                .build();

        Option<IridiumGameOptions.GraphicsQuality> leavesQualityOption = Option.<IridiumGameOptions.GraphicsQuality>createBuilder()
                .name(Text.translatable("iridium.options.quality.leaves"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.quality.leaves.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Low.getText()))
                        .build())
                .binding(IridiumGameOptions.defaults().getLeavesQuality(), this.iridiumGameOptions::getLeavesQuality, this.iridiumGameOptions::setLeavesQuality)
                .customController(option -> new EnumController<>(option, IridiumGameOptions.GraphicsQuality.class))
                .flag(OptionFlag.RELOAD_CHUNKS)
                .build();

        Option<ParticlesMode> particlesOption = Option.<ParticlesMode>createBuilder()
                .name(Text.translatable("options.particles"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.quality.particles.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Low.getText()))
                        .build())
                .binding(Binding.minecraft(this.client.options.getParticles()))
                .customController(option -> new EnumController<>(option, ParticlesMode.class))
                .build();

        Option<Boolean> smoothLightingOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("options.ao"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.quality.smoothLighting.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Low.getText()))
                        .build())
                .binding(Binding.minecraft(this.client.options.getSmoothLighting()))
                .customController(BooleanController::new)
                .build();

        Option<Integer> biomeBlendOption = Option.<Integer>createBuilder()
                .name(Text.translatable("iridium.options.quality.biomeBlend"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.quality.biomeBlend.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Low.getText()))
                        .build())
                .binding(Binding.minecraft(this.client.options.getBiomeBlendRadius()))
                .customController(option -> new IntegerSliderController(option, 0, 7, 1, value -> Text.of(String.format("%d block(s)", value))))
                .build();

        Option<Double> entityDistanceOption = Option.<Double>createBuilder()
                .name(Text.translatable("options.entityDistanceScaling"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.quality.entityDistance.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Medium.getText()))
                        .build())
                .binding(100.0d, () -> (double) Math.round(this.client.options.getEntityDistanceScaling().get() * 100.0d), newValue -> this.client.options.getEntityDistanceScaling().set(newValue / 100.0d))
                .customController(option -> new DoubleSliderController(option, 50, 500, 25, value -> Text.of(String.format("%d%c", value.intValue(), '%'))))
                .build();

        Option<Boolean> entityShadowsOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("iridium.options.quality.entityShadows"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.quality.entityShadows.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Low.getText()))
                        .build())
                .binding(Binding.minecraft(this.client.options.getEntityShadows()))
                .customController(BooleanController::new)
                .build();

        Option<Boolean> enableVignetteOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("iridium.options.quality.enableVignette"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.quality.enableVignette.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Low.getText()))
                        .build())
                .binding(IridiumGameOptions.defaults().isVignetteEnabled(), this.iridiumGameOptions::isVignetteEnabled, this.iridiumGameOptions::setVignetteEnabled)
                .customController(BooleanController::new)
                .build();

        Option<Double> distortionEffectsOption = Option.<Double>createBuilder()
                .name(Text.translatable("options.screenEffectScale"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.quality.distortionEffects.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Low.getText()))
                        .build())
                .binding(100.0d, () -> (double) Math.round(this.client.options.getDistortionEffectScale().get() * 100.0d), newValue -> this.client.options.getDistortionEffectScale().set(newValue / 100.0d))
                .customController(option -> new DoubleSliderController(option, 0, 100, 1, value -> Text.of(String.format("%d%c", value.intValue(), '%'))))
                .build();

        Option<Double> fovEffectsOption = Option.<Double>createBuilder()
                .name(Text.translatable("options.fovEffectScale"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.quality.fovEffects.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Low.getText()))
                        .build())
                .binding(100.0d, () -> (double) Math.round(Math.pow(this.client.options.getFovEffectScale().get(), 2.0d) * 100.0d), newValue -> this.client.options.getFovEffectScale().set(Math.sqrt(newValue / 100.0d)))
                .customController(option -> new DoubleSliderController(option, 0, 100, 1, value -> Text.of(String.format("%d%c", value.intValue(), '%'))))
                .build();

        Option<Integer> mipmapLevelsOption = Option.<Integer>createBuilder()
                .name(Text.translatable("options.mipmapLevels"))
                .binding(Binding.minecraft(this.client.options.getMipmapLevels()))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.quality.mipmapLevels.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Medium.getText()))
                        .build())
                .customController(option -> new IntegerSliderController(option, 0, 4, 1, value -> Text.of(value + "x")))
                .flag(OptionFlag.ASSET_RELOAD)
                .build();

        Collections.addAll(this.qualityOptions, graphicsModeOption, cloudsOption, weatherQualityOption, leavesQualityOption, particlesOption, smoothLightingOption,
                biomeBlendOption, entityDistanceOption, entityShadowsOption, enableVignetteOption, distortionEffectsOption, fovEffectsOption, mipmapLevelsOption);
    }

    private void createExtraOptions()
    {
        Option<IridiumGameOptions.OverlayPosition> overlayPositionOption = Option.<IridiumGameOptions.OverlayPosition>createBuilder()
                .name(Text.translatable("iridium.options.extras.overlayPosition"))
                .description(OptionDescription.of(Text.translatable("iridium.options.extras.overlayPosition.description")))
                .binding(IridiumGameOptions.defaults().getOverlayPosition(), this.iridiumGameOptions::getOverlayPosition, this.iridiumGameOptions::setOverlayPosition)
                .customController(option -> new EnumController<>(option, overlayPosition ->
                {
                    return switch (overlayPosition)
                    {
                        case TopLeft -> Text.translatable("iridium.options.overlayPosition.topLeft");
                        case TopRight -> Text.translatable("iridium.options.overlayPosition.topRight");
                        case BottomLeft -> Text.translatable("iridium.options.overlayPosition.bottomLeft");
                        case BottomRight -> Text.translatable("iridium.options.overlayPosition.bottomRight");
                    };
                }, IridiumGameOptions.OverlayPosition.toArray()))
                .build();

        Option<IridiumGameOptions.TextContrast> textContrastOption = Option.<IridiumGameOptions.TextContrast>createBuilder()
                .name(Text.translatable("iridium.options.extras.textContrast"))
                .description(OptionDescription.of(Text.translatable("iridium.options.extras.textContrast.description")))
                .binding(IridiumGameOptions.defaults().getTextContrast(), this.iridiumGameOptions::getTextContrast, this.iridiumGameOptions::setTextContrast)
                .customController(option -> new EnumController<>(option, IridiumGameOptions.TextContrast.class))
                .build();

        Option<Boolean> showFPSOverlayOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("iridium.options.extras.showFPSOverlay"))
                .description(OptionDescription.of(Text.translatable("iridium.options.extras.showFPSOverlay.description")))
                .binding(IridiumGameOptions.defaults().isFPSOverlayEnabled(), this.iridiumGameOptions::isFPSOverlayEnabled, this.iridiumGameOptions::setFPSOverlayEnabled)
                .customController(BooleanController::new)
                .build();

        Option<Boolean> showCoordinatesOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("iridium.options.extras.showCoordinates"))
                .description(OptionDescription.of(Text.translatable("iridium.options.extras.showCoordinates.description")))
                .binding(IridiumGameOptions.defaults().isCoordinatesOverlayEnabled(), this.iridiumGameOptions::isCoordinatesOverlayEnabled, this.iridiumGameOptions::setCoordinatesOverlayEnabled)
                .customController(BooleanController::new)
                .build();

        Collections.addAll(this.extraOptions, overlayPositionOption, textContrastOption, showFPSOverlayOption, showCoordinatesOption);
    }
}
