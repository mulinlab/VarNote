package utils;

import org.mulinlab.varnote.cmdline.abstractclass.CMDProgram;

public final class TestUtils {

    public static void initClass(Class<?> clazz, String[] args) {
        initClass(clazz, args, false);
    }

    public static void initClass(Class<?> clazz, String[] args, boolean printUsage) {
        final CMDProgram program;
        try {
            program = (CMDProgram)clazz.newInstance();
            if(printUsage) System.out.println("\n\n" + program.getUsage() + "\n\n");

            int ret = program.instanceMain(args);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
