package me.ayydan.iridium.options.categories;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionGroup;
import me.ayydan.iridium.IridiumClientMod;
import me.ayydan.iridium.options.IridiumGameOptions;
import net.minecraft.network.chat.Component;

import java.util.List;

public abstract class IridiumOptionCategory
{
    private final ConfigCategory category;

    protected final IridiumGameOptions iridiumGameOptions = IridiumClientMod.getInstance().getGameOptions();

    public IridiumOptionCategory(Component categoryName)
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
    }

    public abstract List<Option<?>> getCategoryOptions();

    public abstract List<OptionGroup> getCategoryGroups();

    public ConfigCategory getYACLCategory()
    {
        return this.category;
    }
}
