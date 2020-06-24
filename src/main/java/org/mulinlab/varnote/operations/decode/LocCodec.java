package org.mulinlab.varnote.operations.decode;

import htsjdk.tribble.AsciiFeatureCodec;
import htsjdk.tribble.readers.LineIterator;
import htsjdk.tribble.util.ParsingUtils;
import org.apache.logging.log4j.Logger;
import org.mulinlab.varnote.utils.LoggingUtils;
import org.mulinlab.varnote.utils.node.LocFeature;
import org.mulinlab.varnote.utils.format.Format;


public abstract class LocCodec extends AsciiFeatureCodec<LocFeature> {
    final static Logger logger = LoggingUtils.logger;

    protected LocFeature intv;
    protected final Format format;
    protected String[] parts;
    protected boolean isFull;

    public LocCodec(final Format format, final int size, final boolean isFull) {
        super(LocFeature.class);
        this.format = format;
        this.intv = new LocFeature();
        this.isFull = isFull;
        if(size > 0) parts = new String[size];
    }

    public abstract LocCodec clone();

    @Override
    public LocFeature decode(final String s) {
        if(parts == null) {
            parts = new String[s.split(format.getDelimStr()).length];
        }
        intv.clear();
        ParsingUtils.split(s, parts, format.getDelimChar(), true);
        processToken();

        intv.origStr = s;
        return intv;
    }

    public void processToken() {
        intv.chr = parts[format.sequenceColumn - 1].toLowerCase().replace("chr", "");
        intv.beg = intv.end = Integer.parseInt(parts[format.startPositionColumn - 1]);

        if(format.refPositionColumn > 1) intv.ref = parts[format.refPositionColumn - 1];
        if(format.altPositionColumn > 1) intv.alt = parts[format.altPositionColumn - 1];

        processBeg();
        processEnd();
        processOther();

        if (intv.beg < 0) intv.beg = 0;
        if (intv.end < 1) intv.end = 1;
        if(isFull) {
            intv.parts = new String[parts.length];
            for (int i = 0; i < parts.length; i++) {
                intv.parts[i] = parts[i];
            }
        }
    }

    public boolean exceedMaxLength() {
        if(intv != null) {
            if(intv.end - intv.beg > format.getMaxVariantLength()) {
                return true;
            }
        }
        return false;
    }

    public abstract void processBeg();
    public abstract void processEnd();
    public abstract void processOther();


    @Override
    public Object readActualHeader(LineIterator reader) {
        return null;
    }

    @Override
    public boolean canDecode(String path) {
        return false;
    }

    public void setFull(final boolean full) {
        isFull = full;
    }


}
