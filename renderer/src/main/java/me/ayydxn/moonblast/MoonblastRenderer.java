package me.ayydxn.moonblast;

import me.ayydxn.moonblast.options.MoonblastRendererOptions;
import me.ayydxn.moonblast.renderer.GraphicsContext;
import me.ayydxn.moonblast.shaders.MoonblastShaderCompiler;
import me.ayydxn.moonblast.utils.MoonblastConstants;
import org.lwjgl.Version;

public class MoonblastRenderer
{
    private static MoonblastRenderer INSTANCE;

    private final GraphicsContext graphicsContext;
    private final MoonblastRendererOptions moonblastRendererOptions;
    private final long windowHandle;

    private MoonblastRenderer(long windowHandle, MoonblastRendererOptions moonblastRendererOptions)
    {
        MoonblastShaderCompiler.initialize();

        this.graphicsContext = new GraphicsContext(moonblastRendererOptions);
        this.graphicsContext.initialize();

        this.moonblastRendererOptions = moonblastRendererOptions;
        this.windowHandle = windowHandle;
    }

    public static void initialize(long windowHandle, MoonblastRendererOptions moonblastRendererOptions)
    {
        if (INSTANCE != null)
        {
            MoonblastConstants.LOGGER.warn("Moonblast cannot be initialized more than once!");
            return;
        }

        MoonblastConstants.LOGGER.info("Initializing Moonblast Renderer...\n- Version: {}\n- LWJGL Version: {}", "2025.1.0", Version.getVersion());

        INSTANCE = new MoonblastRenderer(windowHandle, moonblastRendererOptions);
    }

    public void shutdown()
    {
        MoonblastShaderCompiler.getInstance().shutdown();

        this.graphicsContext.destroy();
    }

    public static MoonblastRenderer getInstance()
    {
        if (INSTANCE == null)
            throw new IllegalStateException("Tried to access an instance of Moonblast before one was available!");

        return INSTANCE;
    }

    public GraphicsContext getGraphicsContext()
    {
        return this.graphicsContext;
    }

    public MoonblastRendererOptions getOptions()
    {
        return this.moonblastRendererOptions;
    }

    public long getWindowHandle()
    {
        return this.windowHandle;
    }
}
