package org.mulinlab.varnote.config.anno.databse;

import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.exceptions.InvalidArgumentException;
import org.mulinlab.varnote.filters.iterator.NoFilterIterator;
import org.mulinlab.varnote.operations.readers.itf.QueryReaderItf;
import org.mulinlab.varnote.operations.readers.query.AbstractFileReader;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.enumset.FileType;
import org.mulinlab.varnote.utils.format.Format;
import java.io.IOException;

public final class HeaderFormatReader {
    public static Format readHeader(Format format, final String path, final FileType fileType) {
        if(format.getHeaderPath() != null) {
            format = readDefaultHeader(format.getHeaderPath(), VannoUtils.checkFileType(format.getHeaderPath()), format, true);
        } else {
            if(format.isHasHeader()) {
                format = readDefaultHeader(path, fileType, format, true);
            } else {
                if(format.getHeaderPart() == null) {
                    format = readDefaultHeader(path, fileType, format, false);
                }
            }
        }
        return format;
    }

    public static Format readDefaultHeader(final String path, final FileType fileType, final Format format, final boolean hasHeader) {
        try {
            QueryReaderItf reader = AbstractFileReader.getReader(path, fileType);
            readDefaultHeader(reader, format, hasHeader);
            reader.closeReader();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return format;
    }

    public static Format readDefaultHeader(QueryReaderItf reader, final Format format, final boolean hasHeader) {
        System.out.println("read header " + reader.getFilePath());

        NoFilterIterator lineIterator = new NoFilterIterator(reader);
        String line;
        String[] parts = null;

        while (lineIterator.hasNext()) {
            line = lineIterator.next();

            if(line.startsWith(format.getCommentIndicator()) || line.equals("")) {
            } else if(line.startsWith(GlobalParameter.VCF_HEADER_INDICATOR) || hasHeader){
                if(format.isHasHeader()) format.setHeader(line);

                if(line.startsWith(GlobalParameter.VCF_HEADER_INDICATOR)) line = line.substring(1);
                parts = VannoUtils.parserHeader(line, GlobalParameter.TAB);
                format.setHeaderPart(parts);
                if(lineIterator.hasNext()) checkDataIsValid(format, lineIterator.next().split(GlobalParameter.TAB));

                break;
            } else {
                format.setDataStr(line);

                parts = VannoUtils.setDefaultCol(VannoUtils.parserHeader(line, GlobalParameter.TAB));
                format.setHeaderPart(parts);
                break;
            }
        }

        return format;
    }

    public static boolean checkDataIsValid(final Format format, final String[] data) {
        if(data.length != format.getHeaderPartSize()) {
            throw new InvalidArgumentException(String.format("The column of data is %d, while the column of header is %d, please check.", data.length, format.getHeaderPartSize()));
        } else {
            try {
                Integer.parseInt(data[format.startPositionColumn - 1]);
            } catch (NumberFormatException e) {
                throw new InvalidArgumentException(String.format("Begin column should be a number, but we get ", data[format.startPositionColumn - 1]));
            }

            try {
                if(format.endPositionColumn > 0) {
                    Integer.parseInt(data[format.endPositionColumn - 1]);
                }
            } catch (NumberFormatException e) {
                throw new InvalidArgumentException(String.format("End column should be a number, but we get ", data[format.endPositionColumn - 1]));
            }
        }
        return true;
    }

    public static Format readHeaderFromArray(final String[] header, final Format format) {
        format.setHeaderPart(header);
        return format;
    }

}
