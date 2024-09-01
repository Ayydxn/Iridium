package me.ayydan.iridium.options.categories;

import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.VideoMode;
import com.mojang.blaze3d.platform.Window;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.gui.controllers.BooleanController;
import dev.isxander.yacl3.gui.controllers.cycling.EnumController;
import dev.isxander.yacl3.gui.controllers.slider.DoubleSliderController;
import dev.isxander.yacl3.gui.controllers.slider.IntegerSliderController;
import me.ayydan.iridium.gui.screens.IridiumOptionsScreen;
import me.ayydan.iridium.options.IridiumGameOptions;
import me.ayydan.iridium.options.util.OptionPerformanceImpact;
import me.ayydan.iridium.render.IridiumRenderer;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.apache.commons.compress.utils.Lists;

import java.util.*;

public class IridiumVideoOptionsCategory extends IridiumOptionCategory
{
    private List<Option<?>> displayOptions;
    private List<Option<?>> graphicsOptions;
    private List<Option<?>> graphicsQualityOptions;
    private List<Option<?>> advancedGraphicsOptions;

    public IridiumVideoOptionsCategory()
    {
        super(Component.translatable("iridium.options.category.video"));
    }

    @Override
    public List<Option<?>> getCategoryOptions()
    {
        return null;
    }

    @Override
    public List<OptionGroup> getCategoryGroups()
    {
        this.createDisplayOptions();
        this.createGraphicsOptions();
        this.createGraphicsQualityOptions();
        this.createAdvancedGraphicsOptions();

        OptionGroup displayOptionsGroup = OptionGroup.createBuilder()
                .name(Component.translatable("iridium.options.group.display"))
                .options(this.displayOptions)
                .build();

        OptionGroup graphicsOptionsGroup = OptionGroup.createBuilder()
                .name(Component.translatable("iridium.options.group.graphics"))
                .options(this.graphicsOptions)
                .build();

        OptionGroup graphicsQualityOptionsGroup = OptionGroup.createBuilder()
                .name(Component.translatable("iridium.options.group.graphicsQuality"))
                .options(this.graphicsQualityOptions)
                .build();

        OptionGroup advancedGraphicsOptionsGroup = OptionGroup.createBuilder()
                .name(Component.translatable("iridium.options.group.advancedGraphics"))
                .options(this.advancedGraphicsOptions)
                .build();

        return List.of(displayOptionsGroup, graphicsOptionsGroup, graphicsQualityOptionsGroup, advancedGraphicsOptionsGroup);
    }

    private void createDisplayOptions()
    {
        this.displayOptions = Lists.newArrayList();

        Minecraft client = Minecraft.getInstance();
        Window window = client.getWindow();
        int videoModeCount = window.findBestMonitor() != null ? window.findBestMonitor().getModeCount() : 0;

        Option<Boolean> fullscreenOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.fullscreen"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("iridium.options.display.fullscreen.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.None.getText()))
                        .build())
                .binding(false, () -> client.options.fullscreen().get(), (newValue) ->
                {
                    client.options.fullscreen().set(newValue);

                    if (window.isFullscreen() != client.options.fullscreen().get())
                    {
                        window.toggleFullScreen();

                        // The client might not be able to enter fullscreen, so we do this just in-case that happens.
                        client.options.fullscreen().set(window.isFullscreen());
                    }
                })
                .customController(BooleanController::new)
                .build();

        Option<Integer> fullscreenResolutionOption = Option.<Integer>createBuilder()
                .name(Component.translatable("options.fullscreen.resolution"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("iridium.options.display.fullscreenResolution.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.High.getText()))
                        .build())
                .binding(0, () ->
                {
                    if (window.findBestMonitor() == null)
                    {
                        return 0;
                    }
                    else
                    {
                        Optional<VideoMode> videoMode = window.getPreferredFullscreenVideoMode();
                        return videoMode.map((vidMode) -> window.findBestMonitor().getVideoModeIndex(vidMode) + 1).orElse(0);
                    }
                }, (newValue) ->
                {
                    if (window.findBestMonitor() != null)
                    {
                        if (newValue == 0)
                        {
                            window.setPreferredFullscreenVideoMode(Optional.empty());
                        }
                        else
                        {
                            window.setPreferredFullscreenVideoMode(Optional.of(window.findBestMonitor().getMode(newValue - 1)));
                        }
                    }

                    window.changeFullscreenVideoMode();
                })
                .customController(option -> new IntegerSliderController(option, 0, videoModeCount, 1, value ->
                {
                    Monitor monitor = window.findBestMonitor();

                    if (monitor == null)
                    {
                        return Component.translatable("options.fullscreen.unavailable");
                    }
                    else
                    {
                        if (value == 0)
                        {
                            return Component.translatable("options.fullscreen.current");
                        }
                        else
                        {
                            VideoMode videoMode = monitor.getMode(value - 1);
                            int width = videoMode.getWidth();
                            int height = videoMode.getHeight();
                            int refreshRate = videoMode.getRefreshRate();
                            int colorDepth = videoMode.getRedBits() + videoMode.getGreenBits() + videoMode.getBlueBits();

                            return Component.literal(String.format("%d x %d (%dhz, %d bits)", width, height, refreshRate, colorDepth));
                        }
                    }
                }))
                .build();

        Option<Boolean> vSyncOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("iridium.options.display.vSync"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("iridium.options.display.vSync.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Varies.getText()))
                        .build())
                .binding(true, () -> client.options.enableVsync().get(), newValue ->
                {
                    client.options.enableVsync().set(newValue);
                    Minecraft.getInstance().getWindow().getSwapChain().enableVSync(newValue);
                })
                .customController(BooleanController::new)
                .build();

        Option<Integer> framerateLimitOption = Option.<Integer>createBuilder()
                .name(Component.translatable("iridium.options.display.framerateLimit"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("iridium.options.display.framerateLimit.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.None.getText()))
                        .build())
                .binding(120, () -> client.options.framerateLimit().get(), newValue ->
                {
                    client.options.framerateLimit().set(newValue);
                    client.getWindow().setFramerateLimit(newValue);
                })
                .customController(option -> new IntegerSliderController(option, 10, 260, 10, value ->
                        value == 260 ? Component.translatable("options.framerateLimit.max") : Component.literal(Integer.toString(value))))
                .build();

        Collections.addAll(this.displayOptions, fullscreenOption, fullscreenResolutionOption, vSyncOption, framerateLimitOption);
    }

    private void createGraphicsOptions()
    {
        this.graphicsOptions = Lists.newArrayList();

        Minecraft client = Minecraft.getInstance();

        Option<Double> brightnessOption = Option.<Double>createBuilder()
                .name(Component.translatable("options.gamma"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("iridium.options.graphics.brightness.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.None.getText()))
                        .build())
                .binding(50.0d, () -> client.options.gamma().get() / 0.01d, newValue -> client.options.gamma().set(newValue * 0.01d))
                .customController(option -> new DoubleSliderController(option, 0, 100, 1, value ->
                        value == 100.0d ? Component.translatable("options.gamma.max") : Component.literal(Double.toString(value))))
                .build();

        Option<Integer> fovOption = Option.<Integer>createBuilder()
                .name(Component.translatable("options.fov"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("iridium.options.graphics.fov.description")
                                .append(" ")
                                .append(Component.translatable("iridium.options.graphics.fov.descriptionLink")
                                        .setStyle(Style.EMPTY
                                                .withBold(true)
                                                .withUnderlined(true)
                                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://minecraft.fandom.com/wiki/Options#Options"))))
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Varies.getText()))
                        .build())
                .binding(Binding.minecraft(client.options.fov()))
                .customController(option -> new IntegerSliderController(option, 30, 110, 1, value ->
                {
                    if (value == 70)
                    {
                        return Component.translatable("iridium.options.graphics.fov.normal", value);
                    }
                    else if (value == 110)
                    {
                        return Component.translatable("iridium.options.graphics.fov.quakePro", value);
                    }

                    return Component.literal(String.valueOf(value));
                }))
                .flag(OptionFlag.WORLD_RENDER_UPDATE)
                .build();

        Option<Integer> guiScaleOption = Option.<Integer>createBuilder()
                .name(Component.translatable("options.guiScale"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("iridium.options.graphics.guiScale.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.None.getText()))
                        .build())
                .binding(0, () -> client.options.guiScale().get(), (newValue) ->
                {
                    client.options.guiScale().set(newValue);
                    client.resizeDisplay();
                })
                .customController(option -> new IntegerSliderController(option, 0, Minecraft.getInstance().getWindow().calculateScale(0, Minecraft.getInstance().isEnforceUnicode()), 1, value ->
                        value == 0 ? Component.translatable("options.guiScale.auto") : Component.literal(value + "x")))
                .build();

        Option<Boolean> viewBobbingOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.viewBobbing"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("iridium.options.graphics.viewBobbing.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Varies.getText()))
                        .build())
                .binding(Binding.minecraft(client.options.bobView()))
                .customController(BooleanController::new)
                .build();

        Option<AttackIndicatorStatus> attackIndicatorOption = Option.<AttackIndicatorStatus>createBuilder()
                .name(Component.translatable("options.attackIndicator"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("iridium.options.graphics.attackIndicator.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.None.getText()))
                        .build())
                .binding(Binding.minecraft(client.options.attackIndicator()))
                .customController(option -> new EnumController<>(option, AttackIndicatorStatus.class))
                .build();

        ButtonOption resourcePacksOption = ButtonOption.createBuilder()
                .name(Component.translatable("iridium.options.graphics.resourcePacks"))
                .description(OptionDescription.of(Component.translatable("iridium.options.graphics.resourcePacks.description")))
                .text(Component.literal(""))
                .action((screen, button) -> client.setScreen(new PackSelectionScreen(client.getResourcePackRepository(), (packManager) ->
                {
                    client.options.updateResourcePacks(packManager);
                    client.setScreen(new IridiumOptionsScreen(null).getHandle());
                }, client.getResourcePackDirectory(), Component.translatable("resourcePack.title"))))
                .build();

        Collections.addAll(this.graphicsOptions, brightnessOption, fovOption, guiScaleOption, viewBobbingOption, attackIndicatorOption, resourcePacksOption);
    }

    @SuppressWarnings("ConstantConditions")
    private void createGraphicsQualityOptions()
    {
        this.graphicsQualityOptions = Lists.newArrayList();

        Minecraft client = Minecraft.getInstance();

        Option<Integer> renderDistanceOption = Option.<Integer>createBuilder()
                .name(Component.translatable("options.renderDistance"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("iridium.options.graphicsQuality.renderDistance.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.High.getText()))
                        .build())
                .binding(Binding.minecraft(client.options.renderDistance()))
                .customController(option -> new IntegerSliderController(option, 2, 32, 1))
                .build();

        Option<Integer> simulationDistanceOption = Option.<Integer>createBuilder()
                .name(Component.translatable("options.simulationDistance"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("iridium.options.graphicsQuality.simulationDistance.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.High.getText()))
                        .build())
                .binding(Binding.minecraft(client.options.simulationDistance()))
                .customController(option -> new IntegerSliderController(option, 5, 32, 1))
                .build();

        Option<Double> entityDistanceOption = Option.<Double>createBuilder()
                .name(Component.translatable("options.entityDistanceScaling"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("iridium.options.graphicsQuality.entityDistance.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Medium.getText()))
                        .build())
                .binding(100.0d, () -> (double) Math.round(client.options.entityDistanceScaling().get() * 100.0d), newValue -> client.options.entityDistanceScaling().set(newValue / 100.0d))
                .customController(option -> new DoubleSliderController(option, 50, 500, 25, value -> Component.literal(String.format("%d%c", value.intValue(), '%'))))
                .build();

        Option<GraphicsStatus> graphicsModeOption = Option.<GraphicsStatus>createBuilder()
                .name(Component.translatable("iridium.options.graphicsQuality.graphicsMode"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("iridium.options.graphicsQuality.graphicsMode.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.High.getText()))
                        .build())
                .binding(Binding.minecraft(client.options.graphicsMode()))
                .customController(option -> new EnumController<>(option, GraphicsStatus.class))
                .flag(OptionFlag.RELOAD_CHUNKS)
                .build();

        Option<CloudStatus> cloudsOption = Option.<CloudStatus>createBuilder()
                .name(Component.translatable("iridium.options.graphicsQuality.clouds"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("iridium.options.graphicsQuality.clouds.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Low.getText()))
                        .build())
                .binding(Binding.minecraft(client.options.cloudStatus()))
                .customController(option -> new EnumController<>(option, CloudStatus.class))
                .build();

        Option<IridiumGameOptions.GraphicsQuality> weatherQualityOption = Option.<IridiumGameOptions.GraphicsQuality>createBuilder()
                .name(Component.translatable("iridium.options.graphicsQuality.weather"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("iridium.options.graphicsQuality.weather.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Medium.getText()))
                        .build())
                .binding(IridiumGameOptions.defaults().qualityOptions.weatherQuality, () -> this.iridiumGameOptions.qualityOptions.weatherQuality, newValue -> this.iridiumGameOptions.qualityOptions.weatherQuality = newValue)
                .customController(option -> new EnumController<>(option, IridiumGameOptions.GraphicsQuality.class))
                .flag(OptionFlag.RELOAD_CHUNKS)
                .build();

        Option<IridiumGameOptions.GraphicsQuality> leavesQualityOption = Option.<IridiumGameOptions.GraphicsQuality>createBuilder()
                .name(Component.translatable("iridium.options.graphicsQuality.leaves"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("iridium.options.graphicsQuality.leaves.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Low.getText()))
                        .build())
                .binding(IridiumGameOptions.defaults().qualityOptions.leavesQuality, () -> this.iridiumGameOptions.qualityOptions.leavesQuality, newValue -> this.iridiumGameOptions.qualityOptions.leavesQuality = newValue)
                .customController(option -> new EnumController<>(option, IridiumGameOptions.GraphicsQuality.class))
                .flag(OptionFlag.RELOAD_CHUNKS)
                .build();

        Option<ParticleStatus> particlesOption = Option.<ParticleStatus>createBuilder()
                .name(Component.translatable("options.particles"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("iridium.options.graphicsQuality.particles.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Low.getText()))
                        .build())
                .binding(Binding.minecraft(client.options.particles()))
                .customController(option -> new EnumController<>(option, ParticleStatus.class))
                .build();

        Option<Boolean> smoothLightingOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.ao"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("iridium.options.graphicsQuality.smoothLighting.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Low.getText()))
                        .build())
                .binding(Binding.minecraft(client.options.ambientOcclusion()))
                .customController(BooleanController::new)
                .build();

        Option<Integer> biomeBlendOption = Option.<Integer>createBuilder()
                .name(Component.translatable("iridium.options.graphicsQuality.biomeBlend"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("iridium.options.graphicsQuality.biomeBlend.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Low.getText()))
                        .build())
                .binding(Binding.minecraft(client.options.biomeBlendRadius()))
                .customController(option -> new IntegerSliderController(option, 0, 7, 1, value -> Component.literal(String.format("%d block(s)", value))))
                .build();

        Option<Boolean> entityShadowsOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("iridium.options.graphicsQuality.entityShadows"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("iridium.options.graphicsQuality.entityShadows.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Low.getText()))
                        .build())
                .binding(Binding.minecraft(client.options.entityShadows()))
                .customController(BooleanController::new)
                .build();

        Option<Boolean> enableVignetteOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("iridium.options.graphicsQuality.enableVignette"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("iridium.options.graphicsQuality.enableVignette.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Low.getText()))
                        .build())
                .binding(IridiumGameOptions.defaults().qualityOptions.enableVignette, () -> this.iridiumGameOptions.qualityOptions.enableVignette, newValue -> this.iridiumGameOptions.qualityOptions.enableVignette = newValue)
                .customController(BooleanController::new)
                .build();

        Option<Double> distortionEffectsOption = Option.<Double>createBuilder()
                .name(Component.translatable("options.screenEffectScale"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("iridium.options.graphicsQuality.distortionEffects.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Low.getText()))
                        .build())
                .binding(100.0d, () -> (double) Math.round(client.options.screenEffectScale().get() * 100.0d), newValue -> client.options.screenEffectScale().set(newValue / 100.0d))
                .customController(option -> new DoubleSliderController(option, 0, 100, 1, value -> Component.literal(String.format("%d%c", value.intValue(), '%'))))
                .build();

        Option<Double> fovEffectsOption = Option.<Double>createBuilder()
                .name(Component.translatable("options.fovEffectScale"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("iridium.options.graphicsQuality.fovEffects.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Low.getText()))
                        .build())
                .binding(100.0d, () -> (double) Math.round(Math.pow(client.options.fovEffectScale().get(), 2.0d) * 100.0d), newValue -> client.options.fovEffectScale().set(Math.sqrt(newValue / 100.0d)))
                .customController(option -> new DoubleSliderController(option, 0, 100, 1, value -> Component.literal(String.format("%d%c", value.intValue(), '%'))))
                .build();

        Option<Integer> mipmapLevelsOption = Option.<Integer>createBuilder()
                .name(Component.translatable("options.mipmapLevels"))
                .binding(Binding.minecraft(client.options.mipmapLevels()))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("iridium.options.graphicsQuality.mipmapLevels.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.Medium.getText()))
                        .build())
                .customController(option -> new IntegerSliderController(option, 0, 4, 1, value -> Component.literal(value + "x")))
                .flag(OptionFlag.ASSET_RELOAD)
                .build();

        Collections.addAll(this.graphicsQualityOptions, renderDistanceOption, simulationDistanceOption, entityDistanceOption, graphicsModeOption, cloudsOption,
                weatherQualityOption, leavesQualityOption, particlesOption, smoothLightingOption, entityShadowsOption, enableVignetteOption, biomeBlendOption,
                fovEffectsOption, distortionEffectsOption, mipmapLevelsOption);
    }

    @SuppressWarnings("ConstantConditions")
    private void createAdvancedGraphicsOptions()
    {
        this.advancedGraphicsOptions = Lists.newArrayList();

        Option<Boolean> showFPSOverlayOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("iridium.options.advancedGraphics.showFPSOverlay"))
                .description(OptionDescription.of(Component.translatable("iridium.options.advancedGraphics.showFPSOverlay.description")))
                .binding(IridiumGameOptions.defaults().advancedGraphicsOptions.showFPSOverlay, () -> this.iridiumGameOptions.advancedGraphicsOptions.showFPSOverlay, newValue -> this.iridiumGameOptions.advancedGraphicsOptions.showFPSOverlay = newValue)
                .customController(BooleanController::new)
                .build();

        Option<Boolean> showCoordinatesOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("iridium.options.advancedGraphics.showCoordinates"))
                .description(OptionDescription.of(Component.translatable("iridium.options.advancedGraphics.showCoordinates.description")))
                .binding(IridiumGameOptions.defaults().advancedGraphicsOptions.showCoordinates, () -> this.iridiumGameOptions.advancedGraphicsOptions.showCoordinates, newValue -> this.iridiumGameOptions.advancedGraphicsOptions.showCoordinates = newValue)
                .customController(BooleanController::new)
                .build();

        Option<IridiumGameOptions.TextContrast> overlayContrastOption = Option.<IridiumGameOptions.TextContrast>createBuilder()
                .name(Component.translatable("iridium.options.advancedGraphics.overlayContrast"))
                .description(OptionDescription.of(Component.translatable("iridium.options.advancedGraphics.textContrast.description")))
                .binding(IridiumGameOptions.defaults().advancedGraphicsOptions.textContrast, () -> this.iridiumGameOptions.advancedGraphicsOptions.textContrast, newValue -> this.iridiumGameOptions.advancedGraphicsOptions.textContrast = newValue)
                .customController(option -> new EnumController<>(option, IridiumGameOptions.TextContrast.class))
                .build();

        Option<IridiumGameOptions.OverlayPosition> overlayPositionOption = Option.<IridiumGameOptions.OverlayPosition>createBuilder()
                .name(Component.translatable("iridium.options.advancedGraphics.overlayPosition"))
                .description(OptionDescription.of(Component.translatable("iridium.options.advancedGraphics.overlayPosition.description")))
                .binding(IridiumGameOptions.defaults().advancedGraphicsOptions.overlayPosition, () -> this.iridiumGameOptions.advancedGraphicsOptions.overlayPosition, newValue -> this.iridiumGameOptions.advancedGraphicsOptions.overlayPosition = newValue)
                .customController(option -> new EnumController<>(option, overlayPosition -> switch (overlayPosition)
                {
                    case TopLeft -> Component.translatable("iridium.options.overlayPosition.topLeft");
                    case TopRight -> Component.translatable("iridium.options.overlayPosition.topRight");
                    case BottomLeft -> Component.translatable("iridium.options.overlayPosition.bottomLeft");
                    case BottomRight -> Component.translatable("iridium.options.overlayPosition.bottomRight");
                }, IridiumGameOptions.OverlayPosition.toArray()))
                .build();

        Collections.addAll(this.advancedGraphicsOptions, showFPSOverlayOption, showCoordinatesOption, overlayContrastOption, overlayPositionOption);
    }
}
