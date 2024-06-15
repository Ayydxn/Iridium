package me.ayydan.iridium.render.vulkan;

import com.mojang.blaze3d.vertex.VertexFormat;
import me.ayydan.iridium.render.shader.IridiumShader;
import org.apache.commons.lang3.NotImplementedException;

public abstract class VulkanPipeline
{
    public abstract void create();

    public abstract void destroy();

    public abstract long getHandle();

    public static class Builder
    {
        private VulkanPipelineType pipelineType;
        private IridiumShader shader;
        private VertexFormat vertexFormat;

        public Builder type(VulkanPipelineType pipelineType)
        {
            this.pipelineType = pipelineType;

            return this;
        }

        public Builder shader(IridiumShader shader)
        {
            this.shader = shader;

            return this;
        }

        public Builder vertexFormat(VertexFormat vertexFormat)
        {
            this.vertexFormat = vertexFormat;

            return this;
        }

        public VulkanPipeline build()
        {
            if (pipelineType == null)
                throw new IllegalArgumentException("A Vulkan pipeline's type must be specified!");

            return switch (pipelineType)
            {
                case Graphics -> new VulkanGraphicsPipeline(this.shader, this.vertexFormat);
                case Compute -> throw new NotImplementedException("Iridium currently does not support compute shaders/pipelines!");
            };
        }
    }
}
