package me.ayydan.iridium.options.minecraft;

import com.google.common.collect.Lists;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.gui.controllers.BooleanController;
import dev.isxander.yacl3.gui.controllers.slider.DoubleSliderController;
import me.ayydan.iridium.options.util.OptionsUtil;
import net.minecraft.client.gui.screen.option.KeyBindOptionsScreen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IridiumControlsOptions extends IridiumMinecraftOptions
{
    private final List<Option<?>> mouseOptions = new ArrayList<>();
    private final List<Option<?>> keyboardOptions = new ArrayList<>();
    private final List<Option<?>> extraControlsOptions = new ArrayList<>();

    private ConfigCategory controlsOptionsCategory;

    public IridiumControlsOptions()
    {
        super(null);
    }

    @Override
    public void create()
    {
        this.createMouseOptions();
        this.createKeyboardOptions();
        this.createExtraControlsOptions();

        OptionGroup mouseOptionsGroup = OptionGroup.createBuilder()
                .name(Text.translatable("iridium.options.group.mouse"))
                .options(this.mouseOptions)
                .build();

        OptionGroup keyboardOptionsGroup = OptionGroup.createBuilder()
                .name(Text.translatable("iridium.options.group.keyboard"))
                .options(this.keyboardOptions)
                .build();

        OptionGroup extraControlsOptionsGroup = OptionGroup.createBuilder()
                .name(Text.translatable("iridium.options.group.extraControls"))
                .options(this.extraControlsOptions)
                .build();

        this.controlsOptionsCategory = ConfigCategory.createBuilder()
                .name(Text.translatable("iridium.options.category.controls"))
                .groups(Lists.newArrayList(mouseOptionsGroup, keyboardOptionsGroup, extraControlsOptionsGroup))
                .build();
    }

    @Override
    public ConfigCategory getYACLCategory()
    {
        return this.controlsOptionsCategory;
    }

    private void createMouseOptions()
    {
        Option<Double> mouseSensitivityOption = Option.<Double>createBuilder()
                .name(Text.translatable("options.sensitivity"))
                .description(OptionDescription.of(Text.translatable("iridium.options.mouse.mouseSensitivity.description")))
                .binding(Binding.minecraft(this.client.options.getMouseSensitivity()))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, value -> OptionsUtil.getPercentValueText(2.0d * value)))
                .build();

        // FIXME: (Ayydan) Values that aren't 0.01 or 10 are considered invalid. I have no idea why.
        Option<Double> mouseWheelSensitivityOption = Option.<Double>createBuilder()
                .name(Text.translatable("options.mouseWheelSensitivity"))
                .description(OptionDescription.of(Text.translatable("iridium.options.mouse.mouseWheelSensitivity.description")))
                .binding(Binding.minecraft(this.client.options.getMouseWheelSensitivity()))
                .customController(option -> new DoubleSliderController(option, 0.1d, 10.0d, 0.01d, value -> Text.literal(String.format("%.2f", value))))
                .build();

        Option<Boolean> rawInputOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("options.rawMouseInput"))
                .description(OptionDescription.of(Text.translatable("iridium.options.mouse.rawMouseInput.description")))
                .binding(Binding.minecraft(this.client.options.getRawMouseInput()))
                .customController(BooleanController::new)
                .build();

        Option<Boolean> discreteScrollingOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("options.discrete_mouse_scroll"))
                .description(OptionDescription.of(Text.translatable("iridium.options.mouse.discreteMouseScroll.description")))
                .binding(Binding.minecraft(this.client.options.getDiscreteMouseScroll()))
                .customController(BooleanController::new)
                .build();

        Option<Boolean> invertMouseOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("options.invertMouse"))
                .description(OptionDescription.of(Text.translatable("iridium.options.mouse.invertMouse.description")))
                .binding(Binding.minecraft(this.client.options.getInvertYMouse()))
                .customController(BooleanController::new)
                .build();

        Option<Boolean> touchscreenModeOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("options.touchscreen"))
                .description(OptionDescription.of(Text.translatable("iridium.options.mouse.touchscreenMode.description")))
                .binding(Binding.minecraft(this.client.options.getTouchscreen()))
                .customController(BooleanController::new)
                .build();

        Collections.addAll(this.mouseOptions, mouseSensitivityOption, mouseWheelSensitivityOption, rawInputOption, discreteScrollingOption, invertMouseOption,
                touchscreenModeOption);
    }

    private void createKeyboardOptions()
    {
        ButtonOption keyBindsOption = ButtonOption.createBuilder()
                .name(Text.translatable("iridium.options.keyboard.customizeKeybinds"))
                .description(OptionDescription.of(Text.translatable("iridium.options.keyboard.customizeKeybinds.description")))
                .text(Text.literal(""))
                .action((screen, button) -> this.client.setScreen(new KeyBindOptionsScreen(screen, this.client.options)))
                .build();

        Collections.addAll(this.keyboardOptions, keyBindsOption);
    }

    private void createExtraControlsOptions()
    {
        Option<Boolean> toggeableSneakOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("key.sneak"))
                .description(OptionDescription.of(Text.translatable("iridium.options.extraControls.sneak.description")))
                .binding(Binding.minecraft(this.client.options.getToggleableSneak()))
                .customController(option -> new BooleanController(option, value -> value ? Text.translatable("options.key.toggle") : Text.translatable("options.key.hold"), false))
                .build();

        Option<Boolean> toggeableSprintOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("key.sprint"))
                .description(OptionDescription.of(Text.translatable("iridium.options.extraControls.sprint.description")))
                .binding(Binding.minecraft(this.client.options.getToggleableSprint()))
                .customController(option -> new BooleanController(option, value -> value ? Text.translatable("options.key.toggle") : Text.translatable("options.key.hold"), false))
                .build();

        Option<Boolean> autoJumpOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("options.autoJump"))
                .description(OptionDescription.of(Text.translatable("iridium.options.extraControls.autoJump.description")))
                .binding(Binding.minecraft(this.client.options.getAutoJump()))
                .customController(BooleanController::new)
                .build();

        Option<Boolean> operatorItemsTabOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("options.operatorItemsTab"))
                .description(OptionDescription.of(Text.translatable("iridium.options.extraControls.operatorItemsTab.description")))
                .binding(Binding.minecraft(this.client.options.getOperatorItemsTab()))
                .customController(BooleanController::new)
                .build();

        Collections.addAll(this.extraControlsOptions, toggeableSneakOption, toggeableSprintOption, autoJumpOption, operatorItemsTabOption);
    }
}
