package me.ayydan.iridium.options.util;

import net.minecraft.text.Text;

public class OptionsUtil
{
    public static Text getPercentValueText(double value)
    {
        return Text.literal((int) (value * 100.0d) + "%");
    }

    public static Text getPixelValueText(int value)
    {
        return Text.literal(value + "px");
    }
}
