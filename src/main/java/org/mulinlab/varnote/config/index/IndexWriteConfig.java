package org.mulinlab.varnote.config.index;

import org.mulinlab.varnote.config.param.IndexParam;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.database.index.IndexFactory;
import org.mulinlab.varnote.utils.database.index.vannoIndex.VannoIndex;
import org.mulinlab.varnote.utils.enumset.IndexType;
import org.mulinlab.varnote.utils.headerparser.BEDHeaderParser;
import org.mulinlab.varnote.exceptions.InvalidArgumentException;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.headerparser.MetaReader;
import org.mulinlab.varnote.utils.gz.MyBlockCompressedOutputStream;
import org.mulinlab.varnote.utils.gz.MyEndianOutputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public final class IndexWriteConfig {
	private IndexParam indexParam;

	public IndexWriteConfig(final IndexParam param) {
		super();
		this.indexParam = param;
	}
	
	public void init() {
		indexParam.checkParam();
	}

	public VannoIndex getIndex() {
		final String dbIndex = indexParam.getInput() + IndexType.VARNOTE.getExtIndex();
		if(!VannoUtils.isExist(dbIndex)) throw new InvalidArgumentException("Cannot find vanno index file: " + dbIndex + " !");
		
		return (VannoIndex) IndexFactory.readIndex(dbIndex);
	}
	
	public void printMetaData() {
		final VannoIndex idx = getIndex();
		MetaReader.readHeader(idx.getFormat(), indexParam.getInput(), "meta data");
	}
	
	public void printHeader() {
		final VannoIndex idx = getIndex();
		List<String> colNames = idx.getHeaderColList();
		if(colNames != null && colNames.size() > 0) {
			System.out.println("Header contains " + colNames.size() + " columns as following:");
			for (String colName : colNames) {
				System.out.println(colName);
			}
		} else {
			System.out.println("No header found!");
		}
	}
	
	public void listChromsome() {
		final VannoIndex idx = getIndex();
		String[] mSeq = idx.getmSeq();
		if(mSeq != null && mSeq.length > 0) {
			System.out.println("List sequence names: ");
			for (String seq : mSeq) {
				System.out.println(seq);
			}
		} else {
			System.out.println("No sequence names found!");
		}
	}
	
	public void replaceHeader(final String header) {
		final VannoIndex idx = getIndex();
		final String dbIndex = indexParam.getInput() + IndexType.VARNOTE.getExtIndex();
		final File tempFile = new File(dbIndex + GlobalParameter.TEMP);
		final MyEndianOutputStream indexLos = new MyEndianOutputStream(new MyBlockCompressedOutputStream(tempFile));
		final List<String> sequenceNames = new ArrayList<String>();
		
		for (String seq : idx.getmSeq()) {
			sequenceNames.add(seq);
		}
		VannoUtils.writeFormats(indexLos, idx.getFormat(), 
				BEDHeaderParser.parserHeaderComma(header.replaceAll("\"", "").replaceAll("“", "").replaceAll("”", "")), 
				sequenceNames, idx.getMinOffForChr());
		try {
			indexLos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		boolean success = tempFile.renameTo(new File(dbIndex));
		if (!success) {
		   System.err.println("Rename from '" + tempFile.getAbsolutePath() + "' to '" + dbIndex + "' with error. ");
		   System.exit(1);
		} 
	}

	public IndexParam getIndexParam() {
		return indexParam;
	}
}
