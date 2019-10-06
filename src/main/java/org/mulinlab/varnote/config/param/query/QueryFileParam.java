package org.mulinlab.varnote.config.param.query;

import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import htsjdk.samtools.util.IOUtil;
import htsjdk.samtools.util.StringUtil;
import org.mulinlab.varnote.exceptions.InvalidArgumentException;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.utils.headerparser.BEDHeaderParser;
import org.mulinlab.varnote.utils.queryreader.ThreadLineReader;
import org.mulinlab.varnote.utils.queryreader.reader.GZipReader;
import org.mulinlab.varnote.utils.queryreader.reader.SpiderReader;
import org.mulinlab.varnote.utils.stream.BZIP2InputStream;
import org.mulinlab.varnote.utils.stream.GZInputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class QueryFileParam extends QueryParam {

    private String queryPath;
    private String queryName;

    private Format queryFormat;
    private VannoUtils.FileType fileType;

    private List<ThreadLineReader> spider;

    public QueryFileParam(final String path) {
        this(path, null);
    }

    public QueryFileParam(final String path, final Format queryFormat) {
        ThreadLineReader.readHeader = false;
        if(path == null || path.trim().equals("")) throw new InvalidArgumentException("Query file is required.");

        if(SeekableStreamFactory.isFilePath(path)) {
            this.queryPath = new File(path).getAbsolutePath();
        } else {
            this.queryPath = path;
        }
        IOUtil.assertInputIsValid(this.queryPath);

        fileType = VannoUtils.checkFileType(this.queryPath);
        queryName = new File(this.queryPath).getName();

        if(queryFormat == null) this.queryFormat = Format.defaultFormat(path, true);
        else this.queryFormat = queryFormat;

        spider = new ArrayList<ThreadLineReader>();
    }

    public void loadSpiderWithThread(final int thread) {
        try {
            if (fileType == VannoUtils.FileType.BGZ || fileType == VannoUtils.FileType.TXT) {
                BZIP2InputStream bz2_text = new BZIP2InputStream(queryPath, thread);
                bz2_text.adjustPos();
                bz2_text.creatSpider();

                if (bz2_text.getThreadNum() != thread) {
                    if (bz2_text.getThreadNum() == 1) {
                        spider.add(new ThreadLineReader(new SpiderReader(bz2_text.spider[0]), queryFormat, 0));
                    } else {
                        throw new InvalidArgumentException("Split file with error!");
                    }
                } else {
                    for (int i = 0; i < thread; i++) {
                        spider.add(new ThreadLineReader(new SpiderReader(bz2_text.spider[i]), queryFormat, i));
                    }
                }
            } else {
                GZInputStream in = new GZInputStream(queryPath, thread);
                for (int i = 0; i < thread; i++) {
                    spider.add(new ThreadLineReader(new GZipReader(in.getReader(i)), queryFormat, i));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ThreadLineReader getSpider(final int index) {
        if((index < 0) || (index >= spider.size())) throw new InvalidArgumentException("Min thread number is 0 and max thread number is " + spider.size());
        return spider.get(index);
    }


    public Format getQueryFormat() {
        return queryFormat;
    }

    public String getQueryName() {
        return queryName;
    }

    public String getQueryPath() {
        return queryPath;
    }

    public void setQueryFormat(Format queryFormat) {
        this.queryFormat = queryFormat;
    }

    public VannoUtils.FileType getFileType() {
        return fileType;
    }

    public int getSpiderSize() {
        return spider.size();
    }

    public void printLog() {
        List<String> colNmaes = queryFormat.getOriginalField();

        logger.info(VannoUtils.printLogHeader("QUERY"));
        logger.info(String.format("Query File: %s", getQueryName()));
        logger.info(String.format("Query Format: %s", queryFormat.logFormat()));

        if(!queryFormat.getCommentIndicator().equals("##")) logger.info(String.format("Comment indicator of query is: %s", queryFormat.getCommentIndicator()));
        if(colNmaes != null && queryFormat.isHasHeader()) logger.info(String.format("QueryRegion header is: %s", StringUtil.join(BEDHeaderParser.COMMA, colNmaes)));

//        log.printKVKCYN("Read QueryRegion File", log.isLog() ? new File(queryPath).getName() : queryPath);
//        log.printKVKCYN("QueryRegion Format is", queryFormat.toString());
    }
}
