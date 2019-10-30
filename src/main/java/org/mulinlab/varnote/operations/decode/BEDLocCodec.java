package org.mulinlab.varnote.operations.decode;

import org.mulinlab.varnote.utils.format.Format;

public final class BEDLocCodec extends LocCodec {

    public BEDLocCodec(final boolean isFull) {
        this(Format.BED, isFull);
    }

    public BEDLocCodec(final Format format, final boolean isFull) {
        super(format, format.getHeaderPart() == null ? -1 : format.getHeaderPartSize(), isFull);
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
