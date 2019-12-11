package org.mulinlab.varnote.config.anno;


import org.mulinlab.varnote.constants.GlobalParameter;

import java.util.StringJoiner;

public class NormalField {

	public static final int FORCE_OVERLAP = -2;
	public static final int NOT_MATCH = -1;

	public static final String NO_VAL = "No-val";
	public final String COMMA = GlobalParameter.COMMA;

	protected final String fileName;
	protected String[] dbValues;
	protected int index;

	public NormalField(final String fileName) {
		this.fileName = fileName;
	}
	
	public void init(final int length) {
		dbValues = new String[length];
		index = 0;
	}

	public void addDBVal(final String val)  {
		try {
			if(val != null && !val.equals(NO_VAL)) {
				dbValues[index++] = val;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(index + ", " + dbValues.length);
		}
	}

	public String getVal()  {
		if(index == 0) {
			return null;
		} else if(index == 1) {
			return dbValues[0];
		} else {
			StringJoiner joiner = new StringJoiner(COMMA);
			for (int i = 0; i < index; i++) {
				joiner.add(dbValues[i]);
			}
			return joiner.toString();
		}
	}

	public String getFileName() {
		return fileName;
	}
}
