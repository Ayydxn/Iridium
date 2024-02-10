package me.ayydan.iridium.client;

import net.minecraft.client.MinecraftClient;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;

public class ClientFramerateTracker
{
    private final Queue<Integer> framerateQueue = new EvictingQueue<>(200);

    private int averageFPS, lowestFPS, highestFPS;

    public void tick(MinecraftClient client)
    {
        int currentClientFPS = client.getCurrentFps();

        this.framerateQueue.add(currentClientFPS);

        this.averageFPS = (int) this.framerateQueue.stream().mapToInt(Integer::intValue).average().orElse(0);
        this.lowestFPS = this.framerateQueue.stream().min(Comparator.comparingInt(value -> value)).orElse(0);
        this.highestFPS = this.framerateQueue.stream().max(Comparator.comparingInt(value -> value)).orElse(0);
    }

    public int getAverageFPS()
    {
        return this.averageFPS;
    }

    public int getLowestFPS()
    {
        return this.lowestFPS;
    }

    public int getHighestFPS()
    {
        return this.highestFPS;
    }

    private static final class EvictingQueue<T> extends LinkedList<T>
    {
        private final int limit;

        public EvictingQueue(int limit)
        {
            this.limit = limit;
        }

        @Override
        public boolean add(T t)
        {
            super.add(t);

            while (this.size() > this.limit)
                super.remove();

            return true;
        }
    }
}
