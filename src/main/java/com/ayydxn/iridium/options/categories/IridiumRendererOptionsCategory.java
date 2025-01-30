package com.ayydxn.iridium.options.categories;

import com.ayydxn.iridium.options.IridiumGameOptions;
import com.ayydxn.iridium.options.util.OptionPerformanceImpact;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionFlag;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.gui.controllers.BooleanController;
import dev.isxander.yacl3.gui.controllers.slider.IntegerSliderController;
import net.minecraft.network.chat.Component;

import java.util.List;

public class IridiumRendererOptionsCategory extends IridiumOptionCategory
{
    public IridiumRendererOptionsCategory()
    {
        super("Renderer", Component.translatable("iridium.options.category.renderer"));
    }

    @Override
    public List<Option<?>> getCategoryOptions()
    {
        Option<Boolean> shaderCachingOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("iridium.options.renderer.enableShaderCaching"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("iridium.options.renderer.enableShaderCaching.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.None.getText()))
                        .build())
                .binding(IridiumGameOptions.defaults().rendererOptions.enableShaderCaching, () -> this.iridiumGameOptions.rendererOptions.enableShaderCaching, newValue -> this.iridiumGameOptions.rendererOptions.enableShaderCaching = newValue)
                .customController(BooleanController::new)
                .build();

        Option<Integer> framesInFlightOption = Option.<Integer>createBuilder()
                .name(Component.translatable("iridium.options.renderer.framesInFlight"))
                .description(OptionDescription.createBuilder()
                        .text(Component.translatable("iridium.options.renderer.framesInFlight.description")
                                .append("\n\n")
                                .append(OptionPerformanceImpact.High.getText()))
                        .build())
                .binding(IridiumGameOptions.defaults().rendererOptions.framesInFlight, () -> this.iridiumGameOptions.rendererOptions.framesInFlight, newValue -> this.iridiumGameOptions.rendererOptions.framesInFlight = newValue)
                .customController(option -> new IntegerSliderController(option, 0, 9, 1))
                .flag(OptionFlag.GAME_RESTART)
                .build();

        return List.of(shaderCachingOption, framesInFlightOption);
    }

    @Override
    public List<OptionGroup> getCategoryGroups()
    {
        return null;
    }
}
