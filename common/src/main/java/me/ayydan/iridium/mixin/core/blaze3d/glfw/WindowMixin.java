package me.ayydan.iridium.mixin.core.blaze3d.glfw;

import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.ScreenManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import me.ayydan.iridium.event.WindowResizeEvent;
import me.ayydan.iridium.render.IridiumRenderer;
import me.ayydan.iridium.render.interfaces.IridiumWindow;
import me.ayydan.iridium.render.vulkan.VulkanSwapChain;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GLCapabilities;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.lwjgl.glfw.GLFW.*;

@Mixin(Window.class)
public class WindowMixin implements IridiumWindow
{
    @Shadow @Final private long window;

    @Shadow private int width;
    @Shadow private int height;

    @Unique
    private VulkanSwapChain vulkanSwapChain;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwCreateWindow(IILjava/lang/CharSequence;JJ)J"), remap = false)
    public void setGLFWClientAPI(WindowEventHandler eventHandler, ScreenManager screenManager, DisplayData displayData, String preferredFullscreenVideoMode,
                                 String title, CallbackInfo ci)
    {
        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwMakeContextCurrent(J)V"), remap = false)
    public void createVulkanSwapChain(WindowEventHandler eventHandler, ScreenManager screenManager, DisplayData displayData, String preferredFullscreenVideoMode,
                                      String title, CallbackInfo ci)
    {

        this.vulkanSwapChain = new VulkanSwapChain(IridiumRenderer.getInstance().getVulkanContext());
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

    @Inject(method = "onFramebufferResize", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/WindowEventHandler;resizeDisplay()V"))
    public void invokeIridiumWindowResizeEvent(long window, int width, int height, CallbackInfo ci)
    {
        WindowResizeEvent.EVENT.invoker().onWindowResize(width, height);
    }

    @Override
    public VulkanSwapChain getSwapChain()
    {
        return this.vulkanSwapChain;
    }
}
