package org.mulinlab.varnote.utils.enumset;

import htsjdk.samtools.util.FileExtensions;

public enum IndexType {
    TBI("tbi", FileExtensions.TABIX_INDEX, FileExtensions.TABIX_INDEX),
    VARNOTE("varnote", ".vanno", ".vanno.vi");

    private final String name;
    private final String ext;
    private final String extIndex;
    IndexType(final String name, final String ext, final String extIndex) {
        this.name = name;
        this.ext = ext;
        this.extIndex = extIndex;
    }
    public String getName() {
        return name;
    }
    public String getExt() {
        return ext;
    }
    public String getExtIndex() {
        return extIndex;
    }
}
