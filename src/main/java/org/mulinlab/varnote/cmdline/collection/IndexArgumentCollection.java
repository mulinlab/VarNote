package org.mulinlab.varnote.cmdline.collection;

import org.mulinlab.varnote.cmdline.constant.Arguments;
import org.broadinstitute.barclay.argparser.Argument;

public final class IndexArgumentCollection {
    private static final long serialVersionUID = 1L;

    @Argument(shortName = Arguments.INDEX_PRINT_HEADER_SHORT, fullName = Arguments.INDEX_PRINT_HEADER_LONG,
            doc = "Print header line(s).", optional = true)
    public Boolean printHeader = false;

    @Argument(shortName = Arguments.INDEX_PRINT_META_SHORT, fullName = Arguments.INDEX_PRINT_META_LONG,
            doc = "Print meta information lines.", optional = true)
    public Boolean printMeta = false;

    @Argument(shortName = Arguments.INDEX_PRINT_FORMAT_SHORT, fullName = Arguments.INDEX_PRINT_FORMAT_LONG,
            doc = "Print database format.", optional = true)
    public Boolean printFormat = false;

    @Argument(shortName = Arguments.INDEX_LITS_CHROM_SHORT, fullName = Arguments.INDEX_LIST_CHROM_LONG,
            doc = "List the sequence names stored in the index file.", optional = true)
    public Boolean listSeq = false;

    @Argument(shortName = Arguments.INDEX_REPLACE_HEADER_SHORT, fullName = Arguments.INDEX_REPLACE_HEADER_LONG,
            doc = "Replace the column names with a comma-separated string containing the column names. Columns name should be separated by comma and included with double quotation.", optional = true)
    public String replaceHeader;

    public boolean isPrintInfo() {
        return printFormat || printHeader || printMeta || listSeq || (replaceHeader != null);
    }
}
