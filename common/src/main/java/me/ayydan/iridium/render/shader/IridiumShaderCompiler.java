package me.ayydan.iridium.render.shader;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import me.ayydan.iridium.IridiumClientMod;
import me.ayydan.iridium.options.IridiumGameOptions;
import me.ayydan.iridium.render.IridiumRenderer;
import me.ayydan.iridium.render.exceptions.IridiumRendererException;
import me.ayydan.iridium.render.shader.utils.ShaderIncludeResolver;
import me.ayydan.iridium.utils.ByteBufferUtils;
import me.ayydan.iridium.utils.IridiumConstants;
import me.ayydan.iridium.utils.PathUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.lwjgl.util.shaderc.Shaderc.*;
import static org.lwjgl.vulkan.VK12.VK_API_VERSION_1_2;

public class IridiumShaderCompiler
{
    private static IridiumShaderCompiler INSTANCE;

    private final long shadercCompiler;
    private final long shadercCompilerOptions;

    private IridiumShaderCompiler()
    {
        this.shadercCompiler = shaderc_compiler_initialize();
        if (this.shadercCompiler == MemoryUtil.NULL)
            throw new IridiumRendererException("Failed to create Shaderc compiler!");

        this.shadercCompilerOptions = shaderc_compile_options_initialize();
        if (this.shadercCompilerOptions == MemoryUtil.NULL)
            throw new IridiumRendererException("Failed to create Shaderc compiler options!");

        ShaderIncludeResolver shaderIncludeResolver = new ShaderIncludeResolver(Lists.newArrayList("/assets/iridium/shaders/minecraft/include"));

        shaderc_compile_options_set_target_env(this.shadercCompilerOptions, shaderc_target_env_vulkan, VK_API_VERSION_1_2);
        shaderc_compile_options_set_optimization_level(this.shadercCompilerOptions, shaderc_optimization_level_performance);
        shaderc_compile_options_set_include_callbacks(this.shadercCompilerOptions, shaderIncludeResolver, (userData, includeResult) -> {}, MemoryUtil.NULL);
    }

    public static void initialize()
    {
        if (INSTANCE != null)
        {
            IridiumRenderer.getLogger().warn("Iridium's shader compiler can only be initialized once!");
            return;
        }

        INSTANCE = new IridiumShaderCompiler();
    }

    public void shutdown()
    {
        shaderc_compiler_release(this.shadercCompiler);
        shaderc_compile_options_release(this.shadercCompilerOptions);

        INSTANCE = null;
    }

    public ShaderSPIRV compileShader(String shaderName, String shaderSource, ShaderStage shaderStage)
    {
        IridiumRenderer.getLogger().info("Compiling shader '{}'...", shaderName);

        long shaderCompilationResult = shaderc_compile_into_spv(this.shadercCompiler, shaderSource, shaderStage.getID(), shaderName, "main",
                this.shadercCompilerOptions);

        if (shaderCompilationResult == MemoryUtil.NULL)
            throw new NullPointerException(String.format("Failed to compile shader '%s'!", shaderName));

        if (shaderc_result_get_compilation_status(shaderCompilationResult) != shaderc_compilation_status_success)
            IridiumRenderer.getLogger().error("Failed to compile shader '{}':\n\n{}", shaderName, shaderc_result_get_error_message(shaderCompilationResult));

        return new ShaderSPIRV(shaderCompilationResult, shaderc_result_get_bytes(shaderCompilationResult));
    }

    public ShaderSPIRV compileShaderFromFile(String shaderFilepath, ShaderStage shaderStage)
    {
        Path shaderCacheDirectory = PathUtils.getShaderCacheDirectory();
        String shaderFilename = StringUtils.substringBefore(FilenameUtils.getName(shaderFilepath), ".");

        IridiumGameOptions iridiumGameOptions = IridiumClientMod.getInstance().getGameOptions();
        if (iridiumGameOptions.rendererOptions.enableShaderCaching)
        {
            Path shaderCacheFilepath = Path.of(shaderCacheDirectory + "/" + shaderFilename + "_" + shaderStage.getCacheID() +
                    IridiumConstants.SHADER_CACHE_FILE_EXTENSION);

            if (Files.exists(shaderCacheFilepath))
            {
                IridiumRenderer.getLogger().info("Loading shader '{}' from cache...", shaderFilepath);

                ByteBuffer cachedShaderBytecode = ByteBufferUtils.readFromFile(shaderCacheFilepath.toString());
                return new ShaderSPIRV(MemoryUtil.NULL, cachedShaderBytecode);
            }
            else
            {
                try
                {
                    URL shaderFileURL = Resources.getResource("assets/iridium/shaders/" + shaderFilepath);
                    String shaderSource = Resources.toString(shaderFileURL, StandardCharsets.UTF_8);

                    ShaderSPIRV shaderSPIRV = this.compileShader(shaderFilename, shaderSource, shaderStage);
                    ByteBufferUtils.writeToFile(shaderSPIRV.getShaderBytecode(), shaderCacheFilepath.toString());

                    return shaderSPIRV;
                }
                catch (IOException exception)
                {
                    exception.printStackTrace();
                }
            }
        }
        else
        {
            try
            {
                URL shaderFileURL = Resources.getResource("assets/iridium/shaders/" + shaderFilepath);
                String shaderSource = Resources.toString(shaderFileURL, StandardCharsets.UTF_8);

                return this.compileShader(shaderFilename, shaderSource, shaderStage);
            }
            catch (IOException exception)
            {
                exception.printStackTrace();
            }
        }

        return null;
    }

    public static IridiumShaderCompiler getInstance()
    {
        return INSTANCE;
    }
}
