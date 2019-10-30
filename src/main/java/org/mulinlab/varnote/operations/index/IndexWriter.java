package org.mulinlab.varnote.operations.index;

import org.mulinlab.varnote.config.param.IndexParam;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.filters.iterator.LineFilterIterator;
import org.mulinlab.varnote.filters.query.line.SkipLineFilter;
import org.mulinlab.varnote.operations.readers.itf.BGZReader;
import org.mulinlab.varnote.operations.readers.query.AbstractFileReader;
import org.mulinlab.varnote.utils.LoggingUtils;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.block.SROB;
import org.mulinlab.varnote.utils.enumset.FileType;
import org.mulinlab.varnote.utils.enumset.IndexType;
import org.mulinlab.varnote.utils.gz.MyBlockCompressedOutputStream;
import org.mulinlab.varnote.utils.gz.MyEndianOutputStream;
import htsjdk.samtools.util.BlockCompressedFilePointerUtil;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.utils.node.LocFeature;
import org.mulinlab.varnote.exceptions.InvalidArgumentException;
import org.apache.logging.log4j.Logger;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class IndexWriter {
	private final Logger logger = LoggingUtils.logger;

	private final IndexParam param;
	private final Format formatSpec;
	private final List<String> sequenceNames = new ArrayList<String>();
	private final Set<String> sequenceNamesSeen = new HashSet<String>();

	private Map<Integer, Long> addressOfChr;
	private MyEndianOutputStream vannoOS;
	private MyEndianOutputStream vannoIndexOS;
	private boolean isBlockStart;
	
	private SROB bin;
	private SROBListBean listBean;
	
	public IndexWriter(final IndexParam param) {
		this.param = param;
		this.formatSpec = param.getFormat();
		this.isBlockStart = true;
		this.addressOfChr = new HashMap<Integer, Long>();
		this.bin = new SROB(-1, 0, 0, 0);

		this.listBean = new SROBListBean();
	}

	public void makeIndex() {
		try {

			VannoUtils.checkValidBGZ(param.getInput());

			final String vannoFile = param.getOutputDir() + File.separator + param.getInputFileName() + IndexType.VARNOTE.getExt(); // + inputName
			final String vannoIndexFile = param.getOutputDir() + File.separator + param.getInputFileName() + IndexType.VARNOTE.getExtIndex();

			final File vannoFileTemp = new File(vannoFile + GlobalParameter.TEMP);
			final File vannoIndexFileTemp = new File(vannoIndexFile + GlobalParameter.TEMP);

			vannoOS = new MyEndianOutputStream(new MyBlockCompressedOutputStream(vannoFileTemp));
			vannoIndexOS = new MyEndianOutputStream(new MyBlockCompressedOutputStream(vannoIndexFileTemp));

			final AbstractFileReader reader = VannoUtils.getReader(new BGZReader(param.getInput()), formatSpec);
			writeIndexForBGZ(reader);

			vannoOS.write(GlobalParameter.VANNO_FILE_END);
			vannoOS.close();

			vannoIndexOS.write(GlobalParameter.VANNO_FILE_END);
			long address = vannoIndexOS.getOut().getFilePointer();

			VannoUtils.writeFormats(vannoIndexOS, formatSpec, reader.getFormat().getHeaderPart(), sequenceNames, addressOfChr);
			vannoIndexOS.close(address);

			moveFile(vannoFileTemp, new File(vannoFile));
			moveFile(vannoIndexFileTemp, new File(vannoIndexFile));

			logger.info(String.format("\nVanno file is done, please find it in %s." , vannoFile));
			logger.info(String.format("Vanno index file is done, please find it in %s. \n" , vannoIndexFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeIndexForBGZ(final AbstractFileReader reader) throws IOException {
		reader.addLineFilters(new SkipLineFilter(formatSpec.numHeaderLinesToSkip));
		reader.readFormatFromHeader();

		logger.info(String.format("Input format: %s", param.getFormat().logFormat()));
		final LineFilterIterator iterator = reader.getFilterIterator();

		String seqName = "";
		BlockBean blockBean = new BlockBean(0), preBlockBean = new BlockBean(0);
		int tid = 0, preBeg = -1;
		LocFeature feature = new LocFeature();

		int c = 0;
		while(iterator.hasNext()) {
			c++;
			feature = iterator.next();
//			System.out.println(feature + ", " + iterator.getPosition());
			if(feature == null) {
				blockBean.setFilePointer(iterator.getPosition());
			} else {
				if(!feature.chr.equals(seqName)) {
					if(!seqName.equals("")) {
						addBin(bin);
						vannoOS.write(GlobalParameter.CHR_START); //new chr start
						vannoIndexOS.writeInt(GlobalParameter.CHR_START);
						listBean.init();
						bin.initSROB(-1, 0, 0, 0);
					}

					isBlockStart = true;
					preBeg = 0;
					
					addSeqsName(feature.chr);
					seqName = feature.chr; tid = sequenceNames.size() - 1;

					addressOfChr.put(tid, vannoIndexOS.getOut().getFilePointer());
					logger.info(String.format("Writing vanno file for chr %s", seqName));
				}
				
				if(feature.beg < preBeg) throw new InvalidArgumentException(String.format("Features added out of order: next start %d < previous start %d, please sorted features by chr column, start column. Unsorted line: %s", feature.beg, preBeg, feature.origStr));
				if(feature.beg > feature.end) throw new InvalidArgumentException(String.format("Feature start position %d > feature end position %d", feature.beg, feature.end));
				
				if(isBlockStart) {
					addBin(bin);
					bin.initSROB(feature.beg, feature.end, vannoOS.getOut().getFilePointer(), blockBean.getFilePointer());
				} else {
					bin.updateMax(feature, blockBean.getBlockOffset() - preBlockBean.getBlockOffset());
				}
				
				preBlockBean.setFilePointer(blockBean.getFilePointer());
				preBeg = feature.beg;
				blockBean.setFilePointer(iterator.getPosition());

				if(blockBean.getBlockAddress() == preBlockBean.getBlockAddress()) {
					isBlockStart = false;
				} else {
					isBlockStart = true;
				}
			}
		}
		if(!seqName.equals("")) {
			addBin(bin);
			vannoIndexOS.writeInt(GlobalParameter.CHR_START);
		}
		iterator.close();
		System.out.println("total: " + c);
	}
	
	public void addBin(final SROB bin) throws IOException {
		if((bin != null) && bin.getMin() != -1 && bin.hasFeature()) {
			
			final List<SROB.Feature> features = bin.getBlockFeature();
			
			int end = features.get(0).getEnd(), avgOffset = bin.getAVGOffset();
			if(end == 1) {
				vannoOS.write(GlobalParameter.BLOCK_START_WO_END);
			} else {
				if(end <= GlobalParameter.MAX_BYTE_UNSIGNED) {
					vannoOS.write(GlobalParameter.BLOCK_START_WITH_END_BTYE);
					vannoOS.write(end); 
				} else if(end <= GlobalParameter.MAX_SHORT_UNSIGNED) {
					vannoOS.write(GlobalParameter.BLOCK_START_WITH_END_SHORT);
					vannoOS.writeShort(end); 
				} else {
					vannoOS.write(GlobalParameter.BLOCK_START_WITH_END_INT);
					vannoOS.writeInt(end); 
				}
			}
			
			vannoOS.writeLong(bin.getBGZFilePointer());
			writeShort(vannoOS, avgOffset);

			int beg, offset , flagBeg = 0, flagEnd = 0;
			int flag = 0;
			SROB.Feature feature = null;
			for (int i = 1; i < features.size(); i++) {
				flagBeg = 0;  flagEnd = 0; flag = 0;
				feature = features.get(i);
				
				beg = feature.getBeg();    end = feature.getEnd();    offset = feature.getOffset() - avgOffset;
				if(beg <= 4) {
					flag += beg;   
				} else {
					flagBeg = getFlag(beg);
				}
				if(flagBeg > 0) flag = flag + flagBeg + 4;

				if(end == 1) {

				} else {
					flagEnd = getFlag(end);
				}
				flag += flagEnd * 8;
				
				if(offset < 0) {       
					flag += 32;      
				} 
				
				offset = Math.abs(offset);
				if(offset > GlobalParameter.MAX_BYTE_UNSIGNED) {
					flag += 64;      
				} 
				
				vannoOS.write(flag);

				if(flagBeg > 0) write(vannoOS, flagBeg, beg);
				if(flagEnd > 0) write(vannoOS, flagEnd, end);
				
				if(offset <= GlobalParameter.MAX_BYTE_UNSIGNED) {
					vannoOS.write(offset);
				} else if(offset <= GlobalParameter.MAX_SHORT) {
					vannoOS.writeShort(offset);
				} else {
					vannoOS.writeShort(GlobalParameter.INT_START);
					vannoOS.writeInt(offset);
				}
				feature = null;
			}
			
			bin.clear();
			if(listBean.isBlockChanged(bin.getVannoFilePointer())) {
				vannoIndexOS.writeInt(GlobalParameter.BLOCK_ADDRESS);
				vannoIndexOS.writeLong(bin.getVannoFilePointer());
			} else {
				vannoIndexOS.writeInt(listBean.getBlockOffset());
			}
			vannoIndexOS.writeInt(listBean.getOffsetMin(bin.getMin()));
			vannoIndexOS.writeInt(bin.getMax() - bin.getMin());
		}
	}

	public int getFlag(final int num) {
		if(num <= GlobalParameter.MAX_BYTE_UNSIGNED ) {
			return  1;
		} else if(num <= GlobalParameter.MAX_SHORT_UNSIGNED) {
			return 2;
		} else {
			return 3;
		}
	}

	public void writeShort(final MyEndianOutputStream os, final int val) throws IOException {
		if(val <= GlobalParameter.MAX_SHORT) {
			os.writeShort(val);
		} else {
			os.writeShort(GlobalParameter.INT_START);
			os.writeInt(val);
		}
	}
	
	public void write(final MyEndianOutputStream os, final int flag, final int val) throws IOException {
		if(flag == 1) {
			os.write(val);
		} else if(flag == 2) {
			os.writeShort(val);
		} else if(flag == 3) {
			os.writeInt(val);
		}
	}


	private void moveFile(final File src, final File des) {
		boolean success = src.renameTo(des);
		if (!success) {
			logger.error(String.format("Rename from %s to %s with error.", src.getAbsolutePath(), des.getAbsolutePath()));
			System.exit(1);
		}
	}

    public void addSeqsName(String sequenceName) {
		if(sequenceNamesSeen.contains(sequenceName)) {
			throw new InvalidArgumentException("chr must in order!");
		}
		sequenceNames.add(sequenceName);
		sequenceNamesSeen.add(sequenceName);
	}

    public final class BlockBean {
		private long filePointer;
    		
		public BlockBean(final long filePointer) {
			this.filePointer = filePointer;
		}
		public void setFilePointer(final long filePointer) {
			this.filePointer = filePointer;
		}
		public long getFilePointer() {
			return filePointer;
		}
		public long getBlockAddress() {
			return BlockCompressedFilePointerUtil.getBlockAddress(filePointer);
		}
		public int getBlockOffset() {
			return BlockCompressedFilePointerUtil.getBlockOffset(filePointer);
		}   		  		
    }
    
    public final class SROBListBean {
		private long blockAddress = -1, preBlockAddress = -1;
		private int blockOffset = 0, preMin = 0;

		public boolean isBlockChanged(final long filePointer) {
			blockAddress = BlockCompressedFilePointerUtil.getBlockAddress(filePointer);
			if(blockAddress != preBlockAddress) {
				preBlockAddress = blockAddress;
				blockOffset = BlockCompressedFilePointerUtil.getBlockOffset(filePointer);
				return true;
			} else {
				blockOffset = BlockCompressedFilePointerUtil.getBlockOffset(filePointer);
				return false;
			}
		}

		public int getOffsetMin(final int min) {
			int offsetMin = min - preMin;
			preMin = min;
			return offsetMin;
		}

		public SROBListBean() {
			super();
			init();
		}

		public void init() {
			blockAddress = -1; preBlockAddress = -1; blockOffset = 0;  preMin = 0;
		}

		public int getBlockOffset() {
			return blockOffset;
		}
    }
}
