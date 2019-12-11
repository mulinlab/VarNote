package org.mulinlab.varnote.config.anno.query;

import java.util.HashMap;
import java.util.StringJoiner;
import htsjdk.samtools.util.StringUtil;
import htsjdk.variant.vcf.VCFCodec;
import htsjdk.variant.vcf.VCFHeader;
import org.mulinlab.varnote.filters.iterator.NoFilterIterator;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.operations.readers.query.AbstractFileReader;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.utils.node.LocFeature;


public final class QueryBEDParser extends AbstractQueryParser{

	private String vcfHeaderPathForBED;
    private HashMap<String, Integer> colMap;

	public QueryBEDParser(final Format format, final String vcfHeaderPathForBED) {
		super(format);
		this.vcfHeaderPathForBED = vcfHeaderPathForBED;

        colMap = new HashMap<>();
        String[] parts = format.getHeaderPart();
        for (int i = 0; i < parts.length; i++) {
            colMap.put(parts[i].toUpperCase(), i+1);
        }
	}

	@Override
	public String toVCFHeader() {
		return VCF_HEADER_INDICATOR + StringUtil.join(TAB, VCF_FIELD) ;
	}

    @Override
    public String toVCFRecord(final LocFeature query, final StringJoiner dbjoiner) {
        StringJoiner joiner = new StringJoiner(TAB);
        String[] parts = query.parts;

        joiner.add(query.chr);
        joiner.add(String.valueOf(query.beg + 1));
        joiner.add(NULLVALUE);
        joiner.add(query.ref);
        joiner.add(query.alt);
        joiner.add(NULLVALUE);
        joiner.add(NULLVALUE);
        joiner.add(getInfo(query, dbjoiner));

        return joiner.toString();
    }

    @Override
    public String toBEDHeader(final StringJoiner joiner) {
        StringJoiner tj = new StringJoiner(TAB);
        tj = getBEDHeaderStart(tj);

        for (int i = 1; i <= format.getHeaderPartSize(); i++) {
            if((i != format.sequenceColumn) && (i != format.startPositionColumn) && (i != format.endPositionColumn))
            tj.add(format.getColumnName(i));
        }
        tj.merge(joiner);
	    return tj.toString();
    }

    @Override
    public String toBEDRecord(final LocFeature query, StringJoiner dbjoiner) {
        StringJoiner joiner = new StringJoiner(TAB);
        joiner = getBEDDataStart(joiner, query);

        String[] parts = query.parts;
        for (int i = 1; i <= parts.length; i++) {
            if(i != format.sequenceColumn && i != format.startPositionColumn && i != format.endPositionColumn) {
                joiner.add(parts[i - 1]);
            }
        }
        if(dbjoiner != null && dbjoiner.length() > 0) joiner.merge(dbjoiner);
        return joiner.toString();
    }

    private String getInfo(final LocFeature query, final StringJoiner dbjoiner) {
        StringJoiner infojoiner = new StringJoiner(INFO_FIELD_SEPARATOR);
        String[] parts = query.parts;

        for (int i = 1; i <= parts.length; i++) {
            if(i != format.sequenceColumn && i != format.startPositionColumn && i != format.endPositionColumn &&
                    i != format.refPositionColumn && i != format.altPositionColumn) {
                infojoiner.add(format.getColumnName(i) + VCF_INFO_EQUAL + parts[i - 1]);
            }
        }
        if(dbjoiner != null && dbjoiner.length() > 0) infojoiner.merge(dbjoiner);
        return infojoiner.toString();
    }

    private String getDefaultVal(final String val) {
        return (val == null || val.equals("")) ? NULLVALUE : val;
    }

	@Override
	public VCFHeader getVCFHeader() {
		if(vcfHeaderPathForBED == null) {
            logger.info(String.format("%sNote: Your output format is vcf, you should define --vcf-header-for-bed or -V for vcf header or we won't output header.%s" ,GlobalParameter.KRED, GlobalParameter.KNRM));
			return null;
		} else {
			NoFilterIterator iterator = new NoFilterIterator(vcfHeaderPathForBED, VannoUtils.checkFileType(vcfHeaderPathForBED));
			VCFHeader header = (VCFHeader) (new VCFCodec()).readActualHeader(iterator);
            iterator.close();
			return header;
		}
	}
}
