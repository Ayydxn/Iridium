package me.ayydxn.iridium.shaders;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import me.ayydxn.iridium.IridiumRenderer;
import me.ayydxn.iridium.renderer.exceptions.IridiumRendererException;
import me.ayydxn.iridium.shaders.utils.ShaderIncludeResolver;
import me.ayydxn.iridium.utils.ByteBufferUtils;
import me.ayydxn.iridium.utils.IridiumConstants;
import me.ayydxn.iridium.utils.PathUtils;
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

    private final long shaderCompiler;
    private final long shaderCompilerOptions;

    private IridiumShaderCompiler()
    {
        this.shaderCompiler = shaderc_compiler_initialize();
        if (this.shaderCompiler == MemoryUtil.NULL)
            throw new IridiumRendererException("Failed to create Shaderc compiler!");

        this.shaderCompilerOptions = shaderc_compile_options_initialize();
        if (this.shaderCompilerOptions == MemoryUtil.NULL)
            throw new IridiumRendererException("Failed to create Shaderc compiler options!");

        // TODO: (Ayydxn) Make this configurable by users somehow.
        ShaderIncludeResolver shaderIncludeResolver = new ShaderIncludeResolver(Lists.newArrayList());

        shaderc_compile_options_set_target_env(this.shaderCompilerOptions, shaderc_target_env_vulkan, VK_API_VERSION_1_2);
        shaderc_compile_options_set_optimization_level(this.shaderCompilerOptions, shaderc_optimization_level_performance);
        shaderc_compile_options_set_include_callbacks(this.shaderCompilerOptions, shaderIncludeResolver, (userData, includeResult) -> {}, MemoryUtil.NULL);
    }

    public static void initialize()
    {
        if (INSTANCE != null)
        {
            IridiumConstants.LOGGER.warn("Iridium's shader compiler can only be initialized once!");
            return;
        }

        INSTANCE = new IridiumShaderCompiler();
    }

    public void shutdown()
    {
        shaderc_compiler_release(this.shaderCompiler);
        shaderc_compile_options_release(this.shaderCompilerOptions);

        INSTANCE = null;
    }

    public ShaderSPIRV compileShader(String shaderName, String shaderSource, ShaderStage shaderStage)
    {
        IridiumConstants.LOGGER.info("Compiling shader '{}{}'", shaderName, shaderStage.getFileExtension());

        long shaderCompilationResult = shaderc_compile_into_spv(this.shaderCompiler, shaderSource, shaderStage.getID(), shaderName, "main",
                this.shaderCompilerOptions);

        if (shaderCompilationResult == MemoryUtil.NULL)
            throw new NullPointerException(String.format("Failed to compile shader '%s'!", shaderName));

        if (shaderc_result_get_compilation_status(shaderCompilationResult) != shaderc_compilation_status_success)
            IridiumConstants.LOGGER.error("Failed to compile shader '{}':\n\n{}", shaderName, shaderc_result_get_error_message(shaderCompilationResult));

        return new ShaderSPIRV(shaderCompilationResult, shaderc_result_get_bytes(shaderCompilationResult));
    }

    public ShaderSPIRV compileShaderFromFile(String shaderFilepath, ShaderStage shaderStage) throws IOException
    {
        Path shaderCacheDirectory = PathUtils.getShaderCacheDirectory();
        String shaderFilename = StringUtils.substringBefore(FilenameUtils.getName(shaderFilepath), ".");

        if (IridiumRenderer.getInstance().getOptions().rendererOptions.enableShaderCaching)
        {
            Path shaderCacheFilepath = Path.of(shaderCacheDirectory + "/" + shaderFilename + "_" + shaderStage.getCacheID() +
                    IridiumConstants.SHADER_CACHE_FILE_EXTENSION);

            if (Files.exists(shaderCacheFilepath))
            {
                IridiumConstants.LOGGER.info("Loading shader '{}' from cache...", shaderFilepath);

                ByteBuffer cachedShaderBytecode = ByteBufferUtils.readFromFile(shaderCacheFilepath.toString());
                return new ShaderSPIRV(MemoryUtil.NULL, cachedShaderBytecode);
            }
            else
            {
                try
                {
                    URL shaderFileURL = Resources.getResource(shaderFilepath);
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
                URL shaderFileURL = Resources.getResource(shaderFilepath);
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
        if (INSTANCE == null)
            throw new IllegalStateException("Tried to access an instance of the shader compiler before one was available!");

        return INSTANCE;
    }
}
