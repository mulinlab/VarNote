package bean.node;

import java.util.ArrayList;
import java.util.HashMap;
import postanno.FormatWithRef;

public class NodeFactory {
	private static final String SEPERATOR = "\t";
	
	public static void checkTab(String s) {
		if(s.indexOf(SEPERATOR, 0) == -1) 
			throw new IllegalArgumentException("Doesn't detect the Tab Delimiter in line '" + s + "' , please check the file format. " + s);
	}
	
//	public static void checkDBInput(String s, AnnoConfigBean db) {
//		checkTab(s);
//		
//		String[] tokens = s.split(SEPERATOR);
//		int minSize;
//		
//		ResultFileFormat format = db.getDbFormat();
//		if(format == ResultFileFormat.VCF) {
//			minSize = VCF_INFO;
//		} else if(format == ResultFileFormat.BED) {
//			minSize = BED_INFO;
//		} else {
//			throw new IllegalArgumentException("We currently support VCF and BED");
//		}
//		
//		if(tokens.length < minSize) {
//			throw new IllegalArgumentException(db.getDbFormat() + " file have at least " + minSize + " columns.");
//		} 
//			
//		try {
//			Integer.parseInt(tokens[1]); 
//			
//			if(format == ResultFileFormat.BED) {
//				Integer.parseInt(tokens[2]); 
//			}
//		} catch (NumberFormatException e){
//			throw new IllegalArgumentException("NumberFormatException: column should be integer.");
//		}
//	}
	
	public static VCFNode computeEndForVCF(VCFNode node, String end) {
		int maxL = 1;
		if(node.ref.length() > maxL) {
			maxL = node.ref.length();
		}
		for (String string : node.alts) 
			if(string.length() > maxL) maxL = string.length();
		node.end = node.beg + maxL;
		int e = getEND(end);
		if(e != -1) node.end = e;
		return node;
	}
	
	public static Node createBasicWithOri(String s, FormatWithRef queryFormat) {
		Node intv = createBasic(s, queryFormat);
		intv.origStr = s;
		return intv;
	}
	
	public static int getEND(String alt) {
		int e_off = -1, i = alt.indexOf("END=");
		if (i == 0) e_off = 4;
		else if (i > 0) {
			i = alt.indexOf(";END=");
			if (i >= 0) e_off = i + 5;
		}
		if (e_off > 0) {
			i = alt.indexOf(';', e_off);
			return Integer.parseInt(i > e_off ? alt.substring(e_off,i) : alt.substring(e_off));
		}
		return -1;
	}
	
	public static RefNode createRefAlt(String s, FormatWithRef format) {
		if(format.flag == FormatWithRef.VCF_FORMAT) {
			return NodeFactory.createRefAltVCF(s, format);
		} else {
			return NodeFactory.createRefAltBED(s, format);
		}
	}
	
	public static VCFNode createRefAltVCF(String s, FormatWithRef format) {
		
		String[] tokens = s.split(SEPERATOR);
		VCFNode node = new VCFNode();
	
		node.otherFields = new ArrayList<String>();
		node.otherFieldsMap = new HashMap<Integer, String>();
		for (int i = 0; i < tokens.length; i++) {
			if(i == (format.tbiFormat.startPositionColumn - 1)) {
				node.beg = Integer.parseInt(tokens[i]) - 1;
			} else if(i == (format.refCol - 1)) {
				node.ref = tokens[i];
			} else if(i == (format.altCol - 1)) {
				node.alts = tokens[i].split(",");
			} else if(i == (format.tbiFormat.sequenceColumn - 1)) {
				node.chr =  tokens[i];
			} else if(i == 5) {
				node.qulity =  tokens[i];
			} else if(i == 6) {
				node.filter =  tokens[i];
			} else if(i == (format.infoCol - 1)) {
				node.info = tokens[i];
			} else {
				node.otherFields.add(tokens[i]);
				node.otherFieldsMap.put(i, tokens[i]);
			}
		}
		node = computeEndForVCF(node, tokens[format.infoCol - 1]);
		return node;
	}
			
	//create node with ref and val
	public static BedNode createRefAltBED(String s, FormatWithRef format) {
		String[] tokens = s.split(SEPERATOR);
		
		BedNode node = new BedNode();
//		node.otherFields = new ArrayList<String>();
//		node.otherFieldsMap = new HashMap<Integer, String>();
		
		
		
		node.allFieldsMap = new HashMap<Integer, String>();
		
		for (int i = 0; i < tokens.length; i++) {
			node.allFieldsMap.put(i, tokens[i]);
			if(i == (format.tbiFormat.startPositionColumn - 1)) {
				node.beg = Integer.parseInt(tokens[i]);
			} else if(i == (format.tbiFormat.endPositionColumn - 1)) {
				node.end = Integer.parseInt(tokens[i]);
			} else if(i == (format.refCol - 1)) {
				node.ref = tokens[i];
			} else if(i == (format.altCol - 1)) {
				node.alts = tokens[i].split(",");
			} else if(i == (format.tbiFormat.sequenceColumn - 1)) {
				node.chr =  tokens[i];
			} 
//			else {
//				node.otherFieldsMap.put(i, tokens[i]);
//			}
		}
		return node;
	}
	
//	public static NodeWithRefAlt createQueryNodeWithRV(String s, FormatWithRef format) {
//		String[] tokens = s.split(SEPERATOR);
//		
//		NodeWithRefAlt node = new NodeWithRefAlt(0, 0 , tokens[format.tbiFormat.sequenceColumn - 1]);
//		
//		node.ref = tokens[format.refCol - 1];
//		node.alts = tokens[format.altCol - 1].split(",");
//		
//		if(format.tbiFormat.flags == TabixFormat.VCF_FLAGS) {
//			node.beg = Integer.parseInt(tokens[format.tbiFormat.startPositionColumn - 1]) - 1;
//			node.info = tokens[format.infoCol - 1];
//		} else {
//			node.beg = Integer.parseInt(tokens[format.tbiFormat.startPositionColumn - 1]);
//			node.end = Integer.parseInt(tokens[format.tbiFormat.endPositionColumn - 1]);
//			node.colToValMap = new HashMap<Integer, String>();
//			for (int i = 0; i < tokens.length; i++) {
//				node.colToValMap.put(i, tokens[i]);
//			}
//		}
//		return node;
//	}
	
	public static Node createNodeIndex(String s, String id) {
		int beg = s.indexOf(SEPERATOR);
		if(beg==-1) {
			System.out.println("s=" + s);
		}
		Node node = new Node();
		node.index = Integer.parseInt(s.substring(0, beg));
		node.origStr = s.substring(beg + 1); 
		node.dbID = id;
		return node;
	}
	
	
	public static void main(String[] args) {
//		Node node = NodeFactory.createQueryNodeForResult("1	19390	19391	1	19390	19391	G	A	A|0.00199681|0.0068|0.0014|0|0|0|||||||||||||||");
//		System.out.println(node);
	}
	

	public static Node createBasic(String s, FormatWithRef queryFormat) {
		Node intv = new Node();
		
		int maxL = 1;
		int col = 0, end = 0, beg = 0;
	
		while ((end = s.indexOf('\t', beg)) >= 0 || end == -1) {
			++col;
			if (col == queryFormat.tbiFormat.sequenceColumn) {
				intv.chr = end != -1 ? s.substring(beg, end) : s.substring(beg);
			} else if (col == queryFormat.tbiFormat.startPositionColumn) {
			
				intv.beg = Integer.parseInt(end != -1 ? s.substring(beg, end): s.substring(beg));
				intv.end = intv.beg;

				if ((queryFormat.tbiFormat.flags & 0x10000) != 0) ++intv.end;
				else --intv.beg;

				if (intv.beg < 0) intv.beg = 0;
				if (intv.end < 1) intv.end = 1;
			} else { 
				if (queryFormat.flag == 2) { // generic
					if (col == queryFormat.tbiFormat.endPositionColumn)
						intv.end = Integer.parseInt(end != -1 ? s.substring(beg, end) : s.substring(beg));
				} else {
					if((queryFormat.refCol > 0) || (queryFormat.altCol > 0)) {
						String alt = end >= 0 ? s.substring(beg, end) : s.substring(beg);
						
						if (col == queryFormat.refCol) { // REF
							if (!alt.isEmpty()) maxL = alt.length();
						} else if (col == queryFormat.altCol) { // VAL
							if (!alt.isEmpty()){
								for (String string : alt.split(",")) 
									if(string.length() > maxL) maxL = string.length();
							}
							if(maxL > 1) intv.end = intv.beg + maxL;
						} else if (col == queryFormat.infoCol) { // INFO
							int e = getEND(alt);
							if(e != -1) intv.end = e;
						}
					}
				}
			}
			if (end == -1)  break;
			beg = end + 1;
		}
		return intv;
	}
}
