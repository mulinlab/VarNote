package test;

import htsjdk.samtools.util.IOUtil;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import bean.config.run.DBRunConfigBean;
import postanno.FormatWithRef;
import constants.VannoUtils;
import constants.VannoUtils.PROGRAM;


public class ParseConfigFile {

	
	public static final String ANNO = "[anno]";
	public static final String FILED_BEGIN = "[field]";
	public static final String NAME = "name";
	public static final String OUT_FORMAT = "out_format";
	public static final String OUT_NAME = "out_name";
	public static final String FORMAT = "format";

	public static final String FORCE_OVERLAP = "force_overlap";
	public static final String EXTRACT = "extract_all";
	public static final String BED_NUMBER = "number";
	public static final String BED_TYPE = "type";
	public static final String BED_HEADER = "header_fields";
	public static final String BED_DESCRIPTION = "description";
	public static final String QUERY_HEADER_LINES = "query_header_lines";
	
	
	public static RunConfigBean parseRunConfigForAnno(String filePath) {
		IOUtil.assertInputIsValid(filePath);
		RunConfigBean config = new RunConfigBean();
		DBRunConfigBean db = null;
		try {
			final BufferedReader reader = VannoUtils.getReader(filePath);
			String line, val;
			String[] lineSplit = null;
			
			
			while((line = reader.readLine()) != null) {
				line = line.trim();
				if(line.startsWith(OVERLAP_NOTE)) {
					if(line.indexOf(OVERLAP_EQUAL) != -1) {
						lineSplit = line.split("=");
					} else {
						throw new IllegalArgumentException("Cannot parse line : \"" + line +"\", properties should have a format like key=value.");
					}
					
					lineSplit[0] = lineSplit[0].trim().toLowerCase();
					val = lineSplit[1].trim();
					switch (lineSplit[0]) {
						case QUERY:
							config.setQueryFile(val);
							break;
						case QUERY_FORMAT:
							config.setQueryFormat(new FormatWithRef(val));
							break;
						case DB_PATH:
							if(db != null)  {
								VannoUtils.addFileForPattern(db, config.getDbs()); 
							}
							db = new DBRunConfigBean();
							db.setDb(val);
							break;	
						case DB_FORMAT:
							db.setDbFormat(new FormatWithRef(val));
							break;
						case LABEL:
							db.setLable(val);
							break;
						default:
							break;
					}
				} else if(line.startsWith(OVERLAP_NOTE + END)){
					break;
				} else {
					continue;
				}
			}
			if(db != null)  VannoUtils.addFileForPattern(db, config.getDbs()); 
		
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		config.setPro(PROGRAM.ANNO);
		if(config != null) config.checkDBConfig();
		return config;
	}
	
	public static RunConfigBean parseRunConfig(String filePath, PROGRAM pro) {
		IOUtil.assertInputIsValid(filePath);
		
		System.out.println("---- Parsing file:" + filePath + " ----");
		RunConfigBean config = new RunConfigBean();
		DBRunConfigBean db = null;
		
		try {
			final BufferedReader reader = VannoUtils.getReader(filePath);
			String line, val;
			String[] lineSplit = null;
			
			
			while((line = reader.readLine()) != null) {
				
				line = line.trim();
				if(line.startsWith(COMMAND_LINE) || line.equals("") || line.equalsIgnoreCase(REQUIRED_BEGIN) || line.equalsIgnoreCase(OPTIONAL_BEGIN)) continue;
				
				if(line.equalsIgnoreCase(DB_BEGIN)) {
					if(db != null)  {
						VannoUtils.addFileForPattern(db, config.getDbs()); 
					}
					db = new DBRunConfigBean();
					continue;
				} 

				if(line.indexOf('=') != -1) {
					lineSplit = line.split("=");
				} else if(line.indexOf(':') != -1) {
					lineSplit = line.split(":");
				} else {
					throw new IllegalArgumentException("Cannot parse line : \"" + line +"\", properties should have a format like key=value or key:value.");
				}
				
				lineSplit[0] = lineSplit[0].trim().toLowerCase();
				val = lineSplit[1].trim();
				
				switch (lineSplit[0]) {
					case QUERY:
						config.setQueryFile(val);
						break;
					case OUTPUT_FOLDER:
						config.setOutputFolder(val);
						break;
//					case STEPS:
//						config.setStep(val);
//						break;
					case LOJ:
						config.setLoj(val);
						break;
					case OUTPUT_MODE:
						config.setOutMode(Integer.parseInt(val));
						break;
					case FC:
						config.setFc(val);
						break;
//					case ANNO_CONFIG:
//						config.setAnnoConfig(val);
//						break;
					case MODE:
						config.setMode(Integer.parseInt(val));
						break;
					case THREAD:
						config.setThread(Integer.parseInt(val));
						break;
					case QUERY_FORMAT:
						config.setQueryFormat(val);
						break;
					case EXACT:
						config.setExact(val);
						break;
					case DB_PATH:
						db.setDb(val);
						break;	
					case INDEX_TYPE:
						db.setIndexFormat(val);
						break;	
					case LABEL:
						db.setLable(val);
						break;
					default:
						break;
				}
			}
			if(db != null)  {
				VannoUtils.addFileForPattern(db, config.getDbs()); 
			}
			reader.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(config != null) {
			config.setPro(pro);
			config.checkDBConfig();
		}
		return config;
	}

	
	public static int[] splitInt(String s) {
		s = s.replace("[", "").replace("]", "");
		String[] split = s.split(",");
		int[] arr = new int[split.length];
		for (int i = 0; i < split.length; i++) {
			arr[i] = Integer.parseInt(split[i].trim());
		}
		return arr;
	}
	
	public static String[] splitStr(String s) {
		char c;
		int len = s.length();
		if((s.charAt(0) != '[') || (s.charAt(len - 1) != ']')) {
			throw new IllegalArgumentException("array should hava a format like ['a','b','c']");
		}
		List<String> arr = new ArrayList<String>(20);
		StringBuffer newStr = null;
		boolean begin = false;
		for (int i = 1; i < s.length() ; i++) {
			c = s.charAt(i);
			if(c == ' ' || c == '\t' ||  c == '\f' ||  c == '\\') {
				if(begin) {
					newStr.append(c);
				}
				continue;
			}
			
			if( c == '\'' || c == '"') {
				if(!begin) {
					begin = true;
					newStr = new StringBuffer();
				} else {
					if(newStr != null)
						arr.add(newStr.toString());
					begin = false;
				}
				continue;
			}
			if(begin) {
				newStr.append(c);
			}
		}
		
		return arr.toArray(new String[arr.size()]);
	}
	
	public static void main(String[] args) {
//		String[] a = ParseConfigFile.splitStr("[\"OneKG\\_AFR_AF\", \"OneKG_AMR_AF\", \"OneKG_EAS_AF\"]");
//		for (String string : a) {
//			System.out.println(string);
//		}
//		
//		ParseConfigFile.parseRunConfig("/Users/mulin/Desktop/out_linlin/linlin.config");
//		Map<String, AnnoConfigBean> config = ParseConfigFile.parsePostANNConfig("/Users/mulin/Desktop/out_linlin/anno.config");
//		System.out.println(config);
	}
}
