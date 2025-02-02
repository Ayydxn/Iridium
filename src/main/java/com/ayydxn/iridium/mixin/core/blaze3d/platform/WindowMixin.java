package com.ayydxn.iridium.mixin.core.blaze3d.platform;

import com.ayydxn.iridium.interfaces.VulkanContextHandler;
import com.ayydxn.iridium.render.vulkan.VulkanContext;
import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.ScreenManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public class WindowMixin implements VulkanContextHandler
{
    @Unique
    private VulkanContext vulkanContext;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwCreateWindow(IILjava/lang/CharSequence;JJ)J"), remap = false)
    public void createVulkanContext(WindowEventHandler eventHandler, ScreenManager screenManager, DisplayData displayData, String preferredFullscreenVideoMode, String title, CallbackInfo ci)
    {
        this.vulkanContext = new VulkanContext();
        this.vulkanContext.initialize();
    }

    @Inject(method = "close", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwTerminate()V"), remap = false)
    public void destroyVulkanContext(CallbackInfo ci)
    {
        this.vulkanContext.destroy();
    }

    @Override
    public VulkanContext getVulkanContext()
    {
        return this.vulkanContext;
    }
}
