package org.mulinlab.varnote.cmdline.collection;

import org.mulinlab.varnote.cmdline.constant.Arguments;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.format.Format;
import org.broadinstitute.barclay.argparser.Argument;

import java.io.File;

public final class InputFileArgumentCollection {
    private static final long serialVersionUID = 1L;

    @Argument(fullName = Arguments.INTERSECT_INPUT_LONG, shortName = Arguments.INTERSECT_INPUT_SHORT, doc = Arguments.QUERY_DOC)
    protected TagArgument queryFile = null;

    @Argument(shortName = Arguments.FORMAT_HEADER_SHORT, fullName = Arguments.FORMAT_HEADER_LONG, optional = true,
            doc = "Indicate whether input file contains a column header line. If --" + Arguments.FORMAT_HEADER_LONG + " is included, the first line below the comment line will be considered as a header line.")
    public Boolean hasHeader = GlobalParameter.DEFAULT_HAS_HEADER;

    @Argument(shortName = Arguments.FORMAT_HEADER_PATH_SHORT, fullName = Arguments.FORMAT_HEADER_PATH_LONG, optional = true,
            doc = "Path of external file to include the header lines.")
    public File extHeaderFile;

    public String getQueryFilePath() {
        return queryFile.getArgValue();
    }

    public Format getFormat(final String queryFilePath, final boolean isQuery) {
        Format format = queryFile.getFormat(false);

        if(format == null) format = Format.defaultFormat(queryFilePath, isQuery);
        format = queryFile.setFormat(format);

        if(hasHeader) format.setHasHeaderInFile(true);
        if(extHeaderFile != null) format.setHeaderPath(extHeaderFile.getAbsolutePath());

//        format.setDelimiter(delimiter);

        return format;
    }
}
