package me.ayydan.iridium.mixin.blaze3d.glfw;

import com.mojang.blaze3d.glfw.Window;
import com.mojang.blaze3d.glfw.WindowSettings;
import com.mojang.blaze3d.glfw.monitor.MonitorTracker;
import me.ayydan.iridium.event.WindowEventHandler;
import org.lwjgl.opengl.GLCapabilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.lwjgl.glfw.GLFW.*;

@Mixin(Window.class)
public class WindowMixin
{
    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwCreateWindow(IILjava/lang/CharSequence;JJ)J"))
    public void setGLFWClientAPI(com.mojang.blaze3d.glfw.WindowEventHandler eventHandler, MonitorTracker monitorTracker, WindowSettings settings, String videoMode,
                                 String title, CallbackInfo ci)
    {
        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwWindowHint(II)V"))
    public void redirectGLFWWindowHint(int hint, int value)
    {
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwMakeContextCurrent(J)V"))
    public void redirectGLFWMakeContextCurrent(long window)
    {
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL;createCapabilities()Lorg/lwjgl/opengl/GLCapabilities;"))
    public GLCapabilities redirectGLCreateCapabilities()
    {
        return null;
    }

    @Inject(method = "onFramebufferSizeChanged", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/glfw/WindowEventHandler;onResolutionChanged()V"))
    public void invokeIridiumWindowResizeEvent(long window, int width, int height, CallbackInfo ci)
    {
        WindowEventHandler.EVENT.invoker().onWindowResize(width, height);
    }
}
