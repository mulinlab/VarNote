package org.mulinlab.varnote.operations.readers.query;

import htsjdk.variant.vcf.VCFHeader;
import org.mulinlab.varnote.config.anno.databse.VCFParser;
import org.mulinlab.varnote.config.param.FilterParam;
import org.mulinlab.varnote.filters.query.line.VCFHeaderLineFilter;
import org.mulinlab.varnote.filters.iterator.NoFilterIterator;
import org.mulinlab.varnote.operations.readers.itf.QueryReaderItf;
import org.mulinlab.varnote.operations.decode.VCFLocCodec;
import org.mulinlab.varnote.filters.iterator.LineFilterIterator;
import org.mulinlab.varnote.filters.iterator.LocFilterIterator;
import org.mulinlab.varnote.filters.iterator.VCFFilterIterator;
import org.mulinlab.varnote.utils.enumset.FileType;
import org.mulinlab.varnote.utils.format.Format;
import java.util.List;


public final class VCFFileReader extends AbstractFileReader {

    private FilterParam filterParam;
    private VCFParser vcfParser;
    private boolean isDecodeLoc = true;

    public VCFFileReader(QueryReaderItf itf, Format format) {
        super(itf, format);
    }

    public VCFFileReader(final String path) {
        super(path, Format.VCF);
    }
    public VCFFileReader(final String path, final Format format) {
        super(path, format);
    }
    public VCFFileReader(final String path, final FileType fileType, final Format format) {
        super(path, fileType, format);
    }

    public void setVcfParser(final VCFParser vcfParser) {
        this.vcfParser = vcfParser;
    }

    @Override
    public void checkFormat() {

    }

    public VCFParser getVcfParser() {
        if(vcfParser == null) {
            vcfParser = VCFParser.defaultVCFParser(format, getFilePath(), getFileType());
        }
        return vcfParser;
    }

    @Override
    protected void initLineFilters() {
        lineFilters.add(new VCFHeaderLineFilter());
    }

    public VCFHeader getHeader() {
        return (VCFHeader)getVcfParser().getVcfHeader();
    }

    public void setVariantFilters(final FilterParam filterParam) {
        if(filterParam.isNotNull()) {
            this.isDecodeLoc = false;
            this.filterParam = filterParam;
        }
    }

    public void setDecodeLoc(boolean decodeLoc) {
        isDecodeLoc = decodeLoc;
    }

    public VCFLocCodec getVCFcodec() {
        return new VCFLocCodec(format, decodeFull, getVcfParser().getCodec());
    }

    public VCFLocCodec getLoccodec() {
        return new VCFLocCodec(format, decodeFull);
    }

    @Override
    public LineFilterIterator getFilterIterator() {
        if(iterator == null) {
            if(isDecodeLoc) {
                iterator = new LocFilterIterator(new NoFilterIterator(reader), lineFilters, getLoccodec());
            } else {
                iterator = new VCFFilterIterator(new NoFilterIterator(reader), lineFilters, filterParam, getVCFcodec());
            }
        }
        return iterator;
    }

    public List<String> getInfoKeys() {
        return getVcfParser().getInfoKeys();
    }
}
