package me.ayydan.iridium.options.minecraft;

import dev.isxander.yacl3.api.Binding;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.gui.controllers.BooleanController;
import dev.isxander.yacl3.gui.controllers.cycling.EnumController;
import dev.isxander.yacl3.gui.controllers.slider.DoubleSliderController;
import me.ayydan.iridium.options.util.OptionsUtil;
import net.minecraft.client.gui.hud.chat.ChatHud;
import net.minecraft.client.option.ChatVisibility;
import net.minecraft.client.option.NarratorMode;
import net.minecraft.text.Text;

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
                .name(Text.translatable("iridium.options.category.chat"))
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
        Option<ChatVisibility> chatVisibilityOption = Option.<ChatVisibility>createBuilder()
                .name(Text.translatable("iridium.options.chat.chatVisibility"))
                .description(OptionDescription.of(Text.translatable("iridium.options.chat.chatVisibility.description")))
                .binding(Binding.minecraft(this.client.options.getChatVisibility()))
                .customController(option -> new EnumController<>(option, value -> Text.translatable(value.getTranslationKey()), ChatVisibility.values()))
                .build();

        Option<Boolean> chatColorsOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("iridium.options.chat.chatColors"))
                .description(OptionDescription.of(Text.translatable("iridium.options.chat.chatColors.description")))
                .binding(Binding.minecraft(this.client.options.getChatColors()))
                .customController(BooleanController::new)
                .build();

        Option<Boolean> chatLinksOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("options.chat.links"))
                .description(OptionDescription.of(Text.translatable("iridium.options.chat.links.description")))
                .binding(Binding.minecraft(this.client.options.getChatLinks()))
                .customController(BooleanController::new)
                .build();

        Option<Boolean> propmtOnLinksOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("options.chat.links.prompt"))
                .description(OptionDescription.of(Text.translatable("iridium.options.chat.links.prompt.description")))
                .binding(Binding.minecraft(this.client.options.getChatLinksPrompt()))
                .customController(BooleanController::new)
                .build();

        Option<Double> chatTextOpacityOption = Option.<Double>createBuilder()
                .name(Text.translatable("options.chat.opacity"))
                .description(OptionDescription.of(Text.translatable("iridium.options.chat.opacity.description")))
                .binding(Binding.minecraft(this.client.options.getChatOpacity()))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, value -> OptionsUtil.getPercentValueText(value * 0.9d + 0.1d)))
                .build();

        Option<Double> textBackgroundOpacityOption = Option.<Double>createBuilder()
                .name(Text.translatable("options.accessibility.text_background_opacity"))
                .description(OptionDescription.of(Text.translatable("iridium.options.chat.textBackgroundOpacity.description")))
                .binding(0.5d, () -> this.client.options.getTextBackgroundOpacity().get(), newValue ->
                {
                    this.client.options.getTextBackgroundOpacity().set(newValue);
                    this.client.inGameHud.getChatHud().reset();
                })
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, value -> OptionsUtil.getPercentValueText(value * 0.9d + 0.1d)))
                .build();

        Option<Double> chatTextSizeOption = Option.<Double>createBuilder()
                .name(Text.translatable("options.chat.scale"))
                .description(OptionDescription.of(Text.translatable("iridium.options.chat.scale.description")))
                .binding(1.0d, () -> this.client.options.getChatScale().get(), newValue ->
                {
                    this.client.options.getChatScale().set(newValue);
                    this.client.inGameHud.getChatHud().reset();
                })
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, value -> OptionsUtil.getPercentValueText(value * 0.9d + 0.1d)))
                .build();

        Option<Double> chatLineSpacingOption = Option.<Double>createBuilder()
                .name(Text.translatable("options.chat.line_spacing"))
                .description(OptionDescription.of(Text.translatable("iridium.options.chat.lineSpacing.description")))
                .binding(Binding.minecraft(this.client.options.getChatLineSpacing()))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, OptionsUtil::getPercentValueText))
                .build();

        Option<Double> chatDelayOption = Option.<Double>createBuilder()
                .name(Text.translatable("iridium.options.chat.chatDelay"))
                .description(OptionDescription.of(Text.translatable("iridium.options.chat.chatDelay.description")))
                .binding(Binding.minecraft(this.client.options.getChatDelay()))
                .customController(option -> new DoubleSliderController(option, 0.0d, 6.0d, 0.01d, value ->
                {
                    if (value <= 0.0d)
                        return Text.translatable("iridium.options.chat.chatDelay.none");

                    return Text.translatable("iridium.options.chat.chatDelay.seconds", String.format("%.1f", value));
                }))
                .build();

        Option<Double> chatWidthOption = Option.<Double>createBuilder()
                .name(Text.translatable("options.chat.width"))
                .description(OptionDescription.of(Text.translatable("iridium.options.chat.width.description")))
                .binding(1.0d, () -> this.client.options.getChatWidth().get(), newValue ->
                {
                    this.client.options.getChatWidth().set(newValue);
                    this.client.inGameHud.getChatHud().reset();
                })
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, value -> OptionsUtil.getPixelValueText(ChatHud.getWidth(value))))
                .build();

        Option<Double> focusedHeightOption = Option.<Double>createBuilder()
                .name(Text.translatable("options.chat.height.focused"))
                .description(OptionDescription.of(Text.translatable("iridium.options.chat.focusedHeight.description")))
                .binding(1.0d, () -> this.client.options.getFocusedChatHeight().get(), newValue ->
                {
                    this.client.options.getFocusedChatHeight().set(newValue);
                    this.client.inGameHud.getChatHud().reset();
                })
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, value -> OptionsUtil.getPixelValueText(ChatHud.getHeight(value))))
                .build();

        Option<Double> unfocusedHeightOption = Option.<Double>createBuilder()
                .name(Text.translatable("options.chat.height.unfocused"))
                .description(OptionDescription.of(Text.translatable("iridium.options.chat.unfocusedHeight.description")))
                .binding(ChatHud.getDefaultUnfocusedHeight(), () -> this.client.options.getUnfocusedChatHeight().get(), newValue ->
                {
                    this.client.options.getUnfocusedChatHeight().set(newValue);
                    this.client.inGameHud.getChatHud().reset();
                })
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, value -> OptionsUtil.getPixelValueText(ChatHud.getHeight(value))))
                .build();

        Option<Boolean> commandSuggestionsOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("options.autoSuggestCommands"))
                .description(OptionDescription.of(Text.translatable("iridium.options.chat.commandSuggestions.description")))
                .binding(Binding.minecraft(this.client.options.getCommandSuggestions()))
                .customController(BooleanController::new)
                .build();

        Option<Boolean> hideMatchedNamesOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("options.hideMatchedNames"))
                .description(OptionDescription.of(Text.translatable("iridium.options.chat.hideMatchedNames.description")))
                .binding(Binding.minecraft(this.client.options.getHideMatchedNames()))
                .customController(BooleanController::new)
                .build();

        Option<Boolean> reducedDebugInfoOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("options.reducedDebugInfo"))
                .description(OptionDescription.of(Text.translatable("iridium.options.chat.reducedDebugInfo.description")))
                .binding(Binding.minecraft(this.client.options.getReducedDebugInfo()))
                .customController(BooleanController::new)
                .build();

        Option<Boolean> onlyShowSecureChatOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("options.onlyShowSecureChat"))
                .description(OptionDescription.of(Text.translatable("iridium.options.chat.onlyShowSecureChat.description")))
                .binding(Binding.minecraft(this.client.options.getOnlyShowSecureChat()))
                .customController(BooleanController::new)
                .build();

        Collections.addAll(this.chatOptions, chatVisibilityOption, chatColorsOption, chatLinksOption, propmtOnLinksOption, chatTextOpacityOption, textBackgroundOpacityOption,
                chatTextSizeOption, chatLineSpacingOption, chatDelayOption, chatWidthOption, focusedHeightOption, unfocusedHeightOption, commandSuggestionsOption,
                hideMatchedNamesOption, reducedDebugInfoOption, onlyShowSecureChatOption);
    }
}
