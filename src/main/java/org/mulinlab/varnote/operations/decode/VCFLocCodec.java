package org.mulinlab.varnote.operations.decode;

import org.mulinlab.varnote.config.anno.VCFParser;
import org.mulinlab.varnote.utils.format.Format;

public final class VCFLocCodec extends LocCodec {

    protected final static int INFO_FIELD = 8;

    public VCFLocCodec() {
        this(Format.VCF);
    }

    public VCFLocCodec(final Format format) {
        super(format, INFO_FIELD + 1);
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
//        ajustSVTYPE(parts[INFO_FIELD - 1]);
    }

    public void ajustSVTYPE(final String info) {
        if(info == null) return;
        ajustEND(info);
        if((info.indexOf("SVTYPE") != -1) && (info.indexOf("CIPOS") != -1) && (info.indexOf("CIEND") != -1)) {
            int posbegin = info.indexOf("CIPOS"), posend = info.indexOf(VCFParser.INFO_FIELD_SEPARATOR, info.indexOf("CIPOS")),
                    endbegin = info.indexOf("CIEND"), endend = info.indexOf(VCFParser.INFO_FIELD_SEPARATOR, info.indexOf("CIEND"));
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
