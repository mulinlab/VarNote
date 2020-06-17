package org.mulinlab.varnote.operations.decode;

import htsjdk.tribble.util.ParsingUtils;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.utils.node.LocFeature;

public class RSIDLocCodec extends LocCodec {

    public RSIDLocCodec(final Format format) {
        super(format, 0, false);
    }

    @Override
    public LocFeature decode(String s) {
        intv.clear();
        if(parts == null) {
            parts = new String[s.split(format.getDelimStr()).length];
        }
        ParsingUtils.split(s, parts, format.getDelimChar(), true);

        String rsid = parts[0];
        if(VannoUtils.validRsid(rsid)) {
            int pos = Integer.parseInt(rsid.substring(2));
            intv.chr = rsid.substring(2, 3);
            intv.beg = pos - 1;
            intv.end = pos;

            intv.parts = new String[parts.length];
            for (int i = 0; i < parts.length; i++) {
                intv.parts[i] = parts[i];
            }

            return intv;
        } else {
            invalidRSID(s);
            return null;
        }
    }

    protected void invalidRSID(final String rsid) {
        logger.error(String.format("Failed to parser invalid rsid \"%s\"", rsid));
    }

    @Override
    public void processBeg() {
    }

    @Override
    public void processEnd() {
    }

    @Override
    public void processOther() {
    }

    @Override
    public RSIDLocCodec clone() {
        return new RSIDLocCodec(this.format);
    }
}
