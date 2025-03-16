package com.ayydxn.iridium.options.categories;

import com.ayydxn.iridium.IridiumClientMod;
import com.ayydxn.iridium.options.IridiumGameOptions;
import com.google.common.collect.Lists;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionGroup;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class IridiumOptionCategory
{
    private final ConfigCategory category;
    private final String name;

    protected final IridiumGameOptions iridiumGameOptions = IridiumClientMod.getInstance().getGameOptions();

    public IridiumOptionCategory(String name, Component categoryName)
    {
        List<Option<?>> categoryOptions = this.getCategoryOptions();
        List<OptionGroup> categoryGroups = this.getCategoryGroups();

        ConfigCategory.Builder builder = ConfigCategory.createBuilder()
                .name(categoryName);

        if (!categoryGroups.isEmpty())
            builder.groups(categoryGroups);

        if (!categoryOptions.isEmpty())
            builder.options(categoryOptions);

        if (categoryOptions.isEmpty() && categoryGroups.isEmpty()) {
            throw new IllegalArgumentException("A category must have either options or groups or both.");
        }

        this.category = builder.build();
        this.name = name;
    }

    @NotNull
    public abstract List<Option<?>> getCategoryOptions();

    @NotNull
    public abstract List<OptionGroup> getCategoryGroups();

    public ConfigCategory getYACLCategory()
    {
        return this.category;
    }

    public String getName()
    {
        return this.name;
    }
}
