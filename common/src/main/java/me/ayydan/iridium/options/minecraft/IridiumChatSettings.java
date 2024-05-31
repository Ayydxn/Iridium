package me.ayydan.iridium.options.minecraft;

import dev.isxander.yacl3.api.Binding;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.gui.controllers.BooleanController;
import dev.isxander.yacl3.gui.controllers.cycling.EnumController;
import dev.isxander.yacl3.gui.controllers.slider.DoubleSliderController;
import me.ayydan.iridium.options.util.OptionsUtil;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.ChatVisiblity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IridiumChatSettings extends IridiumMinecraftOptions
{
    private final List<Option<?>> chatOptions = new ArrayList<>();

    private ConfigCategory chatOptionsCategory;

    public IridiumChatSettings()
    {
        super(null);
    }

    @Override
    public void create()
    {
        this.createChatOptions();

        this.chatOptionsCategory = ConfigCategory.createBuilder()
                .name(Component.translatable("iridium.options.category.chat"))
                .options(this.chatOptions)
                .build();
    }

    @Override
    public ConfigCategory getYACLCategory()
    {
        return this.chatOptionsCategory;
    }

    private void createChatOptions()
    {
        Option<ChatVisiblity> chatVisibilityOption = Option.<ChatVisiblity>createBuilder()
                .name(Component.translatable("iridium.options.chat.chatVisibility"))
                .description(OptionDescription.of(Component.translatable("iridium.options.chat.chatVisibility.description")))
                .binding(Binding.minecraft(this.client.options.chatVisibility()))
                .customController(option -> new EnumController<>(option, value -> Component.translatable(value.getKey()), ChatVisiblity.values()))
                .build();

        Option<Boolean> chatColorsOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("iridium.options.chat.chatColors"))
                .description(OptionDescription.of(Component.translatable("iridium.options.chat.chatColors.description")))
                .binding(Binding.minecraft(this.client.options.chatColors()))
                .customController(BooleanController::new)
                .build();

        Option<Boolean> chatLinksOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.chat.links"))
                .description(OptionDescription.of(Component.translatable("iridium.options.chat.links.description")))
                .binding(Binding.minecraft(this.client.options.chatLinks()))
                .customController(BooleanController::new)
                .build();

        Option<Boolean> propmtOnLinksOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.chat.links.prompt"))
                .description(OptionDescription.of(Component.translatable("iridium.options.chat.links.prompt.description")))
                .binding(Binding.minecraft(this.client.options.chatLinksPrompt()))
                .customController(BooleanController::new)
                .build();

        Option<Double> chatTextOpacityOption = Option.<Double>createBuilder()
                .name(Component.translatable("options.chat.opacity"))
                .description(OptionDescription.of(Component.translatable("iridium.options.chat.opacity.description")))
                .binding(Binding.minecraft(this.client.options.chatOpacity()))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, value -> OptionsUtil.getPercentValueText(value * 0.9d + 0.1d)))
                .build();

        Option<Double> textBackgroundOpacityOption = Option.<Double>createBuilder()
                .name(Component.translatable("options.accessibility.text_background_opacity"))
                .description(OptionDescription.of(Component.translatable("iridium.options.chat.textBackgroundOpacity.description")))
                .binding(0.5d, () -> this.client.options.textBackgroundOpacity().get(), newValue ->
                {
                    this.client.options.textBackgroundOpacity().set(newValue);
                    this.client.gui.getChat().rescaleChat();
                })
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, value -> OptionsUtil.getPercentValueText(value * 0.9d + 0.1d)))
                .build();

        Option<Double> chatTextSizeOption = Option.<Double>createBuilder()
                .name(Component.translatable("options.chat.scale"))
                .description(OptionDescription.of(Component.translatable("iridium.options.chat.scale.description")))
                .binding(1.0d, () -> this.client.options.chatScale().get(), newValue ->
                {
                    this.client.options.chatScale().set(newValue);
                    this.client.gui.getChat().rescaleChat();
                })
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, value -> OptionsUtil.getPercentValueText(value * 0.9d + 0.1d)))
                .build();

        Option<Double> chatLineSpacingOption = Option.<Double>createBuilder()
                .name(Component.translatable("options.chat.line_spacing"))
                .description(OptionDescription.of(Component.translatable("iridium.options.chat.lineSpacing.description")))
                .binding(Binding.minecraft(this.client.options.chatLineSpacing()))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, OptionsUtil::getPercentValueText))
                .build();

        Option<Double> chatDelayOption = Option.<Double>createBuilder()
                .name(Component.translatable("iridium.options.chat.chatDelay"))
                .description(OptionDescription.of(Component.translatable("iridium.options.chat.chatDelay.description")))
                .binding(Binding.minecraft(this.client.options.chatDelay()))
                .customController(option -> new DoubleSliderController(option, 0.0d, 6.0d, 0.01d, value ->
                {
                    if (value <= 0.0d)
                        return Component.translatable("iridium.options.chat.chatDelay.none");

                    return Component.translatable("iridium.options.chat.chatDelay.seconds", String.format("%.1f", value));
                }))
                .build();

        Option<Double> chatWidthOption = Option.<Double>createBuilder()
                .name(Component.translatable("options.chat.width"))
                .description(OptionDescription.of(Component.translatable("iridium.options.chat.width.description")))
                .binding(1.0d, () -> this.client.options.chatWidth().get(), newValue ->
                {
                    this.client.options.chatWidth().set(newValue);
                    this.client.gui.getChat().rescaleChat();
                })
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, value -> OptionsUtil.getPixelValueText(ChatComponent.getWidth(value))))
                .build();

        Option<Double> focusedHeightOption = Option.<Double>createBuilder()
                .name(Component.translatable("options.chat.height.focused"))
                .description(OptionDescription.of(Component.translatable("iridium.options.chat.focusedHeight.description")))
                .binding(1.0d, () -> this.client.options.chatHeightFocused().get(), newValue ->
                {
                    this.client.options.chatHeightFocused().set(newValue);
                    this.client.gui.getChat().rescaleChat();
                })
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, value -> OptionsUtil.getPixelValueText(ChatComponent.getHeight(value))))
                .build();

        Option<Double> unfocusedHeightOption = Option.<Double>createBuilder()
                .name(Component.translatable("options.chat.height.unfocused"))
                .description(OptionDescription.of(Component.translatable("iridium.options.chat.unfocusedHeight.description")))
                .binding(ChatComponent.defaultUnfocusedPct(), () -> this.client.options.chatHeightUnfocused().get(), newValue ->
                {
                    this.client.options.chatHeightUnfocused().set(newValue);
                    this.client.gui.getChat().rescaleChat();
                })
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, value -> OptionsUtil.getPixelValueText(ChatComponent.getHeight(value))))
                .build();

        Option<Boolean> commandSuggestionsOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.autoSuggestCommands"))
                .description(OptionDescription.of(Component.translatable("iridium.options.chat.commandSuggestions.description")))
                .binding(Binding.minecraft(this.client.options.autoSuggestions()))
                .customController(BooleanController::new)
                .build();

        Option<Boolean> hideMatchedNamesOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.hideMatchedNames"))
                .description(OptionDescription.of(Component.translatable("iridium.options.chat.hideMatchedNames.description")))
                .binding(Binding.minecraft(this.client.options.hideMatchedNames()))
                .customController(BooleanController::new)
                .build();

        Option<Boolean> reducedDebugInfoOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.reducedDebugInfo"))
                .description(OptionDescription.of(Component.translatable("iridium.options.chat.reducedDebugInfo.description")))
                .binding(Binding.minecraft(this.client.options.reducedDebugInfo()))
                .customController(BooleanController::new)
                .build();

        Option<Boolean> onlyShowSecureChatOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.onlyShowSecureChat"))
                .description(OptionDescription.of(Component.translatable("iridium.options.chat.onlyShowSecureChat.description")))
                .binding(Binding.minecraft(this.client.options.onlyShowSecureChat()))
                .customController(BooleanController::new)
                .build();

        Collections.addAll(this.chatOptions, chatVisibilityOption, chatColorsOption, chatLinksOption, propmtOnLinksOption, chatTextOpacityOption, textBackgroundOpacityOption,
                chatTextSizeOption, chatLineSpacingOption, chatDelayOption, chatWidthOption, focusedHeightOption, unfocusedHeightOption, commandSuggestionsOption,
                hideMatchedNamesOption, reducedDebugInfoOption, onlyShowSecureChatOption);
    }
}
