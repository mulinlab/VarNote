package main.java.vanno.bean.node;

import java.io.ByteArrayOutputStream;
import main.java.vanno.bean.config.anno.VCFParser;
import main.java.vanno.bean.format.Format;
import main.java.vanno.constants.InvalidArgumentException;

public final class NodeFactory {
	private static final String SEPERATOR = "\t";
	private static final char SEPERATOR_CHAR = '\t';
	
	public static void checkTab(final String s) {
		if(s.indexOf(SEPERATOR, 0) == -1) 
			throw new InvalidArgumentException("Doesn't detect the Tab Delimiter in line '" + s + "' , please check the file format. " + s);
	}
	
	public static void ajustEND(final String alt, Node intv) {
		int e_off = -1, i = alt.indexOf("END=");
        if (i == 0) e_off = 4;
        else if (i > 0) {
            i = alt.indexOf(";END=");
            if (i >= 0) e_off = i + 5;
        }
        if (e_off > 0) {
            i = alt.indexOf(';', e_off);
            intv.end = Integer.parseInt(i > e_off ? alt.substring(e_off, i) : alt.substring(e_off));
        }
	}

	public static void ajustSVTYPE(final String info, Node intv) {
		if(info == null) return;
		ajustEND(info, intv);
		if((info.indexOf("SVTYPE") != -1) && (info.indexOf("CIPOS") != -1) && (info.indexOf("CIEND") != -1)) {
			int posbegin = info.indexOf("CIPOS"), posend = info.indexOf(VCFParser.INFO_FIELD_SEPARATOR, info.indexOf("CIPOS")),
					endbegin = info.indexOf("CIEND"), endend = info.indexOf(VCFParser.INFO_FIELD_SEPARATOR, info.indexOf("CIEND"));
			if(posend == -1) posend = info.length();
			if(endend == -1) endend = info.length();
			
			String[] CIPOS = info.substring(posbegin + 6, posend).split(",");
			String[] CIEND = info.substring(endbegin + 6, endend).split(",");
			
			if(CIPOS.length == 2 && CIEND.length == 2) {
				intv.beg = intv.beg + Integer.parseInt(CIPOS[0]);
				intv.end = intv.end + Integer.parseInt(CIEND[1]);
			} 
		} 
	}
	
	public static RefNode createRefAlt(final String s, final Format format) {
		String[] tokens = s.split(SEPERATOR);
		
		RefNode node = new RefNode();
		node.origStr = s;
		node.end = 0;
		node.init();;
	
		for (int i = 0; i < tokens.length; i++) {
			node.putColFiled((i+1), tokens[i]);
			tokens[i] = tokens[i].trim();
			
			if(i == (format.getStartPositionColumn() - 1)) {
				node.orgBeg = node.beg = node.end = Integer.parseInt(tokens[i]);
				if ((format.getFlags() & 0x10000 ) != 0) ++node.end;
				else  --node.beg;

				if (node.beg < 0) node.beg = 0;
				if (node.end < 1) node.end = 1;
				
			} else if(((format.getFlags() & 0xffff) == 0) && (i == (format.getEndPositionColumn() - 1))) {
				node.end = Integer.parseInt(tokens[i]);
			} else if(i == (format.getFieldCol(Format.H_FIELD.REF.toString()) - 1)) {
				node.ref = tokens[i];
				if ((format.getFlags() & 0xffff) == 2 || format.isPos()) {
					node.end = node.beg + node.ref.length();
				}
			} else if(i == (format.getFieldCol(Format.H_FIELD.ALT.toString()) - 1)) {
				node.alts = tokens[i].split(",");
			} else if(i == (format.getSequenceColumn() - 1)) {
				node.chr = tokens[i];
			} 
		}
		
		if((format.getFlags() & 0xffff) == 2)  {
			ajustSVTYPE(node.getField((format.getFieldCol(Format.H_FIELD.INFO.toString()))), node);
		} 
		
		node.putColFiled(format.getStartPositionColumn(), node.beg + "");
		node.putColFiled(format.getEndPositionColumn(), node.end + "");
		return node;
	}

	
	public static int prepare(final Format queryFormat, final int col) {
		if((col + 1) == queryFormat.getStartPositionColumn()) {
			return 0;
		} else if((col + 1) == queryFormat.getEndPositionColumn()) {
			return 1;
		} else if(((col + 1) == queryFormat.getSequenceColumn()) || ((col + 1) == queryFormat.getFieldCol(Format.H_FIELD.REF.toString())) || ((col + 1) == queryFormat.getFieldCol(Format.H_FIELD.ALT.toString())) || ((col + 1) == queryFormat.getFieldCol(Format.H_FIELD.INFO.toString()))) {
			return 2;
		} else {
			return 3;
		}
	}

	public static Node createBasic(final String s, final Format queryFormat, Node intv, ByteArrayOutputStream buf) {
		int col = 0, flag = 0;
		try {
			intv.clear(); 
			char c;

			buf.reset();
			flag = prepare(queryFormat, col);
			
			for (int i = 0; i < s.length(); i++) {
				c = s.charAt(i);
				if(c == SEPERATOR_CHAR) {
					col++;
					if(col == queryFormat.getSequenceColumn()) {
						intv.chr = buf.toString().trim();
					} else if(col == queryFormat.getStartPositionColumn()) {
						intv.orgBeg = intv.beg = intv.end = Integer.parseInt(buf.toString().trim());
						if ((queryFormat.getFlags() & 0x10000 ) != 0) ++intv.end;
						else  --intv.beg;

						if (intv.beg < 0) intv.beg = 0;
						if (intv.end < 1) intv.end = 1;
					} else if(col == queryFormat.getRefColumn()){
						if ((queryFormat.getFlags() & 0xffff) == 2 || queryFormat.isPos()) {
							intv.end = intv.beg + buf.toString().length();
						}
					} else if (col == queryFormat.getEndPositionColumn()) {
						if ((queryFormat.getFlags() & 0xffff) == 0) intv.end = Integer.parseInt(buf.toString().trim());
					} else if (col == queryFormat.getFieldCol(Format.H_FIELD.INFO.toString())) {
						if ((queryFormat.getFlags() & 0xffff) == 2) { //vcf INFO
							ajustSVTYPE(buf.toString(), intv);
						}	
					}
					flag = prepare(queryFormat, col); 
					buf.reset();
				} else {
					if(flag <= 2){
						buf.write(c);
					}
				}
			}
			
			col++;
			if(col == queryFormat.getSequenceColumn()) {
				intv.chr = buf.toString();
			} else if(col == queryFormat.getStartPositionColumn()) {
				intv.orgBeg = intv.beg = intv.end = Integer.parseInt(buf.toString().trim());
				if ((queryFormat.getFlags() & 0x10000 ) != 0) ++intv.end;
				else  --intv.beg;

				if (intv.beg < 0) intv.beg = 0;
				if (intv.end < 1) intv.end = 1;
			} else if(col == queryFormat.getRefColumn()){
				if ((queryFormat.getFlags() & 0xffff) == 2 || queryFormat.isPos()) {
					intv.end = intv.beg + buf.toString().length();
				}
			} else if (col == queryFormat.getEndPositionColumn()) {
				if ((queryFormat.getFlags() & 0xffff) == 0) intv.end = Integer.parseInt(buf.toString().trim());
			} else if (col == queryFormat.getFieldCol(Format.H_FIELD.INFO.toString())) { //vcf INFO
				if ((queryFormat.getFlags() & 0xffff) == 2) {
					ajustSVTYPE(buf.toString(), intv);
				}	
			}
		} catch (NumberFormatException e) {
			System.err.println("NumberFormatException: Convert \"" + buf.toString().trim() + "\" to number with error! Column " + col + " of line \"" + s + "\" should be a number, please check.");
			System.exit(1);
		} catch (Exception e) {
			System.err.println("Exception: Parsing line " + s + " with error! " + e);
			System.exit(1);
		}

		return intv;
	}
	
	public static void main(String[] args) {
		Node node = new Node();
		ByteArrayOutputStream buf =  new ByteArrayOutputStream(8192);
		
		Format vcf = Format.newVCF();
		Format bed = Format.newBED();
		Format tab = Format.newTAB();
		tab.updateCols(1, 2, 2, 4, 5);
		
		//basic vcf & bed & tab
		node = NodeFactory.createBasic("1	10177	.	A	A	22041.2", vcf, node, buf);
		System.out.println("1: " + (node.beg == 10176 & node.end == 10177)); 
		node = NodeFactory.createBasic("1	10177	10178", bed, node, buf);
		System.out.println("2: " + (node.beg == 10177 & node.end == 10178));
		node = NodeFactory.createBasic("1	10177	.	A	A	22041.2", tab, node, buf);
		System.out.println("3: " + (node.beg == 10176 & node.end == 10177)); 
		node = NodeFactory.createBasic("1	10177	.	A	ACC	", tab, node, buf);
		System.out.println("4: " + (node.beg == 10176 & node.end == 10177)); 
		node = NodeFactory.createBasic("1	10177	.	ACC	A", tab, node, buf);
		System.out.println("5: " + (node.beg == 10176 & node.end == 10179)); 
		
		tab = Format.newTAB();
		tab.updateCols(1, 2, 2, 4, 5);
		tab.setZeroBased();
		node = NodeFactory.createBasic("1	10177	.	A	A	22041.2", tab, node, buf);
		System.out.println("31: " + (node.beg == 10177 & node.end == 10178)); 
		node = NodeFactory.createBasic("1	10177	.	A	ACC	", tab, node, buf);
		System.out.println("41: " + (node.beg == 10177 & node.end == 10178)); 
		node = NodeFactory.createBasic("1	10177	.	ACC	A", tab, node, buf);
		System.out.println("51: " + (node.beg == 10177 & node.end == 10180)); 
		
		//tab insertion & deletion with two pos
		tab = Format.newTAB();
		tab.updateCols(1, 2, 3, 4, 5);
		node = NodeFactory.createBasic("1	10177	10178	.	A	ACC	", tab, node, buf);
		System.out.println("4: " + (node.beg == 10176 & node.end == 10178)); 
		node = NodeFactory.createBasic("1	10177	10179	.	ACC	A", tab, node, buf);
		System.out.println("5: " + (node.beg == 10176 & node.end == 10179)); 
		
		
		tab.setZeroBased();
		node = NodeFactory.createBasic("1	10177	10178	.	A	ACC	", tab, node, buf);
		System.out.println("6: " + (node.beg == 10177 & node.end == 10178)); 
		node = NodeFactory.createBasic("1	10177	10179	.	ACC	A", tab, node, buf);
		System.out.println("7: " + (node.beg == 10177 & node.end == 10179)); 
		
		
		//tab insertion & deletion with one pos
		tab = Format.newTAB();
		tab.updateCols(1, 2, 2, 4, 5);
		node = NodeFactory.createBasic("1	10177	.	A	A	22041.2", tab, node, buf);
		System.out.println("8: " + (node.beg == 10176 & node.end == 10177)); 
		node = NodeFactory.createBasic("1	10177	.	A	ACC	22041.2", tab, node, buf);
		System.out.println("9: " + (node.beg == 10176 & node.end == 10177)); 
		node = NodeFactory.createBasic("1	10177	.	ACC	A	22041.2", tab, node, buf);
		System.out.println("10: " + (node.beg == 10176 & node.end == 10179)); 
		
		//VCF insertion & deletion
		node = NodeFactory.createBasic("1	10177	.	A	ACC	22041.2	10X_RESCUED_MOLECULE_HIGH_DIVERSITY	.	GT	0/1", vcf, node, buf);
		System.out.println("11: " + (node.beg == 10176 & node.end == 10177)); 
		node = NodeFactory.createBasic("1	10177	.	ACC	A	22041.2	10X_RESCUED_MOLECULE_HIGH_DIVERSITY	.	GT	0/1", vcf, node, buf);
		System.out.println("12: " + (node.beg == 10176 & node.end == 10179)); 
		
		//BED insertion & deletion
		node = NodeFactory.createBasic("1	10177	10178	.	A	ACC	", bed, node, buf);
		System.out.println("13: " + (node.beg == 10177 & node.end == 10178)); 
		node = NodeFactory.createBasic("1	10177	10179	.	ACC	A", bed, node, buf);
		System.out.println("14: " + (node.beg == 10177 & node.end == 10179)); 
		
		//VCF insertion & deletion
		node = NodeFactory.createBasic("1	10177	.	GGCGCG	TCCGCA	701.53	.	AB=0;ABP=0;AC=6;", vcf, node, buf);
		System.out.println("15: " + (node.beg == 10176 & node.end == 10182));
		node = NodeFactory.createBasic("1	10177	rs771917038	A	ACCGTCAGCT	701.53	.	AB=0;ABP=0", vcf, node, buf);
		System.out.println("16: " + (node.beg == 10176 & node.end == 10177));
		node = NodeFactory.createBasic("1	10177	rs781085493	GGGGGGCGC	G	701.53	.	AB=0;ABP=0", vcf, node, buf);
		System.out.println("17: " + (node.beg == 10176 & node.end == 10185));
		node = NodeFactory.createBasic("1	10177	rs534090028	CGCA	TGCA,C	701.53	.	AB=1;ABP=1", vcf, node, buf);
		System.out.println("18: " + (node.beg == 10176 & node.end == 10180));
		
		//VCF has end
		node = NodeFactory.createBasic("1	10177	rs781085493	GGGG	G	701.53	.	END=10180", vcf, node, buf);
		System.out.println("19: " + (node.beg == 10176 & node.end == 10180));
		node = NodeFactory.createBasic("1	10177	rs781085493	G	G	701.53	.	AB=1;END=899941", vcf, node, buf);
		System.out.println("20: " + (node.beg == 10176 & node.end == 899941));
		
		//VCF has SVTYPE
		node = NodeFactory.createBasic("1	869465	1	N	<DEL>	1293.8	.	SVTYPE=DEL;POS=869465;SVLEN=-752;END=870217;STRANDS=+-:31;IMPRECISE;CIPOS=-10,157;CIEND=-84,10;CIPOS95=-3,31;CIEND95=-36,3;", vcf, node, buf);
		System.out.println(node.beg == 869454 & node.end == 870227);
		node = NodeFactory.createBasic("1	1157791	4345_1	N	N[4:76212291[	0.0	.	SVTYPE=BND;POS=1157791;STRANDS=+-:5;IMPRECISE;CIPOS=-8,8;CIEND=-9,6", vcf, node, buf);
		System.out.println(node.beg == 1157782 & node.end == 1157797);
		
		tab = Format.newTAB();
		tab.updateCols(1, 2, 2, 4, 5);
		
		//basic vcf & bed & tab
		node = NodeFactory.createRefAlt("1	10177	.	A	A	22041.2", vcf);
		System.out.println("1: " + (node.beg == 10176 & node.end == 10177)); 
		node = NodeFactory.createRefAlt("1	894441	894448", bed);
		System.out.println("2: " + (node.beg == 894441 & node.end == 894448));
		node = NodeFactory.createRefAlt("1	10177	.	A	A	22041.2", tab);
		System.out.println("3: " + (node.beg == 10176 & node.end == 10177)); 
		
		//tab insertion & deletion with two pos
		tab.updateCols(1, 2, 3, 4, 5);
		
		node = NodeFactory.createRefAlt("1	10177	10180	.	A	ACC	", tab);
		System.out.println("4: " + (node.beg == 10176 & node.end == 10180)); 
		node = NodeFactory.createRefAlt("1	10177	10179	.	ACC	A", tab);
		System.out.println("5: " + (node.beg == 10176 & node.end == 10179)); 
		
		tab.setZeroBased();
		node = NodeFactory.createRefAlt("1	10177	10178	.	A	ACC	", tab);
		System.out.println("6: " + (node.beg == 10177 & node.end == 10178)); 
		node = NodeFactory.createRefAlt("1	10177	10179	.	ACC	A", tab);
		System.out.println("7: " + (node.beg == 10177 & node.end == 10179)); 
		
		
		//tab insertion & deletion with one pos
		tab = Format.newTAB();
		tab.updateCols(1, 2, 2, 4, 5);
		node = NodeFactory.createRefAlt("1	10177	.	A	A	22041.2", tab);
		System.out.println("8: " + (node.beg == 10176 & node.end == 10177)); 
		node = NodeFactory.createRefAlt("1	10177	.	A	ACC	22041.2", tab);
		System.out.println("9: " + (node.beg == 10176 & node.end == 10177)); 
		node = NodeFactory.createRefAlt("1	10177	.	ACC	A	22041.2", tab);
		System.out.println("10: " + (node.beg == 10176 & node.end == 10179)); 
		
		//VCF insertion & deletion
		node = NodeFactory.createRefAlt("1	10177	.	A	ACC	22041.2	10X_RESCUED_MOLECULE_HIGH_DIVERSITY	.	GT	0/1", vcf);
		System.out.println("11: " + (node.beg == 10176 & node.end == 10177)); 
		node = NodeFactory.createRefAlt("1	10177	.	ACC	A	22041.2	10X_RESCUED_MOLECULE_HIGH_DIVERSITY	.	GT	0/1", vcf);
		System.out.println("12: " + (node.beg == 10176 & node.end == 10179)); 
		
		//BED insertion & deletion
		node = NodeFactory.createRefAlt("1	10177	10180	.	A	ACC	", bed);
		System.out.println("13: " + (node.beg == 10177 & node.end == 10180)); 
		node = NodeFactory.createRefAlt("1	10177	10179	.	ACC	A",bed);
		System.out.println("14: " + (node.beg == 10177 & node.end == 10179)); 
		
		//VCF insertion & deletion
		node = NodeFactory.createRefAlt("1	899937	.	GGCGCG	TCCGCA	701.53	.	AB=0;ABP=0;AC=6;", vcf);
		System.out.println("15: " + (node.beg == 899936 & node.end == 899942));
		node = NodeFactory.createRefAlt("1	899937	rs771917038	A	ACCGTCAGCT	701.53	.	AB=0;ABP=0", vcf);
		System.out.println("16: " + (node.beg == 899936 & node.end == 899937));
		node = NodeFactory.createRefAlt("1	899937	rs781085493	GGGGGGCGC	G	701.53	.	AB=0;ABP=0", vcf);
		System.out.println("17: " + (node.beg == 899936 & node.end == 899945));
		node = NodeFactory.createRefAlt("1	899937	rs534090028	CGCA	TGCA,C	701.53	.	AB=1;ABP=1", vcf);
		System.out.println("18: " + (node.beg == 899936 & node.end == 899940));
		
		//VCF has end
		node = NodeFactory.createRefAlt("1	899937	rs781085493	GGGG	G	701.53	.	END=899941", vcf);
		System.out.println("19: " + (node.beg == 899936 & node.end == 899941));
		node = NodeFactory.createRefAlt("1	899937	rs781085493	G	G	701.53	.	AB=1;END=899941", vcf);
		System.out.println("20: " + (node.beg == 899936 & node.end == 899941));
		
		//VCF has SVTYPE
		node = NodeFactory.createRefAlt("1	869465	1	N	<DEL>	1293.8	.	SVTYPE=DEL;POS=869465;SVLEN=-752;END=870217;STRANDS=+-:31;IMPRECISE;CIPOS=-10,157;CIEND=-84,10;CIPOS95=-3,31;CIEND95=-36,3;", vcf);
		System.out.println(node.beg == 869454 & node.end == 870227);
		node = NodeFactory.createRefAlt("1	1157791	4345_1	N	N[4:76212291[	0.0	.	SVTYPE=BND;POS=1157791;STRANDS=+-:5;IMPRECISE;CIPOS=-8,8;CIEND=-9,6", vcf);
		System.out.println(node.beg == 1157782 & node.end == 1157797);
		
		
//		1	10177	.	A	A	22041.2	...      []
//		1	10177	.	A	ACC	22041.2	10X_RESCUED_MOLECULE_HIGH_DIVERSITY	.	GT	0/1
//		1	10177	.	ACC	A	22041.2	10X_RESCUED_MOLECULE_HIGH_DIVERSITY	.	GT	0/1
//		1	899937	.	GGCGCG	TCCGCA	701.53	.	AB=0;ABP=0;AC=6;
//		1	894441	rs771917038	A	ACCGTCAGCT	701.53	.	AB=0;ABP=0
//		1	899933	rs781085493	GGGGGGCGC	G	701.53	.	AB=0;ABP=0
//		1	899947	rs534090028	CGCA	TGCA,C	701.53	.	AB=1;ABP=1
//		1	899933	rs781085493	GG	G	701.53	.	END=899941
//		1	899933	rs781085493	GG	G	701.53	.	AB=1;END=899941
//		1	869465	1	N	<DEL>	1293.8	.	SVTYPE=DEL;POS=869465;END=870217;CIPOS=-10,157;CIEND=-84,10;
//		1	1157791	4345_1	N	N[4:76212291[	0.0	.	SVTYPE=BND;POS=1157791;CIPOS=-8,8;CIEND=-9,6
		
		
//		1	10177	10178
//		1	10177	10178	.	A	ACC
//		1	10177	10179	.	ACC	A
	}
}
