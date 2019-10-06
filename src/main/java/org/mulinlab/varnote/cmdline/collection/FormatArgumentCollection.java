package org.mulinlab.varnote.cmdline.collection;

import org.mulinlab.varnote.cmdline.constant.Arguments;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.enumset.FormatType;
import org.mulinlab.varnote.utils.format.Format;
import org.broadinstitute.barclay.argparser.Argument;

import java.io.File;

public final class FormatArgumentCollection {
    private static final long serialVersionUID = 1L;

    @Argument( shortName = Arguments.FORMAT_SHORT, fullName = Arguments.FORMAT_LONG, optional = true,
            doc = "Format of file to be indexed. The TAB format should be applied together with -" + Arguments.FORMAT_CHROM_SHORT + ", -" +
                    Arguments.FORMAT_BEGIN_SHORT + ", -" + Arguments.FORMAT_END_SHORT + " and -" + Arguments.FORMAT_ZERO_SHORT + "."
    )
    public FormatType formatType = null;

    @Argument(shortName = Arguments.FORMAT_CHROM_SHORT, fullName = Arguments.FORMAT_CHROM_LONG,
            doc = "Used with TAB format(required), column of sequence name (1-based).")
    public Integer chrom = GlobalParameter.DEFAULT_COL;

    @Argument(shortName = Arguments.FORMAT_BEGIN_SHORT, fullName = Arguments.FORMAT_BEGIN_LONG,
            doc = "Used with TAB format(required), column of start chromosomal position (1-based).")
    public Integer begin = GlobalParameter.DEFAULT_COL;

    @Argument(shortName = Arguments.FORMAT_END_SHORT, fullName = Arguments.FORMAT_END_LONG,
            doc = "Used with TAB format(required), column of end chromosomal position. If missing, the program will set it equal to the start position (1-based).")
    public Integer end = GlobalParameter.DEFAULT_COL;

    @Argument(fullName = Arguments.FORMAT_REF_LONG, optional = true,
            doc = "Used with BED or TAB format(optional), column of reference allele. we recommend you to normalize allele before to use the option.\n" +
                    "Note: VarNote will parse chromosome position of each record according to both file format and reference allele composition")
    public Integer ref = GlobalParameter.DEFAULT_COL;

    @Argument(fullName = Arguments.FORMAT_ALT_LONG, optional = true,
            doc = "Used with BED or TAB format(optional), column of alternative allele. we recommend you to normalize allele before to use the option.")
    public Integer alt = GlobalParameter.DEFAULT_COL;

    @Argument(shortName = Arguments.FORMAT_ZERO_SHORT, fullName = Arguments.FORMAT_ZERO_LONG, optional = true,
            doc = "Used with TAB format(optional), specify that the position in the data file is 0-based (e.g. UCSC files) rather than 1-based.")
    public Boolean zeroBased = GlobalParameter.DEFAULT_ZERO_BASED;

    @Argument(shortName = Arguments.FORMAT_HEADER_SHORT, fullName = Arguments.FORMAT_HEADER_LONG, optional = true,
            doc = "Indicate whether input file contains a column header line. If --" + Arguments.FORMAT_HEADER_LONG + " is included, the first line below the comment line will be considered as a header line." +
                    "\nHeader line could start with '#' or have no indicator.\n" +
                    "To facilitate the automatic inference, header should at least include CHROM and POS (or, alternatively, BEGIN and END) columns, while, REF, ALT and other columns are optional. Other column names could be any words defined by user.")
    public Boolean hasHeader = GlobalParameter.DEFAULT_HAS_HEADER;

    @Argument(shortName = Arguments.FORMAT_HEADER_PATH_SHORT, fullName = Arguments.FORMAT_HEADER_PATH_LONG, optional = true,
            doc = "Path of external file to include the header lines.")
    public File extHeaderFile;

    @Argument(shortName = Arguments.FORMAT_COMMENT_SHORT, fullName = Arguments.FORMAT_COMMENT_LONG, optional = true,
            doc = "Used with bed or tab format, skip lines started with comment indicator. Quote in comment indicator will be removed. Example: -C @@ (change comment indicator from " + GlobalParameter.DEFAULT_COMMENT_INDICATOR + " to @@).")
    public String commentIndicator = GlobalParameter.DEFAULT_COMMENT_INDICATOR;

    @Argument(shortName = Arguments.FORMAT_SKIP_SHORT, fullName = Arguments.FORMAT_SKIP_LONG, optional = true,
            doc = "Skip first INT lines(including comment lines) in the data file.")
    public int skip = GlobalParameter.DEFAULT_SKIP;

    public Format getFormat(final String queryFilePath, final boolean isQuery) {
        Format format = null;
        if(formatType == FormatType.BED) {
            format = Format.newBED();
        } else if(formatType == FormatType.VCF) {
            format = Format.newVCF();
        } else if(formatType == FormatType.TAB) {
            format = Format.newTAB();
        }

        if(format == null) format = Format.defaultFormat(queryFilePath, isQuery);
        format = setFormatParam(format);
        return format;
    }

    public Format setFormatParam(Format format) {
        if(!commentIndicator.equals(GlobalParameter.DEFAULT_COMMENT_INDICATOR)) format.setCommentIndicator(commentIndicator);
        if(hasHeader) format.setHasHeader(true);
        if(zeroBased) format.setZeroBased();

        if(chrom > 0) format.setSequenceColumn(chrom);
        if(begin > 0) format.setStartPositionColumn(begin);
        if(end > 0) format.setEndPositionColumn(end);
        if(ref > 0) format.setRefColumn(ref);
        if(alt > 0) format.setAltColumn(alt);

        if(extHeaderFile != null) format.setHeaderPath(extHeaderFile.getAbsolutePath());
        if(skip > 0) format.setNumHeaderLinesToSkip(skip);
        return format;
    }
}
