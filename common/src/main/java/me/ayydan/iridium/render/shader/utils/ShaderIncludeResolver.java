package me.ayydan.iridium.render.shader.utils;

import me.ayydan.iridium.render.IridiumRenderer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.shaderc.ShadercIncludeResolveI;
import org.lwjgl.util.shaderc.ShadercIncludeResult;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ShaderIncludeResolver implements ShadercIncludeResolveI
{
    private final ArrayList<String> includePaths;

    public ShaderIncludeResolver(ArrayList<String> includePaths)
    {
        this.includePaths = new ArrayList<>(includePaths.size());

        for (String includePath : includePaths)
        {
            URL includePathResourceURL = ShaderIncludeResolver.class.getResource(includePath);
            if (includePathResourceURL == null)
            {
                IridiumRenderer.getLogger().warn("Shader include path '{}' is invalid. Skipping...", includePath);
                continue;
            }

            this.includePaths.add(includePathResourceURL.toExternalForm());
        }
    }

    @Override
    public long invoke(long userData, long requestedSrc, int type, long requestingSrc, long includeDepth)
    {
        String requestingSource = MemoryUtil.memASCII(requestingSrc);
        String requestedSource = MemoryUtil.memASCII(requestedSrc);

        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            for (String includePath : this.includePaths)
            {
                Path shaderIncludePath = Paths.get(new URI(String.format("%s/%s", includePath, requestedSource)));

                if (Files.exists(shaderIncludePath))
                {
                    byte[] includedShaderBytes = Files.readAllBytes(shaderIncludePath);

                    return ShadercIncludeResult.calloc(memoryStack)
                            .source_name(memoryStack.ASCII(requestedSource))
                            .content(memoryStack.bytes(includedShaderBytes))
                            .user_data(userData).address();
                }
            }
        }
        catch (IOException | URISyntaxException exception)
        {
            exception.printStackTrace();
        }

        throw new IllegalArgumentException(String.format("%s: Unable to find shader '%s' in include paths!", requestingSource, requestedSource));
    }
}
