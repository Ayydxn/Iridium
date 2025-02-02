package com.ayydxn.iridium.util;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

import java.util.Collection;

public class PointerUtils
{
    public static PointerBuffer asPointerBuffer(Collection<String> collection, MemoryStack memoryStack)
    {
        PointerBuffer result = memoryStack.mallocPointer(collection.size());

        collection.stream()
                .map(memoryStack::UTF8)
                .forEach(result::put);

        return result.rewind();
    }
}
