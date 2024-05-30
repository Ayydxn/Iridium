package me.ayydan.iridium.mixin.core.blaze3d.platform;

import com.mojang.blaze3d.platform.GlDebugInfo;
import me.ayydan.iridium.render.IridiumRenderer;
import me.ayydan.iridium.render.vulkan.VulkanPhysicalDevice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GlDebugInfo.class)
public class GLDebugInfoMixin
{
    @Redirect(method = "getVendor", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_getString(I)Ljava/lang/String;"))
    private static String getVulkanPhysicalDeviceVendor(int name)
    {
        return IridiumRenderer.getInstance().getVulkanContext().getPhysicalDevice().getVendorName();
    }

    @Redirect(method = "getRenderer", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_getString(I)Ljava/lang/String;"))
    private static String getVulkanPhysicalDeviceName(int name)
    {
        return IridiumRenderer.getInstance().getVulkanContext().getPhysicalDevice().getProperties().deviceNameString();
    }

    @Redirect(method = "getVersion", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_getString(I)Ljava/lang/String;"))
    private static String getVulkanPhysicalDeviceDriverVersion(int name)
    {
        return IridiumRenderer.getInstance().getVulkanContext().getPhysicalDevice().getDriverVersion();
    }
}
