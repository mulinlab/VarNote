package org.mulinlab.varnote.config.anno.databse;

import htsjdk.variant.vcf.VCFCodec;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import org.mulinlab.varnote.filters.iterator.NoFilterIterator;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.enumset.FileType;
import org.mulinlab.varnote.utils.format.Format;

import java.util.ArrayList;
import java.util.List;

public final class VCFParser {

    private final VCFHeader vcfHeader;
    private final VCFCodec codec;
    private List<String> infoKeys;

    private String path;
    private FileType fileType;

    public VCFParser(final String path) {
        this(path, VannoUtils.checkFileType(path));
    }

    public VCFParser(final String path, final FileType fileType) {
        this.path = path;
        this.fileType = fileType;

        codec = new VCFCodec();

        NoFilterIterator iterator = new NoFilterIterator(path, fileType);
        vcfHeader = (VCFHeader)codec.readActualHeader(iterator);
        iterator.close();
    }

    @Override
    public VCFParser clone() {
        return new VCFParser(this.path, this.fileType);
    }

    public List<String> getInfoKeys() {
        if(infoKeys == null) {
            infoKeys = new ArrayList<>();
            for (VCFInfoHeaderLine info : vcfHeader.getInfoHeaderLines()) {
                infoKeys.add(info.getID());
            }
        }
        return infoKeys;
    }

    public static VCFParser defaultVCFParser(final Format format, final String filePath, FileType fileType) {
        final String headerPath = (format.getHeaderPath() != null) ? format.getHeaderPath() : filePath;

        if(format.getHeaderPath() != null) {
            fileType = VannoUtils.checkFileType(headerPath);
        }

        return new VCFParser(headerPath, fileType);
    }

    public VCFHeader getVcfHeader() {
        return vcfHeader;
    }

    public VCFCodec getCodec() {
        return codec;
    }

    public VCFInfoHeaderLine getInfoHeaderLine(final String key) {
        return vcfHeader.getInfoHeaderLine(key);
    }
}
