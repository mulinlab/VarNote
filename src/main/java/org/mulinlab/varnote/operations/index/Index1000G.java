package org.mulinlab.varnote.operations.index;

import htsjdk.samtools.util.IOUtil;
import htsjdk.variant.variantcontext.VariantContext;
import org.apache.logging.log4j.Logger;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.filters.iterator.LineFilterIterator;
import org.mulinlab.varnote.operations.readers.query.VCFFileReader;
import org.mulinlab.varnote.utils.LoggingUtils;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.gz.MyBlockCompressedOutputStream;
import org.mulinlab.varnote.utils.gz.MyEndianOutputStream;
import org.mulinlab.varnote.utils.node.LocFeature;
import java.io.File;
import java.io.IOException;
import java.util.*;


public final class Index1000G {
	private final Logger logger = LoggingUtils.logger;

	private final List<String> sequenceNames = new ArrayList<String>();
	private Map<Integer, BinList> chrMap;

	private MyEndianOutputStream gtOS;
	private MyEndianOutputStream gtIndexOS;
	private final VCFFileReader reader;

	public Index1000G(final File file) {
		IOUtil.assertFileIsReadable(file);
		VannoUtils.checkValidBGZ(file.getAbsolutePath());

		chrMap = new HashMap<>();

		reader = new VCFFileReader(file.getAbsolutePath());
		reader.setDecodeLoc(false);

		gtOS = new MyEndianOutputStream(new MyBlockCompressedOutputStream(file.getAbsolutePath() + GlobalParameter.GT_COMPRESS_FILE));
		gtIndexOS = new MyEndianOutputStream(new MyBlockCompressedOutputStream(file.getAbsolutePath() + GlobalParameter.GT_COMPRESS_FILE_IDX));
	}

	public void close() throws IOException {
		gtOS.close();
		gtIndexOS.close();
	}

	public void makeIndex() throws IOException {
		final LineFilterIterator iterator = reader.getFilterIterator();

		String seqName = "";
		int count = 0;


		LocFeature feature;
		VariantContext ctx;
		Variant variant;
		BinList binList = null;

		while(iterator.hasNext()) {
			feature = iterator.next();
			if(feature != null) {
				count ++;
				if(feature.chr.equals("X")) break;
				if (!feature.chr.equals(seqName)) {
					System.out.println(feature.chr);
					sequenceNames.add(feature.chr);
					seqName = feature.chr;

					if(binList != null) {
						chrMap.put(sequenceNames.size() - 1, binList);
					}
					binList = new BinList();
				}

				ctx = feature.variantContext;
				for (int i = 1; i < ctx.getAlleles().size(); i++) {
					variant = new Variant(ctx.getStart(), ctx.getReference(), ctx.getAlternateAllele(i-1));
					variant.setCalls((byte)i, ctx.getGenotypes(), ctx.getNSamples());

					binList.addVariant(variant, gtOS);
					writeVariant(variant);
				}
			}
		}

		if(binList != null) {
			chrMap.put(sequenceNames.size() - 1, binList);
		}

		writeIdx();
		gtOS.write(GlobalParameter.GT_FILE_END);

		iterator.close();
		System.out.println("count = " + count);
	}

	public void writeIdx() throws IOException {
		int len = 0;
		for (int i = 0; i < sequenceNames.size(); i++) {
			len += sequenceNames.get(i).length();
		}
		gtIndexOS.writeInt(sequenceNames.size());

		for (int i = 0; i < sequenceNames.size(); i++) {
			gtIndexOS.writeInt(sequenceNames.get(i).length());
			gtIndexOS.writeBytes(sequenceNames.get(i));
		}

		for (int i = 0; i < sequenceNames.size(); i++) {
			writeBins(chrMap.get(i));
		}
	}

	public void writeBins(final BinList binList) throws IOException {
		List<Bin> bins = binList.getList();
		gtIndexOS.writeInt(bins.size());

		int beg = 0;
		for (int i = 0; i < bins.size(); i++) {
			gtIndexOS.writeInt(bins.get(i).getMin() - beg);
			gtIndexOS.writeInt(bins.get(i).getMax() - bins.get(i).getMin());
			gtIndexOS.writeLong(bins.get(i).getAddress());

			beg = bins.get(i).getMin();
		}
	}

	public void writeVariant(final Variant variant) throws IOException {
		gtOS.writeInt(variant.getPos());

		int len = variant.getRef().getBaseString().length() + variant.getAlt().getBaseString().length() + 1;
		gtOS.writeInt(len);
		gtOS.write(variant.getRef().getBases());
		gtOS.write(0);
		gtOS.write(variant.getAlt().getBases());


		BitSet[] bitSets = variant.getCalls();
		writeBitSet(bitSets[0]);
		writeBitSet(bitSets[1]);
	}

	public void writeBitSet(final BitSet bitSet) throws IOException {
		byte[] bytes = bitSet.toByteArray();

		gtOS.writeShort(bytes.length);
		if(bytes.length > 0) {
			for (int i = 0; i < bytes.length; i++) {
				gtOS.write(bytes[i]);
			}
		}
	}
}
