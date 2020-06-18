package org.mulinlab.varnote.config.anno.databse.anno;

import htsjdk.samtools.util.StringUtil;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import org.mulinlab.varnote.cmdline.txtreader.anno.VcfInfoReader;
import org.mulinlab.varnote.config.param.postDB.DBAnnoParam;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.database.Database;
import org.mulinlab.varnote.utils.enumset.FormatType;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.exceptions.InvalidArgumentException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ExtractConfig {

	public static final String INFO_EXT = ".vcfinfo";
	public static final char EXCLUDE = '-';

	private Map<Integer, String> outputNameMap;
	private Map<String, String> infoNameMap;

	private int[] colToExtract;
	private List<String> infoFiledToExtract;

	private Map<String, VCFInfoHeaderLine> vcfInfoMaps;

	private final DBAnnoParam param;
	private Format format;

	private final Database db;
	private boolean extractInfo = false;

	public ExtractConfig(final DBAnnoParam param, final Database db) {
		this.param = param;
		this.db = db;

		Format dbformat = db.getFormat();
		if(param.isHasHeader()) dbformat.setHasHeaderInFile(true);
		if(param.getHeaderPath() != null) dbformat.setHeaderPath(param.getHeaderPath());
		if(param.getCommentIndicator() != null) dbformat.setCommentIndicator(param.getCommentIndicator());

		if(param.isHasHeader() || param.getHeaderPath() != null || dbformat.getHeaderPart() == null) {
			db.readHeader();
		}
		this.format = db.getFormat();

		initParam();
	}

	public void initParam() {
		outputNameMap = new HashMap<>();
		infoNameMap = new HashMap<>();
		infoFiledToExtract = new ArrayList<>();

		setColToExtract();
		checkHasInfo();
		setInfoField();
		setVcfInfoMaps();
		setOutName();
	}

	private String[] getUniqFields(String[] fields) {
		List<String> keys = new ArrayList<>();
		Map<String, Boolean> keyMap = new HashMap<>();

		for (String field:fields) {
			if(keyMap.get(field) == null) {
				keys.add(field);
				keyMap.put(field, true);
			}
		}
		return keys.toArray(new String[keys.size()]);
	}

	private void setColToExtract() {
		final String[] fields = getARR(param.getFields());
		final String[] cols = getARR(param.getCols());

		if(fields == null && cols == null) throw new InvalidArgumentException(String.format("fields or cols should be defined for %s.", param.getLabel()));
		if(fields != null) {
			if(fields.length == 1 && fields[0].toUpperCase().equals("ALL")) {
				colToExtract = getAllCols();
			} else {
				colToExtract = getColsForField(getUniqFields(fields), param.getFields().charAt(0) == EXCLUDE);
			}
		} else {
			colToExtract = getCols(getUniqFields(cols), param.getCols().charAt(0) == EXCLUDE);
		}
	}

	private void checkHasInfo() {
		if(format.type == FormatType.VCF) {
			for (int col: colToExtract) {
				if(col == GlobalParameter.INFO_COL) {
					extractInfo = true;
					break;
				}
			}
		}
	}

	private int[] getAllCols() {
		int[] cols = new int[format.getHeaderPartSize()];
		for (int i = 1; i <= format.getHeaderPartSize(); i++) {
			cols[i-1] = i;
		}
		return cols;
	}

	private int[] getCols(final String[] colStrs, final  boolean isExclude) {
		int maxSize = format.getHeaderPartSize();

		int[] cols = new int[colStrs.length];
		int col;
		for (int i = 0; i < colStrs.length; i++) {
			col = Integer.parseInt(colStrs[i]);
			if(col > maxSize) throw new InvalidArgumentException(String.format("Column index %d is greater than the max column index %d of %s.", col, maxSize, param.getLabel()));
			cols[i] = col;
		}
		if(!isExclude) {
			return cols;
		} else {
			return getOpposite(cols);
		}
	}

	private int[] getColsForField(final String[] fields, final  boolean isExclude) {
		int col;
		int[] cols = new int[fields.length];
		for (int i = 0; i < fields.length; i++) {
			col = this.format.getCol(fields[i]);
			if(col == -1) throw new InvalidArgumentException(String.format("Unknown field name: %s, possible fields of %s are %s.", fields[i], param.getLabel(), StringUtil.join(GlobalParameter.TAB, this.format.getHeaderPart())));

			cols[i] = col;
		}

		if(!isExclude) {
			return cols;
		} else {
			return getOpposite(cols);
		}
	}

	private void setInfoField() {
		if(extractInfo) {
			String[] infoToExtract = null;

			if(param.getInfofields() != null) {
				infoToExtract = getARR(param.getInfofields());
			}

			if(infoToExtract == null || infoToExtract.length == 0) {
				infoFiledToExtract = db.getVcfParser().getInfoKeys();
			} else {
				VCFHeader header = db.getVcfParser().getVcfHeader();
				for (String key: infoToExtract) {
					final VCFInfoHeaderLine headerLine = header.getInfoHeaderLine(key);
					if (headerLine == null) throw new InvalidArgumentException(String.format("Unknown info key %s for %s", key, param.getLabel()));
					infoFiledToExtract.add(key);
				}
			}
		}
	}

	private int[] getOpposite(final int[] cols) {
		int[] newCols = new int[format.getHeaderPartSize() - cols.length];
		int index = 0;
		boolean has = false;
		for (int i = 1; i <= format.getHeaderPartSize(); i++) {
			for (int c: cols) {
				if(c == i) {
					has = true;
					break;
				}
			}
			if(!has) {
				newCols[index++] = i;
			}
		}
		return newCols;
	}

	private String[] getARR(String val) {
		if(val == null) return null;

		val = val.trim();
		int i = 0, len = val.length();
		if(val.charAt(i) == EXCLUDE) {
			i++;
		}
		if(val.charAt(i) != '[' || val.charAt(len - 1) != ']') throw new InvalidArgumentException("You should set field with format like XXX = [XX1, XX2, XX3] or XXX = -[XX1, XX2, XX3]");
		String[] r = val.substring(i+1, len - 1).split(GlobalParameter.COMMA);

		for (int j = 0; j < r.length; j++) {
			r[j] = r[j].trim();
		}
		return r;
	}

	private int checkColName(String name) {
		name = name.trim().toUpperCase();
		if(name.matches("COL\\d+")) {
			return Integer.parseInt(name.substring(3));
		} else {
			return -1;
		}
	}

	private void setOutName() {
		if(param.getOutNames() != null) {
			String[] outNames = getARR(param.getOutNames());

			if(outNames.length > 0) {

				int beg, col;
				String key, out;
				VCFHeader header = null;
				boolean isInfo;
				for (String str : outNames) {

					beg = str.indexOf(":");
					if(beg == -1)  throw new InvalidArgumentException("out_names should have a format like [a:A, b:B, c:C] or [col1:A, col3:B, col4:C].");
					key = str.substring(0, beg).trim();
					out = str.substring(beg + 1).trim();

					col = format.getCol(key);
					if(col == -1) {
						col = checkColName(key);
					}

					if((col == -1)) {

						isInfo = false;
						if(extractInfo) {
							if(header == null) header = db.getVcfParser().getVcfHeader();
							final VCFInfoHeaderLine headerLine = header.getInfoHeaderLine(key);
							if (headerLine != null) {
								isInfo = true;
								infoNameMap.put(key, out);
							}
						}

						if(!isInfo) throw new InvalidArgumentException(String.format("Unknown field name %s of database %s defined in out_names",  key, getLabel()));
					} else {
						outputNameMap.put(col, out);
					}
				}
			}
		}
	}

	private void setVcfInfoMaps() {
		String infoPath = param.getVcfInfoPath();
		if(infoPath == null) {
			if(new File(db.getDbPath() + INFO_EXT).exists()) {
				infoPath = db.getDbPath() + INFO_EXT;
			}
		}

		if(infoPath != null) {
			vcfInfoMaps = (new VcfInfoReader<Map<String, VCFInfoHeaderLine>>()).read(infoPath);
		}
	}

	public VCFInfoHeaderLine getInfoForField(final String field) {
		if(vcfInfoMaps == null) return null;
		return vcfInfoMaps.get(field);
	}

	public String getOutputName(final Integer key) {
		return outputNameMap.get(key);
	}

	public String getInfoOutputName(final String key) {
		return infoNameMap.get(key);
	}

	public List<String> getInfoFiledToExtract() {
		return infoFiledToExtract;
	}

	public Format getFormat() {
		return format;
	}

	public int[] getColToExtract() {
		return colToExtract;
	}

	public String getLabel() {
		return param.getLabel();
	}

	public Database getDb() {
		return db;
	}
}
