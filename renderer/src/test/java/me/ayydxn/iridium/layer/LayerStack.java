package me.ayydxn.iridium.layer;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;

public class LayerStack implements Iterable<Layer>
{
    private final ArrayList<Layer> layers = Lists.newArrayList();

    public void pushLayer(Layer layer)
    {
        this.layers.add(layer);

        layer.onAttach();
    }

    public void popLayer(Layer layer)
    {
        this.layers.remove(layer);

        layer.onDetach();
    }

    @Override
    public @NotNull Iterator<Layer> iterator()
    {
        return this.layers.iterator();
    }
}
