package org.mulinlab.varnote.operations.decode;

import org.mulinlab.varnote.utils.enumset.VariantType;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.utils.node.LocFeature;

public final class TABLocCodec extends LocCodec {

    public TABLocCodec(final Format format, final boolean isFull) {
        super(format, format.getHeaderPart() == null ? -1 : format.getHeaderPartSize(), isFull);
    }

    @Override
    public LocFeature decode(final String s) {
        super.decode(s);
        if(exceedMaxLength()) {
            intv.vt = VariantType.OML;
        }

        return intv;
    }

    @Override
    public void processBeg() {
        if ((format.getFlags() & 0x10000) != 0) ++intv.end;
        else  --intv.beg;
    }

    @Override
    public void processEnd() {
        if(!format.isPos()) {
            intv.end = Integer.parseInt(parts[format.endPositionColumn - 1]);
        }
    }

    @Override
    public void processOther() {
        if(format.isPos() && format.refPositionColumn > 1) {
            intv.end = intv.beg + parts[format.refPositionColumn - 1].length();
        }
    }

    @Override
    public TABLocCodec clone() {
        return new TABLocCodec(this.format, this.isFull);
    }
}
