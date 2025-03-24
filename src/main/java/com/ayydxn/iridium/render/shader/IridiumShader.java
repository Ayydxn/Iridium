package com.ayydxn.iridium.render.shader;

import com.ayydxn.iridium.render.exceptions.IridiumRendererException;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
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
import java.util.Map;

import static com.ayydxn.iridium.render.vulkan.util.VulkanDebugUtils.vkCheckResult;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.vkCreateShaderModule;

public class IridiumShader
{
    private final VkDevice logicalDevice = Minecraft.getInstance().getWindow().getVulkanContext().getLogicalDevice().getHandle();
    private final Map<ShaderStage, ShaderSPIRV> shaderStageToSPIRV = Maps.newHashMap();
    private final Map<ShaderStage, Long> shaderStageToShaderModule = Maps.newHashMap();

    public IridiumShader(String filepath)
    {
        URL shaderJSONFileURL = IridiumShader.class.getResource("/assets/iridium/shaders/" + filepath + ".json");
        if (shaderJSONFileURL == null)
            throw new IridiumRendererException(String.format("The JSON descriptor file for the shader '%s' wasn't found!", filepath));

        IridiumShaderCompiler shaderCompiler = IridiumShaderCompiler.getInstance();

        try
        {
            Path shaderJSONFilePath = Paths.get(shaderJSONFileURL.toURI());
            String shaderJSONFileContent = Files.readString(shaderJSONFilePath, StandardCharsets.UTF_8);

            JsonObject shaderJSONObject = GsonHelper.parse(shaderJSONFileContent);
            JsonObject shaderStages = GsonHelper.getAsJsonObject(shaderJSONObject, "stages");
            String baseShaderFilepath = FilenameUtils.getPath(filepath);

            for (Map.Entry<String, JsonElement> shaderStage : shaderStages.entrySet())
            {
                ShaderStage stage = ShaderStage.getFromString(shaderStage.getKey());
                String shaderFilepath = baseShaderFilepath + shaderStage.getValue().getAsString() + stage.getFileExtension();
                ShaderSPIRV shaderSPIRV = shaderCompiler.compileShaderFromFile(shaderFilepath, stage);

                shaderStageToSPIRV.put(stage, shaderSPIRV);
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        this.createShaderModules();
    }

    private void createShaderModules()
    {
        this.shaderStageToSPIRV.forEach((shaderStage, shaderSPIRV) ->
        {
            try (MemoryStack memoryStack = MemoryStack.stackPush())
            {
                LongBuffer pShaderModule = memoryStack.mallocLong(1);

                VkShaderModuleCreateInfo shaderModuleCreateInfo = VkShaderModuleCreateInfo.calloc(memoryStack)
                        .sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
                        .pCode(shaderSPIRV.getShaderBytecode());

                vkCheckResult(vkCreateShaderModule(this.logicalDevice, shaderModuleCreateInfo, null, pShaderModule));

                this.shaderStageToShaderModule.put(shaderStage, pShaderModule.get(0));
            }
        });
    }

    public long getShaderModule(ShaderStage shaderStage)
    {
        return this.shaderStageToShaderModule.get(shaderStage);
    }
}
