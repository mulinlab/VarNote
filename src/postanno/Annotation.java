package postanno;


import htsjdk.tribble.readers.LongLineBufferedReader;
import htsjdk.variant.vcf.VCFHeader;
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
import java.util.List;
import java.util.Map;

import constants.VannoUtils;
import bean.config.anno.AbstractParser;
import bean.config.anno.BEDParserDB;
import bean.config.anno.BEDParserQuery;
import bean.config.anno.VCFParserDB;
import bean.config.anno.VCFParserQuery;
import bean.config.readconfig.AbstractReadConfig;
import bean.config.run.AnnoRunBean;
import bean.config.run.AnnoRunBean.FileFormatSupport;
import bean.node.BedNode;
import bean.node.NodeFactory;
import bean.node.RefNode;
import bean.node.VCFNode;

public class Annotation {
	public static final String TAB = VannoUtils.TAB_SPILT;
	public static final String INFO_FIELD_SEPARATOR = AbstractParser.INFO_FIELD_SEPARATOR;
	public static final String NEWLINE = "\n";
	
	private final AnnoRunBean annoRunBean;
	
	public Annotation(final AnnoRunBean annoRunBean) {
		super();
		this.annoRunBean = annoRunBean;
	}

	public void doAnnotation() {
		try {
			String line;

			RefNode queryNode = null;
			final Map<String, AbstractParser> dbParesers = annoRunBean.getDbPareser();
			final LongLineBufferedReader reader = new LongLineBufferedReader(new InputStreamReader(new FileInputStream(annoRunBean.getOverlapFile())));
			final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(annoRunBean.getOutputFile()))));
			
			printHeader(writer);   //print header
			int beg;
			String label;
			AbstractParser dbParser;
			while(((line = reader.readLine()) != null)) {
				if(!line.startsWith(AbstractReadConfig.OVERLAP_NOTE)) {
					beg = line.indexOf("\t");
					if(line.startsWith("#query")) {
						if(queryNode != null) writer.write(combineResults(queryNode) + NEWLINE);
						
						queryNode = NodeFactory.createRefAlt(line.substring(beg + 1), annoRunBean.getQueryFormat());
						for (String field : annoRunBean.getDbNames()) {
							if(dbParesers.get(field) != null) dbParesers.get(field).emptyNodes(queryNode.alts);
						}
						continue;
					}
					
					label = line.substring(0, beg);
					dbParser = dbParesers.get(label);
					if(dbParser == null) continue;		
					
					if(dbParser.getFormat().flag == FormatWithRef.VCF_FORMAT) {
						VCFNode dbNode = NodeFactory.createRefAltVCF(line.substring(beg + 1), dbParser.getFormat());
						if(checkQueryAndDBNodeMatch(queryNode, dbNode, dbParser.isRefAndAltExsit())) ((VCFParserDB)dbParser).addNode(dbNode);
					} else {
						BedNode dbNode = NodeFactory.createRefAltBED(line.substring(beg + 1), dbParser.getFormat());
						if(checkQueryAndDBNodeMatch(queryNode, dbNode, dbParser.isRefAndAltExsit())) ((BEDParserDB)dbParser).addNode(dbNode);
					}
				}
			}
			writer.close();
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean checkQueryAndDBNodeMatch(final RefNode query, final RefNode db, final boolean dbHasRefAndAlt) {
		if(annoRunBean.isForceOverlap()) return true;
		if(!annoRunBean.getQueryParser().isRefAndAltExsit() || !dbHasRefAndAlt) {
			return true;
		} else {
			if(!query.ref.equals(db.ref) || !hasIntersection(query.alts, db.alts)) { //alts cannot match
				return false;
			} else {
				return true;
			}
		}
	}
	
	public static boolean hasIntersection(String[] a, String[] b) {
		for (int i = 0; i < b.length; i++) {
			for (int j = 0; j < a.length; j++) {
				if(a[j].equals(a[i])) return true;
			}
		}
		return false;
	}
	
	public void printMetedata(final BufferedWriter writer, final VCFHeader h) throws IOException {
		for (VCFHeaderLine header : h.getMetaDataInSortedOrder()) {
			writer.write(VCFHeader.METADATA_INDICATOR + header.toString() + NEWLINE);
		}
	}
	
	public void printHeader(final BufferedWriter writer) {
		try {
			if(annoRunBean.getQueryFormat().flag == FormatWithRef.VCF_FORMAT) { //query vcf
				VCFParserQuery parser = (VCFParserQuery)annoRunBean.getQueryParser();
				
				if(annoRunBean.getAnnoOutFormat() == FileFormatSupport.VCF) {
					printMetedata(writer, parser.getOutVCFHeader(dbGetVCFInfos()));
					writer.write(parser.toVCFHeader() + NEWLINE);
				} else {
					writer.write(parser.toBEDHeader1() + getDBHeader() + TAB + parser.toBEDHeader2() + NEWLINE);
				}
			} else {
				BEDParserQuery parser = (BEDParserQuery)annoRunBean.getQueryParser();
				
				if(annoRunBean.getAnnoOutFormat() == FileFormatSupport.VCF) {
					VCFHeader header = parser.outVCFHeader();
					if(header != null) printMetedata(writer, parser.addDBInfos(header, dbGetVCFInfos()));
					writer.write(parser.toVCFHeader() + NEWLINE);
				} else {
					writer.write(parser.toBEDHeader() + getDBHeader() + NEWLINE);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public List<VCFInfoHeaderLine> dbGetVCFInfos() {
		AbstractParser dbp;
		List<VCFInfoHeaderLine> dbInfos = new ArrayList<VCFInfoHeaderLine>();
		for (String field : annoRunBean.getDbNames()) {
			dbp = annoRunBean.getDbPareser().get(field);
			if(dbp.getFormat().flag == FormatWithRef.VCF_FORMAT) {
				dbInfos = addList(dbInfos, ((VCFParserDB)dbp).getVCFInfos());
			} else {
				dbInfos = addList(dbInfos, ((BEDParserDB)dbp).getVCFInfos());
			}	
		}
		return dbInfos;
	}
	
	public String getDBHeader() {
		String dbOut = "";
		AbstractParser dbp;
		String header;
		for (String field : annoRunBean.getDbNames()) {
			dbp = annoRunBean.getDbPareser().get(field);
			
			if(dbp.getFormat().flag == FormatWithRef.VCF_FORMAT) {
				header = ((VCFParserDB)dbp).toBEDHeader();
			} else {
				header = ((BEDParserDB)dbp).toBEDHeader();
			}	
			if(header != null) dbOut += TAB + header;
		}
		return dbOut;
	}
	
	public String combineResults(final RefNode queryNode) {
		String out = "";
		if(annoRunBean.getQueryFormat().flag == FormatWithRef.VCF_FORMAT) { //query vcf
			VCFParserQuery parser = (VCFParserQuery)annoRunBean.getQueryParser();
			VCFNode node = (VCFNode)queryNode;
			if(annoRunBean.getAnnoOutFormat() == FileFormatSupport.VCF) {  // query vcf, out vcf
				out = out + parser.convertQueryToVCF1(node) + convertDBToVCF() + TAB + parser.convertQueryPart2(node);
			}  else {                                                       //query vcf, out bed
				out = out + parser.convertQueryToBED1(node) + convertDBToBED() + TAB + parser.convertQueryPart2(node);
			}
			
		} else {
			BEDParserQuery parser = (BEDParserQuery)annoRunBean.getQueryParser();
			BedNode node = (BedNode)queryNode;
			if(annoRunBean.getAnnoOutFormat() == FileFormatSupport.VCF) {  // query bed, out vcf
				out = out + parser.convertQueryToVCF(node) + convertDBToVCF();
			} else {
				out = out + parser.convertQueryToBED(node) + convertDBToBED();
			}
		}
		return out;
	}
	
	public String convertDBToVCF() {
		String dbOut = "";
		AbstractParser dbp;
		String str;
		for (String db : annoRunBean.getDbNames()) { 
			dbp = annoRunBean.getDbPareser().get(db);
			if(dbp.getFormat().flag == FormatWithRef.VCF_FORMAT) {
				str = ((VCFParserDB)dbp).convertDBNodesToVCF();
			} else {
				str = ((BEDParserDB)dbp).convertDBNodesToVCF();
			}
			if(str != null && !str.equals("")) dbOut += INFO_FIELD_SEPARATOR + str;        //从数据库中提取的数据转VCF 以分号分割
		}
		return dbOut;
	}
	
	public String convertDBToBED() {
		String dbOut = "";
		AbstractParser dbp;
		String str;
		for (String db : annoRunBean.getDbNames()) {
			dbp = annoRunBean.getDbPareser().get(db);
			if(dbp.getFormat().flag == FormatWithRef.VCF_FORMAT) {
				str = ((VCFParserDB)dbp).convertDBNodesToBED();
			} else {
				str = ((BEDParserDB)dbp).convertDBNodesToBED();
			}
			if(str != null) dbOut += TAB + str; //从数据库中提取的数据转BED 以TAB分割
		}
		return dbOut;
	}
	
	public static List<VCFInfoHeaderLine> addList(List<VCFInfoHeaderLine> a, List<VCFInfoHeaderLine> b) {
		if(b == null || b.size() == 0) return a;
		if(a == null || a.size() == 0) return b;
		a.addAll(b);
		return a;
	}
}
