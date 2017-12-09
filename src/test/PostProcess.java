package test;

import htsjdk.samtools.util.StringUtil;
import htsjdk.tribble.index.tabix.TabixFormat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import constants.BasicUtils;
import process.ProcessResult;
import readers.AbstractReader.Iterator;
import bean.config.AnnoConfigBean;
import bean.config.PostConfigBean;
import bean.config.AnnoConfigBean.ResultFileFormat;
import bean.config.PostConfigBean.OutMode;
import bean.config.PostConfigBean.OutType;
import bean.node.Node;
import bean.node.NodeFactory;
import bean.node.ResultNode;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class PostProcess implements ProcessResult{
	private PostConfigBean config;
	private List<ResultNode> result;
	
	public PostProcess(PostConfigBean config) {
		super();
		this.config = config;
		this.result = new ArrayList<ResultNode>();
	}
	
	public void doProcess() {
		try {
			List<PostStack> stacks = new ArrayList<PostStack>();
			PostStack stack;
			
			for (AnnoConfigBean postAnnBean : config.getResultConfigs()) {
				stack = new PostStack(this);
				stack.setIterator(new PostImpl(postAnnBean.getResultPath(), postAnnBean));
				stacks.add(stack);
			}
			
			final BufferedReader qReader = new BufferedReader(new InputStreamReader(new FileInputStream(config.getQueryFile())));
			final BufferedWriter write = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(config.getOutputFile()))));
			String line;
			Node qNode;
			TabixFormat queryFormat = config.getQueryFormatForTabix();
			
			String lastChr = "";
			Node lastNode = null;
			while((line = qReader.readLine()) != null) {
				if(!BasicUtils.isHeader(line)) {
//					System.out.println(line);
					qNode = NodeFactory.createQueryNodeWithRV(line, queryFormat);
					if(!qNode.chr.equals(lastChr)) {
						if(!lastChr.equals("")) {
							for (PostStack postStack : stacks) {
								postStack.updateLastChr(lastChr);
							}
						}
						lastChr = qNode.chr;
					}
				
					if(lastNode == null || (lastNode != null && !lastNode.isPositionEq(qNode))) {
						if(result.size() > 0) result.clear();
						for (PostStack postStack : stacks) {
							postStack.findOverlap(qNode);
						}
					} 

					ResultFileFormat format = config.getOutFormat();
					if(config.getOutType() == OutType.OVERLAP) {
						if(config.isLoj()) {
							write.write(">\t" + qNode.getOrigStr() + "\n");
						} else {
							if(result.size() > 0 && config.getOutMode() != OutMode.DB) {
								write.write(">\t" + qNode.getOrigStr() + "\n");
							}
						}
						
						if(result.size() > 0) {	
							if(config.getOutMode() != OutMode.Query)
								for (ResultNode node : result) {
									//write.write(node.origStr + "\n");
									write.write("<\t" + node.getDb() + "\t" + node.chr + "\t" + node.beg + "\t" + node.end + "\t" + getResultForOverlap(node, format)  + "\n");
								}
						}
					} else {
						String r = joinResult(qNode, result, format);
						if(r == null) {
							if(config.isLoj()) {
								write.write( qNode.origStr + "\t.\n");
							} 
						} else {
							if(config.getOutMode() == OutMode.Query) {
								write.write( qNode.getOrigStr() + "\n");
							} else if(config.getOutMode() == OutMode.DB) {
								write.write( r + "\n");
							} else {
								write.write( qNode.getOrigStr() + "\t" + r +"\n");
							}
						}
					}
					lastNode = qNode;
				} else {
					write.write(line);
				}
			}
			qReader.close();
			write.flush();
			write.close();
			
			for (PostStack postStack : stacks) {
				PostImpl it = (PostImpl)postStack.getIt();
				it.closeReader();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getResultForOverlap(ResultNode node, ResultFileFormat format) {
		List<AnnNode> anns;
		List<String> annStr;
	    if(format == ResultFileFormat.VCF) {
	    	Map<String, String> nameToVal = new HashMap<String, String>();
	    	String vals;
	    	for (String alt : node.alts) {
		    	anns = node.altVals.get(alt);
				for (AnnNode annNode : anns) {
					if(nameToVal.get(annNode.getName()) == null) {
						vals = alt + "|" + annNode.getVal();
						nameToVal.put(annNode.getName(), vals);
					} else {
						vals = nameToVal.get(annNode.getName());
						vals = vals + "," + alt + "|" + annNode.getVal();
						nameToVal.put(annNode.getName(), vals);
					}
				}
	    	}
	    	List<String> result = new ArrayList<String>();
	    	for (String key : nameToVal.keySet()) {
	    		result.add(key + "=" + nameToVal.get(key));
			}
	    	return StringUtil.join(";", result);
	    } else { //ann
	    	List<String> result = new ArrayList<String>();
	    	for (String alt : node.alts) {
				annStr = new ArrayList<String>();
				anns = node.altVals.get(alt);
				for (AnnNode annNode : anns) {
//					System.out.println(annNode.getName() + "=" + annNode.getVal());
					annStr.add(annNode.toString(format));
				}
				result.add(alt + "|" + StringUtil.join("|", annStr));
			}
			return StringUtil.join(",", result);
	    }
	}
	
	public String joinResult(Node query, List<ResultNode> nodes, ResultFileFormat format) {
		HashMap<String, String> vals = null;
		List<AnnNode> anns;
		String s;
	    if(format == ResultFileFormat.VCF) {
	    	Map<String, HashMap<String, String>> nameToVal = new HashMap<String, HashMap<String,String>>();
	    	for (String alt : query.alts) {
				for (ResultNode rnode : nodes) {
					anns = rnode.altVals.get(alt);
					
					if(anns != null)
					for (AnnNode annNode : anns) {
						
						if(nameToVal.get(annNode.getName()) == null) {
							vals = new HashMap<String, String>();
							vals.put(alt, alt + "|" + annNode.getVal());
						} else {
							s = vals.get(alt);
							s = s + "|" + annNode.getVal();
							vals.put(alt, s);
						}
					}
				}
	    	}
	    	List<String> result = new ArrayList<String>();
	    	List<String> altVal;
	    	for (String name : nameToVal.keySet()) {
	    		vals = nameToVal.get(name);
	    		altVal = new ArrayList<String>();
	    		for (String alt : vals.keySet()) {
	    			altVal.add(vals.get(alt));
	    		}
	    		if(altVal.size() > 0) {
	    			result.add(name + "=" + StringUtil.join(",", altVal));
	    		}
			}
	    	if(result.size() > 0) {
	    		return StringUtil.join(";", result);
	    	} else {
	    		return null;
	    	}
	    } else { //ann
	    	Map<String, List<String>> altToVal = new HashMap<String, List<String>>();
	    	List<String> altVals;
	    	for (String alt : query.alts) {
				for (ResultNode rnode : nodes) {
					anns = rnode.altVals.get(alt);
					if(anns != null)
					for (AnnNode annNode : anns) {
					
						if(altToVal.get(alt) == null) {
							altVals = new ArrayList<String>();
							altVals.add(annNode.getVal());
							altToVal.put(alt, altVals);
						} else {
							altVals = altToVal.get(alt);
							altVals.add(annNode.getVal());
							altToVal.put(alt, altVals);
						}
					}
				}
	    	}
	    	
	    	List<String> result = new ArrayList<String>();
	    	for (String alt : query.alts) {
	    		if(altToVal.get(alt) != null)
	    			result.add(alt + "|" + StringUtil.join("|", altToVal.get(alt)));
			}
	    	if(result.size() > 0) {
	    		return StringUtil.join(",", result);
	    	} else {
	    		return null;
	    	}
	    }
	}

	@Override
	public void doProcess(Node q, Node d) {
		result.add((ResultNode)d);
	}
	
	private static OptionParser getParser() {
		OptionParser parser = new OptionParser();
		parser.accepts("help", "Print help information");
		parser.accepts("config", "Config file path").withRequiredArg().describedAs("Config file path").ofType(String.class); 
		parser.accepts("version", "Print program version");
        return parser;
    }
	
	public static void main(String[] args) {

		OptionParser parser = null;
		OptionSet options = null;
		try {
			parser = getParser();
			options = parser.parse(args);
		} catch (OptionException oe) {
			System.out.println("Exception when parsing arguments : " + oe.getMessage());
			return;
		}

		if (options.has("help")) {
			try {
				parser.printHelpOn(System.out);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.exit(0);
		}

		if (options.has("version")) {
			System.out.println("V1.0");
			System.exit(0);
		}

		if (!options.has("config")) {
			System.err.println("Missing required arguments: config.");
		    System.exit(1);
		} else {
			PostProcess pp = new PostProcess(ParseConfigFile.parsePostANNConfig((String)options.valueOf("config")));
			pp.doProcess();
		}
	}
	
	private class PostImpl implements Iterator {
		private final BufferedReader reader;
		private final AnnoConfigBean config;
		private boolean iseof;
		
		private PostImpl(final File path, final AnnoConfigBean _config) throws FileNotFoundException {
	    	iseof = false;
	    	reader = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
	    	config = _config;
	    }
	    		 
		@Override
		public String next() throws IOException {
			return reader.readLine();
		}
		
		@Override
		public Node nextNode() throws IOException {
			if (iseof) return null;
			
			String s = this.next();	
			if(s == null) {
				iseof = true;
				return null;
			} else {
				return NodeFactory.createNodeForResult(s, config);
			}
		}
		
		public void closeReader() throws IOException {
			reader.close();
		}
	}

}
