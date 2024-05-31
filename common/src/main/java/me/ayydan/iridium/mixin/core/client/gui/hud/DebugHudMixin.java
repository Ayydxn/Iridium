package me.ayydan.iridium.mixin.core.client.gui.hud;

import com.google.common.collect.Lists;
import me.ayydan.iridium.render.IridiumRenderer;
import me.ayydan.iridium.render.vulkan.VulkanPhysicalDevice;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;

@Mixin(DebugScreenOverlay.class)
public class DebugHudMixin
{
    @Redirect(method = "getSystemInformation", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayList([Ljava/lang/Object;)Ljava/util/ArrayList;", remap = false))
    private ArrayList<String> addIridiumDebugInfo(Object[] elements)
    {
        ArrayList<String> iridiumDebugStrings = Lists.newArrayList((String[]) elements);
        VulkanPhysicalDevice vulkanPhysicalDevice = IridiumRenderer.getInstance().getVulkanContext().getPhysicalDevice();

        iridiumDebugStrings.add(String.format("Vulkan API Version: %s", vulkanPhysicalDevice.getVulkanAPIVersion()));

        return iridiumDebugStrings;
    }
}
