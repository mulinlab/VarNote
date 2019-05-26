package main.java.vanno.run;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import joptsimple.HelpFormatter;
import joptsimple.OptionDescriptor;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import main.java.vanno.bean.config.run.AnnoRunConfig;
import main.java.vanno.bean.config.run.OverlapRunConfig;
import main.java.vanno.constants.BasicUtils;
import main.java.vanno.constants.VannoUtils;
import main.java.vanno.constants.VannoUtils.PROGRAM;
import main.java.vanno.index.IndexWriteConfig;

public final class VannoSearch {
   
    private final static String MAX_INDEX = "100";
    private final static HelpFormatter formatter = new HelpFormatter() {
		
		@Override
		public String format(Map<String, ? extends OptionDescriptor> options) {
			StringBuilder buffer = new StringBuilder();
		
			Map<Integer, OptionDescriptor> requiredOption = new HashMap<Integer, OptionDescriptor>();
			Map<Integer, OptionDescriptor> otherOption = new HashMap<Integer, OptionDescriptor>();
			
			String ad;
			Integer index;
            for ( OptionDescriptor each : new HashSet<>( options.values() ) ) {
            		ad = each.argumentDescription();
            		index = Integer.parseInt((ad.indexOf(BasicUtils.OPTION_SEPERATE) != -1) ? ad.split(BasicUtils.OPTION_SEPERATE)[0] : MAX_INDEX);
            		if(each.requiresArgument()) {
            			requiredOption.put(index, each);
            		} else {
            			otherOption.put(index, each);
            		}
            }
            
            buffer.append(BasicUtils.KBLU + "     Required Arguments\n");
            Integer[] keys = (Integer[]) requiredOption.keySet().toArray(new Integer[0]);
            Arrays.sort(keys);
            for(Integer key : keys) {
            		buffer.append( lineFor(requiredOption.get(key)) );
            }
            
            buffer.append("\n\n" + BasicUtils.KBLU + "     Optional Arguments\n");
            keys = (Integer[]) otherOption.keySet().toArray(new Integer[0]);
            Arrays.sort(keys);
            for(Integer key : keys) {
            	 	if (!otherOption.get(key).representsNonOptions() )
            	 		buffer.append( lineFor(otherOption.get(key)) );
            }
            buffer.append("\n\n");
            return buffer.toString();
		}
		
		private String lineFor( OptionDescriptor descriptor ) {
			String header = createOptionDisplay(descriptor);
			String desc =  descriptor.description();
			String ad = descriptor.argumentDescription();
			return String.format("     %s%-25s%s%-25s%s%-10s%s\n", BasicUtils.KBLU, header, BasicUtils.KGRN, (ad.indexOf(":") != -1) ? ad.split(":")[1] : ad, BasicUtils.KCYN, desc, BasicUtils.KNRM); //KGRN, descriptor.argumentTypeIndicator(),
        }
		
		protected String createOptionDisplay(OptionDescriptor descriptor) {
			StringBuilder buffer = new StringBuilder(); //descriptor.requiresArgument() ? " <" : " ["

			for (Iterator<String> i = descriptor.options().iterator(); i.hasNext();) {
				String option = i.next();
				buffer.append(option.length() > 1 ? BasicUtils.DOUBLE_HYPHEN : BasicUtils.HYPHEN_CHAR); //KGRN
				buffer.append(option);

				if (i.hasNext())
					buffer.append(", ");
			}
			return buffer.toString();
		}
	};
	
	public static void printProUsage(final OptionParser parser, final PROGRAM program) throws IOException {
		//System.out.print(KBLDRED + "Available Programs:\n" + KNRM);
		System.out.print(String.format("\n\n     %s%-50s%-10s%s\n", BasicUtils.KBLDRED, program.getName() + ":", program.getDesc(), BasicUtils.KNRM));
		System.out.print(BasicUtils.KWHT + "---------------------------------------------------------------------------------------------------------------------------\n" + BasicUtils.KNRM);
		parser.formatHelpWith(formatter);
		parser.printHelpOn(System.out);
	}
	
	public static void printPro(final PROGRAM program) {
		System.out.print(String.format("  %s%-13s %s%-10s%s\n", BasicUtils.KGRN, program.getName() + ":", BasicUtils.KCYN, program.getDesc(), BasicUtils.KNRM));
	}
	/**
	 * @param args
	 * @throws IOException 
	 */
    public static void printUsage() throws IOException {
    		//java -jar /path/to/VarNote.jar <command> [options]
    		System.out.print(BasicUtils.KBLDRED + "\nUSAGE: " + BasicUtils.PRO_CMD + " " + BasicUtils.KGRN + "<program>" + BasicUtils.KBLDRED + " [options]\n\n" + BasicUtils.KNRM);
    		System.out.print(BasicUtils.KRED + "where " + BasicUtils.KGRN + "<program>" + BasicUtils.KRED + " is one of:\n" + BasicUtils.KNRM);
    		printPro(PROGRAM.INDEX);
    		printPro(PROGRAM.QUERY);
    		printPro(PROGRAM.OVERLAP);
    		printPro(PROGRAM.ANNO);
    		System.out.print(BasicUtils.KRED + "\nmore details, please use: " + BasicUtils.PRO_CMD + " " + BasicUtils.KGRN + "<program>" + BasicUtils.KRED + " " + BasicUtils.HELP_OPTION_ABBR + " for help\n" + BasicUtils.KNRM);
    		System.out.print(BasicUtils.KRED + "example:  " + BasicUtils.PRO_CMD + " index " + BasicUtils.HELP_OPTION_ABBR + "\n\n " + BasicUtils.KNRM);
    }
    
	public static void main(String[] args) {
		OptionParser parser = null;
		OptionSet options = null;
		try {
			if (args.length < 1) {
				printUsage();
			} else if (args[0].equals(BasicUtils.HELP_OPTION_ABBR) || args[0].equals(BasicUtils.HELP_OPTION)) {
				printUsage();
			} else {
				PROGRAM program = VannoUtils.checkARG(args[0]);
				if (program == PROGRAM.OVERLAP) {
					parser = OptionParserFactory.getParserForOverlap();
				} else if (program == PROGRAM.QUERY) {
					parser = OptionParserFactory.getParserForQuery();
				} else if (program == PROGRAM.ANNO) {
					parser = OptionParserFactory.getParserForAnno();
				} else {
					parser = OptionParserFactory.getParserForIndex();
				}
				
				if(args.length == 1 || args[1].equals(BasicUtils.HELP_OPTION_ABBR) || args[1].equals(BasicUtils.HELP_OPTION)) {
					printProUsage(parser, program);
				} else {
					options = parser.parse(args);

					if (program == PROGRAM.INDEX) {
						IndexWriteConfig indexConfig = ReadConfigFactory.readIndexConfig(options);
						if (indexConfig != null) {
							RunFactory.writeIndex(indexConfig);
						}
					} else if (program == PROGRAM.QUERY) {
						RunFactory.runQuery(ReadConfigFactory.readQueryConfig(options));
					} else {
						OverlapRunConfig config = ReadConfigFactory.readConfig(options, program);
						if (program == PROGRAM.ANNO && ((AnnoRunConfig) config).getOverlapFile() != null) {
							RunFactory.runAnnoFromOverlapFile((AnnoRunConfig) config);
						} else {
							RunFactory.run(config);
						}
					}
				}
			}
		} catch (OptionException oe) {
			System.out.println("Exception when parsing arguments: " + oe.getMessage());
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
