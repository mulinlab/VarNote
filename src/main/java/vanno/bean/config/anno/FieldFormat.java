package main.java.vanno.bean.config.anno;

import java.util.ArrayList;
import java.util.List;

import main.java.vanno.constants.InvalidArgumentException;

public final class FieldFormat {
	
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
	
	
	private final String fieldName;
	private List<FormatVariable> seperators;
	private List<FormatVariable> outSeperators;
	
	private boolean formatHasAllele;
	private boolean outFormatHasAllele;
	
//	private final String format;
//	private final String outFormat;
	
	
	public FieldFormat(final String format, final String outFormat, final String fieldName) {
		super();
		this.fieldName = fieldName;
//		this.format = format;
//		this.outFormat = outFormat;
		formatHasAllele = false;
		outFormatHasAllele = false;
		
		parserFormat(format, false);
		parserFormat(outFormat, true);
	}

	public String getFieldName() {
		return fieldName;
	}
	
	public void parserFormat(String format, boolean isOut) {
		int beg = 0, end, vnum, preNum = -4;
		if(isOut) {
			outSeperators = new ArrayList<FormatVariable>();
		} else {
			seperators = new ArrayList<FormatVariable>();
		}

		format = format.trim();
		if(format.indexOf(" ") != -1) throw new InvalidArgumentException("Format has space, please check " + format);
		
		String variable, sep;
		int index = 0;
		while((end = format.indexOf(VARIABLE_BEGIN)) != -1) {
			vnum = getVariableName(format, end + 1);
			if(!isOut && vnum > 0 && preNum > 0 && (preNum + 1) != vnum) throw new InvalidArgumentException("Please set format variable in order like $1,$2,$3...");
			if(vnum == ALLELE_INDENTIFIER) {
				variable = VARIABLE_BEGIN + ALLELE;
				if(isOut) {
					outFormatHasAllele = true;
				} else {
					formatHasAllele = true;
				}
			} else if(vnum == N_INDENTIFIER) {
				variable = VARIABLE_BEGIN + N;
			} else if(vnum == LOOP_INDENTIFIER) {
				if(index < 1)  throw new InvalidArgumentException("Invalid format " + format + ", Please set format like $1|$2..$n"); 
				
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
			
			if(isOut) {
				outSeperators.add(new FormatVariable(variable, vnum, sep));
			} else {
				seperators.add(new FormatVariable(variable, vnum, sep));
			}

			format = format.substring(end + variable.length());
			beg = 0;
		}
	}
	
	public ValWithAllele getFieldValWithFormat(String val) {
		List<String> variableVals = new ArrayList<String>();
		
		int beg, end, size = seperators.size();
		FormatVariable fv, nextFV;
		String vval, allele = null;
		for (int i = 0; i < size; i++) {
			fv = seperators.get(i);
			if(fv.variable.matches("\\$\\d+\\.\\.\\$n")) {
				if(i != (size - 1)) throw new InvalidArgumentException("..$n must be the last variable.");
			
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
					if(val.indexOf(fv.seperator) == -1) throw new InvalidArgumentException(val + " don't match format for field " + fieldName);
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
				throw new InvalidArgumentException(val + " don't match format for field " + fieldName);
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
				sb.append(VARIABLE_BEGIN + ALLELE);
//				sb.append(allele);
			} else if(outFV.variable.equals(VARIABLE_BEGIN + N)) {
				sb.append(variableVals.get(variableVals.size() - 1));
			} else if(outFV.variable.matches("\\$\\d+\\.\\.\\$n")) {
				if(index == -1) {
					throw new InvalidArgumentException("Invalid format for field " + fieldName + ", Please use format like $2..$n or $3..$n and so on.");
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
		return new ValWithAllele(allele, sb.toString());
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
				} else throw new InvalidArgumentException(EXCEPTION + f);
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
//					if(f.substring(index-3, index+1).equals(LOOP)) throw new IllegalArgumentException("Wrong format: " + f);
					return N_INDENTIFIER;
				}
			case '0': 
				if(num.size() == 0) throw new InvalidArgumentException(EXCEPTION + f);
				else num.add(0); break;
			case '.':
				if(index + 4 > f.length()) throw new InvalidArgumentException("wrong format ofr field " + fieldName);
				if(f.substring(index, index+4).equals(LOOP)) {
					if(num.size() == 0) throw new InvalidArgumentException(EXCEPTION + f + ", you should use format like $1|$2..$n");
					
					return LOOP_INDENTIFIER;
				} else {
					int end = f.indexOf(VARIABLE_BEGIN, index);
					if(end != -1 && f.charAt(end + 1) == N_CHAR)  throw new InvalidArgumentException("Wrong format: " + f);
			
					return compute(num);
				}
			default:
				if(num.size() == 0) throw new InvalidArgumentException(EXCEPTION + f);
				else return compute(num);
			}
			index++;
		}
		if(num.size() == 0) throw new InvalidArgumentException(EXCEPTION + f);
		else return compute(num);
	}
	
	
	public boolean isFormatHasAllele() {
		return formatHasAllele;
	}

	public boolean isOutFormatHasAllele() {
		return outFormatHasAllele;
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
	
	public static void main(String[] args) {
		FieldFormat format;
		ValWithAllele out;
		
		format = new FieldFormat("$allele|$1|$2..$n", "$allele|$1|$2|$3", "col5");
		out = format.getFieldValWithFormat("G|0.0696885|0.0635|0.0288|0.0784|0.0746|0.093|0.080666|0.084142|0.077322||||||||||||");
		System.out.println(out.allele + "," + out.str);
		
		format = new FieldFormat("$allele|$1|$2..$n", "$2,$8,$n", "col5");
		out = format.getFieldValWithFormat("G|0.0696885|0.0635|0.0288|0.0784|0.0746|0.093|0.080666|0.084142|0.077322||||||||||||0.3");
		System.out.println(out.allele + "," + out.str);
		
		format = new FieldFormat("$allele|$1|$2..$n", "$allele|$1|$2..$n", "col5");
		out = format.getFieldValWithFormat("G|0.0696885|0.0635|0.0288|0.0784|0.0746|0.093|0.080666|0.084142|0.077322||||||||||||0.3");
		System.out.println(out.allele + "," + out.str);
	}
}


