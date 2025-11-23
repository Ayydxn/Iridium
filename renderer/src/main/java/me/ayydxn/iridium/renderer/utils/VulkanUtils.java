package me.ayydxn.iridium.renderer.utils;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import static org.lwjgl.vulkan.KHRSynchronization2.*;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanUtils
{
    public static void transitionImageLayout(VkCommandBuffer commandBuffer, long image, int srcStageMask, int dstStageMask, int srcAccessMask, int dstAccessMask, int currentImageLayout, int newImageLayout)
    {
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            VkImageMemoryBarrier2KHR.Buffer imageMemoryBarrier = VkImageMemoryBarrier2KHR.calloc(1, memoryStack)
                    .sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER_2_KHR)
                    .srcStageMask(srcStageMask)
                    .dstStageMask(dstStageMask)
                    .srcAccessMask(srcAccessMask)
                    .dstAccessMask(dstAccessMask)
                    .srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
                    .dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
                    .oldLayout(currentImageLayout)
                    .newLayout(newImageLayout)
                    .image(image);

            imageMemoryBarrier.subresourceRange()
                    .aspectMask(newImageLayout == VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL ? VK_IMAGE_ASPECT_DEPTH_BIT : VK_IMAGE_ASPECT_COLOR_BIT)
                    .baseMipLevel(0)
                    .levelCount(1)
                    .baseArrayLayer(0)
                    .layerCount(1);

            VkDependencyInfo dependencyInfo = VkDependencyInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_DEPENDENCY_INFO_KHR)
                    .pImageMemoryBarriers(imageMemoryBarrier);

            vkCmdPipelineBarrier2KHR(commandBuffer, dependencyInfo);
        }
    }

    public static void copyBufferToImage(VkCommandBuffer commandBuffer, long srcBuffer, long dstImage, int dstImageWidth, int dstImageHeight, int dstImageLayout)
    {
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            VkBufferImageCopy.Buffer bufferImageCopy = VkBufferImageCopy.calloc(1, memoryStack)
                    .bufferOffset(0L)
                    .bufferRowLength(0)
                    .bufferImageHeight(0)
                    .imageOffset(VkOffset3D.calloc(memoryStack).set(0, 0, 0))
                    .imageExtent(VkExtent3D.calloc(memoryStack).set(dstImageWidth, dstImageHeight, 1));

            bufferImageCopy.imageSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                    .mipLevel(0)
                    .baseArrayLayer(0)
                    .layerCount(1);

            vkCmdCopyBufferToImage(commandBuffer, srcBuffer, dstImage, dstImageLayout, bufferImageCopy);
        }
    }
}
