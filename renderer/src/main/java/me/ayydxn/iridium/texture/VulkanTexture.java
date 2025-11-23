package me.ayydxn.iridium.texture;

import me.ayydxn.iridium.IridiumRenderer;
import me.ayydxn.iridium.renderer.CommandBuffer;
import me.ayydxn.iridium.renderer.memory.AllocatedBuffer;
import me.ayydxn.iridium.renderer.memory.AllocatedImage;
import me.ayydxn.iridium.renderer.memory.VulkanMemoryAllocator;
import me.ayydxn.iridium.renderer.utils.VulkanUtils;
import me.ayydxn.iridium.utils.IridiumConstants;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import static me.ayydxn.iridium.renderer.debug.VulkanDebugUtils.vkCheckResult;
import static org.lwjgl.util.vma.Vma.VMA_MEMORY_USAGE_CPU_TO_GPU;
import static org.lwjgl.util.vma.Vma.VMA_MEMORY_USAGE_GPU_ONLY;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanTexture
{
    private final VkDevice logicalDevice = IridiumRenderer.getInstance().getGraphicsContext().getGraphicsDevice().getLogicalDevice();
    private final VulkanMemoryAllocator vulkanMemoryAllocator = VulkanMemoryAllocator.getInstance();
    private final int width;
    private final int height;
    private final ImageFormat format;
    private final int usageFlags;

    private AllocatedImage allocatedImage;
    private long imageView;
    private long sampler;

    private VulkanTexture(int width, int height, ImageFormat format, int usageFlags)
    {
        this.width = width;
        this.height = height;
        this.format = format;
        this.usageFlags = usageFlags;
    }

    public void create(@NotNull ByteBuffer data)
    {
        int bytesPerPixel = this.width * this.height * 4;
        int dataSize = data.remaining();

        if (bytesPerPixel != dataSize)
            IridiumConstants.LOGGER.warn("Texture data size mismatch! Expected {} bytes, but got {} bytes", bytesPerPixel, dataSize);

        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            // Create and allocate the image
            VkImageCreateInfo imageCreateInfo = VkImageCreateInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO)
                    .imageType(VK_IMAGE_TYPE_2D)
                    .extent(VkExtent3D.calloc(memoryStack).set(this.width, this.height, 1))
                    .mipLevels(1)
                    .arrayLayers(1)
                    .format(this.format.getVulkanID())
                    .tiling(VK_IMAGE_TILING_OPTIMAL)
                    .initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
                    .usage(this.usageFlags | VK_IMAGE_USAGE_SAMPLED_BIT)
                    .sharingMode(VK_SHARING_MODE_EXCLUSIVE)
                    .samples(VK_SAMPLE_COUNT_1_BIT);

            this.allocatedImage = this.vulkanMemoryAllocator.allocateImage(imageCreateInfo, VMA_MEMORY_USAGE_GPU_ONLY);

            // Create the image view
            VkImageViewCreateInfo imageViewCreateInfo = VkImageViewCreateInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
                    .image(this.allocatedImage.image())
                    .viewType(VK_IMAGE_VIEW_TYPE_2D)
                    .format(this.format.getVulkanID());

            imageViewCreateInfo.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                    .baseMipLevel(0)
                    .levelCount(1)
                    .baseArrayLayer(0)
                    .layerCount(1);

            LongBuffer pImageView = memoryStack.longs(VK_NULL_HANDLE);
            vkCheckResult(vkCreateImageView(this.logicalDevice, imageViewCreateInfo, null, pImageView));

            this.imageView = pImageView.get(0);

            // Create the sampler
            VkSamplerCreateInfo samplerCreateInfo = VkSamplerCreateInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO)
                    .minFilter(VK_FILTER_NEAREST)
                    .magFilter(VK_FILTER_NEAREST)
                    .addressModeU(VK_SAMPLER_ADDRESS_MODE_REPEAT)
                    .addressModeV(VK_SAMPLER_ADDRESS_MODE_REPEAT)
                    .addressModeW(VK_SAMPLER_ADDRESS_MODE_REPEAT)
                    .borderColor(VK_BORDER_COLOR_INT_OPAQUE_WHITE)
                    .unnormalizedCoordinates(false)
                    .compareEnable(false)
                    .compareOp(VK_COMPARE_OP_ALWAYS)
                    .mipmapMode(VK_SAMPLER_MIPMAP_MODE_NEAREST)
                    .mipLodBias(0.0f)
                    .minLod(0.0f)
                    .maxLod(0.0f);

            LongBuffer pSampler = memoryStack.longs(VK_NULL_HANDLE);
            vkCheckResult(vkCreateSampler(this.logicalDevice, samplerCreateInfo, null, pSampler));

            this.sampler = pSampler.get(0);

            // Create a staging buffer
            VkBufferCreateInfo stagingBufferCreateInfo = VkBufferCreateInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
                    .size(bytesPerPixel)
                    .usage(VK_BUFFER_USAGE_TRANSFER_SRC_BIT)
                    .sharingMode(VK_SHARING_MODE_EXCLUSIVE);

            AllocatedBuffer stagingBuffer = this.vulkanMemoryAllocator.allocateBuffer(stagingBufferCreateInfo, VMA_MEMORY_USAGE_CPU_TO_GPU);

            PointerBuffer pDestinationBuffer = this.vulkanMemoryAllocator.mapMemory(stagingBuffer.bufferAllocation());

            MemoryUtil.memCopy(data, pDestinationBuffer.getByteBuffer(0, bytesPerPixel));

            this.vulkanMemoryAllocator.unmapMemory(stagingBuffer.bufferAllocation());

            // Copy the texture data to the image
            CommandBuffer commandBuffer = new CommandBuffer(1);
            commandBuffer.begin();

            VkCommandBuffer activeCommandBuffer = commandBuffer.getActiveCommandBuffer();

            VulkanUtils.transitionImageLayout(activeCommandBuffer, this.allocatedImage.image(), VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT,
                    0, VK_ACCESS_TRANSFER_WRITE_BIT, VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);

            VulkanUtils.copyBufferToImage(activeCommandBuffer, stagingBuffer.buffer(), this.allocatedImage.image(), this.width, this.height,
                    VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);

            VulkanUtils.transitionImageLayout(activeCommandBuffer, this.allocatedImage.image(), VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT,
                    VK_ACCESS_TRANSFER_WRITE_BIT, VK_ACCESS_SHADER_READ_BIT, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);

            commandBuffer.end();
            commandBuffer.submit();

            // Don't need the command buffer and staging buffer anymore, so destroy them
            this.vulkanMemoryAllocator.destroyBuffer(stagingBuffer);
            commandBuffer.destroy();
        }
    }

    public void destroy()
    {
        vkDestroyImageView(this.logicalDevice, this.imageView, null);
        vkDestroySampler(this.logicalDevice, this.sampler, null);

        this.vulkanMemoryAllocator.destroyImage(this.allocatedImage);
    }

    public long getImageView()
    {
        return this.imageView;
    }

    public long getSampler()
    {
        return this.sampler;
    }

    public static final class Builder
    {
        private int width, height;
        private ImageFormat format;
        private int usageFlags;

        public Builder()
        {
            // Set everything to default values
            this.width = 1;
            this.height = 1;
            this.format = ImageFormat.RGBA;
            this.usageFlags = VK_IMAGE_USAGE_TRANSFER_DST_BIT;
        }

        public Builder dimensions(int width, int height)
        {
            this.width = width;
            this.height = height;

            return this;
        }

        public Builder format(ImageFormat format)
        {
            this.format = format;

            return this;
        }

        public Builder usageFlags(int usageFlags)
        {
            this.usageFlags = usageFlags;

            return this;
        }

        public VulkanTexture build()
        {
            return new VulkanTexture(this.width, this.height, this.format, this.usageFlags);
        }
    }
}
