package main.java.vanno.index;

import java.io.IOException;
import main.java.vanno.bean.format.Format;
import main.java.vanno.constants.VannoUtils;



public final class MetaReader {
	public static void readHeader(final Format formatSpec, final String path, final String label, final boolean useJDK) {
		
		VannoUtils.checkValidBGZ(path);
		final MyBlockCompressedInputStream reader = VannoUtils.makeBGZ(path, useJDK);

		String line;
		int i = 0;
		try {
			while((line = reader.readLine()) != null) {
				line = line.trim();
				if(line.startsWith(formatSpec.getCommentIndicator()) || line.equals("")) {
					System.out.println(line);
					i ++;
				} else if(formatSpec.isHasHeader() || line.startsWith(Format.VCF_HEADER_INDICATOR)) {
					System.out.println(line);
					i ++;
					break;
				} else {
					break;
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(i == 0) {
			System.out.println("No " + label + " found!");
		}
	}
}
