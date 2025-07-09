package me.ayydxn.moonblast.options;

import org.lwjgl.system.MemoryUtil;

public record RendererConfig(long windowHandle, boolean enableVSync, boolean enableShaderCaching, boolean enableValidationLayers)
{
    public RendererConfig
    {
        if (windowHandle == MemoryUtil.NULL)
            throw new IllegalArgumentException("Window handle must not be null!");
    }
}
