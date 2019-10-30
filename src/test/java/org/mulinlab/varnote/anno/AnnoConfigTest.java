package org.mulinlab.varnote.anno;

import org.junit.Assert;
import org.junit.Test;
import org.mulinlab.varnote.cmdline.txtreader.anno.AnnoConfigReader;
import org.mulinlab.varnote.config.anno.databse.anno.ExtractConfig;
import org.mulinlab.varnote.config.param.postDB.DBAnnoParam;
import org.mulinlab.varnote.config.param.DBParam;
import org.mulinlab.varnote.utils.database.Database;
import org.mulinlab.varnote.utils.database.DatabaseFactory;
import java.util.Map;

public class AnnoConfigTest {

    @Test
    public void testBED() {
        final String path = "src/test/resources/config/q3.extract.d1";
        final Map<String, DBAnnoParam> map = new AnnoConfigReader<Map<String, DBAnnoParam>>().read(path);

        Database db = DatabaseFactory.readDatabase(new DBParam("src/test/resources/database1.sorted.bed.gz"));
        ExtractConfig annoConfig = new ExtractConfig(map.get("database1.sorted.bed.gz"), db);

        int[] col = annoConfig.getColToExtract();
        Assert.assertEquals(col[0], 6);
        Assert.assertEquals(annoConfig.getOutputName(6), "d1_VAL");
    }

    @Test
    public void testVCF() {
        final String path = "src/test/resources/config/q3.extract.d4";
        final Map<String, DBAnnoParam> map = new AnnoConfigReader<Map<String, DBAnnoParam>>().read(path);

        Database db = DatabaseFactory.readDatabase(new DBParam("src/test/resources/database4.sorted.vcf.gz"));
        ExtractConfig annoConfig = new ExtractConfig(map.get("database4.sorted.vcf.gz"), db);

        int[] col = annoConfig.getColToExtract();
        Assert.assertEquals(col[0], 4);
        Assert.assertEquals(col[1], 8);
        Assert.assertEquals(annoConfig.getOutputName(4), "d4_ref");
        Assert.assertEquals(annoConfig.getInfoOutputName("AF"), "d4_AF");
        Assert.assertEquals(annoConfig.getInfoOutputName("AC"), "d4_AC");
    }
}
