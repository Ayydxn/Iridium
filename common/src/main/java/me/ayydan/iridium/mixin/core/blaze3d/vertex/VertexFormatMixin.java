package me.ayydan.iridium.mixin.core.blaze3d.vertex;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.ayydan.iridium.render.interfaces.VertexFormatAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(VertexFormat.class)
public class VertexFormatMixin implements VertexFormatAccessor
{
    @Shadow
    @Final
    private IntList offsets;

    @Unique
    private ObjectArrayList<VertexFormatElement> vertexFormatElementFastList;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void createFastList(ImmutableMap<String, VertexFormatElement> elementMap, CallbackInfo ci)
    {
        ObjectArrayList<VertexFormatElement> vertexFormatElements = new ObjectArrayList<>();
        vertexFormatElements.addAll(elementMap.values());

        this.vertexFormatElementFastList = vertexFormatElements;
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
