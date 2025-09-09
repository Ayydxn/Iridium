package me.ayydxn.iridium.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public class IridiumConstants
{
    public static final Logger LOGGER = (Logger) LogManager.getLogger("Iridium Renderer");
    public static final String SHADER_CACHE_FILE_EXTENSION = ".isc"; // .isc = Iridium Shader Cache

    public static final long UINT64_MAX = 0xFFFFFFFFFFFFFFFFL;
    public static final int UINT32_MAX = 0xFFFFFFFF;
}
