package bean.config.anno;

import htsjdk.samtools.util.IOUtil;
import htsjdk.variant.vcf.VCFCodec;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFInfoHeaderLine;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import postanno.FormatWithRef;
import postanno.LineIteratorImpl;

public abstract class AbstractVCFParser extends AbstractParser{

	private final VCFCodec codec;
	
	protected VCFHeader vcfHeader;
	protected List<String> infoKeys;
	
	public AbstractVCFParser(final FormatWithRef format, final String path) {
		super(format);
		this.codec = new VCFCodec();

		IOUtil.assertInputIsValid(path);
		readHeaderFiles(new File(path));
	}

	public void readHeaderFiles(File file) {
		System.out.println("Reading VCF header from file: " + file.getAbsolutePath() + "\n");
		try {
			vcfHeader = (VCFHeader)codec.readActualHeader(new LineIteratorImpl(file));
			infoKeys = new ArrayList<String>();
			for (VCFInfoHeaderLine info : vcfHeader.getInfoHeaderLines()) {
				infoKeys.add(info.getID());
			}
		} catch (FileNotFoundException e) {
			System.out.println("File Path" + file.getAbsolutePath());
			e.printStackTrace();
		}
	}

	@Override
	public abstract void emptyNodes(final String[] query_alts);
	
}
