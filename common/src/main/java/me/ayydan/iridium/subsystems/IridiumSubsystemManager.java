package me.ayydan.iridium.subsystems;

import me.ayydan.iridium.utils.logging.IridiumLogger;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class IridiumSubsystemManager
{
    private static IridiumSubsystemManager INSTANCE;
    private static IridiumLogger LOGGER;

    private final HashMap<String, IridiumSubsystem> iridiumSubsystems;

    private IridiumSubsystemManager()
    {
        this.iridiumSubsystems = new HashMap<>();
    }

    public static void initialize()
    {
        if (INSTANCE != null)
        {
            LOGGER.warn("Iridium's subsystem manager cannot be initialized more than once!");
            return;
        }

        LOGGER = new IridiumLogger("Iridium Subsystem Manager");
        LOGGER.info("Initializing Iridium Subsystem Manager...");

        INSTANCE = new IridiumSubsystemManager();
    }

    public void shutdown()
    {
        for (IridiumSubsystem iridiumSubsystem : this.iridiumSubsystems.values())
        {
            LOGGER.info("Shutting down subsystem '{}'...", iridiumSubsystem.getName());

            iridiumSubsystem.shutdown();
        }
    }

    public void addSubsystem(IridiumSubsystem iridiumSubsystem)
    {
        LOGGER.info("Adding subsystem '{}'", iridiumSubsystem.getName());

        this.iridiumSubsystems.put(iridiumSubsystem.getName(), iridiumSubsystem);

        if (iridiumSubsystem.shouldInitializeSubsystem())
        {
            LOGGER.info("Initializing subsystem '{}'...", iridiumSubsystem.getName());

            iridiumSubsystem.initialize();
        }
    }

    public static IridiumSubsystemManager getInstance()
    {
        return INSTANCE;
    }

    // (Ayydan) Don't really like the current implementation of this method, but at least it gets the job done.
    // That doesn't mean I wouldn't like to clean it up though.
    public IridiumSubsystem getSubsystemInstance(Class<? extends IridiumSubsystem> subsystem)
    {
        AtomicReference<IridiumSubsystem> subsystemInstance = new AtomicReference<>(null);

        for (IridiumSubsystem iridiumSubsystem : this.iridiumSubsystems.values())
        {
            Stream.of(subsystem.getDeclaredMethods())
                    .filter(method -> method.getName().equalsIgnoreCase("getName"))
                    .findFirst()
                    .ifPresent(method ->
                    {
                        try
                        {
                            String subsystemName = (String) method.invoke(subsystem.getDeclaredConstructor().newInstance());

                            if (iridiumSubsystem.getName().equals(subsystemName))
                                subsystemInstance.set(iridiumSubsystem);
                        }
                        catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException exception)
                        {
                            exception.printStackTrace();
                        }
                    });

        }

        if (subsystemInstance.get() == null)
            throw new SubsystemNotFoundException(String.format("Failed to get the subsystem instance for '%s'!", subsystem.getName()));

        return subsystemInstance.get();
    }
}
