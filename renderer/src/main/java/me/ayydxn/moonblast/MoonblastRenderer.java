package me.ayydxn.moonblast;

import me.ayydxn.moonblast.options.RendererConfig;
import me.ayydxn.moonblast.renderer.GraphicsContext;
import me.ayydxn.moonblast.shaders.MoonblastShaderCompiler;
import me.ayydxn.moonblast.utils.MoonblastConstants;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.lwjgl.Version;

public class MoonblastRenderer
{
    private static MoonblastRenderer INSTANCE;

    private final GraphicsContext graphicsContext;
    private final RendererConfig rendererConfig;

    private MoonblastRenderer(RendererConfig rendererConfig)
    {
        MoonblastShaderCompiler.initialize();

        this.graphicsContext = new GraphicsContext(rendererConfig);
        this.graphicsContext.initialize();

        this.rendererConfig = rendererConfig;
    }

    public static void initialize(RendererConfig rendererConfig)
    {
        if (INSTANCE != null)
        {
            MoonblastConstants.LOGGER.warn("Moonblast cannot be initialized more than once!");
            return;
        }

        MoonblastConstants.LOGGER.info("Initializing Moonblast Renderer...\n- Version: {}\n- LWJGL Version: {}", "2025.1.0", Version.getVersion());

        INSTANCE = new MoonblastRenderer(rendererConfig);
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

    public RendererConfig getRendererConfig()
    {
        return this.rendererConfig;
    }
}
