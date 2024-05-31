package me.ayydan.iridium.options.util;

import net.minecraft.network.chat.Component;

public class OptionsUtil
{
    public static Component getPercentValueText(double value)
    {
        return Component.literal((int) (value * 100.0d) + "%");
    }

    public static Component getPixelValueText(int value)
    {
        return Component.literal(value + "px");
    }
}
