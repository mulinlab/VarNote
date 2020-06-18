package org.mulinlab.varnote.utils;

import htsjdk.samtools.util.Log;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

public final class LoggingUtils {

    public static final org.apache.logging.log4j.Logger logger = LogManager.getLogger();

    public static void setLoggingLevel(final Log.LogLevel verbosity) {
        // Call the Picard API to establish the logging level used by Picard
        Log.setGlobalLogLevel(verbosity);
        // set the Log4JLoggingLevel
        setLog4JLoggingLevel(verbosity);
        // set the java.util.logging Level
        setJavaUtilLoggingLevel(verbosity);
    }

    private static void setJavaUtilLoggingLevel(final Log.LogLevel verbosity) {
        Logger topLogger = java.util.logging.Logger.getLogger("");

        Handler consoleHandler = null;
        for (Handler handler : topLogger.getHandlers()) {
            if (handler instanceof ConsoleHandler) {
                consoleHandler = handler;
                break;
            }
        }

        if (consoleHandler == null) {
            consoleHandler = new ConsoleHandler();
            topLogger.addHandler(consoleHandler);
        }
        if(verbosity == Log.LogLevel.ERROR) {
            consoleHandler.setLevel(java.util.logging.Level.SEVERE);
        } else if(verbosity == Log.LogLevel.INFO) {
            consoleHandler.setLevel(java.util.logging.Level.INFO);
        }
    }

    private static void setLog4JLoggingLevel(Log.LogLevel verbosity) {
        // Now establish the logging level used by log4j by propagating the requested
        // logging level to all loggers associated with our logging configuration.
        final LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
        final Configuration loggerContextConfig = loggerContext.getConfiguration();
        final String contextClassName = LoggingUtils.class.getName();
        final LoggerConfig loggerConfig = loggerContextConfig.getLoggerConfig(contextClassName);

        if(verbosity == Log.LogLevel.ERROR) {
            loggerConfig.setLevel(Level.ERROR);
        } else if(verbosity == Log.LogLevel.INFO) {
            loggerConfig.setLevel(Level.INFO);
        }

        loggerContext.updateLoggers();
    }

    public static void setLog4JLoggingPath(final String path, final String jobName) {

        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();

        org.apache.logging.log4j.core.Logger log = ctx.getLogger("org.mulinlab.varnote.utils.LoggingUtils");
        Layout layout = PatternLayout.createDefaultLayout();
        Appender appender = FileAppender
                .createAppender(path, "false", "false", jobName, "true",
                "false", "false", "4000", layout, null, "false", null, config);
        appender.start();
        log.addAppender(appender);

        ctx.updateLoggers();
    }

    public static void removeLog4JLoggingPath(final String jobName) {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);

        org.apache.logging.log4j.core.Logger log = ctx.getLogger("org.mulinlab.varnote.utils.LoggingUtils");
        for (Appender appender: log.getAppenders().values()) {
            if(appender.getName().equals(jobName)) {
                appender.stop();
                log.removeAppender(appender);
            }
        }

        ctx.updateLoggers();
    }
}
