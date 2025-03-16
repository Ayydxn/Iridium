package com.ayydxn.iridium.options.categories;

import com.ayydxn.iridium.options.util.OptionsUtil;
import dev.isxander.yacl3.api.Binding;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.gui.controllers.BooleanController;
import dev.isxander.yacl3.gui.controllers.cycling.EnumController;
import dev.isxander.yacl3.gui.controllers.slider.DoubleSliderController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.ChatVisiblity;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class IridiumChatOptionsCategory extends IridiumOptionCategory
{
    public IridiumChatOptionsCategory()
    {
        super("Chat", Component.translatable("iridium.options.category.chat"));
    }

    @Override
    public @NotNull List<Option<?>> getCategoryOptions()
    {
        Minecraft client = Minecraft.getInstance();
        
        Option<ChatVisiblity> chatVisibilityOption = Option.<ChatVisiblity>createBuilder()
                .name(Component.translatable("iridium.options.chat.chatVisibility"))
                .description(OptionDescription.of(Component.translatable("iridium.options.chat.chatVisibility.description")))
                .binding(Binding.minecraft(client.options.chatVisibility()))
                .customController(option -> new EnumController<>(option, value -> Component.translatable(value.getKey()), ChatVisiblity.values()))
                .build();

        Option<Boolean> chatColorsOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("iridium.options.chat.chatColors"))
                .description(OptionDescription.of(Component.translatable("iridium.options.chat.chatColors.description")))
                .binding(Binding.minecraft(client.options.chatColors()))
                .customController(BooleanController::new)
                .build();

        Option<Boolean> chatLinksOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.chat.links"))
                .description(OptionDescription.of(Component.translatable("iridium.options.chat.links.description")))
                .binding(Binding.minecraft(client.options.chatLinks()))
                .customController(BooleanController::new)
                .build();

        Option<Boolean> propmtOnLinksOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.chat.links.prompt"))
                .description(OptionDescription.of(Component.translatable("iridium.options.chat.links.prompt.description")))
                .binding(Binding.minecraft(client.options.chatLinksPrompt()))
                .customController(BooleanController::new)
                .build();

        Option<Double> chatTextOpacityOption = Option.<Double>createBuilder()
                .name(Component.translatable("options.chat.opacity"))
                .description(OptionDescription.of(Component.translatable("iridium.options.chat.opacity.description")))
                .binding(Binding.minecraft(client.options.chatOpacity()))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, value -> OptionsUtil.getPercentValueText(value * 0.9d + 0.1d)))
                .build();

        Option<Double> textBackgroundOpacityOption = Option.<Double>createBuilder()
                .name(Component.translatable("options.accessibility.text_background_opacity"))
                .description(OptionDescription.of(Component.translatable("iridium.options.chat.textBackgroundOpacity.description")))
                .binding(0.5d, () -> client.options.textBackgroundOpacity().get(), newValue ->
                {
                    client.options.textBackgroundOpacity().set(newValue);
                    client.gui.getChat().rescaleChat();
                })
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, value -> OptionsUtil.getPercentValueText(value * 0.9d + 0.1d)))
                .build();

        Option<Double> chatTextSizeOption = Option.<Double>createBuilder()
                .name(Component.translatable("options.chat.scale"))
                .description(OptionDescription.of(Component.translatable("iridium.options.chat.scale.description")))
                .binding(1.0d, () -> client.options.chatScale().get(), newValue ->
                {
                    client.options.chatScale().set(newValue);
                    client.gui.getChat().rescaleChat();
                })
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, value -> OptionsUtil.getPercentValueText(value * 0.9d + 0.1d)))
                .build();

        Option<Double> chatLineSpacingOption = Option.<Double>createBuilder()
                .name(Component.translatable("options.chat.line_spacing"))
                .description(OptionDescription.of(Component.translatable("iridium.options.chat.lineSpacing.description")))
                .binding(Binding.minecraft(client.options.chatLineSpacing()))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, OptionsUtil::getPercentValueText))
                .build();

        Option<Double> chatDelayOption = Option.<Double>createBuilder()
                .name(Component.translatable("iridium.options.chat.chatDelay"))
                .description(OptionDescription.of(Component.translatable("iridium.options.chat.chatDelay.description")))
                .binding(Binding.minecraft(client.options.chatDelay()))
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
                .binding(1.0d, () -> client.options.chatWidth().get(), newValue ->
                {
                    client.options.chatWidth().set(newValue);
                    client.gui.getChat().rescaleChat();
                })
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, value -> OptionsUtil.getPixelValueText(ChatComponent.getWidth(value))))
                .build();

        Option<Double> focusedHeightOption = Option.<Double>createBuilder()
                .name(Component.translatable("options.chat.height.focused"))
                .description(OptionDescription.of(Component.translatable("iridium.options.chat.focusedHeight.description")))
                .binding(1.0d, () -> client.options.chatHeightFocused().get(), newValue ->
                {
                    client.options.chatHeightFocused().set(newValue);
                    client.gui.getChat().rescaleChat();
                })
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, value -> OptionsUtil.getPixelValueText(ChatComponent.getHeight(value))))
                .build();

        Option<Double> unfocusedHeightOption = Option.<Double>createBuilder()
                .name(Component.translatable("options.chat.height.unfocused"))
                .description(OptionDescription.of(Component.translatable("iridium.options.chat.unfocusedHeight.description")))
                .binding(ChatComponent.defaultUnfocusedPct(), () -> client.options.chatHeightUnfocused().get(), newValue ->
                {
                    client.options.chatHeightUnfocused().set(newValue);
                    client.gui.getChat().rescaleChat();
                })
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, value -> OptionsUtil.getPixelValueText(ChatComponent.getHeight(value))))
                .build();

        Option<Boolean> commandSuggestionsOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.autoSuggestCommands"))
                .description(OptionDescription.of(Component.translatable("iridium.options.chat.commandSuggestions.description")))
                .binding(Binding.minecraft(client.options.autoSuggestions()))
                .customController(BooleanController::new)
                .build();

        Option<Boolean> hideMatchedNamesOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.hideMatchedNames"))
                .description(OptionDescription.of(Component.translatable("iridium.options.chat.hideMatchedNames.description")))
                .binding(Binding.minecraft(client.options.hideMatchedNames()))
                .customController(BooleanController::new)
                .build();

        Option<Boolean> reducedDebugInfoOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.reducedDebugInfo"))
                .description(OptionDescription.of(Component.translatable("iridium.options.chat.reducedDebugInfo.description")))
                .binding(Binding.minecraft(client.options.reducedDebugInfo()))
                .customController(BooleanController::new)
                .build();

        Option<Boolean> onlyShowSecureChatOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.onlyShowSecureChat"))
                .description(OptionDescription.of(Component.translatable("iridium.options.chat.onlyShowSecureChat.description")))
                .binding(Binding.minecraft(client.options.onlyShowSecureChat()))
                .customController(BooleanController::new)
                .build();

        return List.of(chatVisibilityOption, chatColorsOption, chatLinksOption, propmtOnLinksOption, chatTextOpacityOption, textBackgroundOpacityOption,
                chatTextSizeOption, chatLineSpacingOption, chatDelayOption, chatWidthOption, focusedHeightOption, unfocusedHeightOption, commandSuggestionsOption,
                hideMatchedNamesOption, reducedDebugInfoOption, onlyShowSecureChatOption);
    }

    @Override
    public @NotNull List<OptionGroup> getCategoryGroups()
    {
        return Lists.newArrayList();
    }
}
