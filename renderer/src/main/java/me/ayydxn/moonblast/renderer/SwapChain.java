package me.ayydxn.moonblast.renderer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import me.ayydxn.moonblast.MoonblastRenderer;
import me.ayydxn.moonblast.options.MoonblastRendererOptions;
import me.ayydxn.moonblast.renderer.exceptions.MoonblastRendererException;
import me.ayydxn.moonblast.renderer.utils.QueueFamilyIndices;
import me.ayydxn.moonblast.utils.MoonblastConstants;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.List;

import static me.ayydxn.moonblast.renderer.debug.VulkanDebugUtils.vkCheckResult;
import static org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

public class SwapChain
{
    private final GraphicsContext graphicsContext;
    private final GraphicsDevice graphicsDevice;

    private List<Long> swapChainImages;
    private List<Long> swapChainImageViews;
    private List<Long> waitFences;
    private VkExtent2D swapChainExtent;
    private long presentCompleteSemaphore;
    private long renderCompleteSemaphore;
    private int swapChainImageFormat;
    private int swapChainColorSpace;

    private int width;
    private int height;
    private boolean vSync;
    private boolean isInitialized;

    private long swapChainHandle;
    private long windowSurface;
    private int presentFamily;

    public SwapChain()
    {
        this.graphicsContext = MoonblastRenderer.getInstance().getGraphicsContext();
        this.graphicsDevice = graphicsContext.getGraphicsDevice();

        this.width = 0;
        this.height = 0;
        this.vSync = false;
        this.isInitialized = false;

        this.swapChainHandle = VK_NULL_HANDLE;
        this.windowSurface = VK_NULL_HANDLE;
        this.presentFamily = 0;
    }

    public void initialize()
    {
        this.windowSurface = this.createWindowSurface(MoonblastRenderer.getInstance().getWindowHandle(), this.graphicsContext.getVulkanInstance());

        if (!this.isPresentationSupported(this.graphicsDevice.getPhysicalDevice(), this.windowSurface))
            throw new MoonblastRendererException("The selected physical device doesn't support presenting to a window surface!");

        this.selectImageFormatAndColorSpace();

        this.vSync = MoonblastRenderer.getInstance().getOptions().rendererOptions.enableVSync;
        this.isInitialized = true;
    }

    public void create(int width, int height)
    {
        if (!this.isInitialized)
            throw new IllegalStateException("You must initialize a swapchain before creating it!");

        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            QueueFamilyIndices queueFamilyIndices = QueueFamilyIndices.findQueueFamilies(this.graphicsDevice.getPhysicalDevice());
            long oldSwapChain = this.swapChainHandle;

            VkSurfaceCapabilitiesKHR surfaceCapabilities = VkSurfaceCapabilitiesKHR.calloc(memoryStack);
            vkGetPhysicalDeviceSurfaceCapabilitiesKHR(this.graphicsDevice.getPhysicalDevice(), this.windowSurface, surfaceCapabilities);

            IntBuffer presentModeCount = memoryStack.ints(0);
            vkGetPhysicalDeviceSurfacePresentModesKHR(this.graphicsDevice.getPhysicalDevice(), this.windowSurface, presentModeCount, null);

            IntBuffer presentModes = memoryStack.mallocInt(presentModeCount.get(0));
            vkGetPhysicalDeviceSurfacePresentModesKHR(this.graphicsDevice.getPhysicalDevice(), this.windowSurface, presentModeCount, presentModes);

            VkExtent2D swapChainExtent = VkExtent2D.calloc(memoryStack);

            if (surfaceCapabilities.currentExtent().width() == MoonblastConstants.UINT32_MAX)
            {
                swapChainExtent.width(width);
                swapChainExtent.height(height);
            }
            else
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

            if (!this.vSync)
            {
                for (int i = 0; i < presentModeCount.get(0); i++)
                {
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

            int minimumImageCount = surfaceCapabilities.minImageCount() + 1;
            if (surfaceCapabilities.maxImageCount() > 0 && minimumImageCount > surfaceCapabilities.maxImageCount())
                minimumImageCount = surfaceCapabilities.maxImageCount();

            int swapChainPreTransform;
            if ((surfaceCapabilities.supportedTransforms() & VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR) == VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR)
            {
                swapChainPreTransform = VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR;
            }
            else
            {
                swapChainPreTransform = surfaceCapabilities.currentTransform();
            }

            Preconditions.checkState(swapChainPreTransform != -1);

            int swapChainCompositeAlpha = -1;

            // While you would normally use VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR, not all devices support it.
            // So we just select the first one from this list that is available.
            List<Integer> compositeAlphas = Lists.newArrayList(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR, VK_COMPOSITE_ALPHA_PRE_MULTIPLIED_BIT_KHR,
                    VK_COMPOSITE_ALPHA_POST_MULTIPLIED_BIT_KHR, VK_COMPOSITE_ALPHA_INHERIT_BIT_KHR);

            for (int compositeAlpha : compositeAlphas)
            {
                if ((surfaceCapabilities.supportedCompositeAlpha() & compositeAlpha) == compositeAlpha)
                {
                    swapChainCompositeAlpha = compositeAlpha;
                    break;
                }
            }

            Preconditions.checkState(swapChainCompositeAlpha != -1);

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
                    .minImageCount(minimumImageCount)
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
            vkCheckResult(vkCreateSwapchainKHR(this.graphicsDevice.getLogicalDevice(), swapChainCreateInfo, null, pSwapChain));

            this.swapChainHandle = pSwapChain.get(0);

            if (oldSwapChain != VK_NULL_HANDLE)
                vkDestroySwapchainKHR(this.graphicsDevice.getLogicalDevice(), oldSwapChain, null);

            if (this.swapChainImageViews != null)
            {
                for (long imageView : this.swapChainImageViews)
                    vkDestroyImageView(this.graphicsDevice.getLogicalDevice(), imageView, null);
            }

            IntBuffer swapChainImageCount = memoryStack.ints(0);
            vkGetSwapchainImagesKHR(this.graphicsDevice.getLogicalDevice(), this.swapChainHandle, swapChainImageCount, null);

            LongBuffer pSwapChainImages = memoryStack.mallocLong(swapChainImageCount.get(0));
            vkGetSwapchainImagesKHR(this.graphicsDevice.getLogicalDevice(), this.swapChainHandle, swapChainImageCount, pSwapChainImages);

            this.swapChainImages = Lists.newArrayListWithCapacity(swapChainImageCount.get(0));

            for (int i = 0; i < swapChainImageCount.get(0); i++)
                this.swapChainImages.add(pSwapChainImages.get(i));

            this.createImageViews();

            if (this.presentCompleteSemaphore == VK_NULL_HANDLE || this.renderCompleteSemaphore == VK_NULL_HANDLE)
            {
                LongBuffer pPresentAvailableSemaphore = memoryStack.longs(VK_NULL_HANDLE);
                LongBuffer pRenderFinishedSemaphore = memoryStack.longs(VK_NULL_HANDLE);

                VkSemaphoreCreateInfo semaphoreCreateInfo = VkSemaphoreCreateInfo.calloc(memoryStack);
                semaphoreCreateInfo.sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);

                vkCheckResult(vkCreateSemaphore(this.graphicsDevice.getLogicalDevice(), semaphoreCreateInfo, null, pPresentAvailableSemaphore));
                vkCheckResult(vkCreateSemaphore(this.graphicsDevice.getLogicalDevice(), semaphoreCreateInfo, null, pRenderFinishedSemaphore));

                this.presentCompleteSemaphore = pPresentAvailableSemaphore.get(0);
                this.renderCompleteSemaphore = pRenderFinishedSemaphore.get(0);
            }

            if (this.waitFences == null)
            {
                this.waitFences = Lists.newArrayListWithCapacity(this.swapChainImages.size());

                LongBuffer pWaitFence = memoryStack.longs(VK_NULL_HANDLE);

                VkFenceCreateInfo fenceCreateInfo = VkFenceCreateInfo.calloc(memoryStack);
                fenceCreateInfo.sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);
                fenceCreateInfo.flags(VK_FENCE_CREATE_SIGNALED_BIT);

                for (int i = 0; i < this.swapChainImages.size(); i++)
                {
                    vkCheckResult(vkCreateFence(this.graphicsDevice.getLogicalDevice(), fenceCreateInfo, null, pWaitFence));

                    this.waitFences.add(pWaitFence.get(0));
                }
            }
        }
    }

    public void destroy()
    {
        this.graphicsDevice.waitIdle();

        vkDestroySwapchainKHR(this.graphicsDevice.getLogicalDevice(), this.swapChainHandle, null);
        vkDestroySurfaceKHR(this.graphicsContext.getVulkanInstance(), this.windowSurface, null);
        vkDestroySemaphore(this.graphicsDevice.getLogicalDevice(), this.presentCompleteSemaphore, null);
        vkDestroySemaphore(this.graphicsDevice.getLogicalDevice(), this.renderCompleteSemaphore, null);

        for (long swapChainImageView : this.swapChainImageViews)
            vkDestroyImageView(this.graphicsDevice.getLogicalDevice(), swapChainImageView, null);

        for (long waitFence : this.waitFences)
            vkDestroyFence(this.graphicsDevice.getLogicalDevice(), waitFence, null);
        
        this.graphicsDevice.waitIdle();
    }

    private long createWindowSurface(long window, VkInstance vulkanInstance)
    {
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            LongBuffer pWindowSurface = memoryStack.longs(VK_NULL_HANDLE);
            vkCheckResult(glfwCreateWindowSurface(vulkanInstance, window, null, pWindowSurface));

            return pWindowSurface.get(0);
        }
    }

    private boolean isPresentationSupported(VkPhysicalDevice physicalDevice, long windowSurface)
    {
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            IntBuffer queueFamilyCount = memoryStack.ints(0);
            vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, queueFamilyCount, null);

            IntBuffer physicalDeviceSurfaceSupport = memoryStack.ints(VK_FALSE);
            boolean isPresentingSupported = false;

            for (int i = 0; i < queueFamilyCount.get(0); i++)
            {
                vkGetPhysicalDeviceSurfaceSupportKHR(physicalDevice, i, windowSurface, physicalDeviceSurfaceSupport);

                if (physicalDeviceSurfaceSupport.get(0) == VK_TRUE)
                {
                    this.presentFamily = i;

                    isPresentingSupported = true;
                    break;
                }
            }

            return isPresentingSupported;
        }
    }

    private void selectImageFormatAndColorSpace()
    {
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            IntBuffer surfaceFormatCount = memoryStack.ints(0);
            vkGetPhysicalDeviceSurfaceFormatsKHR(this.graphicsDevice.getPhysicalDevice(), this.windowSurface, surfaceFormatCount, null);

            if (surfaceFormatCount.get(0) == 0)
            {
                throw new MoonblastRendererException(String.format("The selected physical device (%s) has no available surface formats!",
                        this.graphicsDevice.getDeviceInfo().deviceProperties.deviceNameString()));
            }

            VkSurfaceFormatKHR.Buffer surfaceFormats = VkSurfaceFormatKHR.calloc(surfaceFormatCount.get(0), memoryStack);
            vkGetPhysicalDeviceSurfaceFormatsKHR(this.graphicsDevice.getPhysicalDevice(), this.windowSurface, surfaceFormatCount, surfaceFormats);

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
            this.swapChainImageViews = Lists.newArrayListWithCapacity(this.swapChainImages.size());

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

                vkCheckResult(vkCreateImageView(this.graphicsDevice.getLogicalDevice(), imageViewCreateInfo, null, pImageView));

                this.swapChainImageViews.add(pImageView.get(0));
            }
        }
    }

    public int getWidth()
    {
        return this.width;
    }

    public int getHeight()
    {
        return this.height;
    }

    public int getImageCount()
    {
        return this.swapChainImages.size();
    }

    public VkExtent2D getExtent()
    {
        return this.swapChainExtent;
    }

    public int getImageFormat()
    {
        return this.swapChainImageFormat;
    }
}
