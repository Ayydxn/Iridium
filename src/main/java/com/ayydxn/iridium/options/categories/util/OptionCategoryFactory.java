package com.ayydxn.iridium.options.categories.util;

import com.ayydxn.iridium.options.categories.IridiumOptionCategory;

@FunctionalInterface
public interface OptionCategoryFactory
{
    IridiumOptionCategory createCategory();
}
