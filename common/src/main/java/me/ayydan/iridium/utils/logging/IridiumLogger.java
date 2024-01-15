package me.ayydan.iridium.utils.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;

public class IridiumLogger
{
    private Logger logger;

    private String loggerName;

    public IridiumLogger(String name)
    {
        this.logger = (Logger) LogManager.getLogger(name);

        this.loggerName = name;
    }

    public void trace(String message, Object ... arguments)
    {
        this.logger.trace("[{}] {}", this.loggerName, ParameterizedMessage.format(message, arguments));
    }

    public void info(String message, Object ... arguments)
    {
        this.logger.info("[{}] {}", this.loggerName, ParameterizedMessage.format(message, arguments));
    }

    public void debug(String message, Object ... arguments)
    {
        this.logger.debug("[{}] {}", this.loggerName, ParameterizedMessage.format(message, arguments));
    }

    public void warn(String message, Object ... arguments)
    {
        this.logger.warn("[{}] {}", this.loggerName, ParameterizedMessage.format(message, arguments));
    }

    public void error(String message, Object ... arguments)
    {
        this.logger.error("[{}] {}", this.loggerName, ParameterizedMessage.format(message, arguments));
    }

    public void fatal(String message, Object ... arguments)
    {
        this.logger.fatal("[{}] {}", this.loggerName, ParameterizedMessage.format(message, arguments));
    }

    public String getName()
    {
        return this.loggerName;
    }

    public void setName(String name)
    {
        this.loggerName = name;

        this.logger = (Logger) LogManager.getLogger(name);
    }
}
