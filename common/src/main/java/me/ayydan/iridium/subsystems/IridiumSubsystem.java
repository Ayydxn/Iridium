package me.ayydan.iridium.subsystems;

public abstract class IridiumSubsystem
{
    public abstract void initialize();

    public abstract void shutdown();

    public abstract boolean shouldInitializeSubsystem();

    public abstract String getName();
}
