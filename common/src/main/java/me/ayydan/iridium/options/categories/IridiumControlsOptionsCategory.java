package me.ayydan.iridium.options.categories;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.gui.controllers.BooleanController;
import dev.isxander.yacl3.gui.controllers.slider.DoubleSliderController;
import me.ayydan.iridium.options.util.OptionsUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;
import org.apache.commons.compress.utils.Lists;

import java.util.Collections;
import java.util.List;

public class IridiumControlsOptionsCategory extends IridiumOptionCategory
{
    private List<Option<?>> mouseOptions;
    private List<Option<?>> keyboardOptions;
    private List<Option<?>> extraControlsOptions;

    public IridiumControlsOptionsCategory()
    {
        super(Component.translatable("iridium.options.category.controls"));
    }

    @Override
    public List<Option<?>> getCategoryOptions()
    {
        return null;
    }

    @Override
    public List<OptionGroup> getCategoryGroups()
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

        return List.of(mouseOptionsGroup, keyboardOptionsGroup, extraControlsOptionsGroup);
    }

    private void createMouseOptions()
    {
        this.mouseOptions = Lists.newArrayList();

        Minecraft client = Minecraft.getInstance();

        Option<Double> mouseSensitivityOption = Option.<Double>createBuilder()
                .name(Component.translatable("options.sensitivity"))
                .description(OptionDescription.of(Component.translatable("iridium.options.mouse.mouseSensitivity.description")))
                .binding(Binding.minecraft(client.options.sensitivity()))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, value -> OptionsUtil.getPercentValueText(2.0d * value)))
                .build();

        // FIXME: (Ayydan) Values that aren't 0.01 or 10 are considered invalid. I have no idea why.
        Option<Double> mouseWheelSensitivityOption = Option.<Double>createBuilder()
                .name(Component.translatable("options.mouseWheelSensitivity"))
                .description(OptionDescription.of(Component.translatable("iridium.options.mouse.mouseWheelSensitivity.description")))
                .binding(Binding.minecraft(client.options.mouseWheelSensitivity()))
                .customController(option -> new DoubleSliderController(option, 0.1d, 10.0d, 0.01d, value -> Component.literal(String.format("%.2f", value))))
                .build();

        Option<Boolean> rawInputOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.rawMouseInput"))
                .description(OptionDescription.of(Component.translatable("iridium.options.mouse.rawMouseInput.description")))
                .binding(Binding.minecraft(client.options.rawMouseInput()))
                .customController(BooleanController::new)
                .build();

        Option<Boolean> discreteScrollingOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.discrete_mouse_scroll"))
                .description(OptionDescription.of(Component.translatable("iridium.options.mouse.discreteMouseScroll.description")))
                .binding(Binding.minecraft(client.options.discreteMouseScroll()))
                .customController(BooleanController::new)
                .build();

        Option<Boolean> invertMouseOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.invertMouse"))
                .description(OptionDescription.of(Component.translatable("iridium.options.mouse.invertMouse.description")))
                .binding(Binding.minecraft(client.options.invertYMouse()))
                .customController(BooleanController::new)
                .build();

        Option<Boolean> touchscreenModeOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.touchscreen"))
                .description(OptionDescription.of(Component.translatable("iridium.options.mouse.touchscreenMode.description")))
                .binding(Binding.minecraft(client.options.touchscreen()))
                .customController(BooleanController::new)
                .build();

        Collections.addAll(this.mouseOptions, mouseSensitivityOption, mouseWheelSensitivityOption, rawInputOption, discreteScrollingOption, invertMouseOption,
                touchscreenModeOption);
    }

    private void createKeyboardOptions()
    {
        this.keyboardOptions = Lists.newArrayList();

        Minecraft client = Minecraft.getInstance();

        ButtonOption keyBindsOption = ButtonOption.createBuilder()
                .name(Component.translatable("iridium.options.keyboard.customizeKeybinds"))
                .description(OptionDescription.of(Component.translatable("iridium.options.keyboard.customizeKeybinds.description")))
                .text(Component.literal(""))
                .action((screen, button) -> client.setScreen(new KeyBindsScreen(screen, client.options)))
                .build();

        Collections.addAll(this.keyboardOptions, keyBindsOption);
    }

    private void createExtraControlsOptions()
    {
        this.extraControlsOptions = Lists.newArrayList();
        
        Minecraft client = Minecraft.getInstance();

        Option<Boolean> toggeableSneakOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("key.sneak"))
                .description(OptionDescription.of(Component.translatable("iridium.options.extraControls.sneak.description")))
                .binding(Binding.minecraft(client.options.toggleCrouch()))
                .customController(option -> new BooleanController(option, value -> value ? Component.translatable("options.key.toggle") : Component.translatable("options.key.hold"), false))
                .build();

        Option<Boolean> toggeableSprintOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("key.sprint"))
                .description(OptionDescription.of(Component.translatable("iridium.options.extraControls.sprint.description")))
                .binding(Binding.minecraft(client.options.toggleSprint()))
                .customController(option -> new BooleanController(option, value -> value ? Component.translatable("options.key.toggle") : Component.translatable("options.key.hold"), false))
                .build();

        Option<Boolean> autoJumpOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.autoJump"))
                .description(OptionDescription.of(Component.translatable("iridium.options.extraControls.autoJump.description")))
                .binding(Binding.minecraft(client.options.autoJump()))
                .customController(BooleanController::new)
                .build();

        Option<Boolean> operatorItemsTabOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.operatorItemsTab"))
                .description(OptionDescription.of(Component.translatable("iridium.options.extraControls.operatorItemsTab.description")))
                .binding(Binding.minecraft(client.options.operatorItemsTab()))
                .customController(BooleanController::new)
                .build();

        Collections.addAll(this.extraControlsOptions, toggeableSneakOption, toggeableSprintOption, autoJumpOption, operatorItemsTabOption);
    }
}
