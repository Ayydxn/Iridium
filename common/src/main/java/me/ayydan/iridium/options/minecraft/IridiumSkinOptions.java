package me.ayydan.iridium.options.minecraft;

import dev.isxander.yacl3.api.Binding;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.gui.controllers.BooleanController;
import dev.isxander.yacl3.gui.controllers.cycling.EnumController;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;

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
                .name(Text.translatable("options.skinCustomisation.title"))
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
                    .name(playerModelPart.getOptionName())
                    .description(OptionDescription.of(this.getPlayerModelPartDescription(playerModelPart)))
                    .binding(true, () -> this.client.options.isPlayerModelPartEnabled(playerModelPart), newValue -> this.client.options.togglePlayerModelPart(playerModelPart, newValue))
                    .customController(BooleanController::new)
                    .build();

            this.skinOptions.add(playerModelPartOption);
        }

        Option<Arm> mainHandOption = Option.<Arm>createBuilder()
                .name(Text.translatable("options.mainHand"))
                .description(OptionDescription.of(Text.translatable("iridium.options.skinCustomisation.mainHand.description")))
                .binding(Binding.minecraft(this.client.options.getMainArm()))
                .customController(option -> new EnumController<>(option, arm ->
                {
                    return switch (arm)
                    {
                        case RIGHT -> Text.translatable("options.mainHand.right");
                        case LEFT -> Text.translatable("options.mainHand.left");
                    };
                }, Arm.values()))
                .build();

        this.skinOptions.add(mainHandOption);
    }

    private Text getPlayerModelPartDescription(PlayerModelPart playerModelPart)
    {
        return switch (playerModelPart)
        {
            case CAPE -> Text.translatable("iridium.options.skinCustomisation.cape.description");
            case JACKET -> Text.translatable("iridium.options.skinCustomisation.jacket.description");
            case LEFT_SLEEVE -> Text.translatable("iridium.options.skinCustomisation.leftSleeve.description");
            case RIGHT_SLEEVE -> Text.translatable("iridium.options.skinCustomisation.rightSleeve.description");
            case LEFT_PANTS_LEG -> Text.translatable("iridium.options.skinCustomisation.leftPantsLeg.description");
            case RIGHT_PANTS_LEG -> Text.translatable("iridium.options.skinCustomisation.rightPantsLeg.description");
            case HAT -> Text.translatable("iridium.options.skinCustomisation.hat.description");
            default -> throw new IllegalArgumentException("Failed to get description for player model part: " + playerModelPart.getName());
        };
    }
}
