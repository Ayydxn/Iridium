package me.ayydan.iridium.render.vulkan;

import com.google.common.collect.Lists;
import me.ayydan.iridium.IridiumClientMod;
import me.ayydan.iridium.event.WindowResizeEvent;
import me.ayydan.iridium.render.IridiumRenderer;
import me.ayydan.iridium.render.exceptions.IridiumRendererException;
import me.ayydan.iridium.render.memory.AllocatedImage;
import me.ayydan.iridium.utils.IridiumConstants;
import net.minecraft.client.Minecraft;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;

import static me.ayydan.iridium.render.vulkan.VulkanValidation.vkCheckResult;
import static org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanSwapChain implements WindowResizeEvent
{
    private final VkInstance vulkanInstance;
    private final VulkanPhysicalDevice vulkanPhysicalDevice;
    private final VulkanLogicalDevice vulkanLogicalDevice;
    private final VulkanMemoryAllocator vulkanMemoryAllocator;

    private ArrayList<Long> swapChainImages;
    private ArrayList<Long> swapChainImageViews;
    private VkExtent2D swapChainExtent;
    private AllocatedImage swapChainDepthImage;
    private long swapChainDepthImageView;
    private int swapChainImageFormat;
    private int swapChainColorSpace;

    private ArrayList<Long> waitFences;
    private long presentCompleteSemaphore;
    private long renderCompleteSemaphore;
    private int width;
    private int height;
    private boolean isVSyncEnabled;
    private boolean isInitialized;

    private long windowSurface;
    private int presentFamily;
    private long swapChainHandle;

    public VulkanSwapChain(VulkanContext vulkanContext)
    {
        this.vulkanInstance = vulkanContext.getVulkanInstance();
        this.vulkanPhysicalDevice = vulkanContext.getPhysicalDevice();
        this.vulkanLogicalDevice = vulkanContext.getLogicalDevice();
        this.vulkanMemoryAllocator = VulkanMemoryAllocator.getInstance();

        this.width = 0;
        this.height = 0;
        this.isVSyncEnabled = true;
        this.isInitialized = false;

        this.windowSurface = VK_NULL_HANDLE;
        this.swapChainHandle = VK_NULL_HANDLE;
    }

    public void initialize(long window)
    {
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            /*-------------------------------*/
            /* -- Create a window surface -- */
            /*-------------------------------*/

            LongBuffer pWindowSurface = memoryStack.longs(VK_NULL_HANDLE);
            vkCheckResult(glfwCreateWindowSurface(this.vulkanInstance, window, null, pWindowSurface));

            this.windowSurface = pWindowSurface.get(0);

            /*-------------------------------------------------------------------*/
            /* -- Check if the selected physical device supports presentation -- */
            /*-------------------------------------------------------------------*/

            IntBuffer queueFamilyCount = memoryStack.ints(0);
            vkGetPhysicalDeviceQueueFamilyProperties(this.vulkanPhysicalDevice.getHandle(), queueFamilyCount, null);

            IntBuffer physicalDeviceSurfaceSupport = memoryStack.ints(VK_FALSE);
            boolean isPresentingSupported = false;

            for (int i = 0; i < queueFamilyCount.get(0); i++)
            {
                vkGetPhysicalDeviceSurfaceSupportKHR(this.vulkanPhysicalDevice.getHandle(), i, this.windowSurface, physicalDeviceSurfaceSupport);

                if (physicalDeviceSurfaceSupport.get(0) == VK_TRUE)
                {
                    this.presentFamily = i;

                    isPresentingSupported = true;
                    break;
                }
            }

            if (!isPresentingSupported)
                throw new IridiumRendererException("The selected physical device doesn't support presenting to the window surface!");
        }

        this.selectImageFormatAndColorSpace();

        WindowResizeEvent.EVENT.register(this);

        this.isInitialized = true;
    }

    public void create(int width, int height, boolean enableVSync)
    {
        if (!this.isInitialized)
            throw new IllegalStateException("You must initialize the Vulkan swap chain before creating it!");

        this.isVSyncEnabled = enableVSync;

        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            long oldSwapChain = this.swapChainHandle;
            QueueFamilyIndices queueFamilyIndices = QueueFamilyIndices.findQueueFamilies(this.vulkanPhysicalDevice.getHandle());
            VkDevice logicalDevice = this.vulkanLogicalDevice.getHandle();

            VkSurfaceCapabilitiesKHR surfaceCapabilities = VkSurfaceCapabilitiesKHR.calloc(memoryStack);
            vkGetPhysicalDeviceSurfaceCapabilitiesKHR(this.vulkanPhysicalDevice.getHandle(), this.windowSurface, surfaceCapabilities);

            IntBuffer presentModeCount = memoryStack.ints(0);
            vkGetPhysicalDeviceSurfacePresentModesKHR(this.vulkanPhysicalDevice.getHandle(), this.windowSurface, presentModeCount, null);

            IntBuffer presentModes = memoryStack.mallocInt(presentModeCount.get(0));
            vkGetPhysicalDeviceSurfacePresentModesKHR(this.vulkanPhysicalDevice.getHandle(), this.windowSurface, presentModeCount, presentModes);

            VkExtent2D swapChainExtent = VkExtent2D.calloc(memoryStack);

            // If the width (and height) the extent of the surface capabilities is the max value of an uint32, the extent will use the specified width and height.
            if (surfaceCapabilities.currentExtent().width() == IridiumConstants.UINT32_MAX)
            {
                swapChainExtent.width(width);
                swapChainExtent.height(height);
            }
            else // The extent of the surface capabilities has proper values, we'll use that.
            {
                swapChainExtent = surfaceCapabilities.currentExtent();

                width = surfaceCapabilities.currentExtent().width();
                height = surfaceCapabilities.currentExtent().height();
            }

            this.swapChainExtent = swapChainExtent;
            this.width = width;
            this.height = height;

            if (width == 0 || height == 0)
                return;

            /*
             * VK_PRESENT_MODE_FIFO_KHR is guaranteed to be available as per the Vulkan specification.
             *
             * If we aren't enabling VSync, we use it as a fallback in the event that neither VK_PRESENT_MODE_MAILBOX_KHR or VK_PRESENT_MODE_IMMEDIATE_KHR are available.
             */
            int swapChainPresentMode = VK_PRESENT_MODE_FIFO_KHR;

            if (!enableVSync)
            {
                for (int i = 0; i < presentModeCount.get(0); i++)
                {
                    // If mailbox is supported, we use it.
                    if (presentModes.get(i) == VK_PRESENT_MODE_MAILBOX_KHR)
                    {
                        swapChainPresentMode = VK_PRESENT_MODE_MAILBOX_KHR;
                        break;
                    }

                    // If mailbox isn't supported, we fall back to immediate.
                    if (presentModes.get(i) == VK_PRESENT_MODE_IMMEDIATE_KHR)
                    {
                        swapChainPresentMode = VK_PRESENT_MODE_IMMEDIATE_KHR;
                        break;
                    }
                }
            }

            int minimumSwapChainImageCount = surfaceCapabilities.minImageCount() + 1;
            if (surfaceCapabilities.maxImageCount() > 0 && minimumSwapChainImageCount > surfaceCapabilities.maxImageCount())
                minimumSwapChainImageCount = surfaceCapabilities.maxImageCount();

            int swapChainPreTransform;
            if ((surfaceCapabilities.supportedTransforms() & VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR) == VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR)
            {
                swapChainPreTransform = VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR;
            }
            else
            {
                swapChainPreTransform = surfaceCapabilities.currentTransform();
            }

            int swapChainCompositeAlpha = 0;

            // While you would normally use VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR, not all devices support it.
            // So we just select the first one from this list that is available.
            ArrayList<Integer> compositeAlphas = Lists.newArrayList(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR, VK_COMPOSITE_ALPHA_PRE_MULTIPLIED_BIT_KHR,
                    VK_COMPOSITE_ALPHA_POST_MULTIPLIED_BIT_KHR, VK_COMPOSITE_ALPHA_INHERIT_BIT_KHR);

            for (int compositeAlpha : compositeAlphas)
            {
                if ((surfaceCapabilities.supportedCompositeAlpha() & compositeAlpha) == compositeAlpha)
                {
                    swapChainCompositeAlpha = compositeAlpha;
                    break;
                }
            }

            int swapChainImageUsage = VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT;

            if ((surfaceCapabilities.supportedUsageFlags() & VK_IMAGE_USAGE_TRANSFER_SRC_BIT) == VK_IMAGE_USAGE_TRANSFER_SRC_BIT)
                swapChainImageUsage |= VK_IMAGE_USAGE_TRANSFER_SRC_BIT;

            if ((surfaceCapabilities.supportedUsageFlags() & VK_IMAGE_USAGE_TRANSFER_DST_BIT) == VK_IMAGE_USAGE_TRANSFER_DST_BIT)
                swapChainImageUsage |= VK_IMAGE_USAGE_TRANSFER_DST_BIT;

            /*-------------------------------*/
            /* -- Creating the swap chain -- */
            /*-------------------------------*/

            VkSwapchainCreateInfoKHR swapChainCreateInfo = VkSwapchainCreateInfoKHR.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
                    .surface(this.windowSurface)
                    .minImageCount(minimumSwapChainImageCount)
                    .imageFormat(this.swapChainImageFormat)
                    .imageColorSpace(this.swapChainColorSpace)
                    .imageExtent(swapChainExtent)
                    .imageArrayLayers(1)
                    .imageUsage(swapChainImageUsage);

            if (queueFamilyIndices.getGraphicsFamily() != this.presentFamily)
            {
                swapChainCreateInfo.imageSharingMode(VK_SHARING_MODE_CONCURRENT)
                        .queueFamilyIndexCount(2)
                        .pQueueFamilyIndices(memoryStack.ints(queueFamilyIndices.getGraphicsFamily(), this.presentFamily));
            }
            else
            {
                swapChainCreateInfo.imageSharingMode(VK_SHARING_MODE_EXCLUSIVE)
                        .queueFamilyIndexCount(0)
                        .pQueueFamilyIndices(null);
            }

            swapChainCreateInfo.preTransform(swapChainPreTransform)
                    .compositeAlpha(swapChainCompositeAlpha)
                    .presentMode(swapChainPresentMode)
                    .clipped(true)
                    .oldSwapchain(oldSwapChain);

            LongBuffer pSwapChain = memoryStack.longs(VK_NULL_HANDLE);
            vkCheckResult(vkCreateSwapchainKHR(this.vulkanLogicalDevice.getHandle(), swapChainCreateInfo, null, pSwapChain));

            this.swapChainHandle = pSwapChain.get(0);

            if (oldSwapChain != VK_NULL_HANDLE)
                vkDestroySwapchainKHR(logicalDevice, oldSwapChain, null);

            if (this.swapChainImageViews != null)
            {
                for (long imageView : this.swapChainImageViews)
                    vkDestroyImageView(logicalDevice, imageView, null);
            }

            IntBuffer swapChainImageCount = memoryStack.ints(0);
            vkGetSwapchainImagesKHR(logicalDevice, this.swapChainHandle, swapChainImageCount, null);

            LongBuffer pSwapChainImages = memoryStack.mallocLong(swapChainImageCount.get(0));
            vkGetSwapchainImagesKHR(logicalDevice, this.swapChainHandle, swapChainImageCount, pSwapChainImages);

            this.swapChainImages = new ArrayList<>(swapChainImageCount.get(0));

            for (int i = 0; i < swapChainImageCount.get(0); i++)
                this.swapChainImages.add(pSwapChainImages.get(i));

            this.createImageViews();
            this.createDepthImageAndImageView();

            if (this.presentCompleteSemaphore == VK_NULL_HANDLE || this.renderCompleteSemaphore == VK_NULL_HANDLE)
            {
                LongBuffer pPresentAvailableSemaphore = memoryStack.longs(VK_NULL_HANDLE);
                LongBuffer pRenderFinishedSemaphore = memoryStack.longs(VK_NULL_HANDLE);

                VkSemaphoreCreateInfo semaphoreCreateInfo = VkSemaphoreCreateInfo.calloc(memoryStack);
                semaphoreCreateInfo.sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);

                vkCheckResult(vkCreateSemaphore(logicalDevice, semaphoreCreateInfo, null, pPresentAvailableSemaphore));
                vkCheckResult(vkCreateSemaphore(logicalDevice, semaphoreCreateInfo, null, pRenderFinishedSemaphore));

                this.presentCompleteSemaphore = pPresentAvailableSemaphore.get(0);
                this.renderCompleteSemaphore = pRenderFinishedSemaphore.get(0);
            }

            if (this.waitFences == null)
            {
                this.waitFences = new ArrayList<>(this.swapChainImages.size());

                LongBuffer pWaitFence = memoryStack.longs(VK_NULL_HANDLE);

                VkFenceCreateInfo fenceCreateInfo = VkFenceCreateInfo.calloc(memoryStack);
                fenceCreateInfo.sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);
                fenceCreateInfo.flags(VK_FENCE_CREATE_SIGNALED_BIT);

                for (int i = 0; i < this.swapChainImages.size(); i++)
                {
                    vkCheckResult(vkCreateFence(logicalDevice, fenceCreateInfo, null, pWaitFence));

                    this.waitFences.add(pWaitFence.get(0));
                }
            }
        }
    }

    public void destroy()
    {
        this.vulkanLogicalDevice.waitIdle();

        this.vulkanMemoryAllocator.destroyImage(this.swapChainDepthImage);

        vkDestroySurfaceKHR(this.vulkanInstance, this.windowSurface, null);
        vkDestroySwapchainKHR(this.vulkanLogicalDevice.getHandle(), this.swapChainHandle, null);
        vkDestroySemaphore(this.vulkanLogicalDevice.getHandle(), this.presentCompleteSemaphore, null);
        vkDestroySemaphore(this.vulkanLogicalDevice.getHandle(), this.renderCompleteSemaphore, null);
        vkDestroyImageView(this.vulkanLogicalDevice.getHandle(), this.swapChainDepthImageView, null);

        for (long swapChainImageView : this.swapChainImageViews)
            vkDestroyImageView(this.vulkanLogicalDevice.getHandle(), swapChainImageView, null);

        for (long waitFence : this.waitFences)
            vkDestroyFence(this.vulkanLogicalDevice.getHandle(), waitFence, null);

        this.vulkanLogicalDevice.waitIdle();
    }

    public void present()
    {
        if (IridiumRenderer.getInstance().isCurrentFrameBeingSkipped())
            return;

        VkDevice logicalDevice = this.vulkanLogicalDevice.getHandle();
        int currentSwapChainImageIndex = this.acquireNextImage();
        int currentFrameIndex = IridiumRenderer.getInstance().currentFrameIndex;
        long waitFence = this.waitFences.get(currentFrameIndex);

        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            VkSubmitInfo submitInfo = VkSubmitInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
                    .pWaitDstStageMask(memoryStack.ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT))
                    .pWaitSemaphores(memoryStack.longs(this.presentCompleteSemaphore))
                    .waitSemaphoreCount(1)
                    .pSignalSemaphores(memoryStack.longs(this.renderCompleteSemaphore));

            VkPresentInfoKHR presentInfo = VkPresentInfoKHR.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
                    .pSwapchains(memoryStack.longs(this.swapChainHandle))
                    .swapchainCount(1)
                    .pImageIndices(memoryStack.ints(currentSwapChainImageIndex))
                    .pWaitSemaphores(memoryStack.longs(this.renderCompleteSemaphore));

            vkCheckResult(vkResetFences(logicalDevice, waitFence));
            vkCheckResult(vkQueueSubmit(this.vulkanLogicalDevice.getGraphicsQueue(), submitInfo, waitFence));

            int presentResult = vkQueuePresentKHR(this.vulkanLogicalDevice.getGraphicsQueue(), presentInfo);

            if (presentResult != VK_SUCCESS)
            {
                if (presentResult == VK_ERROR_OUT_OF_DATE_KHR || presentResult == VK_SUBOPTIMAL_KHR)
                {
                    this.onWindowResize(this.width, this.height);
                }
                else
                {
                    vkCheckResult(presentResult);
                }
            }
        }

        IridiumRenderer.getInstance().currentFrameIndex = (IridiumRenderer.getInstance().currentFrameIndex + 1) % IridiumClientMod.getInstance().getGameOptions().rendererOptions.framesInFlight;

        // Wait and make sure that the frame that we're going to present has finished rendering.
        vkCheckResult(vkWaitForFences(logicalDevice, waitFence, true, IridiumConstants.UINT64_MAX));
    }

    public void enableVSync(boolean enableVSync)
    {
        this.isVSyncEnabled = enableVSync;

        this.vulkanLogicalDevice.waitIdle();

        this.create(this.width, this.height, enableVSync);
    }

    private void selectImageFormatAndColorSpace()
    {
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            IntBuffer surfaceFormatCount = memoryStack.ints(0);
            vkGetPhysicalDeviceSurfaceFormatsKHR(this.vulkanPhysicalDevice.getHandle(), this.windowSurface, surfaceFormatCount, null);

            if (surfaceFormatCount.get(0) == 0)
            {
                throw new IridiumRendererException(String.format("The selected physical device (%s) has no available surface formats!",
                        this.vulkanPhysicalDevice.getProperties().deviceNameString()));
            }

            VkSurfaceFormatKHR.Buffer surfaceFormats = VkSurfaceFormatKHR.calloc(surfaceFormatCount.get(0), memoryStack);
            vkGetPhysicalDeviceSurfaceFormatsKHR(this.vulkanPhysicalDevice.getHandle(), this.windowSurface, surfaceFormatCount, surfaceFormats);

            boolean foundB8G8R8A8Format = false;
            for (VkSurfaceFormatKHR surfaceFormat : surfaceFormats)
            {
                // If we find VK_FORMAT_B8G8R8A8_UNORM, we use that and the associated color space.
                if (surfaceFormat.format() == VK_FORMAT_B8G8R8A8_UNORM)
                {
                    this.swapChainImageFormat = surfaceFormat.format();
                    this.swapChainColorSpace = surfaceFormat.colorSpace();

                    foundB8G8R8A8Format = true;
                    break;
                }
            }

            // If we failed to find VK_FORMAT_B8G8R8A8_UNORM, we use the image format and color space of the first surface format.
            if (!foundB8G8R8A8Format)
            {
                this.swapChainImageFormat = surfaceFormats.get(0).format();
                this.swapChainColorSpace = surfaceFormats.get(0).colorSpace();
            }
        }
    }

    private void createImageViews()
    {
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            this.swapChainImageViews = new ArrayList<>(this.swapChainImages.size());

            LongBuffer pImageView = memoryStack.mallocLong(1);

            for (Long swapChainImage : this.swapChainImages)
            {
                VkComponentMapping componentMapping = VkComponentMapping.calloc(memoryStack).set(VK_COMPONENT_SWIZZLE_R, VK_COMPONENT_SWIZZLE_G,
                        VK_COMPONENT_SWIZZLE_B, VK_COMPONENT_SWIZZLE_A);

                VkImageViewCreateInfo imageViewCreateInfo = VkImageViewCreateInfo.calloc(memoryStack)
                        .sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
                        .image(swapChainImage)
                        .viewType(VK_IMAGE_VIEW_TYPE_2D)
                        .format(this.swapChainImageFormat)
                        .components(componentMapping);

                imageViewCreateInfo.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                        .baseMipLevel(0)
                        .levelCount(1)
                        .baseArrayLayer(0)
                        .layerCount(1);

                vkCheckResult(vkCreateImageView(this.vulkanLogicalDevice.getHandle(), imageViewCreateInfo, null, pImageView));

                this.swapChainImageViews.add(pImageView.get(0));
            }
        }
    }

    private void createDepthImageAndImageView()
    {
        VkDevice logicalDevice = this.vulkanLogicalDevice.getHandle();

        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            VkImageCreateInfo imageCreateInfo = VkImageCreateInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO)
                    .imageType(VK_IMAGE_TYPE_2D)
                    .extent(VkExtent3D.calloc(memoryStack).set(this.swapChainExtent.width(), this.swapChainExtent.height(), 1))
                    .mipLevels(1)
                    .arrayLayers(1)
                    .format(this.vulkanPhysicalDevice.getDepthFormat())
                    .tiling(VK_IMAGE_TILING_OPTIMAL)
                    .initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
                    .usage(VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT)
                    .samples(VK_SAMPLE_COUNT_1_BIT) // TODO: (Ayydan) When multisampling is configurable, make this match the config's value.
                    .sharingMode(VK_SHARING_MODE_EXCLUSIVE);

            AllocatedImage depthImage = this.vulkanMemoryAllocator.allocateImage(imageCreateInfo);

            VkImageViewCreateInfo imageViewCreateInfo = VkImageViewCreateInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
                    .image(depthImage.image())
                    .viewType(VK_IMAGE_VIEW_TYPE_2D)
                    .format(this.vulkanPhysicalDevice.getDepthFormat());

            imageViewCreateInfo.subresourceRange().aspectMask(VK_IMAGE_ASPECT_DEPTH_BIT)
                    .baseMipLevel(0)
                    .levelCount(1)
                    .baseArrayLayer(0)
                    .layerCount(1);

            LongBuffer pDepthImageView = memoryStack.longs(VK_NULL_HANDLE);
            vkCheckResult(vkCreateImageView(logicalDevice, imageViewCreateInfo, null, pDepthImageView));

            this.swapChainDepthImage = depthImage;
            this.swapChainDepthImageView = pDepthImageView.get(0);
        }
    }

    private int acquireNextImage()
    {
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            VkDevice logicalDevice = this.vulkanLogicalDevice.getHandle();

            IntBuffer pSwapChainImageIndex = memoryStack.ints(-1);
            int acquireNextImageResult = vkAcquireNextImageKHR(logicalDevice, this.swapChainHandle, IridiumConstants.UINT64_MAX, this.presentCompleteSemaphore,
                    VK_NULL_HANDLE, pSwapChainImageIndex);

            if (acquireNextImageResult != VK_SUCCESS)
            {
                if (acquireNextImageResult == VK_ERROR_OUT_OF_DATE_KHR || acquireNextImageResult == VK_SUBOPTIMAL_KHR)
                {
                    this.onWindowResize(this.width, this.height);

                    vkCheckResult(vkAcquireNextImageKHR(logicalDevice, this.swapChainHandle, IridiumConstants.UINT64_MAX, this.presentCompleteSemaphore, VK_NULL_HANDLE,
                            pSwapChainImageIndex));
                }
            }

            return pSwapChainImageIndex.get(0);
        }
    }

    @Override
    public void onWindowResize(int newWindowWidth, int newWindowHeight)
    {
        this.width = newWindowWidth;
        this.height = newWindowHeight;

        this.vulkanLogicalDevice.waitIdle();

        this.create(newWindowWidth, newWindowHeight, this.isVSyncEnabled);
    }

    public ArrayList<Long> getImages()
    {
        return this.swapChainImages;
    }

    public ArrayList<Long> getImageViews()
    {
        return this.swapChainImageViews;
    }

    public VkExtent2D getExtent()
    {
        return this.swapChainExtent;
    }

    public AllocatedImage getDepthImage()
    {
        return this.swapChainDepthImage;
    }

    public long getDepthImageView()
    {
        return this.swapChainDepthImageView;
    }

    public int getImageFormat()
    {
        return this.swapChainImageFormat;
    }

    public int getWidth()
    {
        return this.width;
    }

    public int getHeight()
    {
        return this.height;
    }
}
