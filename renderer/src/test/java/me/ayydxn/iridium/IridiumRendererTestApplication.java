package me.ayydxn.iridium;

import me.ayydxn.iridium.buffers.IndexBuffer;
import me.ayydxn.iridium.buffers.UniformBuffer;
import me.ayydxn.iridium.buffers.VertexBuffer;
import me.ayydxn.iridium.options.IridiumRendererOptions;
import me.ayydxn.iridium.renderer.GraphicsPipeline;
import me.ayydxn.iridium.renderer.SwapChain;
import me.ayydxn.iridium.shaders.IridiumShader;
import me.ayydxn.iridium.shaders.ShaderDataTypes;
import me.ayydxn.iridium.util.OrthographicCamera;
import me.ayydxn.iridium.vertex.VertexBufferElement;
import me.ayydxn.iridium.vertex.VertexBufferLayout;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWVulkan.glfwVulkanSupported;

public class IridiumRendererTestApplication
{
    private static final Pair<Integer, Integer> WINDOW_SIZE = new ImmutablePair<>(800, 600);
    private static final String VERSION = "2025.1.0";

    /* -- Config Options -- */
    private static final boolean ENABLE_VSYNC = true;
    private static final boolean ENABLE_SHADER_CACHING = false;
    private static final boolean ENABLE_VALIDATION = true;

    private static final int FRAMES_IN_FLIGHT = 3;
    /*----------------------*/

    private static boolean isWindowMinimized = false;

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

        VertexBufferLayout vertexBufferLayout = new VertexBufferLayout(List.of(
                new VertexBufferElement("Positions", ShaderDataTypes.Float3),
                new VertexBufferElement("Colors", ShaderDataTypes.Float3)
        ));

        OrthographicCamera camera = new OrthographicCamera(-1.6f, 1.6f, -0.9f, 0.9f);

        float[] vertices =
        {
                 -0.5f, -0.5f, 0.0f,    1.0f, 0.0f, 0.0f,
                  0.5f, -0.5f, 0.0f,    0.0f, 1.0f, 0.0f,
                  0.5f,  0.5f, 0.0f,    0.0f, 0.0f, 1.0f,
                 -0.5f,  0.5f, 0.0f,    1.0f, 0.0f, 0.0f,
        };

        int[] indices = { 0, 1, 2, 2, 3, 0 };

        FloatBuffer vertexData = BufferUtils.createFloatBuffer(vertices.length);
        vertexData.put(vertices).flip();

        IntBuffer indexData = BufferUtils.createIntBuffer(indices.length);
        indexData.put(indices).flip();

        GraphicsPipeline graphicsPipeline = new GraphicsPipeline(new IridiumShader("shaders/default_shader"), vertexBufferLayout, swapChain);
        graphicsPipeline.create();

        VertexBuffer vertexBuffer = new VertexBuffer(MemoryUtil.memByteBuffer(vertexData));
        vertexBuffer.create();

        IndexBuffer indexBuffer = new IndexBuffer(MemoryUtil.memByteBuffer(indexData));
        indexBuffer.create();

        UniformBuffer uniformBuffer = new UniformBuffer(camera.getViewProjectionMatrixBuffer());
        uniformBuffer.create();

        glfwSetFramebufferSizeCallback(window.getHandle(), (appWindow, newWidth, newHeight) ->
        {
            isWindowMinimized = newWidth == 0 || newHeight == 0;

            swapChain.onResize(newWidth, newHeight);
        });

        while (!window.shouldWindowClose())
        {
            window.update();

            IridiumRenderer.getInstance().beginFrame(swapChain);

            if (!isWindowMinimized)
            {
                graphicsPipeline.bindUniformBuffer("u_Camera", uniformBuffer);

                IridiumRenderer.getInstance().draw(graphicsPipeline, vertexBuffer, indexBuffer);
            }

            IridiumRenderer.getInstance().endFrame();

            swapChain.present();
        }

        vertexBuffer.destroy();
        indexBuffer.destroy();
        uniformBuffer.destroy();
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
