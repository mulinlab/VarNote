package org.mulinlab.varnote.operations.decode;

import org.mulinlab.varnote.utils.enumset.VariantType;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.utils.node.LocFeature;

public final class BEDLocCodec extends LocCodec {

    public BEDLocCodec(final boolean isFull) {
        this(Format.newBED(), isFull);
    }

    public BEDLocCodec(final Format format, final boolean isFull) {
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

    }

    @Override
    public void processEnd() {
        intv.end = Integer.parseInt(parts[format.endPositionColumn - 1]);
    }

    @Override
    public void processOther() {
    }

    @Override
    public BEDLocCodec clone() {
        return new BEDLocCodec(this.format, this.isFull);
    }
}
