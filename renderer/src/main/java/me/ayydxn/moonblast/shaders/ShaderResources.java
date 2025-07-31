package me.ayydxn.moonblast.shaders;

import com.google.common.collect.Maps;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import java.util.HashMap;

import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_ALL;

public class ShaderResources
{
    public static final class UniformBuffer
    {
        String name = new String();
        public int size = 0;
        public int bindingPoint = 0;
        public int shaderStage = VK_SHADER_STAGE_ALL;
    }

    public static final class ShaderDescriptorSet
    {
        // Uniform buffer binding -> The uniform buffer itself
        public HashMap<Integer, UniformBuffer> uniformBuffers = Maps.newHashMap();

        // Resource name -> The VkWriteDescriptorSet object
        public HashMap<String, VkWriteDescriptorSet> writeDescriptorSets = Maps.newHashMap();
    }
}
