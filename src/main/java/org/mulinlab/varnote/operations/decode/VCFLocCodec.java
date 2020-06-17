package org.mulinlab.varnote.operations.decode;

import htsjdk.variant.vcf.*;
import org.mulinlab.varnote.config.anno.databse.VCFParser;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.enumset.VariantType;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.utils.node.LocFeature;
import java.util.HashMap;
import java.util.Map;

public final class VCFLocCodec extends LocCodec {

    private VCFCodec codec;
    private VCFParser vcfParser;
    private VCFHeader vcfHeader;
    private Map<String, Boolean> altIDs;

    public VCFLocCodec(final Format format, final boolean isFull, VCFHeader vcfHeader) {
        super(format, isFull ? (format.getHeaderPart() == null ? -1 : format.getHeaderPartSize()) : 8, isFull);
        this.vcfHeader = vcfHeader;

        if(!format.isAllowLargeVariants() && vcfHeader != null) {
            altIDs = new HashMap<>();
            for (VCFIDHeaderLine line:vcfHeader.getIDHeaderLines()) {
                if(line instanceof VCFAltHeaderLine) {
                    altIDs.put(line.getID(), true);
                    altIDs.put("<" + line.getID() + ">", true);
                }
            }
        }
    }

    public VCFLocCodec(final Format format, final boolean isFull, final VCFParser vcfParser) {
        this(format, isFull, vcfParser.getVcfHeader());
        this.vcfParser = vcfParser;
        this.codec = vcfParser.getCodec();
    }

    @Override
    public VCFLocCodec clone() {
        if(vcfParser == null) {
            return new VCFLocCodec(this.format, this.isFull, vcfHeader);
        } else {
            return new VCFLocCodec(this.format, this.isFull, this.vcfParser.clone());
        }
    }

    @Override
    public LocFeature decode(final String s) {
        super.decode(s);

        if (altIDs != null && altIDs.get(intv.alt) != null) {
            intv.vt = VariantType.LV;
        } else if(exceedMaxLength()) {
            intv.vt = VariantType.OML;
        }

        if(codec != null) {
            intv.variantContext = codec.decode(s);
        }
        return intv;
    }

    @Override
    public void processBeg() {
        intv.beg = intv.beg - 1;
    }

    @Override
    public void processEnd() {

    }

    @Override
    public void processOther() {
        intv.end = intv.beg + intv.ref.length();
        if(parts != null && parts.length > 7) {
            ajustSVTYPE(parts[7]);
        }
    }

    public void ajustSVTYPE(final String info) {
        if(info == null) return;
        ajustEND(info);
        if((info.indexOf("SVTYPE") != -1) && (info.indexOf("CIPOS") != -1) && (info.indexOf("CIEND") != -1)) {
            int posbegin = info.indexOf("CIPOS"), posend = info.indexOf(GlobalParameter.INFO_FIELD_SEPARATOR, info.indexOf("CIPOS")),
                    endbegin = info.indexOf("CIEND"), endend = info.indexOf(GlobalParameter.INFO_FIELD_SEPARATOR, info.indexOf("CIEND"));
            if(posend == -1) posend = info.length();
            if(endend == -1) endend = info.length();

            String[] CIPOS = info.substring(posbegin + 6, posend).split(",");
            String[] CIEND = info.substring(endbegin + 6, endend).split(",");

            if(CIPOS.length == 2 && CIEND.length == 2) {
                intv.beg = intv.beg + Integer.parseInt(CIPOS[0]);
                intv.end = intv.end + Integer.parseInt(CIEND[1]);
            }
        }
    }

    public void ajustEND(final String info) {
        int e_off = -1, i = info.indexOf("END=");
        if (i == 0) e_off = 4;
        else if (i > 0) {
            i = info.indexOf(";END=");
            if (i >= 0) e_off = i + 5;
        }
        if (e_off > 0) {
            i = info.indexOf(';', e_off);
            intv.end = Integer.parseInt(i > e_off ? info.substring(e_off, i) : info.substring(e_off));
        }
    }
}
