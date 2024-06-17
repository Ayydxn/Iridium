package me.ayydan.iridium.render.vulkan.utils;

import com.mojang.blaze3d.vertex.VertexFormatElement;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDependencyInfo;
import org.lwjgl.vulkan.VkImageMemoryBarrier2;

import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK13.*;

public class VulkanUtils
{
    public static void transitionImageLayout(VkCommandBuffer commandBuffer, long image, int currentImageLayout, int newImageLayout)
    {
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            VkImageMemoryBarrier2.Buffer imageMemoryBarrier = VkImageMemoryBarrier2.calloc(1, memoryStack)
                    .sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER_2)
                    .srcStageMask(VK_PIPELINE_STAGE_2_ALL_COMMANDS_BIT)
                    .dstStageMask(VK_PIPELINE_STAGE_2_ALL_COMMANDS_BIT)
                    .srcAccessMask(VK_ACCESS_2_MEMORY_WRITE_BIT)
                    .dstAccessMask(VK_ACCESS_2_MEMORY_WRITE_BIT | VK_ACCESS_2_MEMORY_READ_BIT)
                    .srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
                    .dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
                    .oldLayout(currentImageLayout)
                    .newLayout(newImageLayout)
                    .image(image);

            imageMemoryBarrier.subresourceRange()
                    .aspectMask(newImageLayout == VK_IMAGE_LAYOUT_DEPTH_ATTACHMENT_OPTIMAL ? VK_IMAGE_ASPECT_DEPTH_BIT : VK_IMAGE_ASPECT_COLOR_BIT)
                    .baseMipLevel(0)
                    .levelCount(1)
                    .baseArrayLayer(0)
                    .layerCount(1);

            VkDependencyInfo dependencyInfo = VkDependencyInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_DEPENDENCY_INFO)
                    .pImageMemoryBarriers(imageMemoryBarrier);

            vkCmdPipelineBarrier2(commandBuffer, dependencyInfo);
        }
    }

    public static int getVertexFormatElementVulkanFormat(VertexFormatElement vertexFormatElement)
    {
        VertexFormatElement.Type elementDataType = vertexFormatElement.getType();

        return switch (vertexFormatElement.getUsage())
        {
            case POSITION ->
            {
                if (elementDataType == VertexFormatElement.Type.FLOAT)
                    yield VK_FORMAT_R32G32B32_SFLOAT;

                if (elementDataType == VertexFormatElement.Type.SHORT)
                    yield VK_FORMAT_R16G16B16A16_SINT;

                if (elementDataType == VertexFormatElement.Type.BYTE)
                    yield VK_FORMAT_R8G8B8A8_SINT;

                // Return undefined if we don't have a format for a specific data type.
                yield VK_FORMAT_UNDEFINED;
            }

            case COLOR -> VK_FORMAT_R8G8B8A8_UNORM;

            case UV ->
            {
                if (elementDataType == VertexFormatElement.Type.FLOAT)
                    yield VK_FORMAT_R32G32_SFLOAT;

                if (elementDataType == VertexFormatElement.Type.SHORT)
                    yield VK_FORMAT_R16G16_SINT;

                if (elementDataType == VertexFormatElement.Type.USHORT)
                    yield VK_FORMAT_R16G16_UINT;

                // Return undefined if we don't have a format for a specific data type.
                yield VK_FORMAT_UNDEFINED;
            }

            case NORMAL -> VK_FORMAT_R8G8B8A8_SNORM;

            case PADDING, GENERIC -> VK_FORMAT_UNDEFINED;

            default -> throw new IllegalArgumentException("Failed to get Vulkan format for vertex format element: " + vertexFormatElement.getType());
        };
    }
}
