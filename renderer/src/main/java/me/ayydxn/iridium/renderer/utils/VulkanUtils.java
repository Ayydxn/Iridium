package me.ayydxn.iridium.renderer.utils;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDependencyInfo;
import org.lwjgl.vulkan.VkImageMemoryBarrier2KHR;

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
}
