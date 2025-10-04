package me.ayydxn.iridium.layer;

public abstract class Layer
{
    private final String name;

    public Layer(String name)
    {
        this.name = name;
    }

    public abstract void onAttach();

    public abstract void onUpdate();

    public abstract void onRender();

    public abstract void onDetach();

    public String getName()
    {
        return this.name;
    }
}
