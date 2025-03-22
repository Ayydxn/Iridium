package com.ayydxn.iridium.mixin.core.blaze3d.platform;

import com.ayydxn.iridium.event.WindowEvents;
import com.ayydxn.iridium.interfaces.VulkanContextHandler;
import com.ayydxn.iridium.render.IridiumRenderSystem;
import com.ayydxn.iridium.render.vulkan.VulkanContext;
import com.ayydxn.iridium.render.vulkan.VulkanSwapChain;
import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.ScreenManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GLCapabilities;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.lwjgl.glfw.GLFW.*;

@Mixin(Window.class)
public class WindowMixin implements VulkanContextHandler
{
    @Shadow @Final private long window;

    @Shadow private int width;
    @Shadow private int height;

    @Unique
    private VulkanContext vulkanContext;

    @Unique
    private VulkanSwapChain vulkanSwapChain;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwCreateWindow(IILjava/lang/CharSequence;JJ)J"), remap = false)
    public void setGLFWClientAPI(WindowEventHandler eventHandler, ScreenManager screenManager, DisplayData displayData, String preferredFullscreenVideoMode,
                                 String title, CallbackInfo ci)
    {
        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwCreateWindow(IILjava/lang/CharSequence;JJ)J"), remap = false)
    public void createVulkanContext(WindowEventHandler eventHandler, ScreenManager screenManager, DisplayData displayData, String preferredFullscreenVideoMode,
                                    String title, CallbackInfo ci)
    {
        this.vulkanContext = new VulkanContext();
        this.vulkanContext.initialize();
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwMakeContextCurrent(J)V", shift = At.Shift.AFTER), remap = false)
    public void createVulkanSwapChain(WindowEventHandler eventHandler, ScreenManager screenManager, DisplayData displayData, String preferredFullscreenVideoMode,
                                      String title, CallbackInfo ci)
    {
        this.vulkanSwapChain = new VulkanSwapChain(this.vulkanContext);
        this.vulkanSwapChain.initialize(this.window);
        this.vulkanSwapChain.create(this.width, this.height, Minecraft.getInstance().options.enableVsync().get());
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwWindowHint(II)V"), remap = false)
    public void redirectGLFWWindowHint(int hint, int value)
    {
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwMakeContextCurrent(J)V"), remap = false)
    public void redirectGLFWMakeContextCurrent(long window)
    {
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL;createCapabilities()Lorg/lwjgl/opengl/GLCapabilities;"), remap = false)
    public GLCapabilities redirectGLCreateCapabilities()
    {
        return null;
    }

    /**
     * Despite {@link IridiumRenderSystem#getMaxSupportedTextureSize()} providing a function for this, a window object isn't available
     * when Minecraft calls this function. Therefore, resulting in a crash as {@link Minecraft#getWindow()} will return null.
     */
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;maxSupportedTextureSize()I"), remap = false)
    public int getMaxSupportedTextureSize()
    {
        return this.vulkanContext.getPhysicalDevice().getProperties().limits().maxImageDimension2D();
    }

    @Inject(method = "close", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwTerminate()V"), remap = false)
    public void destroyVulkanContext(CallbackInfo ci)
    {
        this.vulkanSwapChain.destroy();
        this.vulkanContext.destroy();
    }

    @Inject(method = "onFramebufferResize", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/WindowEventHandler;resizeDisplay()V"))
    public void invokeIridiumWindowResizeEvent(long window, int framebufferWidth, int framebufferHeight, CallbackInfo ci)
    {
        WindowEvents.RESIZE.invoker().onWindowResize(framebufferWidth, framebufferHeight);
    }

    @Override
    public VulkanContext getVulkanContext()
    {
        return this.vulkanContext;
    }

    @Override
    public VulkanSwapChain getSwapChain()
    {
        return this.vulkanSwapChain;
    }
}
