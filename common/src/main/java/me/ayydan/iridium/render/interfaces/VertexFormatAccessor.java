package me.ayydan.iridium.render.interfaces;

import com.mojang.blaze3d.vertex.VertexFormatElement;

import java.util.List;

public interface VertexFormatAccessor
{
    int getOffset(int index);

    List<VertexFormatElement> getFastList();
}
