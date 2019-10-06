package org.mulinlab.varnote.cmdline.abstractclass;


import htsjdk.samtools.util.Log;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.broadinstitute.barclay.argparser.*;
import org.mulinlab.varnote.cmdline.constant.Arguments;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.LoggingUtils;

import java.util.Collections;
import java.util.HashSet;


public abstract class CMDProgram {
    protected final Logger logger = LoggingUtils.logger;

    private CommandLineParser commandLineParser;
    private String commandLine;

    protected void onStartup() {}
    protected abstract int doWork();
    protected void onShutdown() {}

    @Argument(fullName = Arguments.LOG_LONG, doc = "Whether to print log.", optional = true)
    protected Boolean islog = GlobalParameter.DEFAULT_LOG;

    public final int runTool() {
        try {
//            logger.info("Initializing engine");
            onStartup();
//            logger.info("Done initializing engine");
            return doWork();
        } finally {
            onShutdown();
        }
    }

    public int instanceMain(final String[] args) {
        if (!parseArgs(args)) {
            return 1;
        }
        try {
            if(!islog) LoggingUtils.setLoggingLevel(Log.LogLevel.ERROR);
            else LoggingUtils.setLoggingLevel(Log.LogLevel.INFO);

            return runTool();
        } finally {

        }
    }

    protected boolean parseArgs(final String[] argv) {
        commandLineParser = getCommandLineParser();

        boolean ret;
        try {
            ret = commandLineParser.parseArguments(System.err, argv);
        } catch (CommandLineException e) {
            System.err.println(commandLineParser.usage(false,false));
            System.err.println(e.getMessage());
            ret = false;
        }

        commandLine = commandLineParser.getCommandLine();
        if (!ret) {
            return false;
        }

        final String[] customErrorMessages = customCommandLineValidation();
        if (customErrorMessages != null) {
            System.err.print(commandLineParser.usage(false, false));
            for (final String msg : customErrorMessages) {
                System.err.println(msg);
            }
            return false;
        }
        return true;
    }

    protected String[] customCommandLineValidation() {
        return null;
    }

    public final String getUsage(){
        return getCommandLineParser().usage(true, false);
    }

    protected final CommandLineParser getCommandLineParser() {
        if( commandLineParser == null) {
            commandLineParser =
                    new CommandLineArgumentParser(this,
                            Collections.EMPTY_LIST,
                            new HashSet<>(Collections.singleton(CommandLineParserOptions.APPEND_TO_COLLECTIONS)));
        }
        return commandLineParser;
    }

    protected String getVersion() {
        String versionString = this.getClass().getPackage().getImplementationVersion();
        return versionString != null ? versionString : "Unavailable";
    }
}
