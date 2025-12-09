package me.ayydxn.iridium.shaders;

import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ShaderDefinition
{
    @SerializedName("stages")
    private Map<String, String> shaderStagesToSource;

    private final List<ResourceDefinition> resources = Lists.newArrayList();

    @SerializedName("push_constants")
    private final List<PushConstantDefinition> pushConstants = Lists.newArrayList();

    private final List<ShaderDefine> defines = Lists.newArrayList();

    public static class ResourceDefinition
    {
        @SerializedName("name")
        public String name;

        @SerializedName("type")
        public String type;

        @SerializedName("binding")
        public int binding;

        @SerializedName("set")
        public int set;

        @SerializedName("stages")
        public List<String> stages;
    }

    public static class PushConstantDefinition
    {
        @SerializedName("name")
        public String name;

        @SerializedName("size")
        public int size;

        @SerializedName("offset")
        public int offset;

        @SerializedName("stages")
        public List<String> stages;
    }

    public record ShaderDefine(String name, String value)
    {
        public boolean isFlag()
        {
            return this.value == null;
        }

        public String toDefineString()
        {
            return this.isFlag() ? String.format("#define %s", name) : String.format("#define %s %s", name, value);
        }
    }

    /**
     * Returns a map of a shader stage to its corresponding GLSL shader source file.
     *
     * @return Map of shader stages to their GLSL source code file.
     */
    public Map<String, String> getShaderStages()
    {
        return this.shaderStagesToSource;
    }

    public List<ResourceDefinition> getResources()
    {
        return this.resources;
    }

    public List<PushConstantDefinition> getPushConstants()
    {
        return this.pushConstants;
    }

    public List<ShaderDefine> getDefines()
    {
        return this.defines;
    }
}
