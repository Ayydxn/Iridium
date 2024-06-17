package me.ayydan.iridium.render.vulkan;

import com.google.common.collect.Lists;
import me.ayydan.iridium.IridiumClientMod;
import me.ayydan.iridium.render.IridiumRenderer;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.util.List;

import static me.ayydan.iridium.render.vulkan.VulkanValidation.vkCheckResult;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanCommandBuffer
{
    private final List<VkCommandBuffer> commandBuffers;
    private final VkDevice logicalDevice;
    private final long commandPool;

    private VkCommandBuffer activeCommandBuffer;

    public VulkanCommandBuffer(int commandBufferCount)
    {
        if (commandBufferCount == 0)
            commandBufferCount = IridiumClientMod.getInstance().getGameOptions().rendererOptions.framesInFlight;

        this.commandBuffers = Lists.newArrayListWithCapacity(commandBufferCount);
        this.logicalDevice = IridiumRenderer.getInstance().getVulkanContext().getLogicalDevice().getHandle();

        this.commandPool = this.createCommandPool();

        this.allocateCommandBuffers(commandBufferCount);
    }

    public void destroy()
    {
        vkDeviceWaitIdle(this.logicalDevice);

        vkDestroyCommandPool(this.logicalDevice, this.commandPool, null);
    }

    public void begin()
    {
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            int currentFrameIndex = IridiumRenderer.getInstance().currentFrameIndex;
            VkCommandBuffer commandBuffer = this.commandBuffers.get(currentFrameIndex);

            VkCommandBufferBeginInfo commandBufferBeginInfo = VkCommandBufferBeginInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
                    .flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);

            vkCheckResult(vkBeginCommandBuffer(commandBuffer, commandBufferBeginInfo));

            this.activeCommandBuffer = commandBuffer;
        }
    }

    public void end()
    {
        int currentFrameIndex = IridiumRenderer.getInstance().currentFrameIndex;
        VkCommandBuffer commandBuffer = this.commandBuffers.get(currentFrameIndex);

        vkCheckResult(vkEndCommandBuffer(commandBuffer));

        this.activeCommandBuffer = null;
    }

    public void submit()
    {
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            VkQueue graphicsQueue = IridiumRenderer.getInstance().getVulkanContext().getLogicalDevice().getGraphicsQueue();
            VkCommandBuffer commandBuffer = this.commandBuffers.get(IridiumRenderer.getInstance().currentFrameIndex);

            VkSubmitInfo commandBufferSubmitInfo = VkSubmitInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
                    .pCommandBuffers(memoryStack.pointers(commandBuffer));

            vkCheckResult(vkQueueSubmit(graphicsQueue, commandBufferSubmitInfo, VK_NULL_HANDLE));

            vkQueueWaitIdle(graphicsQueue);
        }
    }

    public VkCommandBuffer getCommandBuffer()
    {
        return this.commandBuffers.get(IridiumRenderer.getInstance().currentFrameIndex);
    }

    public VkCommandBuffer getActiveCommandBuffer()
    {
        return this.activeCommandBuffer;
    }

    private long createCommandPool()
    {
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            QueueFamilyIndices queueFamilyIndices = QueueFamilyIndices.findQueueFamilies(this.logicalDevice.getPhysicalDevice());

            VkCommandPoolCreateInfo commandPoolCreateInfo = VkCommandPoolCreateInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO)
                    .queueFamilyIndex(queueFamilyIndices.getGraphicsFamily())
                    .flags(VK_COMMAND_POOL_CREATE_TRANSIENT_BIT | VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);

            LongBuffer pCommandPool = memoryStack.longs(VK_NULL_HANDLE);
            vkCheckResult(vkCreateCommandPool(this.logicalDevice, commandPoolCreateInfo, null, pCommandPool));

            return pCommandPool.get(0);
        }
    }

    private void allocateCommandBuffers(int commandBufferCount)
    {
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            VkCommandBufferAllocateInfo commandBufferAllocateInfo = VkCommandBufferAllocateInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
                    .commandPool(this.commandPool)
                    .level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                    .commandBufferCount(commandBufferCount);

            PointerBuffer pCommandBuffers = memoryStack.mallocPointer(commandBufferCount);
            vkCheckResult(vkAllocateCommandBuffers(this.logicalDevice, commandBufferAllocateInfo, pCommandBuffers));

            for (int i = 0; i < commandBufferCount; i++)
                this.commandBuffers.add(new VkCommandBuffer(pCommandBuffers.get(i), this.logicalDevice));
        }
    }
}
