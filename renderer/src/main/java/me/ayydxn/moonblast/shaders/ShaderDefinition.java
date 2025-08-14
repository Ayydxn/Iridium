package me.ayydxn.moonblast.shaders;

import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class ShaderDefinition
{
    @SerializedName("stages")
    private Map<String, String> shaderStagesToSource;

    private final List<ResourceDefinition> resources = Lists.newArrayList();

    @SerializedName("push_constants")
    private final List<PushConstantDefinition> pushConstants = Lists.newArrayList();

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

        @SerializedName("count")
        public int count = 1;

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
}
