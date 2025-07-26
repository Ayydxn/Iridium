package me.ayydxn.moonblast.vertex;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public class VertexBufferLayout implements Iterable<VertexBufferElement>
{
    private final ImmutableList<VertexBufferElement> vertexBufferElements;

    private int stride;

    public VertexBufferLayout()
    {
        this.vertexBufferElements = ImmutableList.of();
    }

    public VertexBufferLayout(List<VertexBufferElement> vertexBufferElements)
    {
        this.vertexBufferElements = ImmutableList.copyOf(vertexBufferElements);

        this.calculateOffsetsAndStride();
    }

    private void calculateOffsetsAndStride()
    {
        this.stride = 0;
        int offset = 0;

        for (VertexBufferElement vertexBufferElement : this)
        {
            vertexBufferElement.offset = offset;
            offset += vertexBufferElement.size;

            this.stride += vertexBufferElement.size;
        }
    }

    public ImmutableList<VertexBufferElement> getElements()
    {
        return this.vertexBufferElements;
    }

    public int getElementCount()
    {
        return this.vertexBufferElements.size();
    }

    public int getStride()
    {
        return this.stride;
    }

    @Override
    public @NotNull Iterator<VertexBufferElement> iterator()
    {
        return this.getElements().iterator();
    }
}
