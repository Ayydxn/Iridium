package com.ayydxn.iridium.render;

import com.ayydxn.iridium.IridiumClientMod;
import com.ayydxn.iridium.render.exceptions.NotImplementedByIridiumException;
import com.ayydxn.iridium.render.shader.IridiumShaderCompiler;
import com.ayydxn.iridium.render.shader.ShaderSPIRV;
import com.ayydxn.iridium.render.shader.ShaderStage;
import net.minecraft.client.Minecraft;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkViewport;

public class IridiumRenderer
{
    private static IridiumRenderer INSTANCE;

    private boolean shouldSkipCurrentFrame;

    public IridiumRenderer()
    {
        IridiumShaderCompiler.initialize();
    }

    public static void initialize()
    {
        if (INSTANCE != null)
        {
            IridiumClientMod.getLogger().warn("Iridium's renderer cannot be initialized more than once!");
            return;
        }

        IridiumClientMod.getLogger().info("Initializing Iridium renderer...");

        INSTANCE = new IridiumRenderer();
    }

    public void shutdown()
    {
        IridiumShaderCompiler.getInstance().shutdown();

        INSTANCE = null;
    }

    public void beginFrame()
    {
        int swapChainWidth = Minecraft.getInstance().getWindow().getSwapChain().getWidth();
        int swapChainHeight = Minecraft.getInstance().getWindow().getSwapChain().getHeight();

        this.shouldSkipCurrentFrame = swapChainWidth == 0 || swapChainHeight == 0;

        Minecraft.getInstance().noRender = this.shouldSkipCurrentFrame;

        if (this.shouldSkipCurrentFrame)
            return;

        // TODO: (Ayydxn) Implement.
    }

    public void endFrame()
    {
        if (this.shouldSkipCurrentFrame)
            return;

        // TODO: (Ayydxn) Implement.
    }

    public void clearAttachments(int mask)
    {
        // TODO: (Ayydxn) Implement
        //throw new NotImplementedByIridiumException("IridiumRenderer::clearAttachments");
    }

    public void setViewport(int x, int y, int width, int height)
    {
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            VkViewport.Buffer viewport = VkViewport.calloc(1, memoryStack)
                    .x(x)
                    .y(height + y)
                    .width(width)
                    .height(-height)
                    .minDepth(0.0f)
                    .maxDepth(1.0f);

            // TODO: (Ayydxn) Call vkCmdSetViewport
        }
    }

    public boolean shouldSkipCurrentFrame()
    {
        return this.shouldSkipCurrentFrame;
    }

    public static IridiumRenderer getInstance()
    {
        if (INSTANCE == null)
            throw new IllegalStateException("Tried to access an instance of Iridium's renderer before one was available!");

        return INSTANCE;
    }
}
