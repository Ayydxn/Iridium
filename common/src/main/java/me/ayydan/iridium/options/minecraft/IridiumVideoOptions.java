package me.ayydan.iridium.options.minecraft;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.glfw.VideoMode;
import com.mojang.blaze3d.glfw.Window;
import com.mojang.blaze3d.glfw.monitor.Monitor;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.gui.controllers.BooleanController;
import dev.isxander.yacl3.gui.controllers.cycling.EnumController;
import dev.isxander.yacl3.gui.controllers.slider.DoubleSliderController;
import dev.isxander.yacl3.gui.controllers.slider.IntegerSliderController;
import me.ayydan.iridium.gui.screens.IridiumOptionsScreen;
import me.ayydan.iridium.options.IridiumGameOptions;
import me.ayydan.iridium.options.OptionPerformanceImpact;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class IridiumVideoOptions extends IridiumMinecraftOptions
{
    private final List<Option<?>> displayOptions = new ArrayList<>();
    private final List<Option<?>> graphicsOptions = new ArrayList<>();
    private final List<Option<?>> graphicsQualityOptions = new ArrayList<>();
    private final List<Option<?>> advancedGraphicsOptions = new ArrayList<>();

    private ConfigCategory videoOptionsCategory;

    public IridiumVideoOptions(IridiumGameOptions iridiumGameOptions)
    {
        super(iridiumGameOptions);
    }

    @Override
    public void create()
    {
        this.createDisplayOptions();
        this.createGraphicsOptions();
        this.createGraphicsQualityOptions();
        this.createAdvancedGraphicsOptions();

        OptionGroup displayOptionsGroup = OptionGroup.createBuilder()
                .name(Text.translatable("iridium.options.group.display"))
                .options(this.displayOptions)
                .build();

        OptionGroup graphicsOptionsGroup = OptionGroup.createBuilder()
                .name(Text.translatable("iridium.options.group.graphics"))
                .options(this.graphicsOptions)
                .build();

        OptionGroup graphicsQualityOptionsGroup = OptionGroup.createBuilder()
                .name(Text.translatable("iridium.options.group.graphicsQuality"))
                .options(this.graphicsQualityOptions)
                .build();

        OptionGroup advancedGraphicsOptionsGroup = OptionGroup.createBuilder()
                .name(Text.translatable("iridium.options.group.advancedGraphics"))
                .options(this.advancedGraphicsOptions)
                .build();

        this.videoOptionsCategory = ConfigCategory.createBuilder()
                .name(Text.translatable("iridium.options.category.video"))
                .groups(Lists.newArrayList(displayOptionsGroup, graphicsOptionsGroup, graphicsQualityOptionsGroup, advancedGraphicsOptionsGroup))
                .build();
    }

    @Override
    public ConfigCategory getYACLCategory()
    {
        return this.videoOptionsCategory;
    }

    private void createDisplayOptions()
    {
        Window window = this.client.getWindow();
        int videoModeCount = window.getMonitor() != null ? window.getMonitor().getVideoModeCount() : 0;

        Option<Boolean> fullscreenOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("options.fullscreen"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.display.fullscreen.description")
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

        Option<Integer> fullscreenResolutionOption = Option.<Integer>createBuilder()
                .name(Text.translatable("options.fullscreen.resolution"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.display.fullscreenResolution.description")
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

        Option<Boolean> vSyncOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("iridium.options.display.vSync"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.display.vSync.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Varies.getText()))
                        .build())
                .binding(true, () -> this.client.options.getEnableVsync().get(), newValue -> this.client.options.getEnableVsync().set(newValue))
                .customController(BooleanController::new)
                .build();

        Option<Integer> framerateLimitOption = Option.<Integer>createBuilder()
                .name(Text.translatable("iridium.options.display.framerateLimit"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.display.framerateLimit.description")
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

        Collections.addAll(this.displayOptions, fullscreenOption, fullscreenResolutionOption, vSyncOption, framerateLimitOption);
    }

    private void createGraphicsOptions()
    {
        Option<Double> brightnessOption = Option.<Double>createBuilder()
                .name(Text.translatable("options.gamma"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.graphics.brightness.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.None.getText()))
                        .build())
                .binding(50.0d, () -> this.client.options.getGamma().get() / 0.01d, newValue -> this.client.options.getGamma().set(newValue * 0.01d))
                .customController(option -> new DoubleSliderController(option, 0, 100, 1, value ->
                        value == 100.0d ? Text.translatable("options.gamma.max") : Text.of(Double.toString(value))))
                .build();

        Option<Integer> fovOption = Option.<Integer>createBuilder()
                .name(Text.translatable("options.fov"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.graphics.fov.description")
                                .append(" ")
                                .append(Text.translatable("iridium.options.graphics.fov.descriptionLink")
                                        .setStyle(Style.EMPTY
                                                .withBold(true)
                                                .withUnderline(true)
                                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://minecraft.fandom.com/wiki/Options#Options"))))
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Varies.getText()))
                        .build())
                .binding(Binding.minecraft(this.client.options.getFov()))
                .customController(option -> new IntegerSliderController(option, 30, 110, 1, value ->
                {
                    return switch (value)
                    {
                        case 70 -> Text.translatable("iridium.options.graphics.fov.normal", value);
                        case 110 -> Text.translatable("iridium.options.graphics.fov.quakePro", value);
                        default -> Text.literal(String.valueOf(value));
                    };
                }))
                .flag(OptionFlag.WORLD_RENDER_UPDATE)
                .build();

        Option<Integer> guiScaleOption = Option.<Integer>createBuilder()
                .name(Text.translatable("options.guiScale"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.graphics.guiScale.description")
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

        Option<Boolean> viewBobbingOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("options.viewBobbing"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.graphics.viewBobbing.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Varies.getText()))
                        .build())
                .binding(Binding.minecraft(this.client.options.getBobView()))
                .customController(BooleanController::new)
                .build();

        Option<AttackIndicator> attackIndicatorOption = Option.<AttackIndicator>createBuilder()
                .name(Text.translatable("options.attackIndicator"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.graphics.attackIndicator.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.None.getText()))
                        .build())
                .binding(Binding.minecraft(this.client.options.getAttackIndicator()))
                .customController(option -> new EnumController<>(option, AttackIndicator.class))
                .build();

        ButtonOption resourcePacksOption = ButtonOption.createBuilder()
                .name(Text.translatable("iridium.options.graphics.resourcePacks"))
                .description(OptionDescription.of(Text.translatable("iridium.options.graphics.resourcePacks.description")))
                .text(Text.literal(""))
                .action((screen, button) -> this.client.setScreen(new PackScreen(this.client.getResourcePackManager(), (packManager) ->
                {
                    this.client.options.updateResourcePacks(packManager);
                    this.client.setScreen(new IridiumOptionsScreen(null).getHandle());
                }, this.client.getResourcePackDir(), Text.translatable("resourcePack.title"))))
                .build();

        Collections.addAll(this.graphicsOptions, brightnessOption, fovOption, guiScaleOption, viewBobbingOption, attackIndicatorOption, resourcePacksOption);
    }

    private void createGraphicsQualityOptions()
    {
        Option<Integer> renderDistanceOption = Option.<Integer>createBuilder()
                .name(Text.translatable("options.renderDistance"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.graphicsQuality.renderDistance.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.High.getText()))
                        .build())
                .binding(Binding.minecraft(this.client.options.getViewDistance()))
                .customController(option -> new IntegerSliderController(option, 2, 32, 1))
                .build();

        Option<Integer> simulationDistanceOption = Option.<Integer>createBuilder()
                .name(Text.translatable("options.simulationDistance"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.graphicsQuality.simulationDistance.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.High.getText()))
                        .build())
                .binding(Binding.minecraft(this.client.options.getSimulationDistance()))
                .customController(option -> new IntegerSliderController(option, 5, 32, 1))
                .build();

        Option<Double> entityDistanceOption = Option.<Double>createBuilder()
                .name(Text.translatable("options.entityDistanceScaling"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.graphicsQuality.entityDistance.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Medium.getText()))
                        .build())
                .binding(100.0d, () -> (double) Math.round(this.client.options.getEntityDistanceScaling().get() * 100.0d), newValue -> this.client.options.getEntityDistanceScaling().set(newValue / 100.0d))
                .customController(option -> new DoubleSliderController(option, 50, 500, 25, value -> Text.of(String.format("%d%c", value.intValue(), '%'))))
                .build();

        Option<GraphicsMode> graphicsModeOption = Option.<GraphicsMode>createBuilder()
                .name(Text.translatable("iridium.options.graphicsQuality.graphicsMode"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.graphicsQuality.graphicsMode.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.High.getText()))
                        .build())
                .binding(Binding.minecraft(this.client.options.getGraphicsMode()))
                .customController(option -> new EnumController<>(option, GraphicsMode.class))
                .flag(OptionFlag.RELOAD_CHUNKS)
                .build();

        Option<CloudRenderMode> cloudsOption = Option.<CloudRenderMode>createBuilder()
                .name(Text.translatable("iridium.options.graphicsQuality.clouds"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.graphicsQuality.clouds.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Low.getText()))
                        .build())
                .binding(Binding.minecraft(this.client.options.getRenderClouds()))
                .customController(option -> new EnumController<>(option, CloudRenderMode.class))
                .build();

        Option<IridiumGameOptions.GraphicsQuality> weatherQualityOption = Option.<IridiumGameOptions.GraphicsQuality>createBuilder()
                .name(Text.translatable("iridium.options.graphicsQuality.weather"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.graphicsQuality.weather.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Medium.getText()))
                        .build())
                .binding(IridiumGameOptions.defaults().getWeatherQuality(), this.iridiumGameOptions::getWeatherQuality, this.iridiumGameOptions::setWeatherQuality)
                .customController(option -> new EnumController<>(option, IridiumGameOptions.GraphicsQuality.class))
                .flag(OptionFlag.RELOAD_CHUNKS)
                .build();

        Option<IridiumGameOptions.GraphicsQuality> leavesQualityOption = Option.<IridiumGameOptions.GraphicsQuality>createBuilder()
                .name(Text.translatable("iridium.options.graphicsQuality.leaves"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.graphicsQuality.leaves.description")
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
                        .text(Text.translatable("iridium.options.graphicsQuality.particles.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Low.getText()))
                        .build())
                .binding(Binding.minecraft(this.client.options.getParticles()))
                .customController(option -> new EnumController<>(option, ParticlesMode.class))
                .build();

        Option<Boolean> smoothLightingOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("options.ao"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.graphicsQuality.smoothLighting.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Low.getText()))
                        .build())
                .binding(Binding.minecraft(this.client.options.getSmoothLighting()))
                .customController(BooleanController::new)
                .build();

        Option<Integer> biomeBlendOption = Option.<Integer>createBuilder()
                .name(Text.translatable("iridium.options.graphicsQuality.biomeBlend"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.graphicsQuality.biomeBlend.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Low.getText()))
                        .build())
                .binding(Binding.minecraft(this.client.options.getBiomeBlendRadius()))
                .customController(option -> new IntegerSliderController(option, 0, 7, 1, value -> Text.of(String.format("%d block(s)", value))))
                .build();

        Option<Boolean> entityShadowsOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("iridium.options.graphicsQuality.entityShadows"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.graphicsQuality.entityShadows.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Low.getText()))
                        .build())
                .binding(Binding.minecraft(this.client.options.getEntityShadows()))
                .customController(BooleanController::new)
                .build();

        Option<Boolean> enableVignetteOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("iridium.options.graphicsQuality.enableVignette"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.graphicsQuality.enableVignette.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Low.getText()))
                        .build())
                .binding(IridiumGameOptions.defaults().isVignetteEnabled(), this.iridiumGameOptions::isVignetteEnabled, this.iridiumGameOptions::setVignetteEnabled)
                .customController(BooleanController::new)
                .build();

        Option<Double> distortionEffectsOption = Option.<Double>createBuilder()
                .name(Text.translatable("options.screenEffectScale"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.graphicsQuality.distortionEffects.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Low.getText()))
                        .build())
                .binding(100.0d, () -> (double) Math.round(this.client.options.getDistortionEffectScale().get() * 100.0d), newValue -> this.client.options.getDistortionEffectScale().set(newValue / 100.0d))
                .customController(option -> new DoubleSliderController(option, 0, 100, 1, value -> Text.of(String.format("%d%c", value.intValue(), '%'))))
                .build();

        Option<Double> fovEffectsOption = Option.<Double>createBuilder()
                .name(Text.translatable("options.fovEffectScale"))
                .description(OptionDescription.createBuilder()
                        .text(Text.translatable("iridium.options.graphicsQuality.fovEffects.description")
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
                        .text(Text.translatable("iridium.options.graphicsQuality.mipmapLevels.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Medium.getText()))
                        .build())
                .customController(option -> new IntegerSliderController(option, 0, 4, 1, value -> Text.of(value + "x")))
                .flag(OptionFlag.ASSET_RELOAD)
                .build();

        Collections.addAll(this.graphicsQualityOptions, renderDistanceOption, simulationDistanceOption, entityDistanceOption, graphicsModeOption, cloudsOption,
                weatherQualityOption, leavesQualityOption, particlesOption, smoothLightingOption, entityShadowsOption, enableVignetteOption, biomeBlendOption,
                fovEffectsOption, distortionEffectsOption, mipmapLevelsOption);
    }

    private void createAdvancedGraphicsOptions()
    {
        Option<Boolean> showFPSOverlayOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("iridium.options.advancedGraphics.showFPSOverlay"))
                .description(OptionDescription.of(Text.translatable("iridium.options.advancedGraphics.showFPSOverlay.description")))
                .binding(IridiumGameOptions.defaults().isFPSOverlayEnabled(), this.iridiumGameOptions::isFPSOverlayEnabled, this.iridiumGameOptions::setFPSOverlayEnabled)
                .customController(BooleanController::new)
                .build();

        Option<Boolean> showCoordinatesOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("iridium.options.advancedGraphics.showCoordinates"))
                .description(OptionDescription.of(Text.translatable("iridium.options.advancedGraphics.showCoordinates.description")))
                .binding(IridiumGameOptions.defaults().isCoordinatesOverlayEnabled(), this.iridiumGameOptions::isCoordinatesOverlayEnabled, this.iridiumGameOptions::setCoordinatesOverlayEnabled)
                .customController(BooleanController::new)
                .build();

        Option<IridiumGameOptions.TextContrast> overlayContrastOption = Option.<IridiumGameOptions.TextContrast>createBuilder()
                .name(Text.translatable("iridium.options.advancedGraphics.overlayContrast"))
                .description(OptionDescription.of(Text.translatable("iridium.options.advancedGraphics.textContrast.description")))
                .binding(IridiumGameOptions.defaults().getTextContrast(), this.iridiumGameOptions::getTextContrast, this.iridiumGameOptions::setTextContrast)
                .customController(option -> new EnumController<>(option, IridiumGameOptions.TextContrast.class))
                .build();

        Option<IridiumGameOptions.OverlayPosition> overlayPositionOption = Option.<IridiumGameOptions.OverlayPosition>createBuilder()
                .name(Text.translatable("iridium.options.advancedGraphics.overlayPosition"))
                .description(OptionDescription.of(Text.translatable("iridium.options.advancedGraphics.overlayPosition.description")))
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

        Collections.addAll(this.advancedGraphicsOptions, showFPSOverlayOption, showCoordinatesOption, overlayContrastOption, overlayPositionOption);
    }
}
