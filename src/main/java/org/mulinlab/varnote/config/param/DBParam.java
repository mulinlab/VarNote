package org.mulinlab.varnote.config.param;

import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import htsjdk.samtools.util.IOUtil;
import htsjdk.samtools.util.StringUtil;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.enumset.IndexType;
import org.mulinlab.varnote.utils.enumset.IntersectType;
import org.mulinlab.varnote.utils.headerparser.BEDHeaderParser;

import java.io.File;
import java.util.List;

public final class DBParam extends Param {

    private String dbPath;
    private String outName;
    private IntersectType intersect = GlobalParameter.DEFAULT_INTERSECT;;
    private IndexType indexType = IndexType.VARNOTE;

    public DBParam(final String dbPath) {
        if(SeekableStreamFactory.isFilePath(dbPath))
            this.dbPath = new File(dbPath).getAbsolutePath();
        else
            this.dbPath = dbPath;

        IOUtil.assertInputIsValid(this.dbPath);
    }

    public DBParam(final File dbFile) {
        IOUtil.assertFileIsReadable(dbFile);
        this.dbPath = dbFile.getAbsolutePath();
    }

    public DBParam(final String dbFile, final IndexType indexType) {
        this(dbFile);
        setIndexType(indexType);
    }

    public DBParam(final String dbFile, final String outName, final IntersectType intersect, final IndexType indexType) {
        this(dbFile, indexType);
        setOutName(outName);
        setIntersect(intersect);
    }

    public DBParam(final String dbFile, final String outName, final String intersect, final String indexType) {
        this(dbFile);
        setIndexType(indexType);
        setOutName(outName);
        setIntersect(intersect);
    }

    public void printLog() {
        logger.info(VannoUtils.printLogHeader("Database"));
        logger.info(String.format("Database File: %s", new File(dbPath).getName()));
        logger.info(String.format("Query Mode: %s", intersect));
        logger.info(String.format("Index Type: %s", indexType));
        logger.info(String.format("Output Name: %s", outName));
    }

    @Override
    public void checkParam() {
    }

    public String getDbPath() {
        return dbPath;
    }

    public void setDbPath(String dbPath) {
        this.dbPath = dbPath;
    }

    public String getOutName() {
        return outName;
    }

    public void setOutName(String outName) {
        this.outName = outName;
    }

    public IntersectType getIntersect() {
        return intersect;
    }

    public void setIntersect(String intersect) {
        this.intersect = VannoUtils.checkIntersectType(intersect);
    }

    public void setIntersect(IntersectType intersect) {
        this.intersect = intersect;
    }

    public IndexType getIndexType() {
        return indexType;
    }

    public void setIndexType(IndexType indexType) {
        this.indexType = indexType;
    }

    public void setIndexType(String indexType) {
        this.indexType = VannoUtils.checkIndexType(indexType);
    }
}
