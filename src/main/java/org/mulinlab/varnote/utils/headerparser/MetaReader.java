package org.mulinlab.varnote.utils.headerparser;

import java.io.IOException;

import org.mulinlab.varnote.utils.gz.MyBlockCompressedInputStream;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.utils.VannoUtils;


public final class MetaReader {
	public static void readHeader(final Format formatSpec, final String path, final String label) {
		
		VannoUtils.checkValidBGZ(path);
		final MyBlockCompressedInputStream reader = VannoUtils.makeBGZ(path);

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
