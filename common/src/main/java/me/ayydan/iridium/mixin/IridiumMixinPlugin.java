package me.ayydan.iridium.mixin;

import net.caffeinemc.caffeineconfig.AbstractCaffeineConfigMixinPlugin;
import net.caffeinemc.caffeineconfig.CaffeineConfig;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.LoggerFactory;

public class IridiumMixinPlugin extends AbstractCaffeineConfigMixinPlugin
{
    @Override
    public CaffeineConfig createConfig()
    {
        return CaffeineConfig.builder("Iridium")
                .addMixinOption("core", true)
                .addMixinOption("features", true)
                .withLogger(LoggerFactory.getLogger("Iridium Mixin Plugin"))
                .build(FabricLoader.getInstance().getConfigDir().resolve("iridium-mixin-settings.properties"));
    }

    @Override
    public String mixinPackageRoot()
    {
        return "me.ayydan.iridium.mixin.";
    }
}
