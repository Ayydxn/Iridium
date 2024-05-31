package me.ayydan.iridium.render.shader;

import com.google.gson.JsonObject;
import me.ayydan.iridium.render.IridiumRenderer;
import me.ayydan.iridium.render.exceptions.IridiumRendererException;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.FilenameUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

import java.net.URL;
import java.nio.LongBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static me.ayydan.iridium.render.vulkan.VulkanValidation.vkCheckResult;
import static org.lwjgl.vulkan.VK10.*;

public class IridiumShader
{
    private final VkDevice logicalDevice = IridiumRenderer.getInstance().getVulkanContext().getLogicalDevice().getHandle();

    private ShaderSPIRV vertexShaderSPIRV;
    private ShaderSPIRV fragmentShaderSPIRV;
    private long vertexShaderModule;
    private long fragmentShaderModule;

    public IridiumShader(String filepath)
    {
        URL shaderJSONFileURL = IridiumShader.class.getResource("/assets/iridium/shaders/" + filepath + ".json");
        if (shaderJSONFileURL == null)
            throw new IridiumRendererException(String.format("The JSON descriptor file for the shader '%s' wasn't found!", filepath));
        try
        {
            Path shaderJSONFilePath = Paths.get(shaderJSONFileURL.toURI());
            String shaderJSONFileContent = Files.readString(shaderJSONFilePath, StandardCharsets.UTF_8);

            JsonObject shaderJSONObject = GsonHelper.parse(shaderJSONFileContent);
            String vertexShaderName = GsonHelper.getAsString(shaderJSONObject, "vertex", null);
            String fragmentShaderName = GsonHelper.getAsString(shaderJSONObject, "fragment", null);
            String baseShaderFilepath = FilenameUtils.getPath(filepath);

            this.vertexShaderSPIRV = IridiumShaderCompiler.getInstance().compileShaderFromFile(baseShaderFilepath + vertexShaderName + ".vsh", ShaderStage.VertexShader);
            this.fragmentShaderSPIRV = IridiumShaderCompiler.getInstance().compileShaderFromFile(baseShaderFilepath + fragmentShaderName + ".fsh", ShaderStage.FragmentShader);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        this.vertexShaderModule = VK_NULL_HANDLE;
        this.fragmentShaderModule = VK_NULL_HANDLE;

        this.createShaderModules();
    }

    private void createShaderModules()
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
