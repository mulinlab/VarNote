package org.mulinlab.varnote.cmdline.abstractclass;


import org.broadinstitute.barclay.argparser.*;
import java.util.Collections;
import java.util.HashSet;


public abstract class CMDProgram {
//    protected final Logger logger = LoggingUtils.logger;

    private CommandLineParser commandLineParser;
    private String commandLine;

    protected abstract int doWork();
    protected abstract void onShutdown();
    protected abstract void onStartup();

    public final int runTool() {
        try {
            onStartup();
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
