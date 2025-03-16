package com.ayydxn.iridium.options.categories.util;

public class OptionCategoryDefinition
{
    private final OptionCategoryFactory optionCategoryFactory;
    private final int order;

    public OptionCategoryDefinition(OptionCategoryFactory categoryFactory, int order)
    {
        this.optionCategoryFactory = categoryFactory;
        this.order = order;
    }

    public OptionCategoryFactory getFactory()
    {
        return this.optionCategoryFactory;
    }

    public int getOrder()
    {
        return this.order;
    }
}
