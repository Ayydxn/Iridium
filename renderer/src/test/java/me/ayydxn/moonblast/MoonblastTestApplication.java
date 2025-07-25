package me.ayydxn.moonblast;

import me.ayydxn.moonblast.options.MoonblastRendererOptions;
import me.ayydxn.moonblast.renderer.CommandBuffer;
import me.ayydxn.moonblast.renderer.GraphicsPipeline;
import me.ayydxn.moonblast.renderer.SwapChain;
import me.ayydxn.moonblast.shaders.MoonblastShader;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.system.MemoryUtil;

import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWVulkan.glfwVulkanSupported;

public class MoonblastTestApplication
{
    private static final Pair<Integer, Integer> WINDOW_SIZE = new ImmutablePair<>(800, 600);
    private static final String VERSION = "2025.1.0";

    /* -- Config Options -- */
    private static final boolean ENABLE_VSYNC = true;
    private static final boolean ENABLE_SHADER_CACHING = false;
    private static final boolean ENABLE_VALIDATION = true;

    public static void main(String[] args)
    {
        Window window = new Window(String.format("Moonblast Renderer Demo // v%s (Graphics API: Vulkan)", VERSION), WINDOW_SIZE.getLeft(), WINDOW_SIZE.getRight());
        window.create();

        MoonblastRendererOptions moonblastRendererOptions = MoonblastRendererOptions.load();
        moonblastRendererOptions.rendererOptions.enableVSync = ENABLE_VSYNC;
        moonblastRendererOptions.rendererOptions.enableShaderCaching = ENABLE_SHADER_CACHING;
        moonblastRendererOptions.debugOptions.enableValidationLayers = ENABLE_VALIDATION;
        moonblastRendererOptions.write();

        MoonblastRenderer.initialize(window.getHandle(), moonblastRendererOptions);

        SwapChain swapChain = new SwapChain();
        swapChain.initialize();
        swapChain.create(WINDOW_SIZE.getLeft(), WINDOW_SIZE.getRight());

        GraphicsPipeline graphicsPipeline = new GraphicsPipeline(new MoonblastShader("shaders/default_shader"), swapChain);
        graphicsPipeline.create();

        while (!window.shouldWindowClose())
        {
            window.update();

            MoonblastRenderer.getInstance().beginFrame(swapChain);

            MoonblastRenderer.getInstance().draw(graphicsPipeline);

            MoonblastRenderer.getInstance().endFrame();

            swapChain.present();
        }

        graphicsPipeline.destroy();
        swapChain.destroy();
        window.cleanup();
    }

    private static class Window
    {
        private final String title;
        private final int width;
        private final int height;

        private long handle;

        private Window(String title, int width, int height)
        {
            this.title = title;
            this.width = width;
            this.height = height;

            GLFWErrorCallback.createPrint(System.err).set();

            if (!glfwInit())
                throw new IllegalStateException("Unable to initialize GLFW");

            if (!glfwVulkanSupported())
                throw new IllegalStateException("Cannot find a compatible Vulkan installable client driver (ICD)");
        }

        public void create()
        {
            glfwDefaultWindowHints();
            glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
            glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);

            this.handle = glfwCreateWindow(this.width, this.height, this.title, MemoryUtil.NULL, MemoryUtil.NULL);
            if (this.handle == MemoryUtil.NULL)
                throw new RuntimeException("Failed to create the GLFW window");
        }

        public void update()
        {
            glfwPollEvents();
        }

        public void cleanup()
        {
            MoonblastRenderer.getInstance().shutdown();

            glfwFreeCallbacks(this.handle);
            glfwDestroyWindow(this.handle);
            glfwTerminate();

            Objects.requireNonNull(glfwSetErrorCallback(null)).free();
        }

        public boolean shouldWindowClose()
        {
            return glfwWindowShouldClose(this.handle);
        }

        public long getHandle()
        {
            return this.handle;
        }
    }
}
