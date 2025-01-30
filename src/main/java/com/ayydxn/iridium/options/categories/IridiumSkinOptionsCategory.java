package com.ayydxn.iridium.options.categories;

import dev.isxander.yacl3.api.Binding;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.gui.controllers.BooleanController;
import dev.isxander.yacl3.gui.controllers.cycling.EnumController;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;

import java.util.ArrayList;
import java.util.List;

public class IridiumSkinOptionsCategory extends IridiumOptionCategory
{
    public IridiumSkinOptionsCategory()
    {
        super("Skin Customization", Component.translatable("options.skinCustomisation.title"));
    }

    @Override
    public List<Option<?>> getCategoryOptions()
    {
        List<Option<?>> skinCategoryOptions = new ArrayList<>();
        Minecraft client = Minecraft.getInstance();

        for (PlayerModelPart playerModelPart : PlayerModelPart.values())
        {
            Option<Boolean> playerModelPartOption = Option.<Boolean>createBuilder()
                    .name(playerModelPart.getName())
                    .description(OptionDescription.of(this.getPlayerModelPartDescription(playerModelPart)))
                    .binding(true, () -> client.options.isModelPartEnabled(playerModelPart), newValue -> client.options.setModelPart(playerModelPart, newValue))
                    .customController(BooleanController::new)
                    .build();

            skinCategoryOptions.add(playerModelPartOption);
        }

        Option<HumanoidArm> mainHandOption = Option.<HumanoidArm>createBuilder()
                .name(Component.translatable("options.mainHand"))
                .description(OptionDescription.of(Component.translatable("iridium.options.skinCustomisation.mainHand.description")))
                .binding(Binding.minecraft(client.options.mainHand()))
                .customController(option -> new EnumController<>(option, arm ->
                {
                    return switch (arm)
                    {
                        case RIGHT -> Component.translatable("options.mainHand.right");
                        case LEFT -> Component.translatable("options.mainHand.left");
                    };
                }, HumanoidArm.values()))
                .build();

        skinCategoryOptions.add(mainHandOption);

        return skinCategoryOptions;
    }

    @Override
    public List<OptionGroup> getCategoryGroups()
    {
        return null;
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
