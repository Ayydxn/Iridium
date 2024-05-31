package me.ayydan.iridium.options;

import net.minecraft.network.chat.Component;

public enum OptionPerformanceImpact
{
    None("iridium.option.performanceImpact.none"),
    Low("iridium.option.performanceImpact.low"),
    Medium("iridium.option.performanceImpact.medium"),
    High("iridium.option.performanceImpact.high"),
    Varies("iridium.option.performanceImpact.varies");

    private final Component text;

    OptionPerformanceImpact(String translationKey)
    {
        this.text = Component.translatable(translationKey);
    }

    public Component getText()
    {
        return this.text;
    }
}
