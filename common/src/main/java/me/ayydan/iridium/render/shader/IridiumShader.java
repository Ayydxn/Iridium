package me.ayydan.iridium.render.shader;

import me.ayydan.iridium.render.IridiumRenderer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

import java.nio.LongBuffer;

import static me.ayydan.iridium.render.vulkan.VulkanValidation.vkCheckResult;
import static org.lwjgl.vulkan.VK10.*;

public class IridiumShader
{
    private final VkDevice logicalDevice = IridiumRenderer.getVulkanContext().getLogicalDevice().getHandle();

    private final ShaderSPIRV vertexShaderSPIRV;
    private final ShaderSPIRV fragmentShaderSPIRV;

    private long vertexShaderModule;
    private long fragmentShaderModule;

    public IridiumShader(String filepath)
    {
        this.vertexShaderSPIRV = IridiumShaderCompiler.getInstance().compileShaderFromFile(filepath + ".vsh", ShaderStage.VertexShader);
        this.fragmentShaderSPIRV = IridiumShaderCompiler.getInstance().compileShaderFromFile(filepath + ".fsh", ShaderStage.FragmentShader);

        this.vertexShaderModule = VK_NULL_HANDLE;
        this.fragmentShaderModule = VK_NULL_HANDLE;

        this.createShaderModule();
    }

    private void createShaderModule()
    {
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            LongBuffer pVertexShaderModule = memoryStack.mallocLong(1);
            LongBuffer pFragmentShaderModule = memoryStack.mallocLong(1);

            VkShaderModuleCreateInfo vertexShaderModuleCreateInfo = VkShaderModuleCreateInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
                    .pCode(this.vertexShaderSPIRV.getShaderBytecode());

            VkShaderModuleCreateInfo fragmentShaderModuleCreateInfo = VkShaderModuleCreateInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
                    .pCode(this.fragmentShaderSPIRV.getShaderBytecode());

            vkCheckResult(vkCreateShaderModule(this.logicalDevice, vertexShaderModuleCreateInfo, null, pVertexShaderModule));
            vkCheckResult(vkCreateShaderModule(this.logicalDevice, fragmentShaderModuleCreateInfo, null, pFragmentShaderModule));

            this.vertexShaderModule = pVertexShaderModule.get(0);
            this.fragmentShaderModule = pFragmentShaderModule.get(0);
        }
    }

    public long getVertexShaderModule()
    {
        return this.vertexShaderModule;
    }

    public long getFragmentShaderModule()
    {
        return this.fragmentShaderModule;
    }
}
