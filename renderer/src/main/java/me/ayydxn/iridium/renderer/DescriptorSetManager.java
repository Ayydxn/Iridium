package me.ayydxn.iridium.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.ayydxn.iridium.IridiumRenderer;
import me.ayydxn.iridium.buffers.UniformBuffer;
import me.ayydxn.iridium.shaders.IridiumShader;
import me.ayydxn.iridium.shaders.ShaderResource;
import me.ayydxn.iridium.shaders.ShaderResourceBinding;
import me.ayydxn.iridium.texture.VulkanTexture;
import me.ayydxn.iridium.utils.IridiumConstants;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.List;
import java.util.Map;

import static me.ayydxn.iridium.renderer.debug.VulkanDebugUtils.vkCheckResult;
import static org.lwjgl.vulkan.VK10.*;

public class DescriptorSetManager
{
    private final VkDevice logicalDevice = IridiumRenderer.getInstance().getGraphicsContext().getGraphicsDevice().getLogicalDevice();
    private final Map<Integer, Integer> descriptorPoolSizes; // Descriptor Type -> Size
    private final Map<String, ShaderResourceBinding> resourceBindings; // Resource Name -> Shader Resource
    private final Map<String, ByteBuffer> pushConstantsData; // Push Constant Name -> Push Constant Data
    private final Map<Integer, Long> allocatedDescriptorSets; // Descriptor Set Index -> Descriptor Set Handle
    private final int maxDescriptorSets;

    private boolean areDescriptorSetsDirty;
    private long descriptorPool;

    public DescriptorSetManager()
    {
        this.descriptorPoolSizes = Maps.newHashMap();
        this.resourceBindings = Maps.newConcurrentMap();
        this.pushConstantsData = Maps.newConcurrentMap();
        this.allocatedDescriptorSets = Maps.newConcurrentMap();
        this.maxDescriptorSets = 1000; // (Ayydxn) Maybe make this configurable by the caller?
        this.areDescriptorSetsDirty = true;

        this.initializeDefaultDescriptorPoolSizes();
        this.createDescriptorPool();
    }

    public void destroy()
    {
        this.resourceBindings.clear();
        this.pushConstantsData.clear();
        this.allocatedDescriptorSets.clear();

        vkDestroyDescriptorPool(logicalDevice, descriptorPool, null);
    }

    public void updateDescriptorSets(IridiumShader shader)
    {
        if (!this.areDescriptorSetsDirty && !this.areDirtyBindingsPresent())
            return;

        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            IridiumShader.DescriptorSetInfo descriptorSetInfo = shader.getDescriptorSetInfo();

            List<Map.Entry<Integer, Long>> sortedDescriptorSetLayouts = Lists.newArrayList(descriptorSetInfo.descriptorSetLayouts().entrySet());
            sortedDescriptorSetLayouts.sort(Map.Entry.comparingByKey());

            for (Map.Entry<Integer, Long> setLayout : sortedDescriptorSetLayouts)
            {
                int descriptorSetIndex = setLayout.getKey();
                long descriptorSetLayout = setLayout.getValue();

                if (!this.allocatedDescriptorSets.containsKey(descriptorSetIndex))
                {
                    VkDescriptorSetAllocateInfo descriptorSetAllocateInfo = VkDescriptorSetAllocateInfo.calloc(memoryStack)
                            .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
                            .descriptorPool(this.descriptorPool)
                            .pSetLayouts(memoryStack.longs(descriptorSetLayout));

                    LongBuffer pDescriptorSet = memoryStack.longs(VK_NULL_HANDLE);
                    vkCheckResult(vkAllocateDescriptorSets(this.logicalDevice, descriptorSetAllocateInfo, pDescriptorSet));

                    this.allocatedDescriptorSets.put(descriptorSetIndex, pDescriptorSet.get(0));
                }
            }

            List<VkWriteDescriptorSet> writeDescriptorSets = Lists.newArrayList();

            for (Map.Entry<Integer, List<ShaderResource>> resources : descriptorSetInfo.resources().entrySet())
            {
                int setIndex = resources.getKey();
                long descriptorSet = this.allocatedDescriptorSets.get(setIndex);

                if (descriptorSet == VK_NULL_HANDLE)
                {
                    IridiumConstants.LOGGER.warn("Descriptor set {} is not allocated. Not updating whatever resources it has", setIndex);
                    continue;
                }

                for (ShaderResource resource : resources.getValue())
                {
                    ShaderResourceBinding resourceBinding = this.resourceBindings.get(resource.name());
                    if (resourceBinding != null && resourceBinding.isDirty())
                    {
                        VkWriteDescriptorSet writeDescriptorSet = this.createWriteDescriptorSet(descriptorSet, resource, resourceBinding, memoryStack);
                        if (writeDescriptorSet != null)
                            writeDescriptorSets.add(writeDescriptorSet);

                        resourceBinding.markClean();
                    }
                }

                if (!writeDescriptorSets.isEmpty())
                {
                    VkWriteDescriptorSet.Buffer writeSets =  VkWriteDescriptorSet.calloc(writeDescriptorSets.size(), memoryStack);
                    for (int i = 0; i < writeDescriptorSets.size(); i++)
                        writeSets.put(i, writeDescriptorSets.get(i));

                    vkUpdateDescriptorSets(this.logicalDevice, writeSets, null);
                }
            }
        }

        this.areDescriptorSetsDirty = false;
    }

    public void bindDescriptorSets(VkCommandBuffer commandBuffer, long pipelineLayout, int pipelineBindPoint)
    {
        if (!this.allocatedDescriptorSets.isEmpty())
        {
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                LongBuffer pDescriptorSets = stack.mallocLong(this.allocatedDescriptorSets.size());

                List<Map.Entry<Integer, Long>> sortedDescriptorSets = Lists.newArrayList(this.allocatedDescriptorSets.entrySet());
                sortedDescriptorSets.sort(Map.Entry.comparingByKey());

                for (int i = 0; i < sortedDescriptorSets.size(); i++)
                    pDescriptorSets.put(i, sortedDescriptorSets.get(i).getValue());

                vkCmdBindDescriptorSets(commandBuffer, pipelineBindPoint, pipelineLayout, 0, pDescriptorSets, null);
            }
        }
    }

    public void updatePushConstants(VkCommandBuffer commandBuffer, long pipelineLayout, IridiumShader shader)
    {
        for (IridiumShader.PushConstantRange pushConstantRange : shader.getPushConstantRanges())
        {
            ByteBuffer pushConstantData = this.pushConstantsData.get(pushConstantRange.name());
            if (pushConstantData != null)
            {
                pushConstantData.rewind();

                int oldLimit =  pushConstantData.limit();
                if (oldLimit < pushConstantRange.size())
                {
                    IridiumConstants.LOGGER.error("Not enough data was provided to push constant '{}'!", pushConstantRange.name());
                    continue;
                }

                pushConstantData.limit(pushConstantRange.size());

                vkCmdPushConstants(commandBuffer, pipelineLayout, pushConstantRange.shaderStageFlags(), pushConstantRange.offset(), pushConstantData);

                pushConstantData.limit(oldLimit);
            }
        }
    }

    public void bindUniformBuffer(String name, UniformBuffer uniformBuffer)
    {
        ShaderResourceBinding resourceBinding = this.resourceBindings.computeIfAbsent(name, resourceName ->
                new ShaderResourceBinding(resourceName, ShaderResource.Type.UNIFORM_BUFFER));
        resourceBinding.setUniformBuffer(uniformBuffer);

        this.areDescriptorSetsDirty = true;
    }

    public void bindTexture(String name, VulkanTexture texture)
    {
        ShaderResourceBinding resourceBinding = this.resourceBindings.computeIfAbsent(name, resourceName ->
                new ShaderResourceBinding(resourceName, ShaderResource.Type.COMBINED_IMAGE_SAMPLER));
        resourceBinding.setTexture(texture);

        this.areDescriptorSetsDirty = true;
    }

    public void setPushConstant(String name, ByteBuffer data)
    {
        if (data == null)
            throw new IllegalArgumentException("Cannot set the value of a push constant to null!");

        this.pushConstantsData.put(name, data);
    }

    private void initializeDefaultDescriptorPoolSizes()
    {
        this.descriptorPoolSizes.put(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, this.maxDescriptorSets * 2);
        this.descriptorPoolSizes.put(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER, this.maxDescriptorSets);
        this.descriptorPoolSizes.put(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, this.maxDescriptorSets * 4);
        this.descriptorPoolSizes.put(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE, this.maxDescriptorSets * 2);
        this.descriptorPoolSizes.put(VK_DESCRIPTOR_TYPE_STORAGE_IMAGE, this.maxDescriptorSets);
        this.descriptorPoolSizes.put(VK_DESCRIPTOR_TYPE_SAMPLER, this.maxDescriptorSets * 2);
    }

    @SuppressWarnings("resource")
    private void createDescriptorPool()
    {
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            VkDescriptorPoolSize.Buffer poolSizes = VkDescriptorPoolSize.calloc(this.descriptorPoolSizes.size(), memoryStack);

            int index = 0;
            for (Map.Entry<Integer, Integer> entry : this.descriptorPoolSizes.entrySet())
            {
                poolSizes.get(index)
                        .type(entry.getKey())
                        .descriptorCount(entry.getValue());

                index++;
            }

            VkDescriptorPoolCreateInfo descriptorPoolCreateInfo = VkDescriptorPoolCreateInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
                    .pPoolSizes(poolSizes)
                    .maxSets(this.maxDescriptorSets)
                    .flags(VK_DESCRIPTOR_POOL_CREATE_FREE_DESCRIPTOR_SET_BIT);

            LongBuffer pDescriptorPool = memoryStack.longs(VK_NULL_HANDLE);
            vkCheckResult(vkCreateDescriptorPool(this.logicalDevice, descriptorPoolCreateInfo, null, pDescriptorPool));

            this.descriptorPool = pDescriptorPool.get(0);
        }
    }

    private VkWriteDescriptorSet createWriteDescriptorSet(long descriptorSet, ShaderResource shaderResource, ShaderResourceBinding shaderResourceBinding, MemoryStack memoryStack)
    {
        VkWriteDescriptorSet writeDescriptorSet = VkWriteDescriptorSet.calloc(memoryStack)
                .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                .dstSet(descriptorSet)
                .dstBinding(shaderResource.binding())
                .dstArrayElement(0)
                .descriptorCount(1)
                .descriptorType(shaderResource.type().getVulkanDescriptorType());

        switch (shaderResource.type())
        {
            case UNIFORM_BUFFER ->
            {
                if (shaderResourceBinding.getUniformBuffer() != null)
                {
                    UniformBuffer uniformBuffer = shaderResourceBinding.getUniformBuffer();
                    VkDescriptorBufferInfo.Buffer descriptorBufferInfo = VkDescriptorBufferInfo.calloc(1, memoryStack)
                            .buffer(uniformBuffer.getHandle())
                            .range(uniformBuffer.getSize())
                            .offset(0L);

                    writeDescriptorSet.pBufferInfo(descriptorBufferInfo);

                    return writeDescriptorSet;
                }
            }

            case COMBINED_IMAGE_SAMPLER ->
            {
                if (shaderResourceBinding.getTexture() != null)
                {
                    VulkanTexture vulkanTexture =  shaderResourceBinding.getTexture();
                    VkDescriptorImageInfo.Buffer descriptorImageInfo = VkDescriptorImageInfo.calloc(1, memoryStack)
                            .imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
                            .imageView(vulkanTexture.getImageView())
                            .sampler(vulkanTexture.getSampler());

                    writeDescriptorSet.pImageInfo(descriptorImageInfo);

                    return writeDescriptorSet;
                }
            }

            default -> throw new IllegalArgumentException("Unknown/Unsupported shader resource type: " + shaderResource.type());
        }

        return null;
    }

    private boolean areDirtyBindingsPresent()
    {
        return this.resourceBindings.values()
                .stream()
                .anyMatch(ShaderResourceBinding::isDirty);
    }
}
