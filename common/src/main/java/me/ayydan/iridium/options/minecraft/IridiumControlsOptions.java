package me.ayydan.iridium.options.minecraft;

import com.google.common.collect.Lists;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.gui.controllers.BooleanController;
import dev.isxander.yacl3.gui.controllers.slider.DoubleSliderController;
import me.ayydan.iridium.options.util.OptionsUtil;
import net.minecraft.client.gui.screens.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;

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
                .name(Component.translatable("iridium.options.group.mouse"))
                .options(this.mouseOptions)
                .build();

        OptionGroup keyboardOptionsGroup = OptionGroup.createBuilder()
                .name(Component.translatable("iridium.options.group.keyboard"))
                .options(this.keyboardOptions)
                .build();

        OptionGroup extraControlsOptionsGroup = OptionGroup.createBuilder()
                .name(Component.translatable("iridium.options.group.extraControls"))
                .options(this.extraControlsOptions)
                .build();

        this.controlsOptionsCategory = ConfigCategory.createBuilder()
                .name(Component.translatable("iridium.options.category.controls"))
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
                .name(Component.translatable("options.sensitivity"))
                .description(OptionDescription.of(Component.translatable("iridium.options.mouse.mouseSensitivity.description")))
                .binding(Binding.minecraft(this.client.options.sensitivity()))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, value -> OptionsUtil.getPercentValueText(2.0d * value)))
                .build();

        // FIXME: (Ayydan) Values that aren't 0.01 or 10 are considered invalid. I have no idea why.
        Option<Double> mouseWheelSensitivityOption = Option.<Double>createBuilder()
                .name(Component.translatable("options.mouseWheelSensitivity"))
                .description(OptionDescription.of(Component.translatable("iridium.options.mouse.mouseWheelSensitivity.description")))
                .binding(Binding.minecraft(this.client.options.mouseWheelSensitivity()))
                .customController(option -> new DoubleSliderController(option, 0.1d, 10.0d, 0.01d, value -> Component.literal(String.format("%.2f", value))))
                .build();

        Option<Boolean> rawInputOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.rawMouseInput"))
                .description(OptionDescription.of(Component.translatable("iridium.options.mouse.rawMouseInput.description")))
                .binding(Binding.minecraft(this.client.options.rawMouseInput()))
                .customController(BooleanController::new)
                .build();

        Option<Boolean> discreteScrollingOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.discrete_mouse_scroll"))
                .description(OptionDescription.of(Component.translatable("iridium.options.mouse.discreteMouseScroll.description")))
                .binding(Binding.minecraft(this.client.options.discreteMouseScroll()))
                .customController(BooleanController::new)
                .build();

        Option<Boolean> invertMouseOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.invertMouse"))
                .description(OptionDescription.of(Component.translatable("iridium.options.mouse.invertMouse.description")))
                .binding(Binding.minecraft(this.client.options.invertYMouse()))
                .customController(BooleanController::new)
                .build();

        Option<Boolean> touchscreenModeOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.touchscreen"))
                .description(OptionDescription.of(Component.translatable("iridium.options.mouse.touchscreenMode.description")))
                .binding(Binding.minecraft(this.client.options.touchscreen()))
                .customController(BooleanController::new)
                .build();

        Collections.addAll(this.mouseOptions, mouseSensitivityOption, mouseWheelSensitivityOption, rawInputOption, discreteScrollingOption, invertMouseOption,
                touchscreenModeOption);
    }

    private void createKeyboardOptions()
    {
        ButtonOption keyBindsOption = ButtonOption.createBuilder()
                .name(Component.translatable("iridium.options.keyboard.customizeKeybinds"))
                .description(OptionDescription.of(Component.translatable("iridium.options.keyboard.customizeKeybinds.description")))
                .text(Component.literal(""))
                .action((screen, button) -> this.client.setScreen(new KeyBindsScreen(screen, this.client.options)))
                .build();

        Collections.addAll(this.keyboardOptions, keyBindsOption);
    }

    private void createExtraControlsOptions()
    {
        Option<Boolean> toggeableSneakOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("key.sneak"))
                .description(OptionDescription.of(Component.translatable("iridium.options.extraControls.sneak.description")))
                .binding(Binding.minecraft(this.client.options.toggleCrouch()))
                .customController(option -> new BooleanController(option, value -> value ? Component.translatable("options.key.toggle") : Component.translatable("options.key.hold"), false))
                .build();

        Option<Boolean> toggeableSprintOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("key.sprint"))
                .description(OptionDescription.of(Component.translatable("iridium.options.extraControls.sprint.description")))
                .binding(Binding.minecraft(this.client.options.toggleSprint()))
                .customController(option -> new BooleanController(option, value -> value ? Component.translatable("options.key.toggle") : Component.translatable("options.key.hold"), false))
                .build();

        Option<Boolean> autoJumpOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.autoJump"))
                .description(OptionDescription.of(Component.translatable("iridium.options.extraControls.autoJump.description")))
                .binding(Binding.minecraft(this.client.options.autoJump()))
                .customController(BooleanController::new)
                .build();

        Option<Boolean> operatorItemsTabOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.operatorItemsTab"))
                .description(OptionDescription.of(Component.translatable("iridium.options.extraControls.operatorItemsTab.description")))
                .binding(Binding.minecraft(this.client.options.operatorItemsTab()))
                .customController(BooleanController::new)
                .build();

        Collections.addAll(this.extraControlsOptions, toggeableSneakOption, toggeableSprintOption, autoJumpOption, operatorItemsTabOption);
    }
}
