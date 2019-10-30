package org.mulinlab.varnote.anno;


import org.junit.Test;
import org.mulinlab.varnote.cmdline.txtreader.anno.AnnoConfigReader;
import org.mulinlab.varnote.config.anno.databse.anno.DatabaseAnnoBEDParser;
import org.mulinlab.varnote.config.anno.databse.anno.DatabaseAnnoVCFParser;
import org.mulinlab.varnote.config.anno.databse.anno.ExtractConfig;
import org.mulinlab.varnote.config.param.postDB.DBAnnoParam;
import org.mulinlab.varnote.config.param.DBParam;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.operations.decode.VCFLocCodec;
import org.mulinlab.varnote.utils.block.SROB;
import org.mulinlab.varnote.utils.database.Database;
import org.mulinlab.varnote.utils.database.DatabaseFactory;
import org.mulinlab.varnote.utils.enumset.AnnoOutFormat;
import org.mulinlab.varnote.utils.node.LocFeature;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class AnnoDatabaseTest {

    @Test
    public void testVCF() {
        final String extractPath = "src/test/resources/config/q5.extract.d4";
        final Map<String, DBAnnoParam> map = new AnnoConfigReader<Map<String, DBAnnoParam>>().read(extractPath);

        Database db = DatabaseFactory.readDatabase(new DBParam("src/test/resources/database4.sorted.vcf.gz"));

        DatabaseAnnoVCFParser parser = new DatabaseAnnoVCFParser(new ExtractConfig(map.get("database4.sorted.vcf.gz"), db), true, AnnoOutFormat.VCF);

        VCFLocCodec decode = new VCFLocCodec(false);
        LocFeature feature = decode.decode("1\t12854895\tesv3585241\tG\tA\t100\tPASS\tAC=49;AF=0.0325479;END=12919194;");

        List<LocFeature> dblines = new ArrayList<>();
        dblines.add(db.decode("1\t12855318\trs111963106\tG\tA,C\t69.44\t.\tAC=2;AF=0.500;AN=4;\tGT:AD:DP:GQ:PL"));
        dblines.add(db.decode("1\t12855338\t.\tG\tGAA\t56.40\t.\tAC=2;AF=0.500;AN=4;\tGT:AD:DP:GQ:PL"));
        dblines.add(db.decode("1\t12855835\trs1063795\tC\tG\t5055.20\t.\tAC=4;AF=1.00;AN=4;DB;DP=113;\tGT:AD:DP:GQ:PL"));
        dblines.add(db.decode("1\t12855845\trs1063797\tG\tC\t5055.20\t.\tAC=4;AF=1.00;AN=4;DB;DP=85;\tGT:AD:DP:GQ:PL"));
        dblines.add(db.decode("1\t12856010\trs61775053\tC\tG\t3428.44\t.\tAC=2;AF=0.500;AN=4;\tGT:AD:DP:GQ:PL"));

        parser.extractFieldsValue(feature, dblines.toArray(new LocFeature[dblines.size()]));
        StringJoiner join = parser.joinFields(new StringJoiner(GlobalParameter.TAB));

        System.out.println(parser.getHeader(new StringJoiner(GlobalParameter.TAB)).toString());
        System.out.println(join.toString());
    }

    @Test
    public void testBED() {
        final String extractPath = "src/test/resources/config/q5.extract.d3";
        final Map<String, DBAnnoParam> map = new AnnoConfigReader<Map<String, DBAnnoParam>>().read(extractPath);

        Database db = DatabaseFactory.readDatabase(new DBParam("src/test/resources/database3.sorted.tab.gz"));

        DatabaseAnnoBEDParser parser = new DatabaseAnnoBEDParser(new ExtractConfig(map.get("database3.sorted.tab.gz"), db), false, AnnoOutFormat.VCF);

        VCFLocCodec decode = new VCFLocCodec(false);
        LocFeature feature = decode.decode("1\t12854895\tesv3585241\tG\t<CN0>\t100\tPASS\tAC=49;AF=0.0325479;END=12919194;");

        List<LocFeature> dblines = new ArrayList<>();
        dblines.add(db.decode("1\t12855631\tA|||||||||||||2.473e-05"));
        dblines.add(db.decode("1\t12855707\tA|||||||||||||8.238e-06"));
        dblines.add(db.decode("1\t12855897\tT||||||||||"));
        dblines.add(db.decode("1\t12856194\tT|||||||||||||4.260e-05"));
        dblines.add(db.decode("1\t12856422\tC|0.00259585"));

        parser.extractFieldsValue(feature, dblines.toArray(new LocFeature[dblines.size()]));

        StringJoiner join = parser.joinFields(new StringJoiner(GlobalParameter.TAB));

        System.out.println(parser.getHeader(new StringJoiner(GlobalParameter.TAB)).toString());
        System.out.println(join.toString());
    }
}
