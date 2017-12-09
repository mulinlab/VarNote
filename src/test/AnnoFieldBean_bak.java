package test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import test.NodeWithRefAlt;
import test.ParseConfigFile;
import htsjdk.samtools.util.StringUtil;
import htsjdk.variant.vcf.VCFConstants;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;

public class AnnoFieldBean_bak {
	public final static int ALLELE_LENGTH = 6;
	public final static String EXCEPTION = "Can not recognize variable in ";
	public final static String ALLELE = "allele";
	public final static String N = "n";
	public final static char N_CHAR = 'n';
	public final static String VARIABLE_BEGIN = "$";
	public final static String LOOP = ".."+VARIABLE_BEGIN+"n";
	public final static int ALLELE_INDENTIFIER = -1;
	public final static int LOOP_INDENTIFIER = -2;
	public final static int N_INDENTIFIER = -3;
//	public enum ExtractType {
//		ALL,
//		SUB
//	}
	
	private String name;
	private String outName;
	
	private String dbFormat;
	private String outFormat;
	
//	private ExtractType etype;
	private VCFInfoHeaderLine vcfInfo;
	
	private List<FormatVariable> seperators;
	private List<FormatVariable> outSeperators;

	private boolean dbAlleleSpec;
	private boolean outAlleleSpec;
	private VCFHeaderLineCount count;
	private VCFHeaderLineType type;
	private String description;
	public AnnoFieldBean_bak() {
		super();
		dbAlleleSpec = false;
		outAlleleSpec = false;
		description = "";
	}
	
	
	public AnnoFieldBean_bak(String name, VCFInfoHeaderLine vcfInfo) {
		this();
		this.name = name;
		this.vcfInfo = vcfInfo;
	}


	public void checkName() {
		if(name == null) throw new IllegalArgumentException("DB format is vcf, please use name=XXX to define your field to extract.");
	}
	
	public void setBedHeaderInfo(String dbLabel) {
		if(count == null) {
			System.err.println("You should set number for field " + name + " of bed file " + dbLabel + ", or we will use 'number=.' instead. ");
			count = VCFHeaderLineCount.UNBOUNDED;
		}
		if(type == null) {
			System.err.println("You should set type for field " + name + " of bed file " + dbLabel + ", or we will use 'type=string' instead. ");
			type = VCFHeaderLineType.String;
		}
//		if(count == null || type == null) throw new IllegalArgumentException("DB format is bed, please set number and type for field name " + name);
		this.vcfInfo = new VCFInfoHeaderLine((outName != null) ? outName:getName() , count, type, description);
	}
	
	public void checkConfig(String db) {
		if((name == null)) {
			throw new IllegalArgumentException("Field name should be defined for db " + db);
		}
//		if(outFormat == null) {
//			etype = ExtractType.ALL;
//		} else {
//			etype = ExtractType.SUB;
//		}
		if(((outFormat != null) && (dbFormat == null)) || ((outFormat == null) && (dbFormat != null))) {
			throw new IllegalArgumentException("format should be used with out_format.");
		}
		if(dbFormat != null) {
			parserFormat(dbFormat, true);
			parserFormat(outFormat, false);
		}
	}
	
	public int compute(List<Integer> num) {
		int total = 0; 
		for (int i = 0; i < num.size(); i++) {
			total = total*(i+1) + num.get(i);
		}
		return total;
	}
	
	public int getVariableName(String f, int index) {
		char c;
//		System.out.println("f=" + f);
		List<Integer> num = new ArrayList<Integer>(3);
		while(index < f.length()) {
			c = f.charAt(index);
			switch (c) {
			case 'a':
				if(num.size() > 0) return compute(num);
				else if(f.substring(index, index + 6).equals(ALLELE)) {
					return ALLELE_INDENTIFIER;
				} else throw new IllegalArgumentException(EXCEPTION + f);
			case '1':  	num.add(1); break;
			case '2':   num.add(2); break;
			case '3':	num.add(3); break;
			case '4':	num.add(4); break;
			case '5':	num.add(5); break;
			case '6':	num.add(6); break;
			case '7':	num.add(7); break;
			case '8':	num.add(8); break;
			case '9': 	num.add(9); break;
			case 'n': 
				if(num.size() > 0) return compute(num);
				else {
					if(f.substring(index-3, index+1).equals(LOOP)) throw new IllegalArgumentException("Wrong format: " + f);
					return N_INDENTIFIER;
				}
			case '0': 
				if(num.size() == 0) throw new IllegalArgumentException(EXCEPTION + f);
				else num.add(0); break;
			case '.':
				if(f.substring(index, index+4).equals(LOOP)) {
					if(num.size() == 0) throw new IllegalArgumentException(EXCEPTION + f + ", you should use format like $1|$2..$n");
					
					return LOOP_INDENTIFIER;
				} else {
					int end = f.indexOf(VARIABLE_BEGIN, index);
					if(end != -1 && f.charAt(end + 1) == N_CHAR)  throw new IllegalArgumentException("Wrong format: " + f);
			
					return compute(num);
				}
			default:
				if(num.size() == 0) throw new IllegalArgumentException(EXCEPTION + f);
				else return compute(num);
			}
			index++;
		}
		if(num.size() == 0) throw new IllegalArgumentException(EXCEPTION + f);
		else return compute(num);
	}
	
	public void parserFormat(String format, boolean db) {
		int beg = 0, end, vnum, preNum = -4;
		if(db) {
			seperators = new ArrayList<FormatVariable>();
		} else {
			outSeperators = new ArrayList<FormatVariable>();
		}

		format = format.trim();
		if(format.indexOf(" ") != -1) throw new IllegalArgumentException("Format has space, please check " + this.dbFormat);
		
		String variable, sep;
		int index = 0;
		while((end = format.indexOf(VARIABLE_BEGIN)) != -1) {
			vnum = getVariableName(format, end + 1);
			if(db && vnum > 0 && preNum > 0 && (preNum + 1) != vnum) throw new IllegalArgumentException("Please set format variable in order like $1,$2,$3...");
			if(vnum == ALLELE_INDENTIFIER) {
				variable = VARIABLE_BEGIN + ALLELE;
				if(!db) {
					outAlleleSpec = true;
				} else {
					dbAlleleSpec = true;
				}
			} else if(vnum == N_INDENTIFIER) {
				variable = VARIABLE_BEGIN + N;
			} else if(vnum == LOOP_INDENTIFIER) {
				if(index < 1) {
					if(db) throw new IllegalArgumentException("Invalid format " + this.dbFormat); 
					else throw new IllegalArgumentException("Invalid format " + this.outFormat); 
				}
				variable =  VARIABLE_BEGIN + (index + 1) + LOOP;
			} else {
				preNum = vnum;
				variable = VARIABLE_BEGIN + vnum;
			}
			index = vnum;
			sep = format.substring(beg, end);
			if(sep.equals("") && seperators.size() == 0) {
				sep = "$begin";
			}
			
			if(db) {
				seperators.add(new FormatVariable(variable, vnum, sep));
			} else {
				outSeperators.add(new FormatVariable(variable, vnum, sep));
			}

			format = format.substring(end + variable.length());
			beg = 0;
		}
	}
	
	public AlleleStr outWithFormat(String val) {
		List<String> variableVals = new ArrayList<String>();
		
		int beg, end, size = seperators.size();
		FormatVariable fv, nextFV;
		String vval, allele = null;
		for (int i = 0; i < size; i++) {
			fv = seperators.get(i);
			if(fv.variable.matches("\\$\\d+\\.\\.\\$n")) {
				if(i != (size - 1)) throw new IllegalArgumentException("..$n must be the last variable.");
			
				beg = 0;
				while((end = val.indexOf(fv.seperator)) != -1) {
					variableVals.add(val.substring(beg, end));
					
					val = val.substring(end + fv.seperator.length());
					beg = 0;
				}
				variableVals.add(val.substring(beg));
				break;
			}
			
			if((i+1) < size) nextFV = seperators.get(i+1); else nextFV = null;
			if(fv.seperator.equals("$begin")) {
				beg = 0;
			} else {
				if(i == 0) {
					if(val.indexOf(fv.seperator) == -1) throw new IllegalArgumentException(val + " don't match format " + this.dbFormat);
					beg = fv.seperator.length() - 1;
				} else {
					beg = 0;
				}
			}
			if(nextFV != null) {
				end = val.indexOf(nextFV.seperator, beg);
				vval = val.substring(beg, end);
				val = val.substring(end + nextFV.seperator.length());
			} else {
				vval = val.substring(beg);
			}
			
			if(fv.variable.equals(VARIABLE_BEGIN + ALLELE)) {
				allele = vval;
			} else if(fv.variable.equals(VARIABLE_BEGIN + N)) {
				throw new IllegalArgumentException("Invalid format " + this.dbFormat);
			} else {
				variableVals.add(vval);
			}
		}
		
		StringBuffer sb = new StringBuffer();
		int index = -1;

		for (FormatVariable outFV : outSeperators) {
			if(!outFV.seperator.equals("$begin")) {
				sb.append(outFV.seperator);
			}
			if(outFV.variable.equals(VARIABLE_BEGIN + ALLELE)) {
				outAlleleSpec = true;
				sb.append(VARIABLE_BEGIN + ALLELE);
				
			} else if(outFV.variable.equals(VARIABLE_BEGIN + N)) {
				sb.append(variableVals.get(variableVals.size() - 1));
			} else if(outFV.variable.matches("\\$\\d+\\.\\.\\$n")) {
				if(index == -1) {
					throw new IllegalArgumentException("Invalid format " + this.dbFormat + ", Please use format like $2..$n or $3..$n and so on.");
				}
				
				for (int j = index; j < variableVals.size(); j++) {
					
					if(j == (variableVals.size() - 1)) {
						sb.append(variableVals.get(j));
					} else {
						sb.append(variableVals.get(j));
						sb.append(outFV.seperator);
					}
					
				}
			} else {
				index = Integer.parseInt(outFV.variable.replace(VARIABLE_BEGIN, ""));
				sb.append(variableVals.get(index-1));
			}
		}
		return new AlleleStr(allele, sb.toString());
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setOutName(String outName) {
		this.outName = outName;
	}

	public void setFormat(String format) {
		this.dbFormat = format;
	}

	public void setOutFormat(String outFormat) {
		this.outFormat = outFormat;
	}

	public String getName() {
		return name;
	}

	public String getOutName() {
		return outName;
	}
	
	public void setVcfInfo(VCFInfoHeaderLine vcfInfo) {
		this.vcfInfo = new VCFInfoHeaderLine(outName != null ? outName : name, vcfInfo.getCountType(), vcfInfo.getType(), vcfInfo.getDescription());
	}
	
	public String listToStr(List<String> result) {
		if(result.size() > 0) {
			return StringUtil.join(VCFConstants.INFO_FIELD_ARRAY_SEPARATOR, result);
		} else {
			return VCFConstants.EMPTY_ALTERNATE_ALLELE_FIELD;
		}
	}
	
	public String getValForForceOverlap(String val, NodeWithRefAlt query, NodeWithRefAlt db) {
		if(outFormat == null) {
			return val;
		} else {
			List<AlleleStr> partsWithAllele = new ArrayList<AnnoFieldBean_bak.AlleleStr>();
			String[] parts = val.split(VCFConstants.INFO_FIELD_ARRAY_SEPARATOR);
			if((vcfInfo.getCountType() == VCFHeaderLineCount.A) || (vcfInfo.getCountType() == VCFHeaderLineCount.R)) {
				int i = 0;
				if(vcfInfo.getCountType() == VCFHeaderLineCount.R) {
					partsWithAllele.add(new AlleleStr(query.ref, outWithFormat(parts[0]).str));
					i = 1;
				}
				
				for (String alt : db.alts) {
					partsWithAllele.add(new AlleleStr(alt, outWithFormat(parts[i++]).str));
				}
			} else if((vcfInfo.getCountType() == VCFHeaderLineCount.INTEGER) && vcfInfo.getCount() < 2) {
				return val;
			} else {
				if(dbAlleleSpec) {
					AlleleStr a = null;
					for (String string : parts) {
						a = outWithFormat(string);
						partsWithAllele.add(new AlleleStr(a.allele, a.str));
					}
				} else {
					for (String alt : db.alts) {
						for (String part : parts) {
							partsWithAllele.add(new AlleleStr(alt, outWithFormat(part).str));
						}
					}
				}
			}
			
			List<String> result = new ArrayList<String>();
			for (AlleleStr as : partsWithAllele) {
				if(outAlleleSpec) {
					if(as.allele == null) throw new IllegalArgumentException("Sorry, we can't recognize allele for field: " + this.getName() + ", maybe you can use format to define it.");
					else result.add(as.str.replace(VARIABLE_BEGIN + ALLELE, as.allele));
				} else {
					result.add(as.str);
				}
			}
			return listToStr(result);
		}
	}
	
	public String getVal(String val, NodeWithRefAlt query, NodeWithRefAlt db) {
		List<String> queryAlts = Arrays.asList(query.alts);
		List<AlleleStr> partsWithAllele = new ArrayList<AnnoFieldBean_bak.AlleleStr>();

		String[] parts = val.split(VCFConstants.INFO_FIELD_ARRAY_SEPARATOR);
		if((vcfInfo.getCountType() == VCFHeaderLineCount.A) || (vcfInfo.getCountType() == VCFHeaderLineCount.R)) {
			List<String> altsToMatch = new ArrayList<String>();
			if(vcfInfo.getCountType() == VCFHeaderLineCount.R) {
				altsToMatch.add(query.ref);
			}
			
			for (int i = 0; i < db.alts.length; i++) {
				if(!altsToMatch.contains(db.alts[i])) {
					altsToMatch.add(db.alts[i]);
				}
			}
			
			if(parts.length != altsToMatch.size()) throw new IllegalArgumentException("field " + val + " has " + parts.length + " val, but allele size is " + db.alts.length);

			for (int i = 0; i < parts.length; i++) {
				if(queryAlts.contains(altsToMatch.get(i))) {
					if(outFormat == null) {
						partsWithAllele.add(new AlleleStr(altsToMatch.get(i), parts[i]));
					} else{
						partsWithAllele.add(new AlleleStr(altsToMatch.get(i), outWithFormat(parts[i]).str));
					}
				}
					
			}
		} else if(vcfInfo.getCountType() == VCFHeaderLineCount.INTEGER) {
			for (int i = 0; i < parts.length; i++) {
				for (int j = 0; j < db.alts.length; j++) {
					if(queryAlts.contains(db.alts[j])) 
						if(outFormat == null) {
							partsWithAllele.add(new AlleleStr(db.alts[j], parts[i]));
						} else {
							partsWithAllele.add(new AlleleStr(db.alts[j], outWithFormat(parts[i]).str));
						}
				}
			}
		} else if(vcfInfo.getCountType() == VCFHeaderLineCount.G) {
			throw new IllegalArgumentException("todo number=G=========" + val);
		} else {
			if(dbAlleleSpec) {
				AlleleStr a = null;
				for (String string : parts) {
					a = outWithFormat(string);
					if((a != null) && queryAlts.contains(a.allele)) {
						partsWithAllele.add(new AlleleStr(a.allele, a.str));
					}
				}
			} else {
				for (int i = 0; i < parts.length; i++) {
					for (int j = 0; j < db.alts.length; j++) {
						if(queryAlts.contains(db.alts[j]))
							if(outFormat == null) {
								partsWithAllele.add(new AlleleStr(db.alts[j], parts[i]));
							} else {
								partsWithAllele.add(new AlleleStr(db.alts[j], outWithFormat(parts[i]).str));
							}
					}
				}
			}
		}
		
		List<String> result = new ArrayList<String>();
		for (AlleleStr as : partsWithAllele) {
			if(outAlleleSpec) {
				if(as.allele == null) throw new IllegalArgumentException("Sorry, we can't recognize allele for field: " + this.getName() + ", maybe you can use format to define it.");
				else result.add(as.str.replace(VARIABLE_BEGIN + ALLELE, as.allele));
			} else {
				result.add(as.str);
			}
		}
		return listToStr(result);
	}

	public void setCount(String count) {
		this.count = ParseConfigFile.checkCount(count);
	}

	public void setType(String type) {
		this.type = ParseConfigFile.checkType(type);
	}

	public VCFInfoHeaderLine getVcfInfo() {
		return vcfInfo;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public class AlleleStr {
		String allele;
	    String str;
		public AlleleStr(String allele, String str) {
			super();
			this.allele = allele;
			this.str = str;
		}
	}
	
	public class FormatVariable {
		final String variable;
		final int index;
		final String seperator;
		
		public FormatVariable(final String variable, final int index, final String seperator) {
			super();
			this.variable = variable;
			this.index = index;
			this.seperator = seperator;
		}
	}
}
