package me.ayydxn.moonblast.shaders;

import com.google.common.collect.Maps;
import me.ayydxn.moonblast.renderer.exceptions.MoonblastRendererException;
import me.ayydxn.moonblast.utils.MoonblastConstants;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.spvc.Spv;
import org.lwjgl.util.spvc.SpvcReflectedResource;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Objects;

import static org.lwjgl.util.spvc.Spvc.*;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_ALL;

public class ShaderReflector
{
    private final long spvcContext;

    private long spvcCompiler = MemoryUtil.NULL;
    private long shaderResources = MemoryUtil.NULL;

    public ShaderReflector()
    {
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            // Create SPIR-V context
            PointerBuffer pSPIRVCrossContext = memoryStack.pointers(MemoryUtil.NULL);
            if (spvc_context_create(pSPIRVCrossContext) != SPVC_SUCCESS)
                throw new MoonblastRendererException("Failed to create SPRIV-Cross context!");

            this.spvcContext = pSPIRVCrossContext.get(0);

            spvc_context_set_error_callback(this.spvcContext, (userdata, error) ->
            {
                String errorString = MemoryUtil.memUTF8(error);
                MoonblastConstants.LOGGER.error("A SPIR-V Cross Error has occurred: {}", errorString);
            }, MemoryUtil.NULL);
        }
    }

    public ShaderResources.ShaderDescriptorSet reflect(ShaderSPIRV shaderSPIRV)
    {
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            // Get parsed shader IR
            IntBuffer shaderBytecode = shaderSPIRV.getShaderBytecode().asIntBuffer();
            PointerBuffer pParsedIR = memoryStack.pointers(MemoryUtil.NULL);
            if (spvc_context_parse_spirv(this.spvcContext, shaderBytecode, shaderBytecode.limit(), pParsedIR) != SPVC_SUCCESS)
                throw new MoonblastRendererException("Failed to parse SPIR-V bytecode!");

            // Create SPIR-V compiler
            PointerBuffer pSPIRVCompiler = memoryStack.pointers(MemoryUtil.NULL);
            if (spvc_context_create_compiler(this.spvcContext, SPVC_BACKEND_GLSL, pParsedIR.get(0), 0, pSPIRVCompiler) != SPVC_SUCCESS)
                throw new MoonblastRendererException("Failed to create SPIR-V Cross compiler!");

            this.spvcCompiler = pSPIRVCompiler.get(0);

            // Create shader resources
            PointerBuffer pShaderResources = memoryStack.pointers(MemoryUtil.NULL);
            if (spvc_compiler_create_shader_resources(pSPIRVCompiler.get(0), pShaderResources) != SPVC_SUCCESS)
                throw new MoonblastRendererException("Failed to create shader resources!");

            this.shaderResources = pShaderResources.get(0);

            MoonblastConstants.LOGGER.info("==========================================================");
            MoonblastConstants.LOGGER.info("================ SPIR-V Shader Reflection ================");
            MoonblastConstants.LOGGER.info("==========================================================");

            // Parse all resources from the shader and store them.
            ShaderResources.ShaderDescriptorSet shaderDescriptorSet = new ShaderResources.ShaderDescriptorSet();
            shaderDescriptorSet.uniformBuffers = this.parseUniformBuffers(memoryStack);

            return shaderDescriptorSet;
        }
    }

    public void destroy()
    {
        spvc_context_destroy(this.spvcContext);
    }

    private HashMap<Integer, ShaderResources.UniformBuffer> parseUniformBuffers(MemoryStack memoryStack)
    {
        HashMap<Integer, ShaderResources.UniformBuffer> parsedUniformBuffers = Maps.newHashMap();

        // Getting shader resources
        PointerBuffer pUniformBuffersList = memoryStack.pointers(MemoryUtil.NULL);
        PointerBuffer pUniformBuffersCount = memoryStack.pointers(MemoryUtil.NULL);
        spvc_resources_get_resource_list_for_type(this.shaderResources, SPVC_RESOURCE_TYPE_UNIFORM_BUFFER, pUniformBuffersList, pUniformBuffersCount);

        int uniformBuffersCount = (int) pUniformBuffersCount.get(0);

        try (SpvcReflectedResource.Buffer reflectedResources = SpvcReflectedResource.createSafe(pUniformBuffersList.get(0), uniformBuffersCount))
        {
            Objects.requireNonNull(reflectedResources);

            MoonblastConstants.LOGGER.info("Parsing {} uniform {}", uniformBuffersCount, uniformBuffersCount == 1 ? "buffer" : "buffers");

            for (int i = 0; i < uniformBuffersCount; i++)
            {
                SpvcReflectedResource reflectedResource =  reflectedResources.get(i);
                int descriptorSet = spvc_compiler_get_decoration(this.spvcCompiler, reflectedResource.id(), Spv.SpvDecorationDescriptorSet);
                int binding = spvc_compiler_get_decoration(this.spvcCompiler, reflectedResource.id(), Spv.SpvDecorationBinding);
                long typeHandle = spvc_compiler_get_type_handle(this.spvcCompiler, reflectedResource.base_type_id());

                PointerBuffer pStructSize = memoryStack.pointers(MemoryUtil.NULL);
                spvc_compiler_get_declared_struct_size(this.spvcCompiler, typeHandle, pStructSize);

                ShaderResources.UniformBuffer uniformBuffer = new ShaderResources.UniformBuffer();
                uniformBuffer.name = reflectedResource.nameString();
                uniformBuffer.size = (int) pStructSize.get(0);
                uniformBuffer.bindingPoint = binding;
                uniformBuffer.shaderStage = VK_SHADER_STAGE_ALL;

                MoonblastConstants.LOGGER.info("--------------- Uniform Buffer: {} ---------------", uniformBuffer.name);
                MoonblastConstants.LOGGER.info(" - Descriptor Set: {}", descriptorSet);
                MoonblastConstants.LOGGER.info(" - Binding: {}", binding);
                MoonblastConstants.LOGGER.info(" - Member Count: {}", spvc_type_get_num_member_types(typeHandle));
                MoonblastConstants.LOGGER.info(" - Size: {}", pStructSize.get(0));
                MoonblastConstants.LOGGER.info("----------------------------------------------------------");

                parsedUniformBuffers.put(binding, uniformBuffer);
            }
        }

        return parsedUniformBuffers;
    }
}
