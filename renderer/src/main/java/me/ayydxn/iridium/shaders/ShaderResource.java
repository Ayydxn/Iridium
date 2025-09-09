package me.ayydxn.iridium.shaders;

import static org.lwjgl.vulkan.VK10.*;

public record ShaderResource(String name, Type type, int binding, int set, int count, int shaderStageFlags)
{
    public enum Type
    {
        UNIFORM_BUFFER(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER),
        STORAGE_BUFFER(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER),
        SAMPLER(VK_DESCRIPTOR_TYPE_SAMPLER),
        COMBINED_IMAGE_SAMPLER(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER),
        SAMPLED_IMAGE(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE),
        STORAGE_IMAGE(VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);

        private final int vulkanDescriptorType;

        Type(int vulkanDescriptorType)
        {
            this.vulkanDescriptorType = vulkanDescriptorType;
        }

        public static Type getFromString(String type)
        {
            return switch (type)
            {
                case "uniform_buffer" -> UNIFORM_BUFFER;
                case "storage_buffer" -> STORAGE_BUFFER;
                case "sampler" -> SAMPLER;
                case "combined_image_sampler" -> COMBINED_IMAGE_SAMPLER;
                case "sampled_image" -> SAMPLED_IMAGE;
                case "storage_image" -> STORAGE_IMAGE;
                default -> throw new IllegalArgumentException(String.format("Unknown shader resource type: %s", type));
            };
        }

        public int getVulkanDescriptorType()
        {
            return this.vulkanDescriptorType;
        }
    }
}
