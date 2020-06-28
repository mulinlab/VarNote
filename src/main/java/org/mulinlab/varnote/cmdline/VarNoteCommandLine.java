package org.mulinlab.varnote.cmdline;

import org.mulinlab.varnote.cmdline.abstractclass.CMDProgram;
import org.mulinlab.varnote.cmdline.constant.CommandLineDefaults;
import org.mulinlab.varnote.constants.GlobalParameter;
import htsjdk.samtools.util.Log;
import htsjdk.samtools.util.StringUtil;
import org.broadinstitute.barclay.argparser.*;

import java.io.PrintStream;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class VarNoteCommandLine {
    private static final Log log = Log.getInstance(VarNoteCommandLine.class);

    private static String initializeColor(final String color) {
        if (CommandLineDefaults.COLOR_STATUS) return color;
        else return "";
    }

    private final static int HELP_SIMILARITY_FLOOR = CommandLineDefaults.HELP_SIMILARITY_FLOOR;
    private final static int MINIMUM_SUBSTRING_LENGTH = CommandLineDefaults.MINIMUM_SUBSTRING_LENGTH;
    private static final String KNRM = "\u001B[0m"; // reset
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String CYAN = "\u001B[36m";
    private static final String WHITE = "\u001B[37m";
    private static final String BOLDRED = "\u001B[1m\u001B[31m";

    protected static List<String> getPackageList() {
        final List<String> packageList = new ArrayList<String>();
        packageList.add("org");
        return packageList;
    }

    public static void main(final String[] args) {
        System.exit(new VarNoteCommandLine().instanceMain(args, getPackageList(), GlobalParameter.PRO_CMD));
    }

    protected int instanceMain(final String[] args, final List<String> packageList, final String commandLineName) {
        final CMDProgram program = extractCommandLineProgram(args, packageList, commandLineName);
        if (null == program) return 1; // no program found!
        // we can lop off the first two arguments but it requires an array copy or alternatively we could update CLP to remove them
        // in the constructor do the former in this implementation.
        final String[] mainArgs = Arrays.copyOfRange(args, 1, args.length);
        return program.instanceMain(mainArgs);
    }

    protected int instanceMain(final String[] args) {
        return instanceMain(args, getPackageList(), GlobalParameter.PRO_CMD);
    }

    public static CMDProgram extractCommandLineProgram(final String[] args, final List<String> packageList, final String commandLineName) {
        final Map<String, Class<?>> simpleNameToClass = new HashMap<>();
        final List<String> missingAnnotationClasses = new ArrayList<>();
        processAllCommandLinePrograms(
                packageList,
                (Class<CMDProgram> clazz, CommandLineProgramProperties clProperties) -> {
                    // Check for missing annotations
                    if (null == clProperties) {
                        missingAnnotationClasses.add(clazz.getSimpleName());
                    } else if (!clProperties.omitFromCommandLine()) { /** We should check for missing annotations later **/
                        if (simpleNameToClass.containsKey(clazz.getSimpleName())) {
                            throw new RuntimeException("Simple class name collision: " + clazz.getSimpleName());
                        }
                        simpleNameToClass.put(clazz.getSimpleName(), clazz);
                    }
                }
        );
        if (!missingAnnotationClasses.isEmpty()) {
            throw new RuntimeException("The following classes are missing the required CommandLineProgramProperties annotation: " +
                    missingAnnotationClasses.stream().collect(Collectors.joining((", "))));
        }
        final Set<Class<?>> classes = new LinkedHashSet<>();
        classes.addAll(simpleNameToClass.values());

        if (args.length < 1 || args[0].equals("-h") || args[0].equals("--help")) {
            printUsage(System.out, classes, commandLineName);
        } else {
            if (simpleNameToClass.containsKey(args[0])) {
                final Class<?> clazz = simpleNameToClass.get(args[0]);
                try {
                    final Object commandLineProgram = clazz.newInstance();
                    return (CMDProgram) commandLineProgram;
                } catch (final InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            printUsage(System.err, classes, commandLineName);
            printUnknown(classes, args[0]);
        }
        return null;
    }


    public static void processAllCommandLinePrograms(
            final List<String> packageList,
            final BiConsumer<Class<CMDProgram>, CommandLineProgramProperties> clpClassProcessor) {
        final ClassFinder classFinder = new ClassFinder();
        packageList.forEach(pkg -> classFinder.find(pkg, CMDProgram.class));

        for (final Class clazz : classFinder.getClasses()) {
            if (!clazz.isInterface() && !clazz.isSynthetic() && !clazz.isPrimitive() && !clazz.isLocalClass()
                    && !Modifier.isAbstract(clazz.getModifiers())) {
                clpClassProcessor.accept(clazz, VarNoteCommandLine.getProgramProperty(clazz));
            }
        }
    }

    public static CommandLineProgramProperties getProgramProperty(Class clazz) {
        return (CommandLineProgramProperties)clazz.getAnnotation(CommandLineProgramProperties.class);
    }

    public static void printUsage(final PrintStream destinationStream, final Set<Class<?>> classes, final String commandLineName) {
        final StringBuilder builder = new StringBuilder();
        builder.append(BOLDRED + "USAGE: " + commandLineName + " " + GREEN + "<program name>" + BOLDRED + " [-h]\n\n" + KNRM)
                .append(BOLDRED + "Program Summary Table:\n" + KNRM);

        /** Group CommandLinePrograms by CommandLineProgramGroup **/
        final Map<Class<? extends CommandLineProgramGroup>, CommandLineProgramGroup> programGroupClassToProgramGroupInstance = new LinkedHashMap<>();
        final Map<CommandLineProgramGroup, List<Class<?>>> programsByGroup = new TreeMap<>(CommandLineProgramGroup.comparator);
        final Map<Class<?>, CommandLineProgramProperties> programsToProperty = new LinkedHashMap<>();
        for (final Class<?> clazz : classes) {
            // Get the command line property for this command line program
            final CommandLineProgramProperties property = getProgramProperty(clazz);
            if (null == property) {
                throw new RuntimeException(String.format("The class '%s' is missing the required CommandLineProgramProperties annotation.", clazz.getSimpleName()));
            } else if (!property.omitFromCommandLine()) { // only if they are not omit from the command line
                programsToProperty.put(clazz, property);
                // Get the command line program group for the command line property
                // NB: we want to minimize the number of times we make a new instance, hence programGroupClassToProgramGroupInstance
                CommandLineProgramGroup programGroup = programGroupClassToProgramGroupInstance.get(property.programGroup());
                if (null == programGroup) {
                    try {
                        programGroup = property.programGroup().newInstance();
                    } catch (final InstantiationException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                    programGroupClassToProgramGroupInstance.put(property.programGroup(), programGroup);
                }
                List<Class<?>> programs = programsByGroup.get(programGroup);
                if (null == programs) {
                    programsByGroup.put(programGroup, programs = new ArrayList<>());
                }
                programs.add(clazz);
            }
        }

        /** Print out the programs in each group **/
        for (final Map.Entry<CommandLineProgramGroup, List<Class<?>>> entry : programsByGroup.entrySet()) {
            final CommandLineProgramGroup programGroup = entry.getKey();

            builder.append(WHITE + "--------------------------------------------------------------------------------------\n" + KNRM);
            builder.append(String.format("%s%-48s %-45s%s\n", RED, programGroup.getName() + ":", programGroup.getDescription(), KNRM));

            final List<Class<?>> sortedClasses = new ArrayList<>();
            sortedClasses.addAll(entry.getValue());
            Collections.sort(sortedClasses, new SimpleNameComparator());

            for (final Class<?> clazz : sortedClasses) {
                final CommandLineProgramProperties clpProperties = programsToProperty.get(clazz);
                if (null == clpProperties) {
                    throw new RuntimeException(String.format("Unexpected error: did not find the CommandLineProgramProperties annotation for '%s'", clazz.getSimpleName()));
                }
                builder.append(getDisplaySummaryForTool(clazz, clpProperties));
            }
            builder.append(String.format("\n"));
        }
        builder.append(WHITE + "--------------------------------------------------------------------------------------\n" + KNRM);
        destinationStream.println(builder.toString());
    }

    protected static String getDisplaySummaryForTool(final Class<?> toolClass, final CommandLineProgramProperties clpProperties) {
        final BetaFeature betaFeature = toolClass.getAnnotation(BetaFeature.class);
        final ExperimentalFeature experimentalFeature = toolClass.getAnnotation(ExperimentalFeature.class);

        final StringBuilder builder = new StringBuilder();
        final String summaryLine;

        if (experimentalFeature != null) {
            summaryLine = String.format("%s%s %s%s", RED, "(EXPERIMENTAL Tool)", CYAN, clpProperties.oneLineSummary());
        } else if (betaFeature != null) {
            summaryLine = String.format("%s%s %s%s", RED, "(BETA Tool)", CYAN, clpProperties.oneLineSummary());
        } else {
            summaryLine = String.format("%s%s", CYAN, clpProperties.oneLineSummary());
        }
        final String annotatedToolName = getDisplayNameForToolClass(toolClass);
        if (toolClass.getSimpleName().length() >= 45) {
            builder.append(String.format("%s    %s    %s%s\n", GREEN, annotatedToolName, summaryLine, KNRM));
        } else {
            builder.append(String.format("%s    %-45s%s%s\n", GREEN, annotatedToolName, summaryLine, KNRM));
        }
        return builder.toString();
    }

    protected static String getDisplayNameForToolClass(final Class<?> toolClass) {
        if (toolClass == null) {
            throw new IllegalArgumentException("A valid class is required to get a display name");
        }

        return toolClass.getSimpleName();
    }

    private static class SimpleNameComparator implements Comparator<Class> {
        @Override
        public int compare(final Class aClass, final Class bClass) {
            return aClass.getSimpleName().compareTo(bClass.getSimpleName());
        }
    }

    public static void printUnknown(final Set<Class<?>> classes, final String command) {
        final Map<Class, Integer> distances = new HashMap<Class, Integer>();

        int bestDistance = Integer.MAX_VALUE;
        int bestN = 0;

        // Score against all classes
        for (final Class clazz : classes) {
            final String name = clazz.getSimpleName();
            final int distance;
            if (name.equals(command)) {
                throw new RuntimeException("Command matches: " + command);
            }
            if (name.startsWith(command) || (MINIMUM_SUBSTRING_LENGTH <= command.length() && name.contains(command))) {
                distance = 0;
            }
            else {
                distance = StringUtil.levenshteinDistance(command, name, 0, 2, 1, 4);
            }
            distances.put(clazz, distance);

            if (distance < bestDistance) {
                bestDistance = distance;
                bestN = 1;
            }
            else if (distance == bestDistance) {
                bestN++;
            }
        }

        // Upper bound on the similarity score
        if (0 == bestDistance && bestN == classes.size()) {
            bestDistance = HELP_SIMILARITY_FLOOR + 1;
        }

        // IntersetOutput similar matches
        System.err.println(String.format("'%s' is not a valid command. See VarNote -h for more information.", command));
        if (bestDistance < HELP_SIMILARITY_FLOOR) {
            System.err.println(String.format("Did you mean %s?", (bestN < 2) ? "this" : "one of these"));
            for (final Class clazz : classes) {
                if (bestDistance == distances.get(clazz)) {
                    System.err.println(String.format("        %s", clazz.getSimpleName()));
                }
            }
        }
    }
}
