package org.mulinlab.varnote.config;

import org.junit.Assert;
import org.junit.Test;
import org.mulinlab.varnote.cmdline.tools.config.RunAnnotationConfig;
import org.mulinlab.varnote.cmdline.tools.config.RunIntersectConfig;
import org.mulinlab.varnote.cmdline.txtreader.run.AnnoRunReader;
import org.mulinlab.varnote.cmdline.txtreader.run.IntersectRunReader;
import org.mulinlab.varnote.config.param.output.IntersetOutParam;
import org.mulinlab.varnote.config.param.query.QueryFileParam;
import org.mulinlab.varnote.config.run.AnnoRunConfig;
import org.mulinlab.varnote.config.run.OverlapRunConfig;
import org.mulinlab.varnote.utils.database.Database;
import org.mulinlab.varnote.utils.enumset.FormatType;
import org.mulinlab.varnote.utils.enumset.IndexType;
import org.mulinlab.varnote.utils.enumset.IntersectType;
import utils.TestUtils;


import java.util.List;

public class RunConfigTest {

    @Test
    public void testFull() {
        final String file = "src/test/resources/config/q3.sorted.overlap.run.config.full";

        OverlapRunConfig runConfig = new IntersectRunReader<OverlapRunConfig>().read(file);
        runConfig.init();

        QueryFileParam queryFileParam = (QueryFileParam)runConfig.getQueryParam();
        IntersetOutParam outParam = (IntersetOutParam)runConfig.getOutParam();

        List<Database> databases = runConfig.getDatabses();
        Assert.assertEquals(runConfig.getThread(), 4);
        Assert.assertEquals(queryFileParam.getQueryFormat().type, FormatType.VCF);
        Assert.assertEquals(outParam.isRemoveCommemt(), true);
        Assert.assertEquals(outParam.isGzip(), false);

        Assert.assertEquals(databases.size(), 2);
        Assert.assertEquals(databases.get(0).getOutName(), "db2");
        Assert.assertEquals(databases.get(0).getConfig().getIndexType(), IndexType.VARNOTE);
        Assert.assertEquals(databases.get(0).getConfig().getIntersect(), IntersectType.INTERSECT);

        Assert.assertEquals(databases.get(1).getOutName(), "db1");
        Assert.assertEquals(databases.get(1).getConfig().getIndexType(), IndexType.TBI);
        Assert.assertEquals(databases.get(1).getConfig().getIntersect(), IntersectType.EXACT);
    }

    @Test
    public void testMin() {
        final String file = "src/test/resources/config/q3.sorted.overlap.run.config.min";

        OverlapRunConfig runConfig = new IntersectRunReader<OverlapRunConfig>().read(file);
        runConfig.init();

        QueryFileParam queryFileParam = (QueryFileParam)runConfig.getQueryParam();
        IntersetOutParam outParam = (IntersetOutParam)runConfig.getOutParam();

        List<Database> databases = runConfig.getDatabses();
        Assert.assertEquals(runConfig.getThread(), 1);
        Assert.assertEquals(queryFileParam.getQueryFormat().type, FormatType.VCF);
        Assert.assertEquals(outParam.isRemoveCommemt(), false);
        Assert.assertEquals(outParam.isGzip(), true);

        Assert.assertEquals(databases.size(), 2);
        Assert.assertEquals(databases.get(0).getOutName(), "database2.sorted.tab.gz");
        Assert.assertEquals(databases.get(0).getConfig().getIndexType(), IndexType.VARNOTE);
        Assert.assertEquals(databases.get(0).getConfig().getIntersect(), IntersectType.INTERSECT);

        Assert.assertEquals(databases.get(1).getOutName(), "database1.sorted.bed.gz");
        Assert.assertEquals(databases.get(0).getConfig().getIndexType(), IndexType.VARNOTE);
        Assert.assertEquals(databases.get(0).getConfig().getIntersect(), IntersectType.INTERSECT);
    }


    @Test
    public void testAnno() {
        final String file = "src/test/resources/config/q3.sorted.anno.run.config.full";

        AnnoRunConfig runConfig = new AnnoRunReader<AnnoRunConfig>().read(file);
        runConfig.init();

        Assert.assertEquals(runConfig.isForceOverlap(), true);
    }

    @Test
    public void testRunIntersect() {
        String[] args = new String[]{ "-I", "src/test/resources/config/q3.sorted.overlap.run.config.full"};
        TestUtils.initClass(RunIntersectConfig.class, args);
    }

    @Test
    public void testRunAnno() {
        String[] args = new String[]{ "-I", "src/test/resources/config/q3.sorted.anno.run.config.full"};
        TestUtils.initClass(RunAnnotationConfig.class, args);
    }
}
