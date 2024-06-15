package me.ayydan.iridium.render.vulkan.utils;

import com.mojang.blaze3d.vertex.VertexFormatElement;

import static org.lwjgl.vulkan.VK10.*;

public class VulkanUtils
{
    public static int getVertexFormatElementVulkanFormat(VertexFormatElement vertexFormatElement)
    {
        VertexFormatElement.Type elementDataType = vertexFormatElement.getType();

        return switch (vertexFormatElement.getUsage())
        {
            case POSITION ->
            {
                if (elementDataType == VertexFormatElement.Type.FLOAT)
                    yield VK_FORMAT_R32G32B32_SFLOAT;

                if (elementDataType == VertexFormatElement.Type.SHORT)
                    yield VK_FORMAT_R16G16B16A16_SINT;

                if (elementDataType == VertexFormatElement.Type.BYTE)
                    yield VK_FORMAT_R8G8B8A8_SINT;

                // Return undefined if we don't have a format for a specific data type.
                yield VK_FORMAT_UNDEFINED;
            }

            case COLOR -> VK_FORMAT_R8G8B8A8_UNORM;

            case UV ->
            {
                if (elementDataType == VertexFormatElement.Type.FLOAT)
                    yield VK_FORMAT_R32G32_SFLOAT;

                if (elementDataType == VertexFormatElement.Type.SHORT)
                    yield VK_FORMAT_R16G16_SINT;

                if (elementDataType == VertexFormatElement.Type.USHORT)
                    yield VK_FORMAT_R16G16_UINT;

                // Return undefined if we don't have a format for a specific data type.
                yield VK_FORMAT_UNDEFINED;
            }

            case NORMAL -> VK_FORMAT_R8G8B8A8_SNORM;

            case PADDING, GENERIC -> VK_FORMAT_UNDEFINED;

            default -> throw new IllegalArgumentException("Failed to get Vulkan format for vertex format element: " + vertexFormatElement.getType());
        };
    }
}
