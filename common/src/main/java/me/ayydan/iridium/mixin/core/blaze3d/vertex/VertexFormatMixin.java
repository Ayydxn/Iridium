package me.ayydan.iridium.mixin.core.blaze3d.vertex;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.ayydan.iridium.render.interfaces.VertexFormatAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(VertexFormat.class)
public class VertexFormatMixin implements VertexFormatAccessor
{
    @Unique
    private ObjectArrayList<VertexFormatElement> vertexFormatElementFastList;

    @Unique
    private IntList offsets;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void a(List<VertexFormatElement> elements, List<String> names, IntList offsets, int vertexSize, CallbackInfo ci)
    {
        ObjectArrayList<VertexFormatElement> vertexFormatElements = new ObjectArrayList<>();
        vertexFormatElements.addAll(elements);

        this.vertexFormatElementFastList = vertexFormatElements;
        this.offsets = offsets;
    }

    @Override
    public int getOffset(int index)
    {
        return this.offsets.getInt(index);
    }

    @Override
    public List<VertexFormatElement> getFastList()
    {
        return this.vertexFormatElementFastList;
    }
}
