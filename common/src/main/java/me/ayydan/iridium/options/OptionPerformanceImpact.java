package me.ayydan.iridium.options;

import net.minecraft.text.Text;

public enum OptionPerformanceImpact
{
    None("iridium.option.performanceImpact.none"),
    Low("iridium.option.performanceImpact.low"),
    Medium("iridium.option.performanceImpact.medium"),
    High("iridium.option.performanceImpact.high"),
    Varies("iridium.option.performanceImpact.varies");

    private final Text text;

    OptionPerformanceImpact(String translationKey)
    {
        this.text = Text.translatable(translationKey);
    }

    public Text getText()
    {
        return this.text;
    }
}
