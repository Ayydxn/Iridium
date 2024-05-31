package me.ayydan.iridium.options.minecraft;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionFlag;
import dev.isxander.yacl3.gui.controllers.BooleanController;
import dev.isxander.yacl3.gui.controllers.slider.IntegerSliderController;
import me.ayydan.iridium.options.IridiumGameOptions;
import me.ayydan.iridium.options.OptionPerformanceImpact;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;

public class IridiumRendererOptions extends IridiumMinecraftOptions
{
    private final ArrayList<Option<?>> rendererOptions = new ArrayList<>();

    private ConfigCategory rendererOptionsCategory;

    public IridiumRendererOptions(@Nullable IridiumGameOptions iridiumGameOptions)
    {
        super(iridiumGameOptions);
    }

    @Override
    public void create()
    {
        this.createRendererOptions();

        this.rendererOptionsCategory = ConfigCategory.createBuilder()
                .name(Component.translatable("iridium.options.category.renderer"))
                .options(this.rendererOptions)
                .build();
    }

    @Override
    public ConfigCategory getYACLCategory()
    {
        return this.rendererOptionsCategory;
    }

    @SuppressWarnings("ConstantConditions")
    private void createRendererOptions()
    {
        Option<Boolean> shaderCachingOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("iridium.options.renderer.enableShaderCaching"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("iridium.options.renderer.enableShaderCaching.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.None.getText()))
                        .build())
                .binding(IridiumGameOptions.defaults().enableShaderCaching, () -> this.iridiumGameOptions.enableShaderCaching, newValue -> this.iridiumGameOptions.enableShaderCaching = newValue)
                .customController(BooleanController::new)
                .build();

        Option<Integer> framesInFlightOption = Option.<Integer>createBuilder()
                .name(Component.translatable("iridium.options.renderer.framesInFlight"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("iridium.options.renderer.framesInFlight.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.High.getText()))
                        .build())
                .binding(IridiumGameOptions.defaults().framesInFlight, () -> this.iridiumGameOptions.framesInFlight, newValue -> this.iridiumGameOptions.framesInFlight = newValue)
                .customController(option -> new IntegerSliderController(option, 0, 9, 1))
                .flag(OptionFlag.GAME_RESTART)
                .build();

        Collections.addAll(this.rendererOptions, shaderCachingOption, framesInFlightOption);
    }
}
