package me.ayydan.iridium.options;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class GameOptionExclusionStrategy implements ExclusionStrategy
{
    @Override
    public boolean shouldSkipField(FieldAttributes fieldAttributes)
    {
        return fieldAttributes.getAnnotation(GameOption.class) == null;
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz)
    {
        return false;
    }
}
