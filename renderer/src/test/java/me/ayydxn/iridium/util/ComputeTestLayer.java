package me.ayydxn.iridium.util;

import me.ayydxn.iridium.layer.Layer;
import me.ayydxn.iridium.renderer.ComputePipeline;
import me.ayydxn.iridium.shaders.IridiumShader;

public class ComputeTestLayer extends Layer
{
    private ComputePipeline computePipeline;

    public ComputeTestLayer()
    {
        super("Compute Test");
    }

    @Override
    public void onAttach()
    {
        this.computePipeline = new ComputePipeline(new IridiumShader("shaders/default_compute"));
        this.computePipeline.create();
    }

    @Override
    public void onUpdate()
    {

    }

    @Override
    public void onRender()
    {

    }

    @Override
    public void onDetach()
    {
        this.computePipeline.destroy();
    }
}
