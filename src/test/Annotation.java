package test;

import htsjdk.samtools.util.IOUtil;
import htsjdk.samtools.util.StringUtil;
import htsjdk.tribble.index.tabix.TabixFormat;
import htsjdk.tribble.readers.LongLineBufferedReader;
import htsjdk.variant.vcf.VCFCodec;
import htsjdk.variant.vcf.VCFConstants;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFHeader.HEADER_FIELDS;
import htsjdk.variant.vcf.VCFHeaderLine;
import htsjdk.variant.vcf.VCFInfoHeaderLine;

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

import test.AnnoDBBean_bak;
import test.ParseConfigFile;
import test.RunConfigBean;
import test.AnnoDBBean_bak.AnnNode;
import bean.config.AnnoConfigBean;
import bean.config.AnnoFieldBean_bak;
import bean.config.anno.AbstractParser;
import bean.config.run.AnnoRunBean;
import bean.config.run.DBRunConfigBean;
import bean.node.NodeFactory;
import bean.node.RefNode;

public class Annotation {
	private final AnnoRunBean annoRunBean;
//	private final AnnoConfigBean annoConfig;
//	private final String overlapFilePath;
//	private List<String> keys;
//	private Map<String, Boolean> keyFlag;
//	private VCFHeader vcfHeader;
//	private final VCFCodec codec;
//
//	private List<String> infoKeys;
//	public static final String TAB_JION = "\t";
//	public static final String COLEN_JOIN = ";";
//	public static final String NEW_LINE = "\n";
//
//	private final boolean isQueryFormatVCF;
	
	public Annotation(final AnnoRunBean annoRunBean) {
		this.annoRunBean = annoRunBean;
//		super();
//		this.config = config;
//		this.annoConfig = annoConfig;
//		this.codec = new VCFCodec();
//		
//		overlapFilePath = config.getOverlapOutput();
//		IOUtil.assertInputIsValid(overlapFilePath);
//		annoOutFormat = annoConfig.getAnnoOutFormat();
//		queryFormat = this.config.getQueryFormat();
//		isQueryFormatVCF = queryFormat.tbiFormat.flags == TabixFormat.VCF_FLAGS;
//		if(isQueryFormatVCF) {
//			readHeaderFiles(new File(config.getQueryFile()));
//		} else {
//			if(annoConfig.getBedHeaderPath() != null) {
//				readHeaderFiles(annoConfig.getBedHeaderPath());
//			} else {
//				annoConfig.checkHeaderLines();
//			}
//		}
//		
//		Map<String, DBRunConfigBean> dbs = new HashMap<String, DBRunConfigBean>();
//		for (DBRunConfigBean db : config.getDbs()) {
//			dbs.put(db.getLable(), db);
//		}
//		
//		try {
//			AnnoDBBean_bak annoDB;
//			DBRunConfigBean db;
//			keys = new ArrayList<String>();
//			keyFlag = new HashMap<String, Boolean>();
//			
//			for (String label : annoConfig.getDbNames()) {
//				annoDB = annoConfig.getDB(label);
//				db = dbs.get(label);
//				if(db == null) throw new IllegalArgumentException("Sorry, we cannot find config information for " + label + " .");
//				
//				if(annoDB.getDbFormat() == FileFormatSupport.VCF) {
//					final VCFHeader header = (VCFHeader)codec.readActualHeader(new LineIteratorImpl(new File(db.getDbPath())));
//					annoDB.setVcfHeader(header);
//					
//					if(annoDB.isAll()) {
//						for (VCFInfoHeaderLine info : header.getInfoHeaderLines()) {
//							if(annoDB.containsField(info.getID())) {
//								annoDB.setVcfInfo(info.getID(), info);
//							} else {
//								annoDB.addField(new AnnoFieldBean_bak(info.getID(), info));
//							}
//						}
//					} else {
//						annoDB.setVcfInfos();
//					}
//					FormatWithRef format = FormatWithRef.VCF;
//					format.tbiFormat = db.getIdx().getFormat();
//					annoDB.setFormatWithRef(format);
//				} else {      //=================      BED      ===================
//					annoDB.setBedFields(readBedHeader(db.getDbPath()));
//					if(annoDB.isAll()) {
//						List<String> bedFieldKeys = annoDB.getBedFieldKeys();
//						AnnoFieldBean_bak field;
//						if(bedFieldKeys == null || bedFieldKeys.size() == 0) throw new IllegalArgumentException("Please set up header_fields for bed file " + label);
//						for (String string : bedFieldKeys) {
//							field = new AnnoFieldBean_bak(string, null);
//							field.setBedHeaderInfo(label);
//							annoDB.addField(field);
//						}
//					} else {
//						annoDB.checkBed();
//					}
//				}
//				
//				if(vcfHeader != null) {
//					AnnoFieldBean_bak field;
//					VCFInfoHeaderLine dbInfo;
//					for (String fieldName : annoDB.getKeys()) {
//						field = annoDB.getField(fieldName);
//						dbInfo = field.getVcfInfo();
//						if(vcfHeader.getInfoHeaderLine(dbInfo.getID()) != null) throw new IllegalArgumentException("Duplicate name " + fieldName + ", please reset out_name for field " + field.getName() + " of db " + label + ".");
//						vcfHeader.addMetaDataLine(dbInfo);
//					}
//				}
//				
//				String outName;
//				for (String fieldName : annoDB.getKeys()) {
//					outName = (annoDB.getField(fieldName).getOutName() == null) ? annoDB.getField(fieldName).getName() : annoDB.getField(fieldName).getOutName();
//					keys.add(outName);
//					keyFlag.put(outName, (annoDB.getField(fieldName).getVcfInfo().getType() == VCFHeaderLineType.Flag));
//				}
//				
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

//	public Annotation(final RunConfigBean config) {
//		this(config, ParseConfigFile.parsePostANNConfig(config.getAnnoConfig()));
//	}

//	public Annotation(final String overlapPath, final String annoConfig) {
//		this(ParseConfigFile.parseRunConfigForAnno(overlapPath), ParseConfigFile.parsePostANNConfig(annoConfig));
//	}
//	
//	public void readHeaderFiles(File file) {
//		try {
//			vcfHeader = (VCFHeader)codec.readActualHeader(new LineIteratorImpl(file));
//			infoKeys = new ArrayList<String>();
//			for (VCFInfoHeaderLine info : vcfHeader.getInfoHeaderLines()) {
//				infoKeys.add(info.getID());
//			}
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
//	}
//	
//	@SuppressWarnings("resource")
	public void doAnnotation() {
		try {
			String line;

			RefNode query = null, db;
			final Map<String, AbstractParser> dbPareser = annoRunBean.getDbPareser();
			final AbstractParser queryPareser = annoRunBean.getQueryParser();
			
			final LongLineBufferedReader reader = new LongLineBufferedReader(new InputStreamReader(new FileInputStream(annoRunBean.getOverlapFile())));
			final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(annoRunBean.getOutputFile()))));
			
			writer.write(queryPareser.printHeader(annoRunBean.getAnnoOutFormat()));
			
			//write header information
			if(isQueryFormatVCF) { //query is vcf
				String gs = StringUtil.join(TAB_JION, vcfHeader.getGenotypeSamples());
				if(gs.length() > 0) gs = TAB_JION + gs;
				if(annoOutFormat == FileFormatSupport.VCF) {
					outHeader(writer, vcfHeader);
					writer.write(VCFHeader.HEADER_INDICATOR + StringUtil.join(TAB_JION, VCFHeader.HEADER_FIELDS.values()) + gs + NEW_LINE);
				} else {
					String h = StringUtil.join(TAB_JION, VCFHeader.HEADER_FIELDS.values());
					h = h.replace(HEADER_FIELDS.POS.toString(), ParseConfigFile.HEADER_BEG);
					h = h.replace(HEADER_FIELDS.ID.toString(), ParseConfigFile.HEADER_END);
					h = h.replace(HEADER_FIELDS.INFO.toString(), StringUtil.join(TAB_JION, addList(infoKeys, keys)));
					writer.write(VCFHeader.HEADER_INDICATOR + h + gs + NEW_LINE);
				}
			} else {
				final String queryHeader = readBedHeader(config.getQueryFile());
				config.setBedFields(queryHeader);
				final List<String> queryFields = config.getQueryFieldKeysList();
				if(queryFields == null) throw new IllegalArgumentException("Please set up fields in query_file_format for query file in config file.");
				
				if(annoOutFormat == FileFormatSupport.VCF) {
					if(vcfHeader != null) {
						String gs = StringUtil.join(TAB_JION, vcfHeader.getGenotypeSamples());
						if(gs.length() > 0) gs = TAB_JION + gs;
						outHeader(writer, vcfHeader);
						writer.write(VCFHeader.HEADER_INDICATOR + StringUtil.join(TAB_JION, VCFHeader.HEADER_FIELDS.values()) + gs + NEW_LINE);
					} else {
						writer.write(VCFHeader.HEADER_INDICATOR + StringUtil.join(TAB_JION, VCFHeader.HEADER_FIELDS.values()) + NEW_LINE);
					}
				} else {
					writer.write(StringUtil.join(TAB_JION, addList(queryFields, keys)) + NEW_LINE);
				}
			}
			
			final boolean forceOverlap = annoConfig.isForceOverlap();
			while((line = reader.readLine()) != null) {
				beg = line.indexOf("\t");
				if(line.startsWith("#query")) {
					if(query != null) out(writer, results, query, annoDB);
					if(isQueryFormatVCF) {
						query = NodeFactory.createRefAltVCF(line.substring(beg + 1), queryFormat);
					} else {
						query = NodeFactory.createRefAltBED(line.substring(beg + 1), queryFormat, config);
					}
					
					results = new ArrayList<ResultNode>();
					continue;
				}
				
				label = line.substring(0, beg);
				annoDB = annoConfig.getDB(label);
				if(annoDB == null) continue;		
				
				db = NodeFactory.createQueryNodeWithRV(line.substring(beg + 1), annoDB.getFormatWithRef());
				results.add(new ResultNode(db, annoDB.getResult(query, db, forceOverlap)));
			}
			writer.close();
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
//	
//	public static List<String> addList(List<String> a, List<String> b) {
//		if(b == null || b.size() == 0) return a;
//		if(a == null || a.size() == 0) return b;
//		a.addAll(b);
//		return a;
//	}
//	
//	public String readBedHeader(String filePath) throws IOException {
//		LineIteratorImpl reader = new LineIteratorImpl(new File(filePath));
//		String line = null;
//		if(reader.hasNext()) {
//			line = reader.next();
//			if((line.startsWith("#") || line.startsWith("@")) && !line.startsWith("##") && !line.startsWith("@@")) {
//				System.out.println("Find and use header " + line + " in file " + filePath);
//				reader.close();
//				return line.substring(1);
//			} 
//		}
//		reader.close();
//		return null;
//	}
//	
//	
//	public void outHeader(BufferedWriter writer, VCFHeader h) throws IOException {
//		for (VCFHeaderLine header : h.getMetaDataInSortedOrder()) {
//			writer.write(VCFHeader.METADATA_INDICATOR + header.toString() + NEW_LINE);
//		}
//	}
//	public List<String> outInfo(BufferedWriter writer, String info) throws IOException {
//		List<String> result = new ArrayList<String>();
//		int beg, end;
//		for (VCFInfoHeaderLine header : vcfHeader.getInfoHeaderLines()) {
//			beg = info.indexOf(header.getID());
//			if(beg == -1) {
//				result.add(".");
//			} else {
//				if(header.getType() == VCFHeaderLineType.Flag) {
//					result.add(header.getID());
//				} else {
//					beg = info.indexOf("=", beg);
//					end = info.indexOf(VCFConstants.INFO_FIELD_SEPARATOR, beg);
//					result.add(header.getID() + "=" + ((end == -1) ? info.substring(beg + 1) : info.substring(beg + 1, end)));
//				}
//			}
//		}
//		return result;
//	}
//	
//	public void out(BufferedWriter writer, List<ResultNode> results, NodeWithRefAlt query, AnnoDBBean_bak annoDB) throws IOException {
//	    List<String> tokens = new ArrayList<String>();
//	    tokens.add(query.chr);
//		tokens.add(query.beg + "");
//		if(annoOutFormat == FileFormatSupport.VCF) {
//			tokens.add(".");
//		} else {
//			tokens.add(query.end + "");
//		}
//		tokens.add(query.ref);
//		tokens.add(StringUtil.join(",", query.alts));
//		
//		if(annoOutFormat == FileFormatSupport.VCF) {
//			tokens.add((query.qulity == null) ? "." : query.qulity);
//			tokens.add((query.filter == null) ? "." : query.filter);
//			if(isQueryFormatVCF) {
//				tokens.add(query.info + COLEN_JOIN + resultsToStr(results, annoDB));
//				for (String gs : query.vcfGS) {
//					tokens.add(gs);
//				}
//			} else {
//				String val;
//				List<String> info = new ArrayList<String>();
//				for (String key : config.getQueryFieldKeysList()) {
//					
//					val = query.colToValMap.get(config.getQueryFieldCol(key) - 1);
//					if(val != null) {
//						info.add(key + "=" + val);
//					}
//				}
//				if(info.size() > 0) {
//					tokens.add(StringUtil.join(COLEN_JOIN, info) + COLEN_JOIN + resultsToStr(results, annoDB));
//				} else {
//					tokens.add(resultsToStr(results, annoDB));
//				}
//			}
////			writer.write(StringUtil.join(TAB_JION, tokens));
//		} else {
//			if(isQueryFormatVCF) {
//				tokens.add((query.qulity == null) ? "." : query.qulity);
//				tokens.add((query.filter == null) ? "." : query.filter);
//				tokens = addList(tokens, outInfo(writer, query.info));
//				tokens.add(resultsToStr(results, annoDB));
//			} else {
//				if(query.qulity != null) tokens.add(query.qulity);
//				if(query.filter != null) tokens.add(query.filter);
//		
//				String val;
//				for (String key : config.getQueryFieldKeysList()) {
//					val = query.colToValMap.get(config.getQueryFieldCol(key) - 1);
//					if(val != null) {
//						tokens.add(val);
//					}
//				}
//				tokens.add(resultsToStr(results, annoDB));
//			}
//		}
//		writer.write(StringUtil.join(TAB_JION, tokens));
//		writer.write(NEW_LINE);
//	}
//	
//	public Map<String, List<AnnNode>> toMap(List<ResultNode> results) {
//		Map<String, List<AnnNode>> map = new HashMap<String, List<AnnNode>>();
//		List<AnnNode> list;
//		for (ResultNode node : results) {
//			if(node.results != null)
//			for (AnnNode anno : node.results) {
//				if(map.get(anno.getName()) == null) {
//					list = new ArrayList<AnnNode>();
//				} else {
//					list = map.get(anno.getName());
//				}
//				list.add(anno);
//				map.put(anno.getName(), list);
//			}
//		}
//		return map;
//	}
//	
//	public String resultsToStr(List<ResultNode> results, AnnoDBBean_bak annoDB) {
//		List<AnnNode> list;
//		List<String> allValues = new ArrayList<String>();
//		List<String> oneValForkey;
//		Map<String, List<AnnNode>> map = toMap(results);
//		
//		for (String key : keys) {
//			list = map.get(key);
//			oneValForkey = new ArrayList<String>();
//			if(list != null) {
//				for (AnnNode annNode : list) {
//					if(keyFlag.get(key)) {
//						if(annNode.getVal() != null) {
//							oneValForkey.add(annNode.getVal());
//							break;
//						}
//					} else {
//						oneValForkey.add(annNode.getVal());
//					}
//				}
//			}
//			if(annoOutFormat == FileFormatSupport.VCF) {
//				if(keyFlag.get(key)) {
//					if(oneValForkey.size() > 0)
//						allValues.add(oneValForkey.get(0) != null ? oneValForkey.get(0) : "");
//				} else {
//					allValues.add(key + "=" + (oneValForkey.size() == 0 ? ".": StringUtil.join(",", oneValForkey)));
//				}
//			} else {
//				allValues.add(oneValForkey.size() == 0 ? ".": StringUtil.join(",", oneValForkey));
//			}
//		}
//		
//		if(annoOutFormat == FileFormatSupport.VCF) {
//			return StringUtil.join(VCFConstants.INFO_FIELD_SEPARATOR, allValues);
//		} else {
//			return StringUtil.join("\t", allValues);
//		}
//	}
//	
//	public class ResultNode {
//		final NodeWithRefAlt db;
//		final List<AnnNode> results;
//		
//		public ResultNode(NodeWithRefAlt db, List<AnnNode> results) {
//			super();
//			this.db = db;
//			this.results = results;
//		}
//	}
}
