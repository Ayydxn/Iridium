package me.ayydan.iridium.utils.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;

/**
 * A simple logger wrapper class over Log4J which adds the name of the logger before each message.
 */
public class IridiumLogger
{
    private Logger logger;

    private String loggerName;

    /**
     * Creates a new logger.
     *
     * @param name The name of the logger.
     */
    public IridiumLogger(String name)
    {
        this.logger = (Logger) LogManager.getLogger(name);

        this.loggerName = name;
    }

    /**
     * Logs a message with format arguments at the {@link org.apache.logging.log4j.Level#TRACE Trace} log level.
     *
     * @param message The message to log.
     * @param arguments Arguments to format into the message.
     */
    public void trace(String message, Object ... arguments)
    {
        this.logger.trace("[{}] {}", this.loggerName, ParameterizedMessage.format(message, arguments));
    }

    /**
     * Logs a message with format arguments at the {@link org.apache.logging.log4j.Level#INFO Info} log level.
     *
     * @param message The message to log.
     * @param arguments Arguments to format into the message.
     */
    public void info(String message, Object ... arguments)
    {
        this.logger.info("[{}] {}", this.loggerName, ParameterizedMessage.format(message, arguments));
    }

    /**
     * Logs a message with format arguments at the {@link org.apache.logging.log4j.Level#DEBUG Debug} log level.
     *
     * @param message The message to log.
     * @param arguments Arguments to format into the message.
     */
    public void debug(String message, Object ... arguments)
    {
        this.logger.debug("[{}] {}", this.loggerName, ParameterizedMessage.format(message, arguments));
    }

    /**
     * Logs a message with format arguments at the {@link org.apache.logging.log4j.Level#WARN Warn} log level.
     *
     * @param message The message to log.
     * @param arguments Arguments to format into the message.
     */
    public void warn(String message, Object ... arguments)
    {
        this.logger.warn("[{}] {}", this.loggerName, ParameterizedMessage.format(message, arguments));
    }

    /**
     * Logs a message with format arguments at the {@link org.apache.logging.log4j.Level#ERROR Error} log level.
     *
     * @param message The message to log.
     * @param arguments Arguments to format into the message.
     */
    public void error(String message, Object ... arguments)
    {
        this.logger.error("[{}] {}", this.loggerName, ParameterizedMessage.format(message, arguments));
    }

    /**
     * Logs a message with format arguments at the {@link org.apache.logging.log4j.Level#FATAL Fatal} log level.
     *
     * @param message The message to log.
     * @param arguments Arguments to format into the message.
     */
    public void fatal(String message, Object ... arguments)
    {
        this.logger.fatal("[{}] {}", this.loggerName, ParameterizedMessage.format(message, arguments));
    }

    /**
     * Returns the name of the logger.
     *
     * @return Name of the logger.
     */
    public String getName()
    {
        return this.loggerName;
    }

    /**
     * Sets the new name of the logger.
     *
     * @param name The new name of the logger.
     */
    public void setName(String name)
    {
        this.loggerName = name;

        this.logger = (Logger) LogManager.getLogger(name);
    }
}
