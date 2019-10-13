package utils;

import org.junit.Assert;
import org.mulinlab.varnote.cmdline.abstractclass.CMDProgram;
import org.mulinlab.varnote.cmdline.query.Intersect;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public final class TestUtils {

    public static void initClass(Class<?> clazz, String[] args) {
        initClass(clazz, args, false);
    }

    public static void initClass(Class<?> clazz, String[] args, boolean printUsage) {
//        ByteArrayOutputStream stderrStream = new ByteArrayOutputStream();
//        PrintStream newStderr = new PrintStream(stderrStream);
//        System.setErr(newStderr);

        final CMDProgram program;
        try {
            program = (CMDProgram)clazz.newInstance();
            if(printUsage) System.out.println("\n\n" + program.getUsage() + "\n\n");

            int ret = program.instanceMain(args);
//            System.out.println(stderrStream.toString());
//            Assert.assertFalse(stderrStream.toString().contains("ERROR"));

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
