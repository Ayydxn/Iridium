package com.ayydxn.iridium.options.categories.util;

import com.ayydxn.iridium.options.categories.IridiumOptionCategory;
import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

public class OptionCategoryRegistry
{
    private static final Map<String, OptionCategoryDefinition> CATEGORIES = Maps.newHashMap();

    public static void register(String id, OptionCategoryFactory categoryFactory, int order)
    {
        if (CATEGORIES.containsKey(id))
            throw new IllegalArgumentException(String.format("Category with ID '%s' already exists!", id));

        CATEGORIES.put(id, new OptionCategoryDefinition(categoryFactory, order));
    }

    public static IridiumOptionCategory create(String id)
    {
        OptionCategoryDefinition definition = CATEGORIES.get(id);
        if (definition != null)
            return definition.getFactory().createCategory();

        throw new IllegalArgumentException(String.format("No category for ID '%s' exists!", id));
    }

    public static Collection<IridiumOptionCategory> getCategories()
    {
        return CATEGORIES.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> entry.getValue().getOrder()))
                .map(entry -> entry.getValue().getFactory().createCategory())
                .collect(Collectors.toList());
    }
}
