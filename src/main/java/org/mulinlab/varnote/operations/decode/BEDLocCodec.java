package org.mulinlab.varnote.operations.decode;

import org.mulinlab.varnote.utils.format.Format;

public final class BEDLocCodec extends LocCodec {

    public BEDLocCodec() {
        this(Format.BED);
    }

    public BEDLocCodec(final Format format) {
        super(format, format.getHeaderPart() == null ? -1 : format.getHeaderPart().length);
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
}
