package com.ayydxn.iridium.options.categories;

import com.ayydxn.iridium.IridiumClientMod;
import com.ayydxn.iridium.options.IridiumGameOptions;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionGroup;
import net.minecraft.network.chat.Component;

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

        if (categoryGroups == null || categoryGroups.isEmpty())
        {
            if (categoryOptions == null || categoryOptions.isEmpty())
                throw new IllegalArgumentException("A category's options cannot be empty or null if there are no groups!");

            this.category = ConfigCategory.createBuilder()
                    .name(categoryName)
                    .options(this.getCategoryOptions())
                    .build();
        }
        else
        {
            this.category = ConfigCategory.createBuilder()
                    .name(categoryName)
                    .groups(this.getCategoryGroups())
                    .build();
        }

        this.name = name;
    }

    public abstract List<Option<?>> getCategoryOptions();

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
