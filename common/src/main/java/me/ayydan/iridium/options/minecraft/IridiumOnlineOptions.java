package me.ayydan.iridium.options.minecraft;

import dev.isxander.yacl3.api.Binding;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.gui.controllers.BooleanController;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IridiumOnlineOptions extends IridiumMinecraftOptions
{
    private final List<Option<?>> onlineOptions = new ArrayList<>();

    private ConfigCategory onlineOptionsCategory;

    public IridiumOnlineOptions()
    {
        super(null);
    }

    @Override
    public void create()
    {
        this.createOnlineOptions();

        this.onlineOptionsCategory = ConfigCategory.createBuilder()
                .name(Component.translatable("iridium.options.category.online"))
                .options(this.onlineOptions)
                .build();
    }

    @Override
    public ConfigCategory getYACLCategory()
    {
        return this.onlineOptionsCategory;
    }

    private void createOnlineOptions()
    {
        Option<Boolean> realmsNotificationsOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.realmsNotifications"))
                .description(OptionDescription.of(Component.translatable("iridium.options.online.realmsNotifications.description")))
                .binding(Binding.minecraft(this.client.options.realmsNotifications()))
                .customController(BooleanController::new)
                .build();

        Option<Boolean> allowServerListingsOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.allowServerListing"))
                .description(OptionDescription.of(Component.translatable("iridium.options.online.allowServerListing.description")))
                .binding(Binding.minecraft(this.client.options.allowServerListing()))
                .customController(BooleanController::new)
                .build();

        Collections.addAll(this.onlineOptions, realmsNotificationsOption, allowServerListingsOption);
    }
}
