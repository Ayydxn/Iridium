package me.ayydxn.iridium;

import me.ayydxn.iridium.layer.Layer;
import me.ayydxn.iridium.layer.LayerStack;
import me.ayydxn.iridium.options.IridiumRendererOptions;
import me.ayydxn.iridium.renderer.SwapChain;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.system.MemoryUtil;

import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWVulkan.glfwVulkanSupported;

public class IridiumRendererTestApplication
{
    private static final Pair<Integer, Integer> WINDOW_SIZE = new ImmutablePair<>(800, 600);
    private static final LayerStack LAYER_STACK = new LayerStack();
    private static final String VERSION = "2025.1.0";

    /* -- Config Options -- */
    private static final boolean ENABLE_VSYNC = true;
    private static final boolean ENABLE_SHADER_CACHING = false;
    private static final boolean ENABLE_VALIDATION = true;

    private static final int FRAMES_IN_FLIGHT = 3;
    /*----------------------*/

    private static boolean isWindowMinimized = false;
    private static boolean usingCompute = false;

    public static void main(String[] args)
    {
        Window window = new Window(String.format("Iridium Renderer Demo // v%s (Graphics API: Vulkan)", VERSION), WINDOW_SIZE.getLeft(), WINDOW_SIZE.getRight());
        window.create();

        IridiumRendererOptions iridiumRendererOptions = IridiumRendererOptions.load();
        iridiumRendererOptions.rendererOptions.enableVSync = ENABLE_VSYNC;
        iridiumRendererOptions.rendererOptions.enableShaderCaching = ENABLE_SHADER_CACHING;
        iridiumRendererOptions.rendererOptions.framesInFlight = FRAMES_IN_FLIGHT;
        iridiumRendererOptions.debugOptions.enableValidationLayers = ENABLE_VALIDATION;
        iridiumRendererOptions.write();

        IridiumRenderer.initialize(window.getHandle(), iridiumRendererOptions);

        SwapChain swapChain = new SwapChain();
        swapChain.initialize();
        swapChain.create(WINDOW_SIZE.getLeft(), WINDOW_SIZE.getRight());

        GraphicsTestLayer graphicsTestLayer = new GraphicsTestLayer(swapChain);
        ComputeTestLayer computeTestLayer = new ComputeTestLayer();

        LAYER_STACK.pushLayer(graphicsTestLayer);

        // (Ayydxn) Basic toggling between graphics and compute. Going to need a better way to do this.
        // Also, it doesn't work after changing the quad's color once for some reason.
        glfwSetKeyCallback(window.getHandle(), (appWindow, key, scancode, action, mods) ->
        {
            if (key == GLFW_KEY_ENTER && action == GLFW_PRESS)
            {
                usingCompute = !usingCompute;

                if (usingCompute)
                {
                    LAYER_STACK.pushLayer(computeTestLayer);
                    LAYER_STACK.popLayer(graphicsTestLayer);
                }
                else
                {
                    LAYER_STACK.pushLayer(graphicsTestLayer);
                    LAYER_STACK.popLayer(computeTestLayer);
                }
            }
        });

        glfwSetFramebufferSizeCallback(window.getHandle(), (appWindow, newWidth, newHeight) ->
        {
            isWindowMinimized = newWidth == 0 || newHeight == 0;

            swapChain.onResize(newWidth, newHeight);
        });

        while (!window.shouldWindowClose())
        {
            for (Layer layer : LAYER_STACK)
                layer.onUpdate();

            window.update();

            IridiumRenderer.getInstance().beginFrame(swapChain);

            if (!isWindowMinimized)
            {
                for (Layer layer : LAYER_STACK)
                    layer.onRender();
            }

            IridiumRenderer.getInstance().endFrame();

            swapChain.present();
        }

        for (Layer layer : LAYER_STACK)
            layer.onDetach();

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
            IridiumRenderer.getInstance().shutdown();

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
