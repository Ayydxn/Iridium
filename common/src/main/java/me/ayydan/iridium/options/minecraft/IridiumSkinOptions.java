package me.ayydan.iridium.options.minecraft;

import dev.isxander.yacl3.api.Binding;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.gui.controllers.BooleanController;
import dev.isxander.yacl3.gui.controllers.cycling.EnumController;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;

import java.util.ArrayList;
import java.util.List;

public class IridiumSkinOptions extends IridiumMinecraftOptions
{
    private final List<Option<?>> skinOptions = new ArrayList<>();

    private ConfigCategory skinOptionsCategory;

    public IridiumSkinOptions()
    {
        super(null);
    }

    @Override
    public void create()
    {
        this.createSkinOptions();

        this.skinOptionsCategory = ConfigCategory.createBuilder()
                .name(Component.translatable("options.skinCustomisation.title"))
                .options(this.skinOptions)
                .build();
    }

    @Override
    public ConfigCategory getYACLCategory()
    {
        return this.skinOptionsCategory;
    }

    private void createSkinOptions()
    {
        for (PlayerModelPart playerModelPart : PlayerModelPart.values())
        {
            Option<Boolean> playerModelPartOption = Option.<Boolean>createBuilder()
                    .name(playerModelPart.getName())
                    .description(OptionDescription.of(this.getPlayerModelPartDescription(playerModelPart)))
                    .binding(true, () -> this.client.options.isModelPartEnabled(playerModelPart), newValue -> this.client.options.toggleModelPart(playerModelPart, newValue))
                    .customController(BooleanController::new)
                    .build();

            this.skinOptions.add(playerModelPartOption);
        }

        Option<HumanoidArm> mainHandOption = Option.<HumanoidArm>createBuilder()
                .name(Component.translatable("options.mainHand"))
                .description(OptionDescription.of(Component.translatable("iridium.options.skinCustomisation.mainHand.description")))
                .binding(Binding.minecraft(this.client.options.mainHand()))
                .customController(option -> new EnumController<>(option, arm ->
                {
                    return switch (arm)
                    {
                        case RIGHT -> Component.translatable("options.mainHand.right");
                        case LEFT -> Component.translatable("options.mainHand.left");
                    };
                }, HumanoidArm.values()))
                .build();

        this.skinOptions.add(mainHandOption);
    }

    private Component getPlayerModelPartDescription(PlayerModelPart playerModelPart)
    {
        return switch (playerModelPart)
        {
            case CAPE -> Component.translatable("iridium.options.skinCustomisation.cape.description");
            case JACKET -> Component.translatable("iridium.options.skinCustomisation.jacket.description");
            case LEFT_SLEEVE -> Component.translatable("iridium.options.skinCustomisation.leftSleeve.description");
            case RIGHT_SLEEVE -> Component.translatable("iridium.options.skinCustomisation.rightSleeve.description");
            case LEFT_PANTS_LEG -> Component.translatable("iridium.options.skinCustomisation.leftPantsLeg.description");
            case RIGHT_PANTS_LEG -> Component.translatable("iridium.options.skinCustomisation.rightPantsLeg.description");
            case HAT -> Component.translatable("iridium.options.skinCustomisation.hat.description");
            default -> throw new IllegalArgumentException("Failed to get description for player model part: " + playerModelPart.getName());
        };
    }
}
