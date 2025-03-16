package com.ayydxn.iridium.options.categories;

import dev.isxander.yacl3.api.Binding;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.gui.controllers.BooleanController;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class IridiumOnlineOptionsCategory extends IridiumOptionCategory
{
    public IridiumOnlineOptionsCategory()
    {
        super("Online", Component.translatable("iridium.options.category.online"));
    }

    @Override
    public @NotNull List<Option<?>> getCategoryOptions()
    {
        Minecraft client = Minecraft.getInstance();

        Option<Boolean> realmsNotificationsOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.realmsNotifications"))
                .description(OptionDescription.of(Component.translatable("iridium.options.online.realmsNotifications.description")))
                .binding(Binding.minecraft(client.options.realmsNotifications()))
                .customController(BooleanController::new)
                .build();

        Option<Boolean> allowServerListingsOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.allowServerListing"))
                .description(OptionDescription.of(Component.translatable("iridium.options.online.allowServerListing.description")))
                .binding(Binding.minecraft(client.options.allowServerListing()))
                .customController(BooleanController::new)
                .build();

        return List.of(realmsNotificationsOption, allowServerListingsOption);
    }

    @Override
    public @NotNull List<OptionGroup> getCategoryGroups()
    {
        return Lists.newArrayList();
    }
}
