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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.lwjgl.util.shaderc.Shaderc.*;
import static org.lwjgl.vulkan.VK12.VK_API_VERSION_1_2;

public class IridiumShaderCompiler
{
    private static IridiumShaderCompiler INSTANCE;

    private final long shaderCompiler;
    private final long shaderCompilerOptions;

    private IridiumShaderCompiler(List<String> includePaths)
    {
        this.shaderCompiler = shaderc_compiler_initialize();
        if (this.shaderCompiler == MemoryUtil.NULL)
            throw new IridiumRendererException("Failed to create Shaderc compiler!");

        this.shaderCompilerOptions = shaderc_compile_options_initialize();
        if (this.shaderCompilerOptions == MemoryUtil.NULL)
            throw new IridiumRendererException("Failed to create Shaderc compiler options!");

        // TODO: (Ayydxn) Make this configurable by users somehow.
        ShaderIncludeResolver shaderIncludeResolver = new ShaderIncludeResolver(includePaths);

        shaderc_compile_options_set_target_env(this.shaderCompilerOptions, shaderc_target_env_vulkan, VK_API_VERSION_1_2);
        shaderc_compile_options_set_optimization_level(this.shaderCompilerOptions, shaderc_optimization_level_performance);
        shaderc_compile_options_set_include_callbacks(this.shaderCompilerOptions, shaderIncludeResolver, (userData, includeResult) ->
        {
        }, MemoryUtil.NULL);
    }

    public static void initialize(List<String> includePaths)
    {
        if (INSTANCE != null)
        {
            IridiumConstants.LOGGER.warn("Iridium's shader compiler can only be initialized once!");
            return;
        }

        INSTANCE = new IridiumShaderCompiler(includePaths);
    }

    public static void initialize()
    {
        IridiumShaderCompiler.initialize(Lists.newArrayList("/assets/iridium/shaders/minecraft/include"));
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

    public ShaderSPIRV compileShaderFromFile(String shaderFilepath, ShaderStage shaderStage, @Nullable List<ShaderDefinition.ShaderDefine> defines)
            throws IOException
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

                    if (defines != null)
                        shaderSource = this.injectShaderDefines(shaderSource, defines);

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

                if (defines != null)
                    shaderSource = this.injectShaderDefines(shaderSource, defines);

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

    private String injectShaderDefines(String shaderSource, @NotNull List<ShaderDefinition.ShaderDefine> defines)
    {
        // Create line-broken string of include directives
        StringBuilder stringBuilder = new StringBuilder();

        for (ShaderDefinition.ShaderDefine shaderDefine : defines)
        {
            stringBuilder.append(shaderDefine.toDefineString())
                    .append("\n");
        }

        stringBuilder.insert(0, "\n");

        if (!stringBuilder.isEmpty())
            stringBuilder.setLength(stringBuilder.length() - 1);

        String includeDirectivesString = stringBuilder.toString();

        // Find the point in the shader source at which we want to add the includes
        List<String> sourceLines = Lists.newArrayList();
        int injectionPointIndex = 0;
        boolean foundVersionDirective = false;
        int lastIncludeDirectiveIndex = -1;

        try (BufferedReader bufferedReader = new BufferedReader(new StringReader(shaderSource)))
        {
            String currentLine;
            int currentLineIndex = 0;

            while ((currentLine = bufferedReader.readLine()) != null)
            {
                sourceLines.add(currentLine);

                String currentLineTrimmed = currentLine.trim();

                if (currentLineTrimmed.startsWith("#version"))
                {
                    injectionPointIndex = currentLineIndex + 1;
                    foundVersionDirective = true;
                }
                else if (currentLineTrimmed.startsWith("#include"))
                {
                    lastIncludeDirectiveIndex = currentLineIndex;
                }

                currentLineIndex++;
            }
        }
        catch (IOException exception)
        {
            IridiumConstants.LOGGER.error(exception);
        }

        // If any #include directives are present, inject the defines after them instead.
        if (lastIncludeDirectiveIndex != -1)
            injectionPointIndex = lastIncludeDirectiveIndex + 1;

        // Enforce a blank line after #version directive
        if (foundVersionDirective)
        {
            if (injectionPointIndex < sourceLines.size() && !sourceLines.get(injectionPointIndex).trim().isEmpty())
            {
                sourceLines.add(injectionPointIndex, "");
                injectionPointIndex++;
            }
            else if (injectionPointIndex == sourceLines.size())
            {
                sourceLines.add(injectionPointIndex, "");
                injectionPointIndex++;
            }
        }

        // Build the string of the shader's source code with the include directives.
        sourceLines.add(injectionPointIndex, includeDirectivesString);

        return String.join("\n", sourceLines);
    }
}
